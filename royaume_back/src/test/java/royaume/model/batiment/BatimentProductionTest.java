
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
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

import royaume.model.peuple.Canards;
import royaume.model.peuple.Population;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

@DisplayName("Tests unitaires - Bâtiments de production")
class BatimentProductionTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static final Population PEUPLE_TEST =
            new Canards(1, "Canards", "Archiduc", 0.05);

    private BatimentProduction mine;
    private Royaume royaume;

    @BeforeEach
    void setUp() {

        List<QuantiteRessource> ressources = new ArrayList<>();

        ressources.add(new QuantiteRessource(1000, Ressource.OR));
        ressources.add(new QuantiteRessource(1000, Ressource.PIERRE));
        ressources.add(new QuantiteRessource(1000, Ressource.BOIS));
        ressources.add(new QuantiteRessource(7000, Ressource.NOURRITURE));

        royaume = new Royaume(
            1, "Gondor", PEUPLE_TEST, 1,
            500, 500, 0, 100,
            false, ressources
        );

        // Passe au niveau 2 le château
        Chateau chateau = royaume.getChateau();
        chateau.ameliorer();

        mine = BatimentFactory.creerBatimentProduction(
                royaume, 2, "Mine d'or", 2500,
                Ressource.OR, 100, 2
        );
    }

    // =========================================================================
    // 1. Calcul de la production
    // =========================================================================

    @Nested
    @DisplayName("Calcul de la production")
    class CalculProductionTests {

        @Test
        @DisplayName("La mine retourne sa production de base après finalisation du chantier")
        void calculerProduction_RetourneProductionDeBase() {

            // On finalise le chantier sinon le bâtiment n'est pas opérationnel

            mine.getChantier().finaliser(
                    mine.getChantier().getDateFinPrevue()
            );

            assertEquals(100, mine.calculerProduction());
        }

        @Test
        @DisplayName("La production augmente avec le niveau du bâtiment")
        void calculerProduction_AugmenteAvecLeNiveau() {

            mine.getChantier().finaliser(
                    mine.getChantier().getDateFinPrevue()
            );

            // On améliore la mine après avoir finalisé sa construction initiale

            mine.ameliorer(2, royaume);

            mine.getChantier().finaliser(
                    mine.getChantier().getDateFinPrevue()
            );

            assertAll(
                () -> assertEquals(2, mine.getNiveau()),
                () -> assertEquals(200, mine.calculerProduction())
            );
        }

        @Test
        @DisplayName("La production est nulle si le bâtiment est détruit")
        void calculerProduction_RetourneZeroSiBatimentDetruit() {

            mine.subirDegats(50);

            assertEquals(0, mine.calculerProduction());
        }

        @Test
        @DisplayName("La production est nulle si le bâtiment est en construction")
        void calculerProduction_RetourneZeroSiEnConstruction() {

            mine.setEnConstruction(true);

            assertEquals(0, mine.calculerProduction());
        }
    }

    // =========================================================================
    // 2. Validation Bean Validation
    // =========================================================================

    @Nested
    @DisplayName("Validation des contraintes Bean Validation")
    class ValidationTests {

        @Test
        @DisplayName("La mine initiale respecte les contraintes de validation")
        void testValidationMineInitiale() {

            Set<ConstraintViolation<BatimentProduction>> violations =
                    validator.validate(mine);

            assertTrue(
                violations.isEmpty(),
                "La mine initiale ne doit présenter aucune violation : "
                        + violations
            );
        }
    }
}
