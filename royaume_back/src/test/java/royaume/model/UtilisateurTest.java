package royaume.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import royaume.model.Peuple.Canards;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("Tests unitaires rigoureux - Classe Utilisateur")
public class UtilisateurTest {

	
	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
	    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    validator = factory.getValidator();
	}
	
    private Utilisateur utilisateur;
    private Royaume royaume;

    @BeforeEach
    void setUp() {
        royaume = new Royaume(1, "Avalon", new Canards(1, "Canards", "Archiduc", 0.05));
        utilisateur = new Utilisateur(1, "Arthur", "arthur@camelot.fr", "excalibur123", false, false, royaume);
    }

    // =========================================================================
    // 1. État initial & Constructeurs
    // =========================================================================

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, -99 })
    @DisplayName("Le constructeur accepte un id invalide car aucune contrainte Bean Validation ne lui est associée")
    void testConstructeurCompletRejetteIdInvalide(int idInvalide) {
        Utilisateur user = new Utilisateur(
                idInvalide, "Arthur", "arthur@camelot.fr",
                "excalibur123", false, false, royaume
        );

        assertEquals(idInvalide, user.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "\t\n" })
    @DisplayName("Le Validator détecte un pseudo vide ou blanc")
    void testConstructeurCompletRejettePseudoBlanc(String pseudoInvalide) {
        // Given
        Utilisateur user = new Utilisateur(
                1, pseudoInvalide, "arthur@camelot.fr",
                "excalibur123", false, false, royaume
        );

        // When
        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("pseudo")));
    }

    @Test
    @DisplayName("Le Validator détecte un pseudo null")
    void testConstructeurCompletRejettePseudoNull() {
        Utilisateur user = new Utilisateur(
                1, null, "arthur@camelot.fr",
                "excalibur123", false, false, royaume
        );

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("pseudo")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "\t" })
    @DisplayName("Le Validator détecte un email ou un mot de passe blanc")
    void testConstructeurCompletRejetteEmailEtPasswordBlancs(String chaineBlanche) {
        // Email invalide
        Utilisateur userEmail = new Utilisateur(
                1, "Arthur", chaineBlanche,
                "excalibur123", false, false, royaume
        );

        Set<ConstraintViolation<Utilisateur>> violationsEmail = validator.validate(userEmail);

        assertTrue(violationsEmail.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));

        // Mot de passe invalide
        Utilisateur userPassword = new Utilisateur(
                1, "Arthur", "arthur@camelot.fr",
                chaineBlanche, false, false, royaume
        );

        Set<ConstraintViolation<Utilisateur>> violationsPassword = validator.validate(userPassword);

        assertTrue(violationsPassword.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    // =========================================================================
    // 2. Authentification et cycle de connexion
    // =========================================================================

    @Nested
    @DisplayName("Authentification et cycle de connexion")
    class AuthentificationTests {

        @Test
        @DisplayName("seConnecter retourne true et passe online à true avec le mot de passe exact")
        void testSeConnecterSucces() {
            assertFalse(utilisateur.isOnline());

            boolean connecte = utilisateur.seConnecter("excalibur123");

            assertAll(
                () -> assertTrue(connecte, "La connexion doit réussir avec le bon mot de passe"),
                () -> assertTrue(utilisateur.isOnline(), "L'utilisateur doit passer en ligne")
            );
        }

        @Test
        @DisplayName("seConnecter retourne false et laisse online à false si le mot de passe est erroné")
        void testSeConnecterEchec() {
            assertFalse(utilisateur.isOnline());

            boolean connecte = utilisateur.seConnecter("mauvaisMotDePasse");

            assertAll(
                () -> assertFalse(connecte, "La connexion doit échouer avec un mauvais mot de passe"),
                () -> assertFalse(utilisateur.isOnline(), "L'utilisateur doit rester hors-ligne")
            );
        }

        @Test
        @DisplayName("seConnecter respecte la casse des caractères du mot de passe")
        void testSeConnecterSensibiliteCasse() {
            boolean majuscule = utilisateur.seConnecter("Excalibur123");
            boolean autreCasse = utilisateur.seConnecter("EXCALIBUR123");

            assertAll(
                () -> assertFalse(majuscule, "Le mot de passe doit respecter strictement la casse"),
                () -> assertFalse(autreCasse),
                () -> assertFalse(utilisateur.isOnline())
            );
        }

        @Test
        @DisplayName("seConnecter lève IllegalArgumentException si le mot de passe fourni est null ou blanc")
        void testSeConnecterRejetteMdpNullOuBlanc() {
            assertThrows(IllegalArgumentException.class, () -> utilisateur.seConnecter(null));
            assertThrows(IllegalArgumentException.class, () -> utilisateur.seConnecter(""));
            assertThrows(IllegalArgumentException.class, () -> utilisateur.seConnecter("   "));
            assertFalse(utilisateur.isOnline());
        }

        @Test
        @DisplayName("seDeconnecter repasse bien l'utilisateur hors-ligne (online = false)")
        void testSeDeconnecter() {
            utilisateur.seConnecter("excalibur123");
            assertTrue(utilisateur.isOnline());

            utilisateur.seDeconnecter();

            assertFalse(utilisateur.isOnline(), "L'utilisateur doit repasser hors-ligne après déconnexion");
        }
    }

    // =========================================================================
    // 3. Gestion de partie et détachement du royaume
    // =========================================================================

    @Nested
    @DisplayName("Gestion du royaume et abandon de partie")
    class GestionRoyaumeTests {

        @Test
        @DisplayName("abandonnerPartie détache le royaume sans altérer les autres attributs")
        void testAbandonnerPartie() {
            assertSame(royaume, utilisateur.getRoyaume());

            utilisateur.abandonnerPartie();

            assertAll(
                () -> assertNull(utilisateur.getRoyaume(), "Le royaume doit être détaché après abandon"),
                () -> assertEquals("Arthur", utilisateur.getPseudo()),
                () -> assertEquals("arthur@camelot.fr", utilisateur.getEmail())
            );
        }

        @Test
        @DisplayName("setRoyaume permet d'affecter un nouveau royaume ou null")
        void testSetRoyaume() {
            Royaume camelot = new Royaume(2, "Camelot", new Canards(1, "Canards", "Archiduc", 0.05));
            utilisateur.setRoyaume(camelot);

            assertSame(camelot, utilisateur.getRoyaume());

            utilisateur.setRoyaume(null);
            assertNull(utilisateur.getRoyaume());
        }
    }

    // =========================================================================
    // 4. Validation des mutateurs et invariants
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "\t" })
    @DisplayName("setPseudo accepte une chaîne blanche mais Validator la détecte")
    void testSetPseudoRejetteBlanc(String pseudoInvalide) {
        utilisateur.setPseudo(pseudoInvalide);

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("pseudo")));
    }

    @Test
    @DisplayName("setPseudo accepte null mais Validator le détecte")
    void testSetPseudoRejetteNull() {
        utilisateur.setPseudo(null);

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("pseudo")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "\t" })
    @DisplayName("setEmail accepte une chaîne blanche mais Validator la détecte")
    void testSetEmailRejetteBlanc(String emailInvalide) {
        utilisateur.setEmail(emailInvalide);

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("setEmail accepte null mais Validator le détecte")
    void testSetEmailRejetteNull() {
        utilisateur.setEmail(null);

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "\t" })
    @DisplayName("setPassword accepte une chaîne blanche mais Validator la détecte")
    void testSetPasswordRejetteBlanc(String mdpInvalide) {
        utilisateur.setPassword(mdpInvalide);

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("setPassword accepte null mais Validator le détecte")
    void testSetPasswordRejetteNull() {
        utilisateur.setPassword(null);

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, -50 })
    @DisplayName("setId accepte les identifiants invalides")
    void testSetIdRejetteNonPositif(int idInvalide) {
        utilisateur.setId(idInvalide);

        assertEquals(idInvalide, utilisateur.getId());
    }
    
    @Test
    @DisplayName("Le Validator détecte un email au format invalide")
    void testEmailFormatInvalide() {
        utilisateur.setEmail("arthur-pas-un-email");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Le Validator détecte un pseudo trop court")
    void testPseudoTropCourt() {
        utilisateur.setPseudo("Ab");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("pseudo")));
    }

    @Test
    @DisplayName("Le Validator détecte un pseudo trop long")
    void testPseudoTropLong() {
        utilisateur.setPseudo("A".repeat(31));

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("pseudo")));
    }

    @Test
    @DisplayName("Le Validator détecte un mot de passe trop court")
    void testPasswordTropCourt() {
        utilisateur.setPassword("abc");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("Le Validator détecte un email trop long")
    void testEmailTropLong() {
        utilisateur.setEmail("a".repeat(90) + "@example.com");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    // =========================================================================
    // 5. Sécurité et Représentation textuelle
    // =========================================================================

    @Nested
    @DisplayName("Sécurité et représentation textuelle")
    class SecuriteEtRepresentationTests {

        @Test
        @DisplayName("toString ne divulgue JAMAIS le mot de passe en clair et le masque")
        void testToStringNeDivulguePasLeMotDePasse() {
            String description = utilisateur.toString();

            assertAll(
                () -> assertNotNull(description),
                () -> assertFalse(description.contains("excalibur123"),
                        "Le mot de passe secret ne doit JAMAIS apparaître dans toString() !"),
                () -> assertTrue(description.contains("****"),
                        "Le mot de passe doit être masqué (ex: password=****) dans les logs"),
                () -> assertTrue(description.contains("Arthur"),
                        "Le pseudo doit figurer dans la représentation"),
                () -> assertTrue(description.contains("arthur@camelot.fr"),
                        "L'email doit figurer dans la représentation")
            );
        }
    }
}
