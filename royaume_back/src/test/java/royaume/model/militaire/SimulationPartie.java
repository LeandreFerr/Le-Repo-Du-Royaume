package royaume.model.militaire;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import royaume.model.peuple.Canards;
import royaume.model.peuple.Population;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;


// c'est un void main, pour pouvoir print dans le terminal, 
// ce n'est pas des tests 
// mvn test-compile exec:java -Dexec.mainClass=royaume.model.militaire.SimulationPartie -Dexec.classpathScope=test


public class SimulationPartie {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    public static void main(String[] args) {
        Population peuple = new Canards(null, "Canards", "Archiduc", 0.05);
        LocalDateTime t0 = LocalDateTime.of(2026, 1, 1, 12, 0);

        Royaume moi = new Royaume(1, "Moi", peuple);
        Royaume cible = new Royaume(2, "Cible", peuple);

        // --- Mise en place
        // 5 cavaliers : 50 OR, 50 PIERRE, 100 NOURRITURE
        // 2 archers   : 20 OR, 20 BOIS,   20 NOURRITURE
        moi.crediterRessources(Ressource.OR, 70);
        moi.crediterRessources(Ressource.PIERRE, 50);
        moi.crediterRessources(Ressource.BOIS, 20);
        moi.crediterRessources(Ressource.NOURRITURE, 120);

        // La cible : 2 lanciers + 4 archers déjà formés, et 220 ressources à piller
        cible.ajouterTroupes(TypeTroupe.LANCIER, 2);
        cible.ajouterTroupes(TypeTroupe.ARCHER, 4);
        cible.setPopulationMilitaire(6);
        cible.crediterRessources(Ressource.PIERRE, 50);
        cible.crediterRessources(Ressource.BOIS, 30);
        cible.crediterRessources(Ressource.OR, 100);
        cible.crediterRessources(Ressource.NOURRITURE, 40);

        titre("DÉPART");
        afficher(t0, moi, cible);

        // --- Étape 1 : entraînement
        titre("ÉTAPE 1 : entraînement de 5 cavaliers et 2 archers");
        boolean okCavaliers = moi.entrainerTroupe(TypeTroupe.CAVALIER, 5, t0);
        boolean okArchers = moi.entrainerTroupe(TypeTroupe.ARCHER, 2, t0);
        System.out.println("Cavaliers acceptés : " + okCavaliers);
        System.out.println("Archers acceptés   : " + okArchers);
        afficher(t0, moi, cible);

        // --- Étape 2 : fin de la formation (5 min par recrue : cavaliers = 25 min, archers = 10 min)
        LocalDateTime finFormation = t0.plusMinutes(25);
        titre("ÉTAPE 2 : fin de la formation");
        moi.traiterFormations(finFormation);
        afficher(finFormation, moi, cible);

        // --- Étape 3 : départ
        titre("ÉTAPE 3 : départ de l'expédition (30 cases)");
        Attaque attaque = moi.lancerAttaque(cible,
                Map.of(TypeTroupe.CAVALIER, 5, TypeTroupe.ARCHER, 2), 30, finFormation);
        System.out.println("Statut            : " + attaque.getStatut());
        System.out.println("Impact prévu      : " + attaque.getDateImpact().format(FORMAT)
                + " (vitesse du convoi = archers, les plus lents)");
        System.out.println("Capacité d'emport : " + attaque.calculerCapacitePillage());
        afficher(finFormation, moi, cible);

        // --- Étape 4 : impact
        LocalDateTime impact = attaque.getDateImpact();
        titre("ÉTAPE 4 : impact et combat");
        int puissanceAttaque = 0;
        for (ContingentTroupe c : attaque.getTroupesEnvoyees()) {
            puissanceAttaque += c.getQuantite() * c.getType().getAttaque();
        }
        System.out.println("Puissance d'attaque : " + puissanceAttaque);
        System.out.println("Puissance défensive : " + cible.getPuissanceDefensive());
        attaque.resoudreCombat();
        System.out.println("Victoire attaquant  : " + attaque.isVictoireAttaquant());
        System.out.println("Survivants          : " + formaterContingents(attaque.getTroupesEnvoyees()));
        System.out.println("Butin               : " + formaterRessources(attaque.getButin()));
        System.out.println("Statut              : " + attaque.getStatut());
        System.out.println("Retour prévu        : " + attaque.getDateRetourPrevue().format(FORMAT));
        afficher(impact, moi, cible);

        // --- Étape 5 : retour
        LocalDateTime retour = attaque.getDateRetourPrevue();
        titre("ÉTAPE 5 : retour à la base");
        attaque.receptionnerRetour();
        System.out.println("Statut : " + attaque.getStatut());
        afficher(retour, moi, cible);
    }

    // ------------------------------------------------------------ affichage

    private static void titre(String texte) {
        System.out.println();
        System.out.println("=== " + texte + " ===");
    }

    private static void afficher(LocalDateTime heure, Royaume moi, Royaume cible) {
        System.out.println("[" + heure.format(FORMAT) + "]");
        decrire(moi);
        decrire(cible);
    }

    private static void decrire(Royaume r) {
        System.out.printf("  %-6s | dispo=%d, entrainement=%d, militaire=%d | garnison=%s | stocks=%s%n",
                r.getNom(),
                r.getPopulationDisponible(),
                r.getPopulationEnEntrainement(),
                r.getPopulationMilitaire(),
                formaterContingents(r.getGarnison()),
                formaterRessources(r.getRessources()));
    }

    private static String formaterContingents(List<ContingentTroupe> liste) {
        StringBuilder sb = new StringBuilder();
        for (ContingentTroupe c : liste) {
            if (c.getQuantite() > 0) {
                sb.append(c.getQuantite()).append(" ").append(c.getType().getNom()).append(", ");
            }
        }
        return sb.length() == 0 ? "vide" : sb.substring(0, sb.length() - 2);
    }

    private static String formaterRessources(List<QuantiteRessource> liste) {
        if (liste.isEmpty()) {
            return "aucun";
        }
        StringBuilder sb = new StringBuilder();
        for (QuantiteRessource q : liste) {
            sb.append(q.getRessource()).append("=").append(q.getQuantite()).append(" ");
        }
        return sb.toString().trim();
    }
}