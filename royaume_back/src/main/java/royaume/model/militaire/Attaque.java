package royaume.model.militaire;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import royaume.model.QuantiteRessource;
import royaume.model.Royaume;

@Entity
@Table(name = "attaque")
public class Attaque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attaquant_id", nullable = false)
    @NotNull
    private Royaume attaquant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defenseur_id", nullable = false)
    @NotNull
    private Royaume defenseur;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "troupes_envoyees", columnDefinition = "json", nullable = false)
    @NotEmpty
    private List<@Valid ContingentTroupe> troupesEnvoyees = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "butin", columnDefinition = "json", nullable = false)
    @NotNull
    private List<QuantiteRessource> butin = new ArrayList<>();

    @NotNull
    @Column(name = "date_depart", nullable = false)
    private LocalDateTime dateDepart;

    @NotNull
    @Column(name = "date_impact", nullable = false)
    private LocalDateTime dateImpact;

    @Column(name = "date_retour_prevue")   // nullable : seulement en cas de victoire
    private LocalDateTime dateRetourPrevue;

    @NotNull
    @Column(name = "temps_trajet", nullable = false)
    private Duration tempsTrajet;

    @Column(name = "victoire_attaquant", nullable = false)
    private boolean victoireAttaquant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private StatutAttaque statut = StatutAttaque.ALLER;

    /** Requis par JPA. Les invariants sont alors contrôlés par Bean Validation. */
    public Attaque() {}  

    public Attaque(Royaume attaquant, Royaume defenseur,
                   List<ContingentTroupe> troupes, LocalDateTime dateDepart, double distance) {
        if (attaquant == null || defenseur == null) {
            throw new IllegalArgumentException("Attaquant et défenseur requis");
        }
        if (attaquant == defenseur) {
            throw new IllegalArgumentException("Un royaume ne peut pas s'attaquer lui-même");
        }
        if (troupes == null || troupes.isEmpty()) {
            throw new IllegalArgumentException("Aucune troupe envoyée");
        }
        if (dateDepart == null) {
            throw new IllegalArgumentException("Date de départ requise");
        }
        if (!Double.isFinite(distance) || distance <= 0) {
            throw new IllegalArgumentException("La distance doit être strictement positive");
        }
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

    /** Puissance avec dominance : chaque contingent est pondéré par son multiplicateur moyen contre l'adversaire. */
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

        double pA = puissance(troupesEnvoyees, defenseur.getGarnison(), true);
        double pD = puissance(defenseur.getGarnison(), troupesEnvoyees, false);

        victoireAttaquant = pA > pD;   // égalité = victoire du défenseur

        double ratioPerteAttaquant = victoireAttaquant ? pD / pA : 1.0;
        double ratioPerteDefenseur = victoireAttaquant ? 1.0 : (pD == 0 ? 0.0 : pA / pD);

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
            if (qr.getQuantite() > 0) {   // crediterRessources refuse 0
                attaquant.crediterRessources(qr.getRessource(), qr.getQuantite());
            }
        }
        statut = StatutAttaque.TERMINEE;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Royaume getAttaquant() { return attaquant; }
    public Royaume getDefenseur() { return defenseur; }
    public List<ContingentTroupe> getTroupesEnvoyees() { return troupesEnvoyees; }
    public List<QuantiteRessource> getButin() { return butin; }
    public LocalDateTime getDateDepart() { return dateDepart; }
    public LocalDateTime getDateImpact() { return dateImpact; }
    public LocalDateTime getDateRetourPrevue() { return dateRetourPrevue; }
    public Duration getTempsTrajet() { return tempsTrajet; }
    public boolean isVictoireAttaquant() { return victoireAttaquant; }
    public StatutAttaque getStatut() { return statut; }
}