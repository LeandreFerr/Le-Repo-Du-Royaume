package royaume.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import royaume.model.Peuple.*;

@DisplayName("Tests unitaires exhaustifs — Royaume (Socle & Module 2)")
class RoyaumeTest {

    private static final Population PEUPLE_TEST = new Canards(1, "Canards", "Archiduc", 0.05);
    private Royaume royaume;

    @BeforeEach
    void setUp() {
        List<QuantiteRessource> listeRessources = new ArrayList<>(List.of(
                new QuantiteRessource(100, Ressource.BOIS),
                new QuantiteRessource(50, Ressource.PIERRE)
        ));
        royaume = new Royaume(1, "TPT", PEUPLE_TEST, 1, 100, 0, 0, 0, false, listeRessources);
    }

    /**
     * Lecture utilitaire du stock réel exposé par le Royaume pour une ressource donnée.
     */
    private static int getStock(Royaume cible, Ressource type) {
        assertNotNull(cible.getRessources(), "getRessources() ne doit jamais retourner null");
        return cible.getRessources().stream()
                .filter(r -> r.getRessource() == type)
                .mapToInt(QuantiteRessource::getQuantite)
                .sum();
    }

    // =========================================================================
    // 1. Constructeurs, initialisation par défaut & validation d'état
    // =========================================================================

    @Nested
    @DisplayName("Constructeurs et valeurs par défaut")
    class ConstructeursTests {

        @Test
        @DisplayName("Le constructeur par défaut (JPA) initialise les valeurs standard (niveau 1, pop 100, peuple null, 4 ressources à 0)")
        void testConstructeurParDefaut() {
            // When
            Royaume r = new Royaume();

            // Then
            assertAll(
                () -> assertNull(r.getId()),
                () -> assertNull(r.getNom()),
                () -> assertNull(r.getPeuple()),
                () -> assertEquals(1, r.getNiveau()),
                () -> assertEquals(100, r.getPopulationDisponible()),
                () -> assertEquals(0, r.getPopulationOuvriere()),
                () -> assertEquals(0, r.getPopulationEnEntrainement()),
                () -> assertEquals(0, r.getPopulationMilitaire()),
                () -> assertFalse(r.isEnRuine()),
                () -> assertNotNull(r.getRessources()),
                () -> assertEquals(4, r.getRessources().size())
            );
        }

        @Test
        @DisplayName("Le constructeur court (id, nom, peuple) accepte un id null ou positif et initialise ressources non null")
        void testConstructeurCourtNominal() {
            // When
            Royaume avecId = new Royaume(1, "Avalon", PEUPLE_TEST);
            Royaume sansId = new Royaume(null, "Camelot", PEUPLE_TEST);

            // Then
            assertAll(
                () -> assertEquals(1, avecId.getId()),
                () -> assertEquals("Avalon", avecId.getNom()),
                () -> assertSame(PEUPLE_TEST, avecId.getPeuple()),
                () -> assertEquals(1, avecId.getNiveau()),
                () -> assertEquals(100, avecId.getPopulationDisponible()),
                () -> assertNotNull(avecId.getRessources()),
                () -> assertNull(sansId.getId()),
                () -> assertEquals("Camelot", sansId.getNom()),
                () -> assertSame(PEUPLE_TEST, sansId.getPeuple()),
                () -> assertNotNull(sansId.getRessources())
            );
        }

        @Test
        @DisplayName("Les constructeurs rejettent formellement un peuple null via IllegalArgumentException")
        void testConstructeursRejettentPeupleNull() {
            List<QuantiteRessource> vide = new ArrayList<>();

            assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Royaume(1, "Avalon", null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(1, "Avalon", null, 1, 100, 0, 0, 0, false, vide))
            );
        }

        @Test
        @DisplayName("Royaume accepte chacun des 5 peuples disponibles à la construction")
        void testConstructeurAccepteLesCinqPeuplesDisponibles() {
            List<Population> peuplesDisponibles = List.of(
                new Canards(1, "Canards", "Archiduc", 0.05),
                new Gobelins(2, "Gobelins", "Chef", 0.0),
                new Licornes(3, "Licornes", "Impératrice", 0.15),
                new Princesses(4, "Princesses", "Reine", 0.20),
                new Teuteus(5, "Teuteus", "Glouton", 0.0)
            );

            for (Population p : peuplesDisponibles) {
                Royaume r = new Royaume(1, "Terre", p);
                assertSame(p, r.getPeuple());
                assertSame(p, r.getPopulation());
            }
        }

        @Test
        @DisplayName("Royaume rejette toute population qui n'est pas l'un des 5 peuples officiels")
        void testConstructeurRejettePeupleNonDisponible() {
            class PeupleInconnu extends Population {
                public PeupleInconnu() {
                    super(99, "Inconnu", "Chef", 0.0, "mystere");
                }
                @Override
                public double getAvantageCombatContre(Population adversaire) {
                    return 1.0;
                }
                @Override
                public String getDescriptionCapacite() {
                    return "Inconnu";
                }
            }

            PeupleInconnu fauxPeuple = new PeupleInconnu();
            assertThrows(IllegalArgumentException.class, () -> new Royaume(1, "Terre", fauxPeuple));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -99, Integer.MIN_VALUE })
        @DisplayName("Les constructeurs rejettent un id <= 0 via IllegalArgumentException")
        void testConstructeursRejettentIdNonPositif(int idInvalide) {
            List<QuantiteRessource> vide = new ArrayList<>();

            assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(idInvalide, "Avalon", PEUPLE_TEST)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(idInvalide, "Avalon", PEUPLE_TEST, 1, 100, 0, 0, 0, false, vide))
            );
        }

        @Test
        @DisplayName("Les constructeurs rejettent un nom null, vide ou blanc via IllegalArgumentException")
        void testConstructeursRejettentNomInvalide() {
            List<QuantiteRessource> vide = new ArrayList<>();

            assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Royaume(1, null, PEUPLE_TEST)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Royaume(1, "", PEUPLE_TEST)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Royaume(1, "   \t\n ", PEUPLE_TEST)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(1, null, PEUPLE_TEST, 1, 100, 0, 0, 0, false, vide)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(1, "   ", PEUPLE_TEST, 1, 100, 0, 0, 0, false, vide))
            );
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -10, Integer.MIN_VALUE, 11, 50 })
        @DisplayName("Le constructeur complet rejette un niveau hors de l'intervalle [1, 10] via IllegalArgumentException")
        void testConstructeurCompletRejetteNiveauInvalide(int niveauInvalide) {
            List<QuantiteRessource> vide = new ArrayList<>();

            assertThrows(IllegalArgumentException.class,
                    () -> new Royaume(1, "Avalon", PEUPLE_TEST, niveauInvalide, 100, 0, 0, 0, false, vide));
        }

        @Test
        @DisplayName("Le constructeur complet accepte 0 sur toutes les catégories de population mais rejette toute valeur négative")
        void testConstructeurCompletValidationPopulations() {
            List<QuantiteRessource> vide = new ArrayList<>();

            // Cas limite valide : royaume totalement dépeuplé (0 partout)
            Royaume desert = assertDoesNotThrow(
                    () -> new Royaume(1, "Désert", PEUPLE_TEST, 1, 0, 0, 0, 0, false, vide));
            assertEquals(0, desert.getPopulationDisponible());

            // Cas invalides : chacune des 4 populations négative
            assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(1, "R", PEUPLE_TEST, 1, -1, 0, 0, 0, false, vide),
                        "populationDisponible négative interdite"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(1, "R", PEUPLE_TEST, 1, 0, -1, 0, 0, false, vide),
                        "populationOuvriere négative interdite"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(1, "R", PEUPLE_TEST, 1, 0, 0, -1, 0, false, vide),
                        "populationEnEntrainement négative interdite"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(1, "R", PEUPLE_TEST, 1, 0, 0, 0, -1, false, vide),
                        "populationMilitaire négative interdite")
            );
        }

        @Test
        @DisplayName("Passer une liste ressources null au constructeur complet initialise une collection vide non null")
        void testConstructeurCompletAvecListeRessourcesNull() {
            // When
            Royaume sansListe = new Royaume(1, "Avalon", PEUPLE_TEST, 1, 100, 0, 0, 0, false, null);

            // Then
            assertNotNull(sansListe.getRessources(), "ressources null en entrée doit être converti en liste vide sûre");
            assertDoesNotThrow(() -> sansListe.crediterRessources(Ressource.OR, 20));
            assertEquals(20, getStock(sansListe, Ressource.OR));
        }

        @Test
        @DisplayName("Le constructeur complet rejette une liste contenant un élément null ou deux fois la même Ressource")
        void testConstructeurCompletRejetteElementNullOuDoublonRessource() {
            List<QuantiteRessource> avecNull = Arrays.asList(new QuantiteRessource(10, Ressource.OR), null);
            List<QuantiteRessource> avecDoublon = List.of(
                    new QuantiteRessource(30, Ressource.BOIS),
                    new QuantiteRessource(40, Ressource.BOIS)
            );

            assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(1, "Avalon", PEUPLE_TEST, 1, 100, 0, 0, 0, false, avecNull),
                        "Un élément QuantiteRessource null dans la liste doit lever IllegalArgumentException"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Royaume(1, "Avalon", PEUPLE_TEST, 1, 100, 0, 0, 0, false, avecDoublon),
                        "Deux entrées pour la même Ressource doivent lever IllegalArgumentException")
            );
        }
    }

    // =========================================================================
    // 2. Encapsulation défensive de la collection de ressources
    // =========================================================================

    @Nested
    @DisplayName("Encapsulation défensive des ressources")
    class EncapsulationRessourcesTests {

        @Test
        @DisplayName("getRessources() retourne une vue découplée (la mutation externe de la liste n'affecte pas le Royaume)")
        void testGetRessourcesRetourneVueNonModifiable() {
            List<QuantiteRessource> exposition = royaume.getRessources();
            assertThrows(UnsupportedOperationException.class,
                    () -> exposition.add(new QuantiteRessource(999, Ressource.OR)),
                    "La liste retournée par getRessources() doit être immuable");
        }

        @Test
        @DisplayName("Muter la liste externe ou un QuantiteRessource externe après construction n'altère pas le Royaume")
        void testIsolationContreMutationExterneApresConstruction() {
            // Given
            QuantiteRessource orExterne = new QuantiteRessource(100, Ressource.OR);
            List<QuantiteRessource> listeExterne = new ArrayList<>(List.of(orExterne));
            Royaume coffreFort = new Royaume(2, "Coffre", PEUPLE_TEST, 1, 100, 0, 0, 0, false, listeExterne);

            // When : l'appelant vide sa liste locale et gonfle son objet QuantiteRessource local
            listeExterne.clear();
            orExterne.ajouter(5000);

            // Then : le Royaume a copié défensivement la liste ET les QuantiteRessource
            assertEquals(100, getStock(coffreFort, Ressource.OR),
                    "Le stock interne du Royaume doit être totalement isolé des références externes");
        }

        @Test
        @DisplayName("Muter un élément QuantiteRessource obtenu via getRessources() n'altère pas le stock interne du Royaume")
        void testIsolationContreMutationViaElementDeGetRessources() {
            // Given
            QuantiteRessource elementExpose = royaume.getRessources().stream()
                    .filter(r -> r.getRessource() == Ressource.BOIS)
                    .findFirst()
                    .orElseThrow();

            // When : tentative de triche directe sans passer par crediterRessources
            elementExpose.ajouter(9000);

            // Then
            assertEquals(100, getStock(royaume, Ressource.BOIS),
                    "getRessources() ne doit pas exposer les instances mutables internes du Royaume");
        }

        @Test
        @DisplayName("Un Royaume initialisé avec List.of(...) immuable peut tout de même créditer une nouvelle Ressource")
        void testCompatibiliteAvecListOfImmuableEnEntree() {
            // Given : List.of est immuable (cas fréquent dans les configurations et tests)
            List<QuantiteRessource> listeImmuable = List.of(new QuantiteRessource(50, Ressource.BOIS));
            Royaume r = new Royaume(3, "Sylve", PEUPLE_TEST, 1, 100, 0, 0, 0, false, listeImmuable);

            // When / Then : ne doit pas lever UnsupportedOperationException lors de l'ajout d'OR
            assertDoesNotThrow(() -> r.crediterRessources(Ressource.OR, 40));
            assertEquals(40, getStock(r, Ressource.OR));
        }
    }

    // =========================================================================
    // 3. Setters & Maintien des invariants
    // =========================================================================

    @Nested
    @DisplayName("Setters et protection contre les états invalides")
    class SettersTests {

        @Test
        @DisplayName("setId accepte null ou un entier > 0, mais rejette <= 0 sans modifier l'id existant")
        void testSetIdValidation() {
            assertThrows(IllegalArgumentException.class, () -> royaume.setId(0));
            assertThrows(IllegalArgumentException.class, () -> royaume.setId(-5));
            assertEquals(1, royaume.getId());

            royaume.setId(null);
            assertNull(royaume.getId());

            royaume.setId(42);
            assertEquals(42, royaume.getId());
        }

        @Test
        @DisplayName("setNom rejette null ou une chaîne blanche sans écraser l'ancien nom")
        void testSetNomValidation() {
            assertThrows(IllegalArgumentException.class, () -> royaume.setNom(null));
            assertThrows(IllegalArgumentException.class, () -> royaume.setNom(""));
            assertThrows(IllegalArgumentException.class, () -> royaume.setNom("   "));
            assertEquals("TPT", royaume.getNom());

            royaume.setNom("Minas Tirith");
            assertEquals("Minas Tirith", royaume.getNom());
        }

        @Test
        @DisplayName("Le peuple d'un royaume est immuable : setPeuple et setPopulation lèvent UnsupportedOperationException")
        void testPeupleImmuableApresConstruction() {
            Population teuteus = new Teuteus(5, "Teuteus", "Glouton", 0.0);
            assertThrows(UnsupportedOperationException.class, () -> royaume.setPeuple(teuteus));
            assertThrows(UnsupportedOperationException.class, () -> royaume.setPopulation(teuteus));
            assertSame(PEUPLE_TEST, royaume.getPeuple());
        }

        @Test
        @DisplayName("Sur un royaume instancié par défaut (JPA), setPeuple autorise une assignation unique puis devient immuable")
        void testAssignationPeupleSurRoyaumeVidePuisImmuabilite() {
            Royaume royaumeVide = new Royaume();
            assertNull(royaumeVide.getPeuple());

            // Première assignation valide
            royaumeVide.setPeuple(PEUPLE_TEST);
            assertSame(PEUPLE_TEST, royaumeVide.getPeuple());

            // Toute assignation ultérieure est verrouillée
            Population teuteus = new Teuteus(5, "Teuteus", "Glouton", 0.0);
            assertThrows(UnsupportedOperationException.class, () -> royaumeVide.setPeuple(teuteus));
            assertThrows(UnsupportedOperationException.class, () -> royaumeVide.setPopulation(teuteus));
            assertSame(PEUPLE_TEST, royaumeVide.getPeuple());
        }

        @Test
        @DisplayName("setPeuple sur un royaume instancié par défaut rejette null et un peuple non officiel")
        void testSetPeupleValidationSurRoyaumeVide() {
            Royaume royaumeVide = new Royaume();
            assertThrows(IllegalArgumentException.class, () -> royaumeVide.setPeuple(null));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -50, 11, 50 })
        @DisplayName("setNiveau rejette les valeurs < 1 ou > 10 sans modifier le niveau actuel")
        void testSetNiveauValidation(int niveauInvalide) {
            assertThrows(IllegalArgumentException.class, () -> royaume.setNiveau(niveauInvalide));
            assertEquals(1, royaume.getNiveau());

            royaume.setNiveau(10);
            assertEquals(10, royaume.getNiveau());
        }

        @Test
        @DisplayName("Les 4 setters de population acceptent 0 mais rejettent les valeurs négatives en préservant l'état")
        void testSettersPopulationValidation() {
            royaume.setPopulationDisponible(80);
            royaume.setPopulationOuvriere(20);
            royaume.setPopulationEnEntrainement(15);
            royaume.setPopulationMilitaire(35);

            assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> royaume.setPopulationDisponible(-1)),
                () -> assertThrows(IllegalArgumentException.class, () -> royaume.setPopulationOuvriere(-1)),
                () -> assertThrows(IllegalArgumentException.class, () -> royaume.setPopulationEnEntrainement(-1)),
                () -> assertThrows(IllegalArgumentException.class, () -> royaume.setPopulationMilitaire(-1))
            );

            assertAll(
                () -> assertEquals(80, royaume.getPopulationDisponible()),
                () -> assertEquals(20, royaume.getPopulationOuvriere()),
                () -> assertEquals(15, royaume.getPopulationEnEntrainement()),
                () -> assertEquals(35, royaume.getPopulationMilitaire())
            );

            royaume.setPopulationDisponible(0);
            assertEquals(0, royaume.getPopulationDisponible());
        }

        @Test
        @DisplayName("setRessources vérifie l'absence de null et de doublons et clone défensivement")
        void testSetRessourcesValidationEtCopieDefensive() {
            List<QuantiteRessource> avecNull = Arrays.asList(new QuantiteRessource(10, Ressource.OR), null);
            List<QuantiteRessource> avecDoublon = List.of(
                    new QuantiteRessource(10, Ressource.OR),
                    new QuantiteRessource(20, Ressource.OR)
            );

            assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> royaume.setRessources(avecNull)),
                () -> assertThrows(IllegalArgumentException.class, () -> royaume.setRessources(avecDoublon))
            );

            // setRessources valide avec liste mutée ensuite
            List<QuantiteRessource> valides = new ArrayList<>(List.of(new QuantiteRessource(500, Ressource.OR)));
            royaume.setRessources(valides);
            valides.clear();
            assertEquals(500, getStock(royaume, Ressource.OR));

            // setRessources(null) -> initialise liste par défaut
            royaume.setRessources(null);
            assertNotNull(royaume.getRessources());
            assertEquals(0, getStock(royaume, Ressource.BOIS));
        }
    }

    // =========================================================================
    // 4. Méthode métier : aAssezDeRessources(Ressource, int)
    // =========================================================================

    @Nested
    @DisplayName("Méthode métier aAssezDeRessources(Ressource, int)")
    class AAssezDeRessourcesTests {

        @Test
        @DisplayName("Retourne true si le stock est supérieur ou pile égal au montant requis, false sinon")
        void testVerificationStocksNominauxEtLimites() {
            assertAll(
                () -> assertTrue(royaume.aAssezDeRessources(Ressource.BOIS, 1)),
                () -> assertTrue(royaume.aAssezDeRessources(Ressource.BOIS, 50)),
                () -> assertTrue(royaume.aAssezDeRessources(Ressource.BOIS, 100), "Seuil pile égal au stock"),
                () -> assertFalse(royaume.aAssezDeRessources(Ressource.BOIS, 101), "Stock dépassé d'une unité"),
                () -> assertFalse(royaume.aAssezDeRessources(Ressource.OR, 1), "Ressource absente du stock")
            );
        }

        @Test
        @DisplayName("Retourne toujours true pour une quantité requise de 0, même si la ressource est absente de la liste")
        void testQuantiteZeroToujoursSatisfaite() {
            Royaume neuf = new Royaume(5, "Neuf", PEUPLE_TEST);

            assertAll(
                () -> assertTrue(royaume.aAssezDeRessources(Ressource.BOIS, 0)),
                () -> assertTrue(royaume.aAssezDeRessources(Ressource.OR, 0),
                        "Demander 0 d'une ressource absente doit retourner true"),
                () -> assertTrue(neuf.aAssezDeRessources(Ressource.NOURRITURE, 0))
            );
        }

        @Test
        @DisplayName("Lève IllegalArgumentException si la ressource est null ou si la quantité est négative")
        void testRejetArgumentsInvalides() {
            assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> royaume.aAssezDeRessources(null, 10)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> royaume.aAssezDeRessources(null, 0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> royaume.aAssezDeRessources(Ressource.BOIS, -1),
                        "Une quantité négative ne doit jamais retourner true silencieusement"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> royaume.aAssezDeRessources(Ressource.OR, -50))
            );
        }
    }

    // =========================================================================
    // 5. Méthode métier : crediterRessources(Ressource, int)
    // =========================================================================

    @Nested
    @DisplayName("Méthode métier crediterRessources(Ressource, int)")
    class CrediterRessourcesTests {

        @Test
        @DisplayName("Incrémente une ressource déjà présente sans altérer les autres stocks")
        void testCrediterRessourceExistante() {
            // When
            royaume.crediterRessources(Ressource.BOIS, 25);

            // Then
            assertAll(
                () -> assertEquals(125, getStock(royaume, Ressource.BOIS)),
                () -> assertEquals(50, getStock(royaume, Ressource.PIERRE)),
                () -> assertEquals(0, getStock(royaume, Ressource.OR))
            );
        }

        @ParameterizedTest
        @EnumSource(Ressource.class)
        @DisplayName("Crée dynamiquement la ligne de stock si la ressource n'existe pas encore (sans doublon ultérieur)")
        void testCrediterRessourceAbsenteSurNouveauRoyaume(Ressource type) {
            // Given : Royaume neuf sans aucun stock prérempli
            Royaume neuf = new Royaume(10, "Fondation", PEUPLE_TEST);

            // When : deux crédits successifs sur une ressource initialement absente
            neuf.crediterRessources(type, 40);
            neuf.crediterRessources(type, 35);

            // Then
            long nbEntreesPourCeType = neuf.getRessources().stream()
                    .filter(r -> r.getRessource() == type)
                    .count();

            assertAll(
                () -> assertTrue(neuf.aAssezDeRessources(type, 75),
                        "Les 75 unités créditées ne doivent surtout pas être perdues"),
                () -> assertEquals(75, getStock(neuf, type)),
                () -> assertEquals(1L, nbEntreesPourCeType,
                        "Une même ressource ne doit apparaître qu'une seule fois dans la liste")
            );
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -25, Integer.MIN_VALUE })
        @DisplayName("Lève IllegalArgumentException si la quantité à créditer est <= 0 et préserve les stocks")
        void testCrediterQuantiteNonPositive(int quantiteInvalide) {
            assertThrows(IllegalArgumentException.class,
                    () -> royaume.crediterRessources(Ressource.BOIS, quantiteInvalide));
            assertEquals(100, getStock(royaume, Ressource.BOIS));
        }

        @Test
        @DisplayName("Lève IllegalArgumentException si la ressource à créditer est null")
        void testCrediterRessourceNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> royaume.crediterRessources(null, 50));
        }

        @Test
        @DisplayName("Lève ArithmeticException lors d'un dépassement de Integer.MAX_VALUE")
        void testCrediterProtectionOverflow() {
            Royaume riche = new Royaume(9, "Eldorado", PEUPLE_TEST, 1, 100, 0, 0, 0, false,
                    new ArrayList<>(List.of(new QuantiteRessource(Integer.MAX_VALUE - 10, Ressource.OR))));

            assertThrows(ArithmeticException.class,
                    () -> riche.crediterRessources(Ressource.OR, 20));
            assertEquals(Integer.MAX_VALUE - 10, getStock(riche, Ressource.OR));
        }
    }

    // =========================================================================
    // 6. Méthode métier : debiterRessources(Ressource, int)
    // =========================================================================

    @Nested
    @DisplayName("Méthode métier debiterRessources(Ressource, int)")
    class DebiterRessourcesTests {

        @Test
        @DisplayName("Débite le montant et retourne true lorsque le stock est suffisant (partiel puis total jusqu'à 0)")
        void testDebiterSuccesPartielPuisTotal() {
            // Débit partiel : 100 - 30 = 70
            assertTrue(royaume.debiterRessources(Ressource.BOIS, 30));
            assertEquals(70, getStock(royaume, Ressource.BOIS));

            // Débit exact du solde restant : 70 - 70 = 0
            assertTrue(royaume.debiterRessources(Ressource.BOIS, 70));
            assertEquals(0, getStock(royaume, Ressource.BOIS));
            assertEquals(50, getStock(royaume, Ressource.PIERRE), "Les autres ressources restent intactes");
        }

        @Test
        @DisplayName("Retourne false sans altérer aucun stock si le solde est insuffisant ou la ressource absente")
        void testDebiterEchecSoldeInsuffisantOuRessourceAbsente() {
            Royaume neuf = new Royaume(11, "Pauvre", PEUPLE_TEST);

            assertAll(
                () -> assertFalse(royaume.debiterRessources(Ressource.BOIS, 101), "Stock insuffisant"),
                () -> assertEquals(100, getStock(royaume, Ressource.BOIS), "Stock BOIS intact après refus"),
                () -> assertFalse(royaume.debiterRessources(Ressource.OR, 10), "Ressource OR absente"),
                () -> assertFalse(neuf.debiterRessources(Ressource.NOURRITURE, 1), "Royaume neuf sans stock")
            );
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -30, Integer.MIN_VALUE })
        @DisplayName("Lève IllegalArgumentException si la quantité à débiter est <= 0 et préserve le stock")
        void testDebiterQuantiteInvalide(int quantiteInvalide) {
            assertThrows(IllegalArgumentException.class,
                    () -> royaume.debiterRessources(Ressource.BOIS, quantiteInvalide));
            assertEquals(100, getStock(royaume, Ressource.BOIS));
        }

        @Test
        @DisplayName("Lève IllegalArgumentException si la ressource à débiter est null")
        void testDebiterRessourceNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> royaume.debiterRessources(null, 10));
        }
    }

    // =========================================================================
    // 7. Méthode métier : appliquerCroissance(double taux) & toString
    // =========================================================================

    @Nested
    @DisplayName("Méthode métier appliquerCroissance(double) et représentation")
    class CroissanceEtRepresentationTests {

        @Test
        @DisplayName("Augmente la populationDisponible avec arrondi Math.round sans toucher aux autres populations")
        void testAppliquerCroissanceNominaleEtArrondi() {
            // Given
            royaume.setPopulationOuvriere(25);
            royaume.setPopulationMilitaire(10);
            royaume.setPopulationEnEntrainement(5);

            // When : 100 * 1.10 = 110, puis 110 * 1.05 = 115.5 -> arrondi à 116
            royaume.appliquerCroissance(0.10);
            assertEquals(110, royaume.getPopulationDisponible());

            royaume.appliquerCroissance(0.05);

            // Then
            assertAll(
                () -> assertEquals(116, royaume.getPopulationDisponible()),
                () -> assertEquals(25, royaume.getPopulationOuvriere()),
                () -> assertEquals(10, royaume.getPopulationMilitaire()),
                () -> assertEquals(5, royaume.getPopulationEnEntrainement())
            );
        }

        @Test
        @DisplayName("Supporte un taux nul (0.0) ainsi qu'une décroissance valide jusqu'à extinction (-1.0)")
        void testAppliquerCroissanceTauxZeroEtDecroissanceValide() {
            // Taux 0.0 -> inchangé
            royaume.appliquerCroissance(0.0);
            assertEquals(100, royaume.getPopulationDisponible());

            // Décroissance de -25 % -> 75
            royaume.appliquerCroissance(-0.25);
            assertEquals(75, royaume.getPopulationDisponible());

            // Extinction totale (-100 %) -> 0
            royaume.appliquerCroissance(-1.0);
            assertEquals(0, royaume.getPopulationDisponible());

            // Croissance sur population 0 -> reste 0
            royaume.appliquerCroissance(0.50);
            assertEquals(0, royaume.getPopulationDisponible());
        }

        @ParameterizedTest
        @ValueSource(doubles = { -1.0001, -1.5, -10.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY })
        @DisplayName("Lève IllegalArgumentException pour un taux < -1.0, NaN ou Infini et préserve la population")
        void testAppliquerCroissanceRejetteTauxAberrants(double tauxInvalide) {
            assertThrows(IllegalArgumentException.class,
                    () -> royaume.appliquerCroissance(tauxInvalide),
                    "Un taux < -1.0 ou non fini (NaN / Infinity) doit lever IllegalArgumentException");
            assertEquals(100, royaume.getPopulationDisponible(),
                    "La populationDisponible ne doit pas être corrompue");
        }

        @Test
        @DisplayName("Lève ArithmeticException en cas de dépassement de Integer.MAX_VALUE (via Math.toIntExact)")
        void testAppliquerCroissanceProtectionOverflow() {
            // Given : 1.5 milliard d'habitants avec +100 % = 3 milliards (> Integer.MAX_VALUE)
            royaume.setPopulationDisponible(1_500_000_000);

            // When / Then : ne doit surtout pas tronquer un long en int négatif !
            assertThrows(ArithmeticException.class,
                    () -> royaume.appliquerCroissance(1.0),
                    "Un dépassement de Integer.MAX_VALUE doit lever ArithmeticException (ex: Math.toIntExact)");
            assertEquals(1_500_000_000, royaume.getPopulationDisponible());
        }

        @Test
        @DisplayName("toString fonctionne sans exception même sur un Royaume fraîchement instancié")
        void testToStringRobuste() {
            Royaume frais = new Royaume(1, "Frais", PEUPLE_TEST);
            String description = assertDoesNotThrow(frais::toString);

            assertAll(
                () -> assertNotNull(description),
                () -> assertTrue(description.contains("Frais")),
                () -> assertTrue(description.contains("Canards")),
                () -> assertTrue(royaume.toString().contains("TPT"))
            );
        }
    }
}
