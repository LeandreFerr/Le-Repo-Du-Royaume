package royaume.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    @Test
    @DisplayName("Par défaut, un nouveau message est non lu (lu = false)")
    void testEtatInitialParDefaut() {
        // Given / When
        Message message = new Message();

        // Then
        assertFalse(message.isLu(), "Un message nouvellement créé doit être marqué comme non lu");
        assertTrue(message.estGlobal(), "Sans destinataire, le message est considéré global par défaut");
    }

    @Test
    @DisplayName("estGlobal renvoie true lorsqu'aucun destinataire n'est spécifié (chat global)")
    void testEstGlobalRenvoieTrueSansDestinataire() {
        // Given
        Message messageGlobal = new Message(1, "Bonjour à tous les amiiiiis, c'est David Lafarge Pokemonnn et je suis en compagnie de .... Miss Jirachi ... Coucouuuuuuuuuuuu !", dateEnvoi, false, partie, expediteur, null);

        // When
        boolean global = messageGlobal.estGlobal();

        // Then
        assertTrue(global, "Un message avec destinataire == null doit être global");
        assertNull(messageGlobal.getDestinataire());
        assertSame(expediteur, messageGlobal.getExpediteur());
        assertSame(partie, messageGlobal.getPartie());
    }

    @Test
    @DisplayName("estGlobal renvoie false lorsqu'un destinataire est renseigné (message privé)")
    void testEstGlobalRenvoieFalseAvecDestinataire() {
        // Given
        String contenu = "Flip reset musty double tap dans tes dm ... En esperant que le backboard ne soit pas défendu ...";
        Message messagePrive = new Message(2, contenu, dateEnvoi, false, partie, expediteur, destinataire);

        // When
        boolean global = messagePrive.estGlobal();

        // Then
        assertFalse(global, "Un message adressé à un destinataire précis est privé");
        assertSame(destinataire, messagePrive.getDestinataire());
        assertEquals(contenu, messagePrive.getContenu());
        assertEquals(dateEnvoi, messagePrive.getDateEnvoi());
    }

    @Test
    @DisplayName("Le marquage comme lu met bien à jour l'état du message")
    void testMarquerMessageCommeLu() {
        // Given
        Message messagePrive = new Message(3, "POV : Elle te met un vu", dateEnvoi, false, partie, expediteur, destinataire);
        assertFalse(messagePrive.isLu());

        // When
        messagePrive.setLu(true);

        // Then
        assertTrue(messagePrive.isLu());
    }
}

