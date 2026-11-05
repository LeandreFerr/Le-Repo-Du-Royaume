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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests unitaires rigoureux - Classe Message")
public class MessageTest {

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

    @Nested
    @DisplayName("État initial et constructeurs")
    class EtatInitialEtConstructeursTests {

        @Test
        @DisplayName("Le constructeur par défaut initialise un message non lu et global par défaut")
        void testEtatInitialParDefaut() {
            // Given / When
            Message message = new Message();

            // Then
            assertAll(
                () -> assertFalse(message.isLu(), "Un message nouvellement créé doit être marqué comme non lu"),
                () -> assertTrue(message.estGlobal(), "Sans destinataire, le message est considéré global par défaut"),
                () -> assertNull(message.getPartie()),
                () -> assertNull(message.getExpediteur()),
                () -> assertNull(message.getDestinataire())
            );
        }

        @Test
        @DisplayName("Le constructeur complet initialise fidèlement tous les champs")
        void testConstructeurCompletNominal() {
            Message message = new Message(1, "Attaquez au nord !", dateEnvoi, false, partie, expediteur, destinataire);

            assertAll(
                () -> assertEquals(1, message.getId()),
                () -> assertEquals("Attaquez au nord !", message.getContenu()),
                () -> assertEquals(dateEnvoi, message.getDateEnvoi()),
                () -> assertFalse(message.isLu()),
                () -> assertSame(partie, message.getPartie()),
                () -> assertSame(expediteur, message.getExpediteur()),
                () -> assertSame(destinataire, message.getDestinataire())
            );
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -50 })
        @DisplayName("Le constructeur complet rejette un id <= 0 via IllegalArgumentException")
        void testConstructeurCompletRejetteIdInvalide(int idInvalide) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Message(idInvalide, "Bonjour", dateEnvoi, false, partie, expediteur, destinataire));
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   ", "\t\n" })
        @DisplayName("Le constructeur complet rejette un contenu vide ou blanc via IllegalArgumentException")
        void testConstructeurCompletRejetteContenuBlanc(String contenuInvalide) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Message(1, contenuInvalide, dateEnvoi, false, partie, expediteur, destinataire));
        }

        @Test
        @DisplayName("Le constructeur complet rejette un contenu null via IllegalArgumentException")
        void testConstructeurCompletRejetteContenuNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Message(1, null, dateEnvoi, false, partie, expediteur, destinataire));
        }

        @Test
        @DisplayName("Le constructeur complet rejette un expediteur null via IllegalArgumentException")
        void testConstructeurCompletRejetteExpediteurNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Message(1, "Message sans auteur", dateEnvoi, false, partie, null, destinataire));
        }
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

    @Nested
    @DisplayName("Statut de lecture et mutateurs")
    class StatutEtMutateursTests {

        @Test
        @DisplayName("setLu permet de marquer et démarquer un message")
        void testMarquerMessageCommeLu() {
            Message msg = new Message(3, "Notification", dateEnvoi, false, partie, expediteur, destinataire);
            assertFalse(msg.isLu());

            msg.setLu(true);
            assertTrue(msg.isLu());

            msg.setLu(false);
            assertFalse(msg.isLu());
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   ", "\t" })
        @DisplayName("setContenu rejette une chaîne blanche via IllegalArgumentException")
        void testSetContenuRejetteBlanc(String contenuInvalide) {
            Message msg = new Message(1, "Valide", dateEnvoi, false, partie, expediteur, null);

            assertThrows(IllegalArgumentException.class, () -> msg.setContenu(contenuInvalide));
            assertEquals("Valide", msg.getContenu());
        }

        @Test
        @DisplayName("setContenu rejette null via IllegalArgumentException")
        void testSetContenuRejetteNull() {
            Message msg = new Message(1, "Valide", dateEnvoi, false, partie, expediteur, null);

            assertThrows(IllegalArgumentException.class, () -> msg.setContenu(null));
            assertEquals("Valide", msg.getContenu());
        }

        @Test
        @DisplayName("setExpediteur rejette null via IllegalArgumentException")
        void testSetExpediteurRejetteNull() {
            Message msg = new Message(1, "Valide", dateEnvoi, false, partie, expediteur, null);

            assertThrows(IllegalArgumentException.class, () -> msg.setExpediteur(null));
            assertSame(expediteur, msg.getExpediteur());
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -50 })
        @DisplayName("setId rejette les identifiants <= 0 via IllegalArgumentException")
        void testSetIdRejetteInvalide(int idInvalide) {
            Message msg = new Message(1, "Valide", dateEnvoi, false, partie, expediteur, null);

            assertThrows(IllegalArgumentException.class, () -> msg.setId(idInvalide));
            assertEquals(1, msg.getId());
        }
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
}
