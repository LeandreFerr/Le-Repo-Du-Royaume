
package royaume.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import royaume.model.Utilisateur;

@DataJpaTest
class IDAOUtilisateurTest {

    @Autowired
    private IDAOUtilisateur daoUtilisateur;

    @Test
    void testPersistanceUtilisateur() {
        // Given
        Utilisateur utilisateur = new Utilisateur(
            null, "Arthur", "arthur@camelot.fr",
            "excalibur123", false, false, null
        );

        // When
        Utilisateur sauvegarde = daoUtilisateur.saveAndFlush(utilisateur);

        // Then
        assertNotNull(sauvegarde.getId());

        Optional<Utilisateur> resultat =
            daoUtilisateur.findById(sauvegarde.getId());

        assertTrue(resultat.isPresent());
        assertEquals("Arthur", resultat.get().getPseudo());
        assertEquals("arthur@camelot.fr", resultat.get().getEmail());
    }

    @Test
    void testFindByPseudo() {
        // Given
        Utilisateur utilisateur = new Utilisateur(
            null, "Lancelot", "lancelot@camelot.fr",
            "chevalier123", false, false, null
        );

        daoUtilisateur.saveAndFlush(utilisateur);

        // When
        Optional<Utilisateur> resultat =
            daoUtilisateur.findByPseudo("Lancelot");

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("Lancelot", resultat.get().getPseudo());
    }

    @Test
    void testFindByEmail() {
        // Given
        Utilisateur utilisateur = new Utilisateur(
            null, "Merlin", "merlin@camelot.fr",
            "magie123", false, true, null
        );

        daoUtilisateur.saveAndFlush(utilisateur);

        // When
        Optional<Utilisateur> resultat =
            daoUtilisateur.findByEmail("merlin@camelot.fr");

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("Merlin", resultat.get().getPseudo());
    }

    @Test
    void testFindByPseudoInexistant() {
        // When
        Optional<Utilisateur> resultat =
            daoUtilisateur.findByPseudo("Inconnu");

        // Then
        assertTrue(resultat.isEmpty());
    }

    @Test
    void testUnicitePseudo() {
        // Given
        Utilisateur user1 = new Utilisateur(
            null, "Arthur", "arthur1@camelot.fr",
            "password123", false, false, null
        );

        Utilisateur user2 = new Utilisateur(
            null, "Arthur", "arthur2@camelot.fr",
            "password123", false, false, null
        );

        daoUtilisateur.saveAndFlush(user1);

        // When / Then
        assertThrows(Exception.class, () -> {
            daoUtilisateur.saveAndFlush(user2);
        });
    }

    @Test
    void testUniciteEmail() {
        // Given
        Utilisateur user1 = new Utilisateur(
            null, "Arthur", "arthur@camelot.fr",
            "password123", false, false, null
        );

        Utilisateur user2 = new Utilisateur(
            null, "Lancelot", "arthur@camelot.fr",
            "password456", false, false, null
        );

        daoUtilisateur.saveAndFlush(user1);

        // When / Then
        assertThrows(Exception.class, () -> {
            daoUtilisateur.saveAndFlush(user2);
        });
    }
}
