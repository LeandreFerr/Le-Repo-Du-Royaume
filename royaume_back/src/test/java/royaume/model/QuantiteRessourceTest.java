package royaume.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests unitaires exhaustifs — QuantiteRessource")
class QuantiteRessourceTest {

    private QuantiteRessource qr;

    @BeforeEach
    void setUp() {
        qr = new QuantiteRessource(50, Ressource.BOIS);
    }

    // =========================================================================
    // 1. Constructeurs & Invariants à l'instanciation
    // =========================================================================

    @Nested
    @DisplayName("Constructeurs et invariants initiaux")
    class ConstructeursTests {

        @ParameterizedTest
        @EnumSource(Ressource.class)
        @DisplayName("Le constructeur paramétré accepte une quantité positive pour chaque type de Ressource")
        void testConstructeurNominalPourChaqueRessource(Ressource type) {
            // When
            QuantiteRessource stock = new QuantiteRessource(120, type);

            // Then
            assertAll(
                () -> assertEquals(120, stock.getQuantite()),
                () -> assertEquals(type, stock.getRessource())
            );
        }

        @Test
        @DisplayName("Le constructeur paramétré accepte une quantité initiale égale à 0")
        void testConstructeurQuantiteZeroValide() {
            // When
            QuantiteRessource stockVide = new QuantiteRessource(0, Ressource.OR);

            // Then
            assertAll(
                () -> assertEquals(0, stockVide.getQuantite()),
                () -> assertEquals(Ressource.OR, stockVide.getRessource())
            );
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, -10, -500, Integer.MIN_VALUE })
        @DisplayName("Le constructeur paramétré lève IllegalArgumentException si la quantité initiale est négative")
        void testConstructeurRejetteQuantiteNegative(int quantiteInvalide) {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuantiteRessource(quantiteInvalide, Ressource.PIERRE),
                    "Une quantité initiale négative doit lever IllegalArgumentException");
        }

        @Test
        @DisplayName("Le constructeur paramétré lève IllegalArgumentException si la ressource est null")
        void testConstructeurRejetteRessourceNull() {
            assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new QuantiteRessource(50, null),
                        "Une ressource null doit lever IllegalArgumentException"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new QuantiteRessource(0, null),
                        "Même avec une quantité à 0, une ressource null doit lever IllegalArgumentException")
            );
        }

        @Test
        @DisplayName("Le constructeur sans argument initialise une quantité à 0 non négative")
        void testConstructeurParDefaut() {
            QuantiteRessource vide = new QuantiteRessource();
            assertEquals(0, vide.getQuantite());
        }
    }

    // =========================================================================
    // 2. Setters & Protection des invariants contre les mutations directes
    // =========================================================================

    @Nested
    @DisplayName("Mutateurs (setQuantite / setRessource)")
    class SettersTests {

        @ParameterizedTest
        @ValueSource(ints = { 0, 1, 250, Integer.MAX_VALUE })
        @DisplayName("setQuantite accepte 0 et toute valeur positive")
        void testSetQuantiteValide(int nouvelleQuantite) {
            // When
            qr.setQuantite(nouvelleQuantite);

            // Then
            assertEquals(nouvelleQuantite, qr.getQuantite());
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, -42, Integer.MIN_VALUE })
        @DisplayName("setQuantite lève IllegalArgumentException si la quantité est négative et préserve l'état")
        void testSetQuantiteRejetteNegatifSansModifierEtat(int quantiteNegative) {
            // When / Then
            assertThrows(IllegalArgumentException.class,
                    () -> qr.setQuantite(quantiteNegative),
                    "setQuantite ne doit jamais permettre d'injecter un stock négatif");
            assertEquals(50, qr.getQuantite(), "La quantité précédente doit rester intacte");
        }

        @Test
        @DisplayName("setRessource met à jour la ressource lorsqu'elle est non null")
        void testSetRessourceValide() {
            // When
            qr.setRessource(Ressource.NOURRITURE);

            // Then
            assertEquals(Ressource.NOURRITURE, qr.getRessource());
        }

        @Test
        @DisplayName("setRessource lève IllegalArgumentException si la ressource est null et préserve l'état")
        void testSetRessourceRejetteNullSansModifierEtat() {
            // When / Then
            assertThrows(IllegalArgumentException.class,
                    () -> qr.setRessource(null),
                    "setRessource(null) doit lever IllegalArgumentException");
            assertEquals(Ressource.BOIS, qr.getRessource(), "La ressource précédente doit rester intacte");
        }
    }

    // =========================================================================
    // 3. Opération métier : ajouter(int montant)
    // =========================================================================

    @Nested
    @DisplayName("Méthode métier ajouter(int)")
    class AjouterTests {

        @Test
        @DisplayName("ajouter incrémente correctement le stock lors d'appels simples et cumulés")
        void testAjouterSucces() {
            // When
            qr.ajouter(30);
            qr.ajouter(20);

            // Then
            assertEquals(100, qr.getQuantite());
        }

        @Test
        @DisplayName("ajouter fonctionne correctement à partir d'un stock vide à 0")
        void testAjouterDepuisStockZero() {
            // Given
            QuantiteRessource stockVide = new QuantiteRessource(0, Ressource.OR);

            // When
            stockVide.ajouter(1);

            // Then
            assertEquals(1, stockVide.getQuantite());
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -10, -500, Integer.MIN_VALUE })
        @DisplayName("ajouter lève IllegalArgumentException si montant <= 0 et laisse le stock intact")
        void testAjouterMontantInvalide(int montantInvalide) {
            assertThrows(IllegalArgumentException.class, () -> qr.ajouter(montantInvalide));
            assertEquals(50, qr.getQuantite(), "Le stock ne doit pas bouger après un ajout invalide");
        }

        @Test
        @DisplayName("ajouter lève ArithmeticException en cas de dépassement de Integer.MAX_VALUE (via Math.addExact)")
        void testAjouterProtectionOverflow() {
            // Given
            QuantiteRessource stockMax = new QuantiteRessource(Integer.MAX_VALUE - 5, Ressource.OR);

            // When / Then : ne doit surtout pas passer en négatif par débordement binaire
            assertThrows(ArithmeticException.class,
                    () -> stockMax.ajouter(10),
                    "Un dépassement d'entier doit lever ArithmeticException (ex: Math.addExact)");
            assertEquals(Integer.MAX_VALUE - 5, stockMax.getQuantite(),
                    "Le stock ne doit pas être corrompu après un overflow");
        }
    }

    // =========================================================================
    // 4. Opération métier : retirer(int montant)
    // =========================================================================

    @Nested
    @DisplayName("Méthode métier retirer(int)")
    class RetirerTests {

        @Test
        @DisplayName("retirer décrémente correctement la quantité disponible")
        void testRetirerSucces() {
            // When
            qr.retirer(30);

            // Then
            assertEquals(20, qr.getQuantite());
        }

        @Test
        @DisplayName("retirer autorise le retrait exact de la totalité du stock (solde final à 0)")
        void testRetirerTotaliteDuStock() {
            // When
            qr.retirer(50);

            // Then
            assertEquals(0, qr.getQuantite());
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -10, -500, Integer.MIN_VALUE })
        @DisplayName("retirer lève IllegalArgumentException si montant <= 0 et laisse le stock intact")
        void testRetirerMontantInvalide(int montantInvalide) {
            assertThrows(IllegalArgumentException.class, () -> qr.retirer(montantInvalide));
            assertEquals(50, qr.getQuantite(), "Le stock ne doit pas bouger après un retrait invalide");
        }

        @ParameterizedTest
        @ValueSource(ints = { 51, 100, Integer.MAX_VALUE })
        @DisplayName("retirer lève IllegalArgumentException en cas de solde insuffisant et préserve le stock")
        void testRetirerSoldeInsuffisant(int montantExcessif) {
            assertThrows(IllegalArgumentException.class, () -> qr.retirer(montantExcessif));
            assertEquals(50, qr.getQuantite(), "Le stock ne doit pas avoir changé suite à l'exception");
        }

        @Test
        @DisplayName("retirer sur un stock déjà à 0 lève IllegalArgumentException")
        void testRetirerSurStockZero() {
            // Given
            QuantiteRessource stockVide = new QuantiteRessource(0, Ressource.PIERRE);

            // When / Then
            assertThrows(IllegalArgumentException.class, () -> stockVide.retirer(1));
            assertEquals(0, stockVide.getQuantite());
        }
    }

    // =========================================================================
    // 5. Contrat Value Object : equals, hashCode et toString
    // =========================================================================

    @Nested
    @DisplayName("Contrat Value Object (equals, hashCode, toString)")
    class ValueObjectTests {

        @Test
        @DisplayName("Deux QuantiteRessource de même ressource et même quantité respectent equals et hashCode")
        void testEqualsEtHashCodeMemeValeur() {
            // Given
            QuantiteRessource a = new QuantiteRessource(50, Ressource.BOIS);
            QuantiteRessource memeReference = a;
            QuantiteRessource b = new QuantiteRessource(50, Ressource.BOIS);
            QuantiteRessource c = new QuantiteRessource(50, Ressource.BOIS);

            // Then : réflexivité, symétrie, transitivité et cohérence hashCode
            assertAll(
                () -> assertEquals(a, memeReference, "Réflexivité de equals"),
                () -> assertEquals(a, b, "Deux stocks de 50 BOIS doivent être égaux"),
                () -> assertEquals(b, a, "Symétrie de equals"),
                () -> assertEquals(b, c, "Transitivité de equals (b == c)"),
                () -> assertEquals(a, c, "Transitivité de equals (a == c)"),
                () -> assertEquals(a.hashCode(), b.hashCode(), "Deux objets égaux doivent avoir le même hashCode")
            );
        }

        @Test
        @DisplayName("equals retourne false si la quantité diffère, si la ressource diffère, pour null ou un autre type")
        void testInegalitesEquals() {
            QuantiteRessource base = new QuantiteRessource(50, Ressource.BOIS);
            QuantiteRessource autreQuantite = new QuantiteRessource(51, Ressource.BOIS);
            QuantiteRessource autreRessource = new QuantiteRessource(50, Ressource.PIERRE);
            Object referenceNull = null;
            Object autreType = "50 BOIS";

            assertAll(
                () -> assertNotEquals(base, autreQuantite),
                () -> assertNotEquals(base, autreRessource),
                () -> assertNotEquals(base, referenceNull, "equals(null) doit retourner false"),
                () -> assertNotEquals(base, autreType, "equals sur un autre type doit retourner false")
            );
        }

        @Test
        @DisplayName("toString retourne une description lisible contenant la quantité et la ressource")
        void testToStringPertinent() {
            String texte = qr.toString();

            assertAll(
                () -> assertNotNull(texte),
                () -> assertTrue(texte.contains("50"), "toString doit mentionner la quantité"),
                () -> assertTrue(texte.contains("BOIS"), "toString doit mentionner le type de ressource")
            );
        }
    }
}
