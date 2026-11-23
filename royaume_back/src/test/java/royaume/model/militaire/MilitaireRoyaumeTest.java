package royaume.model.militaire;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import royaume.model.peuple.Canards;
import royaume.model.peuple.Population;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

@DisplayName("Tests unitaires — Module 5 : Système militaire")
class MilitaireRoyaumeTest {

    private static final Population PEUPLE_TEST = new Canards(null, "Canards", "Archiduc", 0.05);
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 12, 0);

    private Royaume attaquant;
    private Royaume defenseur;

    @BeforeEach
    void setUp() {
        attaquant = new Royaume(1, "Attaquant", PEUPLE_TEST);
        defenseur = new Royaume(2, "Defenseur", PEUPLE_TEST);
    }

    // ------------------------------------------------------------- helpers

    private static int stock(Royaume r, Ressource type) {
        return r.getRessources().stream()
                .filter(q -> q.getRessource() == type)
                .mapToInt(QuantiteRessource::getQuantite)
                .sum();
    }

    private static int troupes(Royaume r, TypeTroupe type) {
        return r.getGarnison().stream()
                .filter(c -> c.getType() == type)
                .mapToInt(ContingentTroupe::getQuantite)
                .sum();
    }

    private static int total(List<QuantiteRessource> liste) {
        return liste.stream().mapToInt(QuantiteRessource::getQuantite).sum();
    }

    /** Coût de 5 lanciers : 50 OR, 25 BOIS, 50 NOURRITURE. */
    private static void doterPourCinqLanciers(Royaume r) {
        r.crediterRessources(Ressource.OR, 50);
        r.crediterRessources(Ressource.BOIS, 25);
        r.crediterRessources(Ressource.NOURRITURE, 50);
    }

    /** 10 cavaliers (attaque 150) contre 4 SPECIAL (défense 80) : SPECIAL est neutre dans le triangle. */
    private Attaque preparerAttaqueGagnante() {
        attaquant.ajouterTroupes(TypeTroupe.CAVALIER, 10);
        attaquant.setPopulationMilitaire(10);
        defenseur.ajouterTroupes(TypeTroupe.SPECIAL, 4);
        defenseur.setPopulationMilitaire(4);
        return attaquant.lancerAttaque(defenseur, Map.of(TypeTroupe.CAVALIER, 10), 20, T0);
    }

    // =========================================================================
    // 1. Attaque : trajet et capacité
    // =========================================================================

    @Nested
    @DisplayName("Attaque : temps de trajet et capacité de pillage")
    class TrajetEtCapaciteTests {

        @Test
        @DisplayName("Le trajet est calculé sur l'unité la plus lente du convoi")
        void testTempsAllerUniteLaPlusLente() {
            // Given : lancier (vitesse 10) + cavalier (vitesse 15)
            Attaque a = new Attaque(attaquant, defenseur,
                    List.of(new ContingentTroupe(TypeTroupe.LANCIER, 3),
                            new ContingentTroupe(TypeTroupe.CAVALIER, 3)),
                    T0, 20);

            // Then : 20 / 10 = 2 h
            assertAll(
                () -> assertEquals(Duration.ofHours(2), a.calculerTempsAller(20)),
                () -> assertEquals(T0.plusHours(2), a.getDateImpact())
            );
        }

        @Test
        @DisplayName("Un convoi de cavaliers seuls va plus vite")
        void testTempsAllerCavaliersSeuls() {
            Attaque a = new Attaque(attaquant, defenseur,
                    List.of(new ContingentTroupe(TypeTroupe.CAVALIER, 3)), T0, 30);

            assertEquals(Duration.ofHours(2), a.calculerTempsAller(30)); // 30 / 15
        }

        @Test
        @DisplayName("Un convoi vide est refusé")
        void testConvoiVideRefuse() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Attaque(attaquant, defenseur, List.of(), T0, 10));
        }

        @Test
        @DisplayName("La capacité de pillage est la somme quantité x capacité de transport")
        void testCapacitePillage() {
            // 10 lanciers * 25 + 4 cavaliers * 50 = 450
            Attaque a = new Attaque(attaquant, defenseur,
                    List.of(new ContingentTroupe(TypeTroupe.LANCIER, 10),
                            new ContingentTroupe(TypeTroupe.CAVALIER, 4)),
                    T0, 10);

            assertEquals(450, a.calculerCapacitePillage());
        }
    }

    // =========================================================================
    // 2. Royaume : entraînement et formation
    // =========================================================================

    @Nested
    @DisplayName("Royaume : entraînement et formation des troupes")
    class EntrainementTests {

        @Test
        @DisplayName("entrainerTroupe bascule les citoyens en entraînement et débite le coût")
        void testEntrainerNominal() {
            // Given
            doterPourCinqLanciers(attaquant);

            // When
            boolean ok = attaquant.entrainerTroupe(TypeTroupe.LANCIER, 5, T0);

            // Then
            assertAll(
                () -> assertTrue(ok),
                () -> assertEquals(95, attaquant.getPopulationDisponible()),
                () -> assertEquals(5, attaquant.getPopulationEnEntrainement()),
                () -> assertEquals(0, stock(attaquant, Ressource.OR)),
                () -> assertEquals(0, stock(attaquant, Ressource.BOIS)),
                () -> assertEquals(0, stock(attaquant, Ressource.NOURRITURE))
            );
        }

        @Test
        @DisplayName("entrainerTroupe refuse si la population disponible est insuffisante")
        void testEntrainerPopulationInsuffisante() {
            attaquant.setPopulationDisponible(3);
            doterPourCinqLanciers(attaquant);

            assertAll(
                () -> assertFalse(attaquant.entrainerTroupe(TypeTroupe.LANCIER, 5, T0)),
                () -> assertEquals(3, attaquant.getPopulationDisponible()),
                () -> assertEquals(0, attaquant.getPopulationEnEntrainement()),
                () -> assertEquals(50, stock(attaquant, Ressource.OR), "Aucun débit")
            );
        }

        @Test
        @DisplayName("entrainerTroupe refuse si les ressources manquent, sans paiement partiel")
        void testEntrainerRessourcesInsuffisantes() {
            attaquant.crediterRessources(Ressource.OR, 50); // il manque bois et nourriture

            assertAll(
                () -> assertFalse(attaquant.entrainerTroupe(TypeTroupe.LANCIER, 5, T0)),
                () -> assertEquals(50, stock(attaquant, Ressource.OR)),
                () -> assertEquals(100, attaquant.getPopulationDisponible()),
                () -> assertEquals(0, attaquant.getPopulationEnEntrainement())
            );
        }

        @Test
        @DisplayName("recruterTroupe (existante) continue de fonctionner pour 1 recrue")
        void testRecruterTroupeCompatibilite() {
            attaquant.crediterRessources(Ressource.OR, 10);
            attaquant.crediterRessources(Ressource.BOIS, 5);
            attaquant.crediterRessources(Ressource.NOURRITURE, 10);

            assertAll(
                () -> assertTrue(attaquant.recruterTroupe(TypeTroupe.LANCIER)),
                () -> assertEquals(99, attaquant.getPopulationDisponible()),
                () -> assertEquals(1, attaquant.getPopulationEnEntrainement())
            );
        }

        @Test
        @DisplayName("Avant la date de fin, la formation ne change rien")
        void testFormationAvantLaFin() {
            doterPourCinqLanciers(attaquant);
            attaquant.entrainerTroupe(TypeTroupe.LANCIER, 5, T0);

            attaquant.traiterFormations(T0.plusMinutes(1)); // durée : 25 min

            assertAll(
                () -> assertEquals(5, attaquant.getPopulationEnEntrainement()),
                () -> assertEquals(0, attaquant.getPopulationMilitaire()),
                () -> assertEquals(0, troupes(attaquant, TypeTroupe.LANCIER))
            );
        }

        @Test
        @DisplayName("Après la date de fin, les recrues passent en populationMilitaire et en garnison")
        void testFormationApresLaFin() {
            doterPourCinqLanciers(attaquant);
            attaquant.entrainerTroupe(TypeTroupe.LANCIER, 5, T0);

            attaquant.traiterFormations(T0.plusHours(1));

            assertAll(
                () -> assertEquals(0, attaquant.getPopulationEnEntrainement()),
                () -> assertEquals(5, attaquant.getPopulationMilitaire()),
                () -> assertEquals(5, troupes(attaquant, TypeTroupe.LANCIER))
            );
        }

        @Test
        @DisplayName("Une formation ne peut être transférée qu'une seule fois")
        void testFormationNeSeTransfereQuUneFois() {
            doterPourCinqLanciers(attaquant);
            attaquant.entrainerTroupe(TypeTroupe.LANCIER, 5, T0);

            attaquant.traiterFormations(T0.plusHours(1));
            attaquant.traiterFormations(T0.plusHours(2));

            assertAll(
                () -> assertEquals(5, attaquant.getPopulationMilitaire()),
                () -> assertEquals(5, troupes(attaquant, TypeTroupe.LANCIER))
            );
        }
    }

    // =========================================================================
    // 3. Lancement et résolution du combat
    // =========================================================================

    @Nested
    @DisplayName("Lancement d'attaque et résolution du combat")
    class CombatTests {

        @Test
        @DisplayName("lancerAttaque retire les troupes envoyées de la garnison")
        void testLancerAttaqueRetireDeLaGarnison() {
            attaquant.ajouterTroupes(TypeTroupe.CAVALIER, 10);

            attaquant.lancerAttaque(defenseur, Map.of(TypeTroupe.CAVALIER, 4), 20, T0);

            assertEquals(6, troupes(attaquant, TypeTroupe.CAVALIER));
        }

        @Test
        @DisplayName("lancerAttaque refuse si l'effectif est insuffisant et ne retire rien")
        void testLancerAttaqueEffectifInsuffisant() {
            attaquant.ajouterTroupes(TypeTroupe.CAVALIER, 3);

            assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> attaquant.lancerAttaque(defenseur, Map.of(TypeTroupe.CAVALIER, 4), 20, T0)),
                () -> assertEquals(3, troupes(attaquant, TypeTroupe.CAVALIER))
            );
        }

        @Test
        @DisplayName("Victoire de l'attaquant : pertes proportionnelles, défenseur anéanti, statut RETOUR")
        void testResoudreCombatVictoire() {
            Attaque a = preparerAttaqueGagnante();

            a.resoudreCombat();

            // ratio de perte attaquant = 80 / 150 ≈ 0.53 -> 5 cavaliers perdus, 5 survivants
            assertAll(
                () -> assertTrue(a.isVictoireAttaquant()),
                () -> assertEquals(5, a.getTroupesEnvoyees().get(0).getQuantite()),
                () -> assertEquals(5, attaquant.getPopulationMilitaire()),
                () -> assertEquals(0, troupes(defenseur, TypeTroupe.SPECIAL)),
                () -> assertEquals(0, defenseur.getPopulationMilitaire()),
                () -> assertEquals(StatutAttaque.RETOUR, a.getStatut()),
                () -> assertEquals(T0.plusHours(2).plusMinutes(40), a.getDateRetourPrevue())
            );
        }

        @Test
        @DisplayName("Défaite de l'attaquant : il est anéanti, le défenseur perd proportionnellement, statut TERMINEE")
        void testResoudreCombatDefaite() {
            attaquant.ajouterTroupes(TypeTroupe.CAVALIER, 2);   // attaque 30
            defenseur.ajouterTroupes(TypeTroupe.LANCIER, 5);    // défense 75
            Attaque a = attaquant.lancerAttaque(defenseur, Map.of(TypeTroupe.CAVALIER, 2), 20, T0);

            a.resoudreCombat();

            // ratio de perte défenseur = 30 / 75 = 0.4 -> perd 2 lanciers
            assertAll(
                () -> assertFalse(a.isVictoireAttaquant()),
                () -> assertEquals(0, a.getTroupesEnvoyees().get(0).getQuantite()),
                () -> assertEquals(4, troupes(defenseur, TypeTroupe.LANCIER)),
                () -> assertEquals(StatutAttaque.TERMINEE, a.getStatut()),
                () -> assertTrue(a.getButin().isEmpty())
            );
        }

        @Test
        @DisplayName("Un combat ne peut pas être résolu deux fois")
        void testResoudreCombatDeuxFois() {
            Attaque a = preparerAttaqueGagnante();
            a.resoudreCombat();

            assertThrows(IllegalStateException.class, a::resoudreCombat);
        }
    }

    @Test
    @DisplayName("Triangle : 3 archers (60) battent 5 lanciers (75) car l'archer écrase le lancier")
    void testTriangleArcherBatLancier() {
        attaquant.ajouterTroupes(TypeTroupe.ARCHER, 3);
        defenseur.ajouterTroupes(TypeTroupe.LANCIER, 5);
        Attaque a = attaquant.lancerAttaque(defenseur, Map.of(TypeTroupe.ARCHER, 3), 20, T0);

        a.resoudreCombat();

        // attaque 3 x 20 x 1.5 = 90 ; défense 5 x 15 x 0.75 = 56.25
        assertTrue(a.isVictoireAttaquant());
    }

    @Test
    @DisplayName("Multiplicateurs : archer > lancier > cavalier > archer, SPECIAL neutre")
    void testMultiplicateurs() {
        assertAll(
            () -> assertEquals(1.2, TypeTroupe.ARCHER.multiplicateurContre(TypeTroupe.LANCIER)),
            () -> assertEquals(1.2, TypeTroupe.LANCIER.multiplicateurContre(TypeTroupe.CAVALIER)),
            () -> assertEquals(1.2, TypeTroupe.CAVALIER.multiplicateurContre(TypeTroupe.ARCHER)),
            () -> assertEquals(0.8, TypeTroupe.LANCIER.multiplicateurContre(TypeTroupe.ARCHER)),
            () -> assertEquals(1.0, TypeTroupe.SPECIAL.multiplicateurContre(TypeTroupe.ARCHER)),
            () -> assertEquals(1.0, TypeTroupe.ARCHER.multiplicateurContre(TypeTroupe.ARCHER))
        );
    }



    // =========================================================================
    // 4. Pillage et retour
    // =========================================================================

    @Nested
    @DisplayName("Pillage et retour à la base")
    class PillageEtRetourTests {

        @Test
        @DisplayName("subirPillage respecte la capacité d'emport")
        void testPillageLimiteParLaCapacite() {
            defenseur.crediterRessources(Ressource.OR, 100);

            List<QuantiteRessource> pris = defenseur.subirPillage(30);

            assertAll(
                () -> assertEquals(30, total(pris)),
                () -> assertEquals(70, stock(defenseur, Ressource.OR))
            );
        }

        @Test
        @DisplayName("subirPillage ne dépasse pas les stocks réels")
        void testPillageLimiteParLesStocks() {
            defenseur.crediterRessources(Ressource.OR, 100);

            List<QuantiteRessource> pris = defenseur.subirPillage(500);

            assertAll(
                () -> assertEquals(100, total(pris)),
                () -> assertEquals(0, stock(defenseur, Ressource.OR))
            );
        }

        @Test
        @DisplayName("Après une victoire, le butin est limité à la capacité des survivants")
        void testButinLimiteParLesSurvivants() {
            defenseur.crediterRessources(Ressource.OR, 1000);
            Attaque a = preparerAttaqueGagnante();

            a.resoudreCombat();

            // 5 cavaliers survivants * 50 = 250
            assertAll(
                () -> assertEquals(250, total(a.getButin())),
                () -> assertEquals(750, stock(defenseur, Ressource.OR))
            );
        }

        @Test
        @DisplayName("receptionnerRetour reverse les survivants et le butin au royaume attaquant")
        void testReceptionnerRetour() {
            defenseur.crediterRessources(Ressource.OR, 100);
            defenseur.crediterRessources(Ressource.BOIS, 50);
            Attaque a = preparerAttaqueGagnante();
            a.resoudreCombat();

            a.receptionnerRetour();

            assertAll(
                () -> assertEquals(5, troupes(attaquant, TypeTroupe.CAVALIER)),
                () -> assertEquals(100, stock(attaquant, Ressource.OR)),
                () -> assertEquals(50, stock(attaquant, Ressource.BOIS)),
                () -> assertEquals(StatutAttaque.TERMINEE, a.getStatut())
            );
        }

        @Test
        @DisplayName("receptionnerRetour est refusé hors de la phase RETOUR")
        void testReceptionnerRetourHorsPhase() {
            Attaque a = preparerAttaqueGagnante();

            assertThrows(IllegalStateException.class, a::receptionnerRetour);
        }
    }


    // =========================================================================
    // 5. Triangle de force
    // =========================================================================

    @Nested
    @DisplayName("Triangle de force des troupes")
    class TriangleTests {

        private static final double DELTA = 1e-9;

        @Test
        @DisplayName("Multiplicateurs : archer > lancier > cavalier > archer (x1.2 / x0.8), SPECIAL neutre")
        void testMultiplicateurs() {
            assertAll(
                () -> assertEquals(1.2, TypeTroupe.ARCHER.multiplicateurContre(TypeTroupe.LANCIER), DELTA),
                () -> assertEquals(1.2, TypeTroupe.LANCIER.multiplicateurContre(TypeTroupe.CAVALIER), DELTA),
                () -> assertEquals(1.2, TypeTroupe.CAVALIER.multiplicateurContre(TypeTroupe.ARCHER), DELTA),
                () -> assertEquals(0.8, TypeTroupe.LANCIER.multiplicateurContre(TypeTroupe.ARCHER), DELTA),
                () -> assertEquals(0.8, TypeTroupe.CAVALIER.multiplicateurContre(TypeTroupe.LANCIER), DELTA),
                () -> assertEquals(0.8, TypeTroupe.ARCHER.multiplicateurContre(TypeTroupe.CAVALIER), DELTA),
                () -> assertEquals(1.0, TypeTroupe.SPECIAL.multiplicateurContre(TypeTroupe.ARCHER), DELTA),
                () -> assertEquals(1.0, TypeTroupe.ARCHER.multiplicateurContre(TypeTroupe.ARCHER), DELTA)
            );
        }

        @Test
        @DisplayName("Triangle : 3 archers (60 brut) battent 5 lanciers (75 brut) car l'archer écrase le lancier")
        void testArcherBatLancier() {
            attaquant.ajouterTroupes(TypeTroupe.ARCHER, 3);
            defenseur.ajouterTroupes(TypeTroupe.LANCIER, 5);
            Attaque a = attaquant.lancerAttaque(defenseur, Map.of(TypeTroupe.ARCHER, 3), 20, T0);

            a.resoudreCombat();

            // attaque 3 x 20 x 1.2 = 72 ; défense 5 x 15 x 0.8 = 60
            // Sans triangle, l'attaquant perdrait (60 < 75) : le test prouve que la règle joue.
            assertTrue(a.isVictoireAttaquant());
        }

        @Test
        @DisplayName("Triangle inversé : 3 archers (60 brut) perdent contre 5 cavaliers (50 brut) car le cavalier écrase l'archer")
        void testCavalierBatArcher() {
            attaquant.ajouterTroupes(TypeTroupe.ARCHER, 3);
            defenseur.ajouterTroupes(TypeTroupe.CAVALIER, 5);
            Attaque a = attaquant.lancerAttaque(defenseur, Map.of(TypeTroupe.ARCHER, 3), 20, T0);

            a.resoudreCombat();

            // attaque 3 x 20 x 0.8 = 48 ; défense 5 x 10 x 1.2 = 60
            // Sans triangle, l'attaquant gagnerait (60 > 50) : le test prouve que la règle joue.
            assertFalse(a.isVictoireAttaquant());
        }

        @Test
        @DisplayName("SPECIAL est neutre : même puissance qu'en brut, des deux côtés")
        void testSpecialNeutre() {
            attaquant.ajouterTroupes(TypeTroupe.SPECIAL, 5);       // 5 x 20 = 100
            defenseur.ajouterTroupes(TypeTroupe.LANCIER, 6);       // 6 x 15 x 1.0 = 90
            Attaque a = attaquant.lancerAttaque(defenseur, Map.of(TypeTroupe.SPECIAL, 5), 20, T0);

            a.resoudreCombat();

            assertTrue(a.isVictoireAttaquant()); // 100 > 90
        }
    }
}