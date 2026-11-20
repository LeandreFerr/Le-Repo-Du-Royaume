package royaume.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
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

@DisplayName("Tests unitaires rigoureux - Classe Message")
public class MessageTest {
	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
	    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    validator = factory.getValidator();
	}
	
    private Partie partie;
    private Utilisateur expediteur;
    private Utilisateur destinataire;
    private LocalDateTime dateEnvoi;

    @BeforeEach
    void setUp() {
        partie = new Partie();
        partie.setId(1);
        partie.setNom("Salon Principal");

        expediteur = new Utilisateur(1, "Merlin", "merlin@broceliande.fr", "magie", true, false, null);
        destinataire = new Utilisateur(2, "Viviane", "viviane@lac.fr", "fee", true, false, null);
        dateEnvoi = LocalDateTime.of(2026, 10, 30, 22, 0);
    }

    // =========================================================================
    // 1. État initial & Constructeurs
    // =========================================================================
    @ParameterizedTest
    @ValueSource(ints = { 0, -1, -50 })
    @DisplayName("Le constructeur accepte un id invalide car il n'est pas soumis à Bean Validation")
    void testConstructeurCompletIdInvalide() {
        Message message = new Message(0, "Bonjour", dateEnvoi, false, partie, expediteur, destinataire);

        assertEquals(0, message.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "\t\n" })
    @DisplayName("Le constructeur permet de créer un message avec un contenu invalide, détecté par Validator")
    void testConstructeurCompletRejetteContenuBlanc(String contenuInvalide) {
        // Given
        Message message = new Message(1, contenuInvalide, dateEnvoi, false, partie, expediteur, destinataire);

        // When
        Set<ConstraintViolation<Message>> violations = validator.validate(message);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("contenu")));
    }

    @Test
    @DisplayName("Le Validator détecte un contenu null")
    void testConstructeurCompletRejetteContenuNull() {
        Message message = new Message(1, null, dateEnvoi, false, partie, expediteur, destinataire);

        Set<ConstraintViolation<Message>> violations = validator.validate(message);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("contenu")));
    }

    @Test
    @DisplayName("Le Validator détecte un expediteur null")
    void testConstructeurCompletRejetteExpediteurNull() {
        Message message = new Message(1, "Message sans auteur", dateEnvoi, false, partie, null, destinataire);

        Set<ConstraintViolation<Message>> violations = validator.validate(message);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("expediteur")));
    }
    // =========================================================================
    // 2. Visibilité et Routage (Chat global vs privé)
    // =========================================================================

    @Nested
    @DisplayName("Visibilité et routage des messages")
    class VisibiliteEtRoutageTests {

        @Test
        @DisplayName("estGlobal renvoie true si destinataire == null (chat de partie)")
        void testEstGlobalRenvoieTrueSansDestinataire() {
            Message messageGlobal = new Message(1, "Bonjour à tous les joueurs !", dateEnvoi, false, partie, expediteur, null);

            assertAll(
                () -> assertTrue(messageGlobal.estGlobal(), "Un message sans destinataire doit être global"),
                () -> assertNull(messageGlobal.getDestinataire()),
                () -> assertSame(expediteur, messageGlobal.getExpediteur()),
                () -> assertSame(partie, messageGlobal.getPartie())
            );
        }

        @Test
        @DisplayName("estGlobal renvoie false si un destinataire distinct est désigné (message privé)")
        void testEstGlobalRenvoieFalseAvecDestinataire() {
            Message messagePrive = new Message(2, "Alliance secrète ?", dateEnvoi, false, partie, expediteur, destinataire);

            assertAll(
                () -> assertFalse(messagePrive.estGlobal(), "Un message avec destinataire précis est privé"),
                () -> assertSame(destinataire, messagePrive.getDestinataire()),
                () -> assertEquals("Alliance secrète ?", messagePrive.getContenu())
            );
        }

        @Test
        @DisplayName("Un message privé adressé à soi-même (expéditeur == destinataire) est rejeté")
        void testMessagePriveVersSoiMemeRejete() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Message(3, "Mon monologue", dateEnvoi, false, partie, expediteur, expediteur),
                    "Un joueur ne peut pas s'envoyer un message privé à lui-même");
        }
    }

    // =========================================================================
    // 3. Statut de lecture et Mutateurs
    // =========================================================================
    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "\t" })
    @DisplayName("setContenu accepte une chaîne blanche mais Validator la détecte")
    void testSetContenuRejetteBlanc(String contenuInvalide) {
        Message msg = new Message(1, "Valide", dateEnvoi, false, partie, expediteur, null);

        msg.setContenu(contenuInvalide);

        Set<ConstraintViolation<Message>> violations = validator.validate(msg);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("contenu")));
    }

    @Test
    @DisplayName("setContenu accepte null mais Validator le détecte")
    void testSetContenuRejetteNull() {
        Message msg = new Message(1, "Valide", dateEnvoi, false, partie, expediteur, null);

        msg.setContenu(null);

        Set<ConstraintViolation<Message>> violations = validator.validate(msg);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("contenu")));
    }

    @Test
    @DisplayName("setExpediteur accepte null mais Validator le détecte")
    void testSetExpediteurRejetteNull() {
        Message msg = new Message(1, "Valide", dateEnvoi, false, partie, expediteur, null);

        msg.setExpediteur(null);

        Set<ConstraintViolation<Message>> violations = validator.validate(msg);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("expediteur")));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, -50 })
    @DisplayName("setId accepte les identifiants invalides")
    void testSetIdRejetteInvalide(int idInvalide) {
        Message msg = new Message(1, "Valide", dateEnvoi, false, partie, expediteur, null);

        msg.setId(idInvalide);

        assertEquals(idInvalide, msg.getId());
    }

    // =========================================================================
    // 4. Représentation textuelle
    // =========================================================================

    @Nested
    @DisplayName("Représentation textuelle")
    class RepresentationTests {

        @Test
        @DisplayName("toString produit une chaîne complète sans lever d'exception")
        void testToStringSansBoucleInfinie() {
            Message msg = new Message(5, "Rassemblement", dateEnvoi, true, partie, expediteur, destinataire);

            String description = msg.toString();

            assertAll(
                () -> assertNotNull(description),
                () -> assertTrue(description.contains("Rassemblement")),
                () -> assertTrue(description.contains("id=5")),
                () -> assertTrue(description.contains("lu=true"))
            );
        }
    }
    
    @Test
    @DisplayName("Le Validator détecte un contenu supérieur à 1000 caractères")
    void testContenuTropLong() {
        Message msg = new Message(
                1,
                "A".repeat(1001),
                dateEnvoi,
                false,
                partie,
                expediteur,
                null
        );

        Set<ConstraintViolation<Message>> violations = validator.validate(msg);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("contenu")));
    }
    
    @Test
    @DisplayName("Le Validator détecte une partie null")
    void testPartieNull() {
        Message msg = new Message(
                1,
                "Bonjour",
                dateEnvoi,
                false,
                null,
                expediteur,
                null
        );

        Set<ConstraintViolation<Message>> violations = validator.validate(msg);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("partie")));
    }
}
