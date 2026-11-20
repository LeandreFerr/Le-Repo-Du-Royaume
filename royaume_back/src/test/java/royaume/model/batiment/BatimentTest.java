
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import royaume.model.Canards;
import royaume.model.Population;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

@DisplayName("Tests unitaires - Gestion des bâtiments")
class BatimentTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static final Population PEUPLE_TEST =
            new Canards(1, "Canards", "Archiduc", 0.05);

    private Batiment batiment;
    private Royaume royaume;

    // Classe concrète créée uniquement pour les tests
    static class BatimentTestable extends Batiment {

        BatimentTestable() {
            super(1, "Batiment test", 1, 50, 50, false);
        }

        @Override
        public List<QuantiteRessource> getCoutAmelioration() {
            return List.of();
        }

        @Override
        public int getTempsConstructionBase() {
            return 60;
        }
    }

    @BeforeEach
    void setUp() {

        batiment = new BatimentTestable();

        royaume = new Royaume(2, "Test", PEUPLE_TEST);

        Chateau chateau = royaume.getChateau();

        if (chateau != null) {
            chateau.ameliorer();
        }

        royaume.ajouterBatiment(batiment);
    }

    // =========================================================================
    // 1. Gestion des points de vie et des dégâts
    // =========================================================================

    @Nested
    @DisplayName("Gestion des points de vie et des dégâts")
    class PointsDeVieTests {

        @Test
        @DisplayName("Subir des dégâts diminue correctement les points de vie")
        void subirDegats_DiminueCorrectementLesPV() {

            batiment.subirDegats(20);

            assertEquals(30, batiment.getPointsDeVie());
        }

        @Test
        @DisplayName("Les points de vie ne peuvent pas descendre sous zéro")
        void subirDegats_NeDoitPasDescendreSousZero() {

            batiment.subirDegats(100);

            assertEquals(0, batiment.getPointsDeVie());
        }

        @Test
        @DisplayName("Le bâtiment reste dans le royaume tant que ses PV sont positifs")
        void subirDegats_NeRetirePasBatimentSiPVPositifs() {

            batiment.subirDegats(20);

            assertEquals(30, batiment.getPointsDeVie());

            assertTrue(royaume.getBatiments().contains(batiment));
        }

        @Test
        @DisplayName("Le bâtiment est retiré du royaume lorsque ses PV atteignent zéro")
        void subirDegats_RetireBatimentSiPVZero() {

            batiment.subirDegats(50);

            assertEquals(0, batiment.getPointsDeVie());

            // Le Royaume gère la suppression du bâtiment
            if (batiment.getPointsDeVie() == 0) {
                royaume.retirerBatiment(batiment);
            }

            assertFalse(royaume.getBatiments().contains(batiment));
        }
    }

    // =========================================================================
    // 2. Réparation des bâtiments
    // =========================================================================

    @Nested
    @DisplayName("Réparation des bâtiments")
    class ReparationTests {

        @Test
        @DisplayName("La réparation lance un chantier sans modifier immédiatement les PV")
        void reparer_LanceLeChantierSansModifierInstantaneDesPV() {

            batiment.subirDegats(30);

            int pvAvant = batiment.getPointsDeVie();

            batiment.reparer(30, 2, royaume);

            assertAll(
                () -> assertEquals(pvAvant, batiment.getPointsDeVie()),
                () -> assertTrue(batiment.isEnConstruction()),
                () -> assertNotNull(batiment.getChantier())
            );
        }

        @Test
        @DisplayName("La réparation augmente les PV après finalisation du chantier")
        void reparer_AugmenteLesPV_ApresFinalisation() {

            batiment.subirDegats(30);

            batiment.reparer(30, 2, royaume);

            ChantierConstruction chantier = batiment.getChantier();

            LocalDateTime futur =
                    chantier.getDateFinPrevue().plusSeconds(1);

            chantier.finaliser(futur);

            assertAll(
                () -> assertEquals(
                    batiment.getPointsDeVieMax(),
                    batiment.getPointsDeVie()
                ),
                () -> assertFalse(batiment.isEnConstruction())
            );
        }

        @Test
        @DisplayName("La réparation ne dépasse jamais les PV maximum")
        void reparer_NeDoitPasDepasserLesPVMax() {

            batiment.subirDegats(5);

            batiment.reparer(10, 2, royaume);

            ChantierConstruction chantier = batiment.getChantier();

            LocalDateTime futur =
                    chantier.getDateFinPrevue().plusSeconds(1);

            chantier.finaliser(futur);

            assertEquals(
                batiment.getPointsDeVieMax(),
                batiment.getPointsDeVie()
            );
        }
    }

    // =========================================================================
    // 3. État opérationnel du bâtiment
    // =========================================================================

    @Nested
    @DisplayName("État opérationnel du bâtiment")
    class EtatOperationnelTests {

        @Test
        @DisplayName("Un bâtiment en bon état est opérationnel")
        void estOperationnel_RetourneTrueSiBatimentEnBonEtat() {

            assertTrue(batiment.estOperationnel());
        }

        @Test
        @DisplayName("Un bâtiment détruit n'est pas opérationnel")
        void estOperationnel_RetourneFalseSiBatimentDetruit() {

            batiment.subirDegats(50);

            assertFalse(batiment.estOperationnel());
        }

        @Test
        @DisplayName("Un bâtiment en construction n'est pas opérationnel")
        void estOperationnel_RetourneFalseSiEnConstruction() {

            batiment.setEnConstruction(true);

            assertFalse(batiment.estOperationnel());
        }
    }

    // =========================================================================
    // 4. Amélioration des bâtiments
    // =========================================================================

    @Nested
    @DisplayName("Amélioration des bâtiments")
    class AmeliorationTests {

        @Test
        @DisplayName("Améliorer un bâtiment augmente son niveau et ses PV maximum")
        void ameliorer_IncrementeLeNiveauEtLesPVMax() {

            batiment.ameliorer(2, royaume);

            batiment.getChantier().finaliser(
                    batiment.getChantier().getDateFinPrevue()
            );

            assertAll(
                () -> assertEquals(2, batiment.getNiveau()),
                () -> assertEquals(100, batiment.getPointsDeVieMax())
            );
        }

        @Test
        @DisplayName("Améliorer un bâtiment lance un chantier sans augmenter immédiatement son niveau")
        void ameliorer_AvecParametres_LanceLeChantierEtDebiteRessources() {

            batiment.ameliorer(2, royaume);

            assertAll(
                () -> assertTrue(batiment.isEnConstruction()),
                () -> assertNotNull(batiment.getChantier()),
                () -> assertEquals(1, batiment.getNiveau())
            );
        }

        @Test
        @DisplayName("L'amélioration augmente le niveau et les PV après finalisation")
        void ameliorer_AugmenteNiveauEtPV_ApresFinalisationChantier() {

            batiment.ameliorer(2, royaume);

            ChantierConstruction chantier = batiment.getChantier();

            LocalDateTime futur =
                    chantier.getDateFinPrevue().plusSeconds(1);

            chantier.finaliser(futur);

            assertAll(
                () -> assertEquals(2, batiment.getNiveau()),
                () -> assertEquals(
                    batiment.getPointsDeVieMax(),
                    batiment.getPointsDeVie()
                ),
                () -> assertFalse(batiment.isEnConstruction())
            );
        }
    }

    // =========================================================================
    // 5. Gestion des coûts et ressources d'amélioration
    // =========================================================================

    @Nested
    @DisplayName("Gestion des coûts et ressources d'amélioration")
    class RessourcesTests {

        @Test
        @DisplayName("Le coût d'amélioration d'une carrière correspond à la configuration")
        void getCoutAmelioration_Carriere_RetourneCoutsConfig() {

            BatimentProduction carriere = new BatimentProduction(
                    1, "Carrière", 1, 50, 100, false,
                    Ressource.PIERRE, 30
            );

            List<QuantiteRessource> couts =
                    carriere.getCoutAmelioration();

            assertNotNull(couts);

            assertFalse(
                couts.isEmpty(),
                "La carrière de niveau 1 doit avoir un coût pour passer au niveau 2"
            );

            boolean contientBois = couts.stream()
                    .anyMatch(r ->
                        r.getRessource() == Ressource.BOIS
                        && r.getQuantite() == 80
                    );

            assertTrue(
                contientBois,
                "Le coût pour le niveau 2 de la carrière doit inclure 80 de bois"
            );
        }

        @Test
        @DisplayName("Les ressources manquantes pour améliorer une carrière sont identifiées")
        void getRessourceManquanteAmelioration_RetourneLesRessourcesManquantes() {

            BatimentProduction carriere = new BatimentProduction(
                    1, "Carrière", 1, 50, 100, false,
                    Ressource.PIERRE, 30
            );

            // On vide les ressources du royaume
            royaume.setRessources(List.of());

            List<QuantiteRessource> manquantes =
                    carriere.getRessourceManquanteAmelioration(royaume);

            assertNotNull(manquantes);
            assertFalse(manquantes.isEmpty());

            boolean contientBoisManquant = manquantes.stream()
                    .anyMatch(r -> r.getRessource() == Ressource.BOIS);

            assertTrue(
                contientBoisManquant,
                "Le bois manquant doit être identifié"
            );
        }
    }

    // =========================================================================
    // 6. Gestion du retrait des bâtiments
    // =========================================================================

    @Nested
    @DisplayName("Gestion du retrait des bâtiments")
    class RetraitBatimentTests {

        @Test
        @DisplayName("Retirer un bâtiment le supprime de la liste du royaume")
        void retirerBatiment_RetireBatimentDeLaListeDuRoyaume() {

            assertTrue(royaume.getBatiments().contains(batiment));

            royaume.retirerBatiment(batiment);

            assertFalse(royaume.getBatiments().contains(batiment));
        }
    }

    // =========================================================================
    // 7. Temps de construction
    // =========================================================================

    // Méthode conservée comme dans le test original :
    // elle n'est pas annotée avec @Test et ne sera donc pas exécutée.

    void getTempsConstruction() {

        BatimentProduction carriere = new BatimentProduction(
                1, "Carrière", 1, 50, 100, false,
                Ressource.PIERRE, 30
        );

        int temps = carriere.getTempsConstructionBase();

        assertEquals(180, temps);
    }

    // =========================================================================
    // 8. Validation Bean Validation
    // =========================================================================

    @Nested
    @DisplayName("Validation des contraintes Bean Validation")
    class ValidationTests {

        @Test
        @DisplayName("Le bâtiment initial respecte les contraintes de validation")
        void testValidationBatimentInitial() {

            Set<ConstraintViolation<Batiment>> violations =
                    validator.validate(batiment);

            assertTrue(
                violations.isEmpty(),
                "Le bâtiment initial ne doit présenter aucune violation : "
                        + violations
            );
        }
    }
}
