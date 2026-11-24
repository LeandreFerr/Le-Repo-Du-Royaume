package royaume.model.ticker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import royaume.model.Partie;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;
import royaume.model.StatutPartie;
import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.ChantierConstruction;
import royaume.model.militaire.Attaque;
import royaume.model.militaire.ContingentTroupe;
import royaume.model.militaire.StatutAttaque;
import royaume.model.militaire.TypeTroupe;
import royaume.model.peuple.Canards;
import royaume.model.peuple.Gobelins;
import royaume.model.peuple.Population;

@DisplayName("Tests unitaires — MoteurJeu (Simulation du temps / Ticker)")
class MoteurJeuTest {

    private static final LocalDateTime DATE_DEPART = LocalDateTime.of(2026, 11, 23, 12, 0, 0);
    private static final Population PEUPLE_A = new Canards(1, "Canards", "Archiduc", 0.05);
    private static final Population PEUPLE_B = new Gobelins(2, "Gobelins", "Chef", 0.0);

    private HorlogeJeu horloge;
    private Partie partie;
    private Royaume royaumeA;
    private Royaume royaumeB;
    private MoteurJeu moteur;

    @BeforeEach
    void setUp() {
        horloge = new HorlogeJeu(DATE_DEPART);

        royaumeA = new Royaume(1, "Avalon", PEUPLE_A, 1, 100, 0, 0, 0, false, new ArrayList<>());
        royaumeB = new Royaume(2, "Mordor", PEUPLE_B, 1, 100, 0, 0, 0, false, new ArrayList<>());

        partie = new Partie(
                1, "Partie Test", DATE_DEPART, 1.0, 10,
                null, null, new ArrayList<>(), new ArrayList<>(), StatutPartie.EN_ATTENTE
        );
        partie.inscrireRoyaume(royaumeA);
        partie.inscrireRoyaume(royaumeB);
        partie.demarrer();

        moteur = new MoteurJeu(partie, horloge);
    }

    private static int getStock(Royaume royaume, Ressource type) {
        return royaume.getRessources().stream()
                .filter(r -> r.getRessource() == type)
                .mapToInt(QuantiteRessource::getQuantite)
                .sum();
    }

    private static int getEffectifGarnison(Royaume royaume, TypeTroupe type) {
        return royaume.getGarnison().stream()
                .filter(c -> c.getType() == type)
                .mapToInt(ContingentTroupe::getQuantite)
                .sum();
    }

    // =========================================================================
    // 1. Constructeur, validations & avance de l'horloge
    // =========================================================================

    @Nested
    @DisplayName("Constructeur, addAttaque et executerTick")
    class InvariantsTests {

        @Test
        @DisplayName("Le constructeur rejette une partie ou une horloge null")
        void testConstructeurRejetteNull() {
            assertThrows(IllegalArgumentException.class, () -> new MoteurJeu(null, horloge));
            assertThrows(IllegalArgumentException.class, () -> new MoteurJeu(partie, null));
        }

        @Test
        @DisplayName("addAttaque(null) lève IllegalArgumentException")
        void testAddAttaqueNull() {
            assertThrows(IllegalArgumentException.class, () -> moteur.addAttaque(null));
        }

        @Test
        @DisplayName("Les setters rejettent partie, horloge ou liste d'attaques null")
        void testSettersRejettentNull() {
            assertThrows(IllegalArgumentException.class, () -> moteur.setPartie(null));
            assertThrows(IllegalArgumentException.class, () -> moteur.setHorloge(null));
            assertThrows(IllegalArgumentException.class, () -> moteur.setAttaques(null));
        }

        @Test
        @DisplayName("executerTick() rejette une durée null ou négative")
        void testExecuterTickDureeInvalide() {
            assertThrows(IllegalArgumentException.class, () -> moteur.executerTick(null));
            assertThrows(IllegalArgumentException.class, () -> moteur.executerTick(Duration.ofMinutes(-10)));
            assertEquals(DATE_DEPART, horloge.getTempsCourant());
        }

        @Test
        @DisplayName("executerTick() fait bien avancer le temps courant de l'horloge")
        void testExecuterTickAvanceHorloge() {
            moteur.executerTick(Duration.ofMinutes(15));

            assertEquals(DATE_DEPART.plusMinutes(15), horloge.getTempsCourant());
        }
    }

    // =========================================================================
    // 2. Production passive de ressources
    // =========================================================================

    @Nested
    @DisplayName("Production passive de ressources")
    class ProductionPassiveTests {

        @Test
        @DisplayName("Avancer d'1 heure augmente exactement le stock de productionBaseHeure")
        void testProductionUneHeureExacte() {
            BatimentProduction mine = BatimentFactory.creerBatimentProduction(
                    royaumeA, 10, "Mine d'or", 2500, Ressource.OR, 100, 1
            );
            // Rendre la mine opérationnelle avant le tick
            mine.getChantier().finaliser(mine.getChantier().getDateFinPrevue());

            moteur.executerTick(Duration.ofHours(1));

            assertEquals(100, getStock(royaumeA, Ressource.OR));
            assertEquals(0, getStock(royaumeB, Ressource.OR));
        }

        @Test
        @DisplayName("Avancer de 30 minutes crédite la moitié de la production horaire")
        void testProductionDemiHeure() {
            BatimentProduction scierie = BatimentFactory.creerBatimentProduction(
                    royaumeA, 11, "Scierie", 2000, Ressource.BOIS, 60, 1
            );
            scierie.getChantier().finaliser(scierie.getChantier().getDateFinPrevue());

            moteur.executerTick(Duration.ofMinutes(30));

            assertEquals(30, getStock(royaumeA, Ressource.BOIS));
        }

        @Test
        @DisplayName("Un bâtiment en construction ne produit aucune ressource")
        void testBatimentEnConstructionNeProduitPas() {
            BatimentProduction mine = BatimentFactory.creerBatimentProduction(
                    royaumeA, 12, "Mine en travaux", 2500, Ressource.OR, 100, 1
            );
            // On cale l'horloge sur le début du chantier et on n'avance que d'1 minute (< 3 min)
            horloge.initialiser(mine.getChantier().getDateDebut());

            moteur.executerTick(Duration.ofMinutes(1));

            assertTrue(mine.isEnConstruction());
            assertEquals(0, getStock(royaumeA, Ressource.OR));
        }

        @Test
        @DisplayName("Un tick très court produisant 0 ressource ne lève pas d'exception")
        void testTickTresCourtSansErreur() {
            BatimentProduction mine = BatimentFactory.creerBatimentProduction(
                    royaumeA, 13, "Petite mine", 2500, Ressource.OR, 10, 1
            );
            mine.getChantier().finaliser(mine.getChantier().getDateFinPrevue());

            // 10 OR/h pendant 5 secondes = 0.013 -> 0 OR
            moteur.executerTick(Duration.ofSeconds(5));

            assertEquals(0, getStock(royaumeA, Ressource.OR));
        }
    }

    // =========================================================================
    // 3. Chantiers de construction
    // =========================================================================

    @Nested
    @DisplayName("Finalisation des chantiers de construction")
    class ChantiersTests {

        @Test
        @DisplayName("Un chantier s'achève automatiquement quand le temps dépasse sa dateFinPrevue")
        void testChantierSAcheveApresEcheance() {
            // Mine avec 1 ouvrier : tempsBase = 180s (3 minutes)
            BatimentProduction mine = BatimentFactory.creerBatimentProduction(
                    royaumeA, 20, "Mine d'or", 2500, Ressource.OR, 100, 1
            );
            ChantierConstruction chantier = mine.getChantier();
            horloge.initialiser(chantier.getDateDebut());

            moteur.executerTick(Duration.ofMinutes(4));

            assertFalse(mine.isEnConstruction());
            assertNull(mine.getChantier());
            assertTrue(mine.estOperationnel());
            assertEquals(100, royaumeA.getPopulationDisponible());
            assertEquals(0, royaumeA.getPopulationOuvriere());
        }

        @Test
        @DisplayName("Un chantier ne s'achève pas si l'avance de temps est inférieure au temps restant")
        void testChantierNeSAchevePasAvantEcheance() {
            // Mine avec 1 ouvrier : tempsBase = 180s (3 minutes)
            BatimentProduction mine = BatimentFactory.creerBatimentProduction(
                    royaumeA, 21, "Mine d'or", 2500, Ressource.OR, 100, 1
            );
            ChantierConstruction chantier = mine.getChantier();
            horloge.initialiser(chantier.getDateDebut());

            moteur.executerTick(Duration.ofMinutes(2));

            assertTrue(mine.isEnConstruction());
            assertNotNull(mine.getChantier());
            assertFalse(mine.estOperationnel());
            assertEquals(99, royaumeA.getPopulationDisponible());
            assertEquals(1, royaumeA.getPopulationOuvriere());
        }

        @Test
        @DisplayName("Une amélioration de niveau 1 à 2 achevée par le moteur double la production au tick suivant")
        void testAmeliorationBatimentPuisProductionDoublee() {
            // Débloquer le palier 2 du château et fournir les ressources pour améliorer la mine
            royaumeA.getChateau().ameliorer();
            royaumeA.crediterRessources(Ressource.BOIS, 500);
            royaumeA.crediterRessources(Ressource.PIERRE, 500);
            royaumeA.crediterRessources(Ressource.NOURRITURE, 500);
            royaumeA.crediterRessources(Ressource.OR, 500);

            BatimentProduction mine = BatimentFactory.creerBatimentProduction(
                    royaumeA, 22, "Mine d'or", 2500, Ressource.OR, 100, 1
            );
            mine.getChantier().finaliser(mine.getChantier().getDateFinPrevue());

            // Lancer l'amélioration vers le niveau 2 (180s avec 1 ouvrier)
            mine.ameliorer(1, royaumeA);
            int orApresLancement = getStock(royaumeA, Ressource.OR);
            horloge.initialiser(mine.getChantier().getDateDebut());

            // Tick 1 (4 min) : achève le chantier sans produire rétroactivement
            moteur.executerTick(Duration.ofMinutes(4));
            assertEquals(2, mine.getNiveau());
            assertTrue(mine.estOperationnel());
            assertEquals(orApresLancement, getStock(royaumeA, Ressource.OR));

            // Tick 2 (1h) : la mine niveau 2 produit 200 OR/h
            moteur.executerTick(Duration.ofHours(1));
            assertEquals(orApresLancement + 200, getStock(royaumeA, Ressource.OR));
        }
    }

    // =========================================================================
    // 4. Sessions d'entraînement militaire
    // =========================================================================

    @Nested
    @DisplayName("Finalisation des sessions d'entraînement")
    class EntrainementsTests {

        @Test
        @DisplayName("Une session d'entraînement achevée reverse les soldats dans la garnison")
        void testEntrainementTermineReverseDansGarnison() {
            // 2 archers coûtent 20 OR, 20 BOIS, 20 NOURRITURE et durent 10 minutes (5 min * 2)
            royaumeA.crediterRessources(Ressource.OR, 100);
            royaumeA.crediterRessources(Ressource.BOIS, 100);
            royaumeA.crediterRessources(Ressource.NOURRITURE, 100);

            boolean lance = royaumeA.entrainerTroupe(TypeTroupe.ARCHER, 2, horloge.getTempsCourant());
            assertTrue(lance);
            assertEquals(2, royaumeA.getPopulationEnEntrainement());
            assertEquals(0, royaumeA.getPopulationMilitaire());

            // À t+5 min : pas encore terminé
            moteur.executerTick(Duration.ofMinutes(5));
            assertEquals(2, royaumeA.getPopulationEnEntrainement());
            assertEquals(0, getEffectifGarnison(royaumeA, TypeTroupe.ARCHER));

            // À t+11 min : terminé -> bascule en garnison
            moteur.executerTick(Duration.ofMinutes(6));
            assertEquals(0, royaumeA.getPopulationEnEntrainement());
            assertEquals(2, royaumeA.getPopulationMilitaire());
            assertEquals(2, getEffectifGarnison(royaumeA, TypeTroupe.ARCHER));
        }
    }

    // =========================================================================
    // 5. Expéditions militaires (Attaques)
    // =========================================================================

    @Nested
    @DisplayName("Résolution et retour des attaques")
    class AttaquesTests {

        @Test
        @DisplayName("Une attaque passe de ALLER à RETOUR à dateImpact (pertes + butin), puis à TERMINEE à dateRetourPrevue")
        void testAttaqueCycleCompletAllerCombatRetour() {
            // Préparation attaquant : 20 cavaliers (vitesse 15 -> distance 15 = 1h aller, 1h retour)
            royaumeA.ajouterTroupes(TypeTroupe.CAVALIER, 20);
            royaumeA.setPopulationMilitaire(20);

            // Préparation défenseur : 5 archers + 200 OR à piller
            royaumeB.ajouterTroupes(TypeTroupe.ARCHER, 5);
            royaumeB.setPopulationMilitaire(5);
            royaumeB.crediterRessources(Ressource.OR, 200);

            Attaque attaque = royaumeA.lancerAttaque(
                    royaumeB, Map.of(TypeTroupe.CAVALIER, 20), 15.0, horloge.getTempsCourant()
            );
            moteur.addAttaque(attaque);

            // Avant dateImpact (t+30 min) : toujours en ALLER
            moteur.executerTick(Duration.ofMinutes(30));
            assertEquals(StatutAttaque.ALLER, attaque.getStatut());

            // À dateImpact (t+1h) : combat résolu, passage en RETOUR, butin pillé
            moteur.executerTick(Duration.ofMinutes(30));
            assertEquals(StatutAttaque.RETOUR, attaque.getStatut());
            assertTrue(attaque.isVictoireAttaquant());
            assertEquals(0, getStock(royaumeB, Ressource.OR));
            assertFalse(attaque.getButin().isEmpty());
            assertEquals(0, getEffectifGarnison(royaumeA, TypeTroupe.CAVALIER));

            // À dateRetourPrevue (t+2h) : troupes survivantes et butin restitués à l'attaquant
            moteur.executerTick(Duration.ofHours(1));
            assertEquals(StatutAttaque.TERMINEE, attaque.getStatut());
            assertTrue(getEffectifGarnison(royaumeA, TypeTroupe.CAVALIER) > 0);
            assertEquals(200, getStock(royaumeA, Ressource.OR));
        }

        @Test
        @DisplayName("Un tick unique dépassant aller et retour résout le combat et finalise le retour")
        void testAttaqueAllerEtRetourDansUnSeulTick() {
            royaumeA.ajouterTroupes(TypeTroupe.CAVALIER, 20);
            royaumeA.setPopulationMilitaire(20);
            royaumeB.crediterRessources(Ressource.BOIS, 150);

            Attaque attaque = royaumeA.lancerAttaque(
                    royaumeB, Map.of(TypeTroupe.CAVALIER, 20), 15.0, horloge.getTempsCourant()
            );
            moteur.addAttaque(attaque);

            // 1h aller + 1h retour = 2h -> un tick de 3h doit tout boucler
            moteur.executerTick(Duration.ofHours(3));

            assertEquals(StatutAttaque.TERMINEE, attaque.getStatut());
            assertEquals(20, getEffectifGarnison(royaumeA, TypeTroupe.CAVALIER));
            assertEquals(150, getStock(royaumeA, Ressource.BOIS));
        }

        @Test
        @DisplayName("Une attaque perdante passe directement de ALLER à TERMINEE à dateImpact")
        void testAttaquePerdanteTermineeSansRetour() {
            royaumeA.ajouterTroupes(TypeTroupe.ARCHER, 2);
            royaumeA.setPopulationMilitaire(2);

            royaumeB.ajouterTroupes(TypeTroupe.CAVALIER, 30);
            royaumeB.setPopulationMilitaire(30);

            // Archers vitesse 10 -> distance 10 = 1h aller
            Attaque attaque = royaumeA.lancerAttaque(
                    royaumeB, Map.of(TypeTroupe.ARCHER, 2), 10.0, horloge.getTempsCourant()
            );
            moteur.addAttaque(attaque);

            moteur.executerTick(Duration.ofHours(1));

            assertEquals(StatutAttaque.TERMINEE, attaque.getStatut());
            assertFalse(attaque.isVictoireAttaquant());
            assertEquals(0, getEffectifGarnison(royaumeA, TypeTroupe.ARCHER));
        }

        @Test
        @DisplayName("Une attaque déjà TERMINEE ne re-crédite ni troupes ni butin lors des ticks suivants")
        void testAttaqueTermineeIdempotenteSurTicksSuivants() {
            royaumeA.ajouterTroupes(TypeTroupe.CAVALIER, 20);
            royaumeA.setPopulationMilitaire(20);
            royaumeB.crediterRessources(Ressource.OR, 100);

            Attaque attaque = royaumeA.lancerAttaque(
                    royaumeB, Map.of(TypeTroupe.CAVALIER, 20), 15.0, horloge.getTempsCourant()
            );
            moteur.addAttaque(attaque);

            moteur.executerTick(Duration.ofHours(3));
            assertEquals(StatutAttaque.TERMINEE, attaque.getStatut());
            int orApresRetour = getStock(royaumeA, Ressource.OR);
            int cavaliersApresRetour = getEffectifGarnison(royaumeA, TypeTroupe.CAVALIER);

            // Un tick supplémentaire ne doit rien re-créditer
            moteur.executerTick(Duration.ofHours(2));
            assertEquals(StatutAttaque.TERMINEE, attaque.getStatut());
            assertEquals(orApresRetour, getStock(royaumeA, Ressource.OR));
            assertEquals(cavaliersApresRetour, getEffectifGarnison(royaumeA, TypeTroupe.CAVALIER));
        }
    }
}
