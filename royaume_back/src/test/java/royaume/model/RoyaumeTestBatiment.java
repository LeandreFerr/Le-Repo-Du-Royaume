
package royaume.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.ChantierConstruction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;

import royaume.model.peuple.Population;
import royaume.model.peuple.Canards;

@DisplayName("Tests unitaires - Gestion des bâtiments du Royaume")
class RoyaumeTestBatiment {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static final Population PEUPLE_TEST =
            new Canards(1, "Canards", "Archiduc", 0.05);

    private Royaume royaume;

    @BeforeEach
    void setUp() {
        royaume = new Royaume(1, "Royaume test", PEUPLE_TEST);
    }

    // =========================================================================
    // 1. Constructeur et état initial
    // =========================================================================

    @Nested
    @DisplayName("Constructeur et état initial")
    class ConstructeurTests {

        @Test
        @DisplayName("Le constructeur crée automatiquement le château initial")
        void constructeur_CreeAutomatiquementLeChateauInitial() {

            assertEquals(1, royaume.getBatiments().size());

            assertTrue(royaume.getBatiments().get(0) instanceof Chateau);

            Chateau chateau = (Chateau) royaume.getBatiments().get(0);

            assertFalse(chateau.isEnConstruction());
            assertNull(chateau.getChantier());
        }
    }

    // =========================================================================
    // 2. Création des maisons
    // =========================================================================

    @Nested
    @DisplayName("Gestion des maisons")
    class MaisonTests {

        @Test
        @DisplayName("Créer une maison ajoute la maison et son chantier au royaume")
        void creerMaison_AjouteMaisonEtChantierAuRoyaume() {

            Maison maison = BatimentFactory.creerMaison(
                    royaume, 2, "Maison paysanne", 3
            );

            assertAll(
                () -> assertTrue(royaume.getBatiments().contains(maison)),
                () -> assertEquals(2, royaume.getBatiments().size()),
                () -> assertTrue(maison.isEnConstruction()),
                () -> assertNotNull(maison.getChantier()),
                () -> assertEquals(3, maison.getChantier().getOuvriersAssignes()),
                () -> assertEquals(97, royaume.getPopulationDisponible()),
                () -> assertEquals(3, royaume.getPopulationOuvriere())
            );
        }

        @Test
        @DisplayName("La création de plusieurs maisons est autorisée")
        void creerPlusieursMaisons_EstAutorise() {

            Maison maison1 = BatimentFactory.creerMaison(
                    royaume, 2, "Maison 1", 2
            );

            Maison maison2 = BatimentFactory.creerMaison(
                    royaume, 3, "Maison 2", 2
            );

            assertAll(
                () -> assertEquals(3, royaume.getBatiments().size()),
                () -> assertTrue(royaume.getBatiments().contains(maison1)),
                () -> assertTrue(royaume.getBatiments().contains(maison2)),
                () -> assertTrue(maison1.isEnConstruction()),
                () -> assertTrue(maison2.isEnConstruction()),
                () -> assertNotNull(maison1.getChantier()),
                () -> assertNotNull(maison2.getChantier()),
                () -> assertEquals(96, royaume.getPopulationDisponible()),
                () -> assertEquals(4, royaume.getPopulationOuvriere())
            );
        }
    }

    // =========================================================================
    // 3. Création des bâtiments de production
    // =========================================================================

    @Nested
    @DisplayName("Gestion des bâtiments de production")
    class BatimentProductionTests {

        @Test
        @DisplayName("Créer une mine ajoute automatiquement le bâtiment au royaume")
        void creerBatimentProduction_AjouteAutomatiquementLaMine() {

            BatimentProduction mine = BatimentFactory.creerBatimentProduction(
                    royaume, 2, "Mine d'or", 2500,
                    Ressource.OR, 100, 3
            );

            assertAll(
                () -> assertTrue(royaume.getBatiments().contains(mine)),
                () -> assertEquals(2, royaume.getBatiments().size()),
                () -> assertTrue(mine.isEnConstruction()),
                () -> assertNotNull(mine.getChantier()),
                () -> assertEquals(3, mine.getChantier().getOuvriersAssignes()),
                () -> assertEquals(97, royaume.getPopulationDisponible()),
                () -> assertEquals(3, royaume.getPopulationOuvriere())
            );
        }

        @Test
        @DisplayName("Le royaume refuse un deuxième bâtiment de production de même classe")
        void ajouterBatiment_RefuseUnDeuxiemeBatimentProductionDeMemeClasse() {

            BatimentFactory.creerBatimentProduction(
                    royaume, 2, "Mine d'or", 2500,
                    Ressource.OR, 100, 3
            );

            assertThrows(
                IllegalArgumentException.class,
                () -> BatimentFactory.creerBatimentProduction(
                        royaume, 3, "Deuxième mine d'or", 400,
                        Ressource.OR, 100, 3
                )
            );
        }

        @Test
        @DisplayName("Le royaume autorise une taverne et une scierie simultanément")
        void creerTaverneEtScierie_DansLeMemeRoyaume() {

            BatimentProduction taverne = BatimentFactory.creerBatimentProduction(
                    royaume, 5, "Taverne", 7000,
                    Ressource.NOURRITURE, 50, 3
            );

            BatimentProduction scierie = BatimentFactory.creerBatimentProduction(
                    royaume, 4, "Scierie", 3500,
                    Ressource.BOIS, 90, 3
            );

            assertAll(
                () -> assertEquals(3, royaume.getBatiments().size()),
                () -> assertTrue(royaume.getBatiments().contains(taverne)),
                () -> assertTrue(royaume.getBatiments().contains(scierie)),
                () -> assertTrue(taverne.isEnConstruction()),
                () -> assertTrue(scierie.isEnConstruction()),
                () -> assertNotNull(taverne.getChantier()),
                () -> assertNotNull(scierie.getChantier())
            );
        }
    }

    // =========================================================================
    // 4. Gestion du château
    // =========================================================================

    @Nested
    @DisplayName("Gestion du château")
    class ChateauTests {

        @Test
        @DisplayName("Le royaume refuse la création d'un deuxième château")
        void ajouterBatiment_RefuseUnDeuxiemeChateau() {

            assertThrows(
                IllegalArgumentException.class,
                () -> BatimentFactory.creerChateauInitial(royaume)
            );
        }
    }

    // =========================================================================
    // 5. Gestion des chantiers
    // =========================================================================

    @Nested
    @DisplayName("Gestion des chantiers de construction")
    class ChantierTests {

        @Test
        @DisplayName("Le chantier est correctement associé à sa mine et à son royaume")
        void creerMine_ChantierEstBienAssocieAuBatiment() {

            BatimentProduction mine = BatimentFactory.creerBatimentProduction(
                    royaume, 2, "Mine d'or", 2500,
                    Ressource.OR, 100, 3
            );

            ChantierConstruction chantier = mine.getChantier();

            assertAll(
                () -> assertNotNull(chantier),
                () -> assertSame(mine, chantier.getBatiment()),
                () -> assertSame(royaume, chantier.getRoyaume()),
                () -> assertTrue(mine.isEnConstruction())
            );
        }
    }

    // =========================================================================
    // 6. Protection et encapsulation
    // =========================================================================

    @Nested
    @DisplayName("Protection et encapsulation des bâtiments")
    class EncapsulationTests {

        @Test
        @DisplayName("La liste des bâtiments ne peut pas être modifiée directement")
        void getBatiments_NePermetPasDeModifierDirectementLaListe() {

            assertThrows(
                UnsupportedOperationException.class,
                () -> royaume.getBatiments().clear()
            );
        }
    }

    // =========================================================================
    // 7. Validation Bean Validation
    // =========================================================================

    @Nested
    @DisplayName("Validation des contraintes Bean Validation")
    class ValidationTests {

        @Test
        @DisplayName("Le Validator détecte un nom de royaume null")
        void testNomRoyaumeNull() {

            Royaume royaumeInvalide = new Royaume(
                    1, null, PEUPLE_TEST
            );

            Set<ConstraintViolation<Royaume>> violations =
                    validator.validate(royaumeInvalide);

            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("nom")));
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   ", "\t\n" })
        @DisplayName("Le Validator détecte un nom de royaume vide ou blanc")
        void testNomRoyaumeBlanc(String nomInvalide) {

            Royaume royaumeInvalide = new Royaume(
                    1, nomInvalide, PEUPLE_TEST
            );

            Set<ConstraintViolation<Royaume>> violations =
                    validator.validate(royaumeInvalide);

            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("nom")));
        }
    }
}
