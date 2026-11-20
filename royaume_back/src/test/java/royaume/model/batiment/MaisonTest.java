
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

import royaume.model.Canards;
import royaume.model.Population;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

@DisplayName("Tests unitaires - Gestion des maisons")
class MaisonTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static final Population PEUPLE_TEST =
            new Canards(1, "Canards", "Archiduc", 0.05);

    private Maison maison;
    private Royaume royaume;

    @BeforeEach
    void setUp() {

        royaume = new Royaume(1, "Royaume test", PEUPLE_TEST);

        royaume.crediterRessources(Ressource.BOIS, 40);
        royaume.crediterRessources(Ressource.PIERRE, 40);
        royaume.crediterRessources(Ressource.OR, 20);
        royaume.crediterRessources(Ressource.NOURRITURE, 100);

        // Passe au niveau 2 le château
        Chateau chateau = royaume.getChateau();
        chateau.ameliorer();

        maison = new Maison(
                1,
                "Maison paysanne",
                1,
                50,
                50,
                false
        );

        royaume.ajouterBatiment(maison);

        // Initialiser le bonus de la maison
        maison.actualiserBonus(royaume);
    }

    // =========================================================================
    // 1. Gestion des coûts d'amélioration
    // =========================================================================

    @Nested
    @DisplayName("Gestion des coûts d'amélioration")
    class CoutAmeliorationTests {

        @Test
        @DisplayName("Le coût du niveau suivant correspond à la configuration")
        void getCoutAmelioration_NiveauSuivant_RetourneLesCoutsDeLaConfiguration() {

            List<QuantiteRessource> couts =
                    maison.getCoutAmelioration();

            assertNotNull(couts);
            assertFalse(couts.isEmpty());

            boolean contientBois = couts.stream()
                    .anyMatch(r ->
                        r.getRessource() == Ressource.BOIS
                        && r.getQuantite() == 10
                    );

            assertTrue(contientBois);
        }
    }

    // =========================================================================
    // 2. Gestion du bonus de croissance
    // =========================================================================

    @Nested
    @DisplayName("Gestion du bonus de croissance")
    class BonusCroissanceTests {

        @Test
        @DisplayName("Actualiser le bonus ajoute le bonus de la maison au royaume")
        void actualiserBonus_AjouteBonusMaisonAuRoyaume() {

            assertEquals(0.12, royaume.getBonusCroissance(), 0.0001);
        }

        @Test
        @DisplayName("Plusieurs maisons cumulent leurs bonus de croissance")
        void actualiserBonus_AjouteBonusDePlusieursMaisons() {

            Maison maison2 = new Maison(
                    2, "Maison 2", 1, 50, 50, false
            );

            Maison maison3 = new Maison(
                    3, "Maison 3", 1, 50, 50, false
            );

            royaume.ajouterBatiment(maison2);
            royaume.ajouterBatiment(maison3);

            maison2.actualiserBonus(royaume);
            maison3.actualiserBonus(royaume);

            assertEquals(0.36, royaume.getBonusCroissance(), 0.0001);
        }

        @Test
        @DisplayName("Actualiser plusieurs fois une maison ne double pas son bonus")
        void actualiserBonus_NeDoitPasAjouterDeuxFoisLeMemeBonus() {

            maison.actualiserBonus(royaume);

            assertEquals(0.12, royaume.getBonusCroissance(), 0.0001);
        }

        @Test
        @DisplayName("Le bonus de croissance augmente après amélioration de la maison")
        void actualiserBonus_AugmenteBonusApresAmeliorationMaison() {

            maison.ameliorer(2, royaume);

            maison.getChantier().finaliser(
                    maison.getChantier().getDateFinPrevue()
            );

            assertEquals(0.24, royaume.getBonusCroissance(), 0.0001);
        }

        @Test
        @DisplayName("Le bonus de croissance est indépendant pour chaque royaume")
        void bonusCroissance_EstIndependantPourChaqueRoyaume() {

            Royaume royaume2 = new Royaume(
                    2, "Deuxième royaume", PEUPLE_TEST
            );

            Maison maison2 = new Maison(
                    2, "Maison autre royaume", 1, 50, 50, false
            );

            royaume2.ajouterBatiment(maison2);
            maison2.actualiserBonus(royaume2);

            assertEquals(0.12, royaume.getBonusCroissance(), 0.0001);
            assertEquals(0.12, royaume2.getBonusCroissance(), 0.0001);

            // Détruire une maison dans le premier royaume
            royaume.recevoirDegats(maison, 50);

            assertEquals(0.0, royaume.getBonusCroissance(), 0.0001);

            // Le deuxième royaume n'est pas affecté
            assertEquals(0.12, royaume2.getBonusCroissance(), 0.0001);
        }
    }

    // =========================================================================
    // 3. Retrait et destruction des maisons
    // =========================================================================

    @Nested
    @DisplayName("Retrait et destruction des maisons")
    class DestructionMaisonTests {

        @Test
        @DisplayName("Retirer une maison retire son bonus du royaume")
        void retirerBatiment_RetireBonusMaisonDuRoyaume() {

            assertEquals(0.12, royaume.getBonusCroissance(), 0.0001);

            royaume.retirerBatiment(maison);

            assertEquals(0.0, royaume.getBonusCroissance(), 0.0001);

            assertFalse(royaume.getBatiments().contains(maison));
        }

        @Test
        @DisplayName("Détruire une maison retire automatiquement son bonus")
        void detruireMaison_RetireAutomatiquementSonBonus() {

            royaume.recevoirDegats(maison, 50);

            assertEquals(0, maison.getPointsDeVie());

            assertFalse(royaume.getBatiments().contains(maison));

            assertEquals(0.0, royaume.getBonusCroissance(), 0.0001);
        }

        @Test
        @DisplayName("Détruire une maison ne retire pas le bonus des autres maisons")
        void detruireUneMaison_NeRetirePasLeBonusDesAutresMaisons() {

            Maison maison2 = new Maison(
                    2, "Maison 2", 1, 50, 50, false
            );

            royaume.ajouterBatiment(maison2);
            maison2.actualiserBonus(royaume);

            assertEquals(0.24, royaume.getBonusCroissance(), 0.0001);

            // Détruire uniquement la première maison
            royaume.recevoirDegats(maison, 50);

            // Le bonus de la deuxième maison doit rester
            assertEquals(0.12, royaume.getBonusCroissance(), 0.0001);

            assertTrue(royaume.getBatiments().contains(maison2));
            assertFalse(royaume.getBatiments().contains(maison));
        }
    }

    // =========================================================================
    // 4. Validation Bean Validation
    // =========================================================================

    @Nested
    @DisplayName("Validation des contraintes Bean Validation")
    class ValidationTests {

        @Test
        @DisplayName("La maison initiale respecte les contraintes de validation")
        void testValidationMaisonInitiale() {

            Set<ConstraintViolation<Maison>> violations =
                    validator.validate(maison);

            assertTrue(
                violations.isEmpty(),
                "La maison initiale ne doit présenter aucune violation : "
                        + violations
            );
        }
    }
}
