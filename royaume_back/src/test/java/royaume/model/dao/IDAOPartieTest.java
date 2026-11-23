package royaume.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import royaume.dao.IDAOMessage;
import royaume.dao.IDAOPartie;
import royaume.dao.IDAOUtilisateur;
import royaume.model.Message;
import royaume.model.Partie;
import royaume.model.StatutPartie;
import royaume.model.Utilisateur;

@DataJpaTest
class IDAOPartieTest {

    @Autowired
    private IDAOPartie daoPartie;

    @Autowired
    private IDAOMessage daoMessage;
    
    @Autowired
    private IDAOUtilisateur daoUtilisateur;

    @Autowired
    private EntityManager entityManager;

    private Partie creerPartie(String nom, StatutPartie statut) {
        return new Partie(
            null,
            nom,
            LocalDateTime.now(),
            1.0,
            10,
            null,
            null,
            null,
            null,
            statut
        );
    }

    @Test
    void testPersistancePartie() {
        // Given
        Partie partie = creerPartie(
            "Guerre des royaumes",
            StatutPartie.EN_ATTENTE
        );

        // When
        Partie sauvegardee = daoPartie.saveAndFlush(partie);

        // Then
        assertNotNull(sauvegardee.getId());

        Partie resultat = daoPartie.findById(
            sauvegardee.getId()
        ).orElseThrow();

        assertEquals("Guerre des royaumes", resultat.getNom());
        assertEquals(StatutPartie.EN_ATTENTE, resultat.getStatut());
    }

    @Test
    void testFindByStatut() {
        // Given
        daoPartie.save(creerPartie(
            "Partie 1", StatutPartie.EN_ATTENTE
        ));

        daoPartie.save(creerPartie(
            "Partie 2", StatutPartie.EN_ATTENTE
        ));

        daoPartie.save(creerPartie(
            "Partie 3", StatutPartie.EN_COURS
        ));

        daoPartie.flush();

        // When
        List<Partie> resultat =
            daoPartie.findByStatut(StatutPartie.EN_ATTENTE);

        // Then
        assertEquals(2, resultat.size());

        assertTrue(resultat.stream().allMatch(
            p -> p.getStatut() == StatutPartie.EN_ATTENTE
        ));
    }

    @Test
    void testPersistanceCascadeMessages() {
        // Given
        Utilisateur expediteur = new Utilisateur(
            null,
            "Arthur",
            "arthur@camelot.fr",
            "password123",
            false,
            false,
            null
        );

        daoUtilisateur.saveAndFlush(expediteur);

        Partie partie = creerPartie(
            "Partie avec messages",
            StatutPartie.EN_ATTENTE
        );

        Message message1 = new Message(
            null,
            "Bonjour tout le monde !",
            LocalDateTime.now(),
            false,
            partie,
            expediteur,
            null
        );

        Message message2 = new Message(
            null,
            "Bienvenue dans la partie !",
            LocalDateTime.now().plusSeconds(1),
            false,
            partie,
            expediteur,
            null
        );

        partie.ajouterMessage(message1);
        partie.ajouterMessage(message2);

        // When
        Partie sauvegardee = daoPartie.saveAndFlush(partie);

        // On vide le contexte de persistance
        // pour forcer une nouvelle lecture depuis la BDD
        Integer idPartie = sauvegardee.getId();

        entityManager.clear();

        Partie resultat = daoPartie.findById(idPartie)
            .orElseThrow();

        // Then
        assertEquals(2, resultat.getMessages().size());

        assertEquals(
            "Bonjour tout le monde !",
            resultat.getMessages().get(0).getContenu()
        );

        assertEquals(
            "Bienvenue dans la partie !",
            resultat.getMessages().get(1).getContenu()
        );

        assertNotNull(resultat.getMessages().get(0).getId());
        assertNotNull(resultat.getMessages().get(1).getId());
    }
    

@Test
void testSuppressionMessageAvecOrphanRemoval() {
    // Given
    Utilisateur expediteur = new Utilisateur(
        null, "Arthur", "arthur@camelot.fr",
        "password123", false, false, null
    );

    daoUtilisateur.saveAndFlush(expediteur);

    Partie partie = creerPartie(
        "Partie suppression",
        StatutPartie.EN_ATTENTE
    );

    Message message = new Message(
        null,
        "Message à supprimer",
        LocalDateTime.now(),
        false,
        partie,
        expediteur,
        null
    );

    partie.ajouterMessage(message);
    daoPartie.saveAndFlush(partie);

    Integer idPartie = partie.getId();
    Integer idMessage = message.getId();

    // When
    partie = daoPartie.findById(idPartie).orElseThrow();

    Message messageASupprimer = partie.getMessages()
        .stream()
        .filter(m -> m.getId().equals(idMessage))
        .findFirst()
        .orElseThrow();

    partie.getMessages(); // Collection en lecture seule

    // Il faut une méthode métier pour retirer le message
    // ou modifier la collection en interne.

    // Then
    partie.supprimerMessage(messageASupprimer);

    daoPartie.flush();

    assertFalse(daoMessage.existsById(idMessage));
}

}
