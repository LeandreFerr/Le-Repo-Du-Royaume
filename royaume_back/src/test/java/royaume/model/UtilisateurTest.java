package royaume.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UtilisateurTest {

    private Utilisateur utilisateur;
    private Royaume royaume;

    @BeforeEach
    void setUp() {
        royaume = new Royaume(1, "Avalon");
        utilisateur = new Utilisateur(1, "Arthur", "arthur@camelot.fr", "excalibur123", false, false, royaume);
    }

    @Test
    @DisplayName("Par défaut, un nouvel utilisateur est hors-ligne et non administrateur")
    void testEtatInitialParDefaut() {
        // Given / When
        Utilisateur nouveau = new Utilisateur();

        // Then
        assertFalse(nouveau.isOnline(), "Un utilisateur nouvellement instancié doit être hors-ligne");
        assertFalse(nouveau.isAdmin(), "Un utilisateur nouvellement instancié ne doit pas être admin");
        assertNull(nouveau.getRoyaume(), "Aucun royaume ne doit être rattaché par défaut");
    }

    @Test
    @DisplayName("seConnecter retourne true et passe online à true si le mot de passe est valide")
    void testSeConnecterMotDePasseValide() {
        // Given
        assertFalse(utilisateur.isOnline());

        // When
        boolean connecte = utilisateur.seConnecter("excalibur123");

        // Then
        assertTrue(connecte, "La connexion doit réussir avec le bon mot de passe");
        assertTrue(utilisateur.isOnline(), "L'utilisateur doit passer en ligne");
    }

    @Test
    @DisplayName("seConnecter retourne false et laisse online à false si le mot de passe est erroné")
    void testSeConnecterMotDePasseInvalide() {
        // Given
        assertFalse(utilisateur.isOnline());

        // When
        boolean connecte = utilisateur.seConnecter("mauvaisMdp");

        // Then
        assertFalse(connecte, "La connexion doit échouer avec un mauvais mot de passe");
        assertFalse(utilisateur.isOnline(), "L'utilisateur doit rester hors-ligne");
    }

    @Test
    @DisplayName("seConnecter gère proprement un mot de passe null sans lever d'exception")
    void testSeConnecterMotDePasseNull() {
        // Given
        Utilisateur sansMdp = new Utilisateur();

        // When / Then
        assertFalse(utilisateur.seConnecter(null), "Un mot de passe null doit être refusé");
        assertFalse(utilisateur.isOnline());
        assertFalse(sansMdp.seConnecter(null), "Même si this.password est null, null ne doit pas connecter");
        assertFalse(sansMdp.seConnecter("test"));
    }

    @Test
    @DisplayName("seDeconnecter repasse bien l'utilisateur hors-ligne (online = false)")
    void testSeDeconnecter() {
        // Given
        utilisateur.seConnecter("excalibur123");
        assertTrue(utilisateur.isOnline());

        // When
        utilisateur.seDeconnecter();

        // Then
        assertFalse(utilisateur.isOnline(), "L'utilisateur doit repasser hors-ligne après déconnexion");
    }

    @Test
    @DisplayName("abandonnerPartie détache le royaume de l'utilisateur")
    void testAbandonnerPartie() {
        // Given
        assertSame(royaume, utilisateur.getRoyaume());

        // When
        utilisateur.abandonnerPartie();

        // Then
        assertNull(utilisateur.getRoyaume(), "Le royaume doit être détaché après abandon");
        assertEquals("Arthur", utilisateur.getPseudo(), "Les autres attributs ne doivent pas être altérés");
    }
}

