
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.*;

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

import royaume.config.ConfigurationPaliers;
import royaume.model.peuple.Canards;
import royaume.model.PalierNiveau;
import royaume.model.peuple.Population;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

@DisplayName("Tests unitaires - Gestion du château")
class ChateauTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static final Population PEUPLE_TEST =
            new Canards(1, "Canards", "Archiduc", 0.05);

    private Chateau chateau;
    private Maison maison;
    private Royaume royaume;

    @BeforeEach
    void setUp() {

        List<QuantiteRessource> ressources = List.of(
                new QuantiteRessource(10000, Ressource.OR),
                new QuantiteRessource(10000, Ressource.PIERRE),
                new QuantiteRessource(10000, Ressource.BOIS),
                new QuantiteRessource(50000, Ressource.NOURRITURE)
        );

        royaume = new Royaume(
                1, "FarFarAway", PEUPLE_TEST,
                1, 500, 500, 200, 100, false, ressources
        );

        chateau = royaume.getChateau();
        chateau.debloquerPalier();

        maison = new Maison(
                2, "Maison Test", 1, 100, 100, false
        );
    }

    // =========================================================================
    // 1. Conditions de victoire
    // =========================================================================

    @Nested
    @DisplayName("Conditions de victoire")
    class ConditionsVictoireTests {

        @Test
        @DisplayName("La victoire est impossible du niveau 1 au niveau 9")
        void verifierConditionsVictoire_RetourneFalseDuNiveau1AuNiveau9() {

            for (int niveau = 1; niveau <= 9; niveau++) {

                assertFalse(
                    chateau.verifierConditionsVictoire(),
                    "La victoire ne doit pas être possible au niveau " + niveau
                );

                chateau.ameliorer();
            }
        }

        @Test
        @DisplayName("La victoire est possible au niveau 10")
        void verifierConditionsVictoire_RetourneTrueAuNiveau10() {

            for (int i = 1; i < 10; i++) {
                chateau.ameliorer();
            }

            assertEquals(10, chateau.getNiveau());
            assertTrue(chateau.verifierConditionsVictoire());
        }
    }

    // =========================================================================
    // 2. Gestion des paliers du château
    // =========================================================================

    @Nested
    @DisplayName("Gestion des paliers du château")
    class PalierTests {

        @Test
        @DisplayName("Débloquer un palier met à jour le niveau débloqué")
        void debloquerPalier_MetAJourLeNiveauDebloque() {

            chateau.ameliorer();

            chateau.debloquerPalier();

            assertEquals(2, chateau.getNiveauPalierDebloque());
        }
    }

    // =========================================================================
    // 3. Amélioration des bâtiments et respect des paliers
    // =========================================================================

    @Nested
    @DisplayName("Amélioration des bâtiments et respect des paliers")
    class AmeliorationBatimentTests {

        @Test
        @DisplayName("Un bâtiment ne peut pas dépasser le niveau du château")
        void testInterdictionDepasserPalierChateau() {

            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> maison.ameliorer(1, royaume)
            );

            assertAll(
                () -> assertEquals(
                    "Impossible d'améliorer : le niveau du bâtiment ne peut pas dépasser le niveau du château.",
                    exception.getMessage()
                ),
                () -> assertEquals(1, maison.getNiveau()),
                () -> assertFalse(maison.isEnConstruction()),
                () -> assertEquals(null, maison.getChantier())
            );
        }

        @Test
        @DisplayName("Un bâtiment peut être amélioré après le déblocage du palier du château")
        void testAmeliorerBatimentApresAmeliorationChateau() {

            chateau.ameliorer();
            chateau.debloquerPalier();

            // Première amélioration : niveau 1 vers niveau 2
            assertDoesNotThrow(() -> {
                maison.ameliorer(1, royaume);
            });

            // Finalisation du premier chantier
            maison.getChantier().finaliser(
                    maison.getChantier().getDateFinPrevue()
            );

            assertEquals(2, maison.getNiveau());

            // Deuxième amélioration : interdite car le palier est à 2
            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> maison.ameliorer(1, royaume)
            );

            assertAll(
                () -> assertEquals(
                    "Impossible d'améliorer : le niveau du bâtiment ne peut pas dépasser le niveau du château.",
                    exception.getMessage()
                ),
                () -> assertEquals(2, maison.getNiveau()),
                () -> assertFalse(maison.isEnConstruction()),
                () -> assertEquals(null, maison.getChantier())
            );
        }
    }

    // =========================================================================
    // 4. Synchronisation du niveau du royaume et du château
    // =========================================================================

    @Nested
    @DisplayName("Synchronisation du niveau du royaume et du château")
    class SynchronisationNiveauTests {

        @Test
        @DisplayName("Le niveau du château évolue avec celui du royaume")
        void testLeNiveauDuChateauMonteAvecLeRoyaume() {

            List<PalierNiveau> paliers =
                    ConfigurationPaliers.getPaliers();

            assertEquals(1, royaume.getNiveau());
            assertEquals(1, royaume.getChateau().getNiveau());

            if (royaume.peutMonterDeNiveau(paliers)) {
                royaume.ameliorerNiveau(paliers);
            }

            assertAll(
                () -> assertEquals(
                    royaume.getNiveau(),
                    royaume.getChateau().getNiveau()
                ),
                () -> assertEquals(
                    royaume.getNiveau(),
                    royaume.getChateau().getNiveauPalierDebloque()
                )
            );
        }
    }

    // =========================================================================
    // 5. Gestion de la destruction du château
    // =========================================================================

    @Nested
    @DisplayName("Gestion de la destruction du château")
    class DestructionChateauTests {

        @Test
        @DisplayName("Le royaume passe en ruine lorsque les points de vie du château atteignent zéro")
        void chateauEnRuineSiPv0() {

            Chateau chateauHS = royaume.getChateau();

            chateauHS.setPointsDeVie(0);

            assertAll(
                () -> assertEquals(
                    0,
                    chateauHS.getPointsDeVie(),
                    "Les points de vie du château doivent être à 0"
                ),
                () -> assertTrue(
                    royaume.estEnRuine(),
                    "Le royaume doit être en ruine (enRuine = true)"
                )
            );
        }
    }

    // =========================================================================
    // 6. Validation Bean Validation
    // =========================================================================

    @Nested
    @DisplayName("Validation des contraintes Bean Validation")
    class ValidationTests {

        @Test
        @DisplayName("Le château initial respecte les contraintes de validation")
        void testValidationChateauInitial() {

            Set<ConstraintViolation<Chateau>> violations =
                    validator.validate(chateau);

            assertTrue(
                violations.isEmpty(),
                "Le château initial ne doit présenter aucune violation : "
                        + violations
            );
        }
    }
}
