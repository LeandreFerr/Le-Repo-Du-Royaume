package royaume.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import royaume.model.Message;
import royaume.model.Partie;
import royaume.model.StatutPartie;
import royaume.model.Utilisateur;

@DataJpaTest
class IDAOMessageTest {

    @Autowired
    private IDAOMessage daoMessage;

    @Autowired
    private IDAOPartie daoPartie;

    @Autowired
    private IDAOUtilisateur daoUtilisateur;

    private Utilisateur creerUtilisateur() {
        return daoUtilisateur.saveAndFlush(
            new Utilisateur(
                null,
                "Arthur",
                "arthur@camelot.fr",
                "password123",
                false,
                false,
                null
            )
        );
    }

    private Partie creerPartie() {
        return daoPartie.saveAndFlush(
            new Partie(
                null,
                "Partie test",
                LocalDateTime.now(),
                1.0,
                10,
                null,
                null,
                null,
                null,
                StatutPartie.EN_ATTENTE
            )
        );
    }

    @Test
    void testPersistanceMessage() {
        // Given
        Utilisateur expediteur = creerUtilisateur();
        Partie partie = creerPartie();

        Message message = new Message(
            null,
            "Bonjour !",
            LocalDateTime.now(),
            false,
            partie,
            expediteur,
            null
        );

        // When
        Message sauvegarde = daoMessage.saveAndFlush(message);

        // Then
        assertNotNull(sauvegarde.getId());

        Message resultat = daoMessage.findById(
            sauvegarde.getId()
        ).orElseThrow();

        assertEquals("Bonjour !", resultat.getContenu());
        assertFalse(resultat.isLu());
        assertTrue(resultat.estGlobal());
    }

    @Test
    void testFindByPartieIdOrderByDateEnvoiAsc() {
        // Given
        Utilisateur expediteur = creerUtilisateur();
        Partie partie = creerPartie();

        LocalDateTime maintenant = LocalDateTime.now();

        Message messageRecent = new Message(
            null,
            "Message récent",
            maintenant.plusMinutes(2),
            false,
            partie,
            expediteur,
            null
        );

        Message messageAncien = new Message(
            null,
            "Message ancien",
            maintenant,
            false,
            partie,
            expediteur,
            null
        );

        Message messageIntermediaire = new Message(
            null,
            "Message intermédiaire",
            maintenant.plusMinutes(1),
            false,
            partie,
            expediteur,
            null
        );

        daoMessage.save(messageRecent);
        daoMessage.save(messageAncien);
        daoMessage.save(messageIntermediaire);

        daoMessage.flush();

        // When
        List<Message> resultat =
            daoMessage.findByPartieIdOrderByDateEnvoiAsc(
                partie.getId()
            );

        // Then
        assertEquals(3, resultat.size());

        assertEquals(
            "Message ancien",
            resultat.get(0).getContenu()
        );

        assertEquals(
            "Message intermédiaire",
            resultat.get(1).getContenu()
        );

        assertEquals(
            "Message récent",
            resultat.get(2).getContenu()
        );
    }

    @Test
    void testMessageGlobalSansDestinataire() {
        // Given
        Utilisateur expediteur = creerUtilisateur();
        Partie partie = creerPartie();

        Message message = new Message(
            null,
            "Message pour tout le monde",
            LocalDateTime.now(),
            false,
            partie,
            expediteur,
            null
        );

        // When
        Message sauvegarde = daoMessage.saveAndFlush(message);

        Message resultat = daoMessage.findById(
            sauvegarde.getId()
        ).orElseThrow();

        // Then
        assertNull(resultat.getDestinataire());
        assertTrue(resultat.estGlobal());
    }
}
