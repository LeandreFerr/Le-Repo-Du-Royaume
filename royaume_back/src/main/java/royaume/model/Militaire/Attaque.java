package royaume.model.Militaire;

import royaume.model.QuantiteRessource;
import royaume.model.Ressource; // peut devenir inutile, supprime-le si l'IDE le signale
import royaume.model.Royaume;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Attaque {
    private final Royaume attaquant;
    private final Royaume defenseur;
    private final List<ContingentTroupe> troupesEnvoyees;
    private List<QuantiteRessource> butin = new ArrayList<>();

    private final LocalDateTime dateDepart;
    private LocalDateTime dateImpact;
    private LocalDateTime dateRetourPrevue;
    private Duration tempsTrajet;
    private boolean victoireAttaquant;
    private StatutAttaque statut = StatutAttaque.ALLER;

    public Attaque(Royaume attaquant, Royaume defenseur,
                   List<ContingentTroupe> troupes, LocalDateTime dateDepart, double distance) {
        if (troupes.isEmpty()) throw new IllegalArgumentException("Aucune troupe envoyée");
        this.attaquant = attaquant;
        this.defenseur = defenseur;
        this.troupesEnvoyees = troupes;
        this.dateDepart = dateDepart;
        this.tempsTrajet = calculerTempsAller(distance);
        this.dateImpact = dateDepart.plus(tempsTrajet);
    }


    /** Durée = distance / vitesse de l'unité la plus lente. */
    public Duration calculerTempsAller(double distance) {
        int vitesseMin = troupesEnvoyees.stream()
                .filter(c -> c.getQuantite() > 0)
                .mapToInt(c -> c.getType().getVitesse())
                .min().orElseThrow(() -> new IllegalStateException("Convoi vide"));
        return Duration.ofSeconds(Math.round(distance / vitesseMin * 3600));
    }

    public int calculerCapacitePillage() {
        return troupesEnvoyees.stream()
                .mapToInt(c -> c.getQuantite() * c.getType().getCapaciteTransport())
                .sum();
    }

    /* L'attaque de base
    private int puissanceAttaque() {
        return troupesEnvoyees.stream()
                .mapToInt(c -> c.getQuantite() * c.getType().getAttaque()).sum();
    }
    */

    /* L'attaque suivant dominance, on peut revenir a celui d'avant si too much */
    private static double puissance(List<ContingentTroupe> troupes,
                                List<ContingentTroupe> adverses, boolean enAttaque) {
        int effectifAdverse = adverses.stream().mapToInt(ContingentTroupe::getQuantite).sum();
        double total = 0;
        for (ContingentTroupe c : troupes) {
            double base = c.getQuantite()
                    * (enAttaque ? c.getType().getAttaque() : c.getType().getDefense());
            double mult = 1.0;
            if (effectifAdverse > 0) {
                double somme = 0;
                for (ContingentTroupe a : adverses) {
                    somme += a.getQuantite() * c.getType().multiplicateurContre(a.getType());
                }
                mult = somme / effectifAdverse;
            }
            total += base * mult;
        }
        return total;
    }



    public void resoudreCombat() {
        if (statut != StatutAttaque.ALLER) throw new IllegalStateException("Combat déjà résolu");

    /*  Attaque precedante
        int pA = puissanceAttaque();
        int pD = defenseur.getPuissanceDefensive();
    */
    /* Attaque suivant dominance */
        double pA = puissance(troupesEnvoyees, defenseur.getGarnison(), true);
        double pD = puissance(defenseur.getGarnison(), troupesEnvoyees, false);


        victoireAttaquant = pA > pD;   // égalité = victoire du défenseur

        double ratioPerteAttaquant = victoireAttaquant ? (double) pD / pA : 1.0;
        double ratioPerteDefenseur = victoireAttaquant ? 1.0 : (pD == 0 ? 0.0 : (double) pA / pD);

        int pertesAttaquant = 0;
        for (ContingentTroupe c : troupesEnvoyees) {
            int perdus = (int) Math.round(c.getQuantite() * ratioPerteAttaquant);
            c.retirer(perdus);
            pertesAttaquant += perdus;
        }
        attaquant.perdreMilitaires(pertesAttaquant);
        defenseur.appliquerPertes(ratioPerteDefenseur);

        if (victoireAttaquant) {
            butin = defenseur.subirPillage(calculerCapacitePillage());
            statut = StatutAttaque.RETOUR;
            dateRetourPrevue = dateImpact.plus(tempsTrajet);
        } else {
            statut = StatutAttaque.TERMINEE;
        }
    }

    public void receptionnerRetour() {
        if (statut != StatutAttaque.RETOUR) throw new IllegalStateException("Pas en phase de retour");
        for (ContingentTroupe c : troupesEnvoyees) {
            attaquant.ajouterTroupes(c.getType(), c.getQuantite());
        }
        for (QuantiteRessource qr : butin) {
            if (qr.getQuantite() > 0) {                       // crediterRessources refuse 0
                attaquant.crediterRessources(qr.getRessource(), qr.getQuantite());
            }
        }
        statut = StatutAttaque.TERMINEE;

    }

    public Royaume getAttaquant() { return attaquant; }
    public Royaume getDefenseur() { return defenseur; }
    public List<ContingentTroupe> getTroupesEnvoyees() { return troupesEnvoyees; }
    public List<QuantiteRessource> getButin() { return butin; }
    public LocalDateTime getDateDepart() { return dateDepart; }
    public LocalDateTime getDateImpact() { return dateImpact; }
    public LocalDateTime getDateRetourPrevue() { return dateRetourPrevue; }
    public boolean isVictoireAttaquant() { return victoireAttaquant; }
    public StatutAttaque getStatut() { return statut; }
}