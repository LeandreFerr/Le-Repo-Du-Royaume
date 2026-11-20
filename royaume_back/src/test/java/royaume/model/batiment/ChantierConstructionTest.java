
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
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
import royaume.model.Ressource;
import royaume.model.Royaume;

@DisplayName("Tests unitaires - Gestion des chantiers de construction")
class ChantierConstructionTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static final Population PEUPLE_TEST =
            new Canards(1, "Canards", "Archiduc", 0.05);

    private Royaume royaume;
    private ChantierConstruction chantier;
    private BatimentProduction mine;

    @BeforeEach
    void setUp() {

        royaume = new Royaume(1, "Test", PEUPLE_TEST);

        mine = BatimentFactory.creerBatimentProduction(
                royaume, 2, "Mine d'or", 2500,
                Ressource.OR, 100, 2
        );

        chantier = mine.getChantier();
    }

    // =========================================================================
    // 1. Création et initialisation du chantier
    // =========================================================================

    @Nested
    @DisplayName("Création et initialisation du chantier")
    class CreationChantierTests {

        @Test
        @DisplayName("La création du chantier initialise correctement ses attributs")
        void creerChantier() {

            assertAll(
                () -> assertNotNull(chantier),
                () -> assertEquals(mine, chantier.getBatiment()),
                () -> assertNotNull(chantier.getDateDebut()),
                () -> assertNotNull(chantier.getDateFinPrevue()),
                () -> assertTrue(mine.isEnConstruction()),
                () -> assertSame(chantier, mine.getChantier())
            );
        }
    }

    // =========================================================================
    // 2. Gestion des ouvriers
    // =========================================================================

    @Nested
    @DisplayName("Gestion des ouvriers")
    class GestionOuvriersTests {

        @Test
        @DisplayName("Affecter des ouvriers avance la date de fin du chantier")
        void assignerOuvriersAvanceDateFin() {

            LocalDateTime ancienneDate =
                    chantier.getDateFinPrevue();

            chantier.assignerOuvriers(2);

            assertAll(
                () -> assertEquals(4, chantier.getOuvriersAssignes()),
                () -> assertTrue(
                    chantier.getDateFinPrevue().isBefore(ancienneDate)
                ),
                () -> assertEquals(96, royaume.getPopulationDisponible()),
                () -> assertEquals(4, royaume.getPopulationOuvriere())
            );
        }

        @Test
        @DisplayName("Retirer des ouvriers repousse la date de fin du chantier")
        void retirerOuvriersRepousseDateFin() {

            chantier.assignerOuvriers(2);

            LocalDateTime ancienneDate =
                    chantier.getDateFinPrevue();

            chantier.retirerOuvriers(2);

            assertAll(
                () -> assertEquals(2, chantier.getOuvriersAssignes()),
                () -> assertTrue(
                    chantier.getDateFinPrevue().isAfter(ancienneDate)
                ),
                () -> assertEquals(98, royaume.getPopulationDisponible()),
                () -> assertEquals(2, royaume.getPopulationOuvriere())
            );
        }

        @Test
        @DisplayName("Le nombre d'ouvriers ne peut pas passer sous le minimum")
        void retirerOuvriersNePeutPasPasserSousMinimum() {

            assertThrows(
                IllegalArgumentException.class,
                () -> chantier.retirerOuvriers(2)
            );

            assertEquals(2, chantier.getOuvriersAssignes());
        }

        @Test
        @DisplayName("Une population insuffisante refuse l'affectation des ouvriers")
        void populationInsuffisanteRefuseAffectation() {

            assertThrows(
                IllegalStateException.class,
                () -> chantier.assignerOuvriers(200)
            );

            assertAll(
                () -> assertEquals(2, chantier.getOuvriersAssignes()),
                () -> assertEquals(98, royaume.getPopulationDisponible())
            );
        }
    }

    // =========================================================================
    // 3. Calcul du temps de construction
    // =========================================================================

    @Nested
    @DisplayName("Calcul du temps de construction")
    class TempsConstructionTests {

        @Test
        @DisplayName("Le temps restant est correctement calculé")
        void calculerTempsRestant() {

            LocalDateTime maintenant = LocalDateTime.now();

            long restant =
                    chantier.calculerTempsRestant(maintenant);

            assertTrue(restant > 0);
            assertTrue(restant <= 1800);

            LocalDateTime apresFin =
                    chantier.getDateFinPrevue().plusSeconds(10);

            assertEquals(
                0,
                chantier.calculerTempsRestant(apresFin)
            );
        }

        @Test
        @DisplayName("Le chantier respecte son échéance de construction")
        void verifierEcheanceChantier() {

            LocalDateTime avantFin =
                    chantier.getDateFinPrevue().minusSeconds(1);

            LocalDateTime apresFin =
                    chantier.getDateFinPrevue().plusSeconds(1);

            assertAll(
                () -> assertFalse(chantier.estPretAAchever(avantFin)),
                () -> assertTrue(chantier.estPretAAchever(apresFin))
            );
        }

        @Test
        @DisplayName("Le temps de construction de la taverne est recalculé selon les ouvriers")
        void verifierTempsConstructionTaverneEtRecalculDateFin() {

            // ARRANGE : création d'une taverne avec 1 ouvrier

            BatimentProduction taverne =
                    BatimentFactory.creerBatimentProduction(
                        royaume, 2, "Taverne", 7000,
                        Ressource.NOURRITURE, 50, 1
                    );

            ChantierConstruction chantier = taverne.getChantier();

            // VERIFICATION 1 : temps de construction de base

            assertEquals(180, taverne.getTempsConstructionBase());

            assertEquals(
                180,
                chantier.calculerTempsRestant(chantier.getDateDebut())
            );

            // VERIFICATION 2 : avec 1 ouvrier supplémentaire

            LocalDateTime maintenant = chantier.getDateDebut()
                    .plusSeconds(60);

            chantier.assignerOuvriers(1, maintenant);

            assertEquals(2, chantier.getOuvriersAssignes());

            long tempsRestant =
                    chantier.calculerTempsRestant(maintenant);

            assertEquals(60, tempsRestant);

            // VERIFICATION 3 : avec 3 ouvriers supplémentaires

            maintenant = maintenant.plusSeconds(20);

            chantier.assignerOuvriers(3, maintenant);

            assertEquals(5, chantier.getOuvriersAssignes());

            tempsRestant =
                    chantier.calculerTempsRestant(maintenant);

            assertEquals(16, tempsRestant);
        }

        @Test
        @DisplayName("Ajouter des ouvriers recalcule uniquement le temps restant")
        void ajouterOuvriersRecalculeUniquementTempsRestant() {

            // ARRANGE

            BatimentProduction scierie =
                    BatimentFactory.creerBatimentProduction(
                        royaume, 2, "Scierie", 3500,
                        Ressource.BOIS, 90, 1
                    );

            ChantierConstruction chantier = scierie.getChantier();

            // Simuler 60 secondes écoulées

            LocalDateTime maintenant =
                    chantier.getDateDebut().plusSeconds(60);

            // ACT : ajouter un ouvrier

            chantier.assignerOuvriers(1, maintenant);

            // ASSERT

            assertEquals(2, chantier.getOuvriersAssignes());

            long tempsRestant =
                    chantier.calculerTempsRestant(maintenant);

            assertEquals(60, tempsRestant);
        }
    }

    // =========================================================================
    // 4. Finalisation du chantier
    // =========================================================================

    @Nested
    @DisplayName("Finalisation du chantier")
    class FinalisationChantierTests {

        @Test
        @DisplayName("Finaliser le chantier restitue les ouvriers et rend le bâtiment opérationnel")
        void finaliserChantierRestitueOuvriers() {

            LocalDateTime fin =
                    chantier.getDateFinPrevue().plusSeconds(1);

            chantier.finaliser(fin);

            assertAll(
                () -> assertFalse(mine.isEnConstruction()),
                () -> assertNull(mine.getChantier()),
                () -> assertTrue(mine.estOperationnel()),
                () -> assertEquals(100, royaume.getPopulationDisponible()),
                () -> assertEquals(0, royaume.getPopulationOuvriere()),
                () -> assertEquals(0, chantier.getOuvriersAssignes())
            );
        }
    }

    // =========================================================================
    // 5. Validation Bean Validation
    // =========================================================================

    @Nested
    @DisplayName("Validation des contraintes Bean Validation")
    class ValidationTests {

        @Test
        @DisplayName("Le chantier initial respecte les contraintes de validation")
        void testValidationChantierInitial() {

            Set<ConstraintViolation<ChantierConstruction>> violations =
                    validator.validate(chantier);

            assertTrue(
                violations.isEmpty(),
                "Le chantier initial ne doit présenter aucune violation : "
                        + violations
            );
        }
    }
}
