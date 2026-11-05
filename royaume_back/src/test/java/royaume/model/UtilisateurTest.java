package royaume.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests unitaires rigoureux - Classe Utilisateur")
public class UtilisateurTest {

    private Utilisateur utilisateur;
    private Royaume royaume;

    @BeforeEach
    void setUp() {
        royaume = new Royaume(1, "Avalon");
        utilisateur = new Utilisateur(1, "Arthur", "arthur@camelot.fr", "excalibur123", false, false, royaume);
    }

    // =========================================================================
    // 1. État initial & Constructeurs
    // =========================================================================

    @Nested
    @DisplayName("État initial et constructeurs")
    class EtatInitialEtConstructeursTests {

        @Test
        @DisplayName("Le constructeur par défaut initialise un utilisateur hors-ligne et non admin")
        void testEtatInitialParDefaut() {
            // Given / When
            Utilisateur nouveau = new Utilisateur();

            // Then
            assertAll(
                () -> assertFalse(nouveau.isOnline(), "Un utilisateur nouvellement instancié doit être hors-ligne"),
                () -> assertFalse(nouveau.isAdmin(), "Un utilisateur ne doit pas être administrateur par défaut"),
                () -> assertNull(nouveau.getRoyaume(), "Aucun royaume rattaché par défaut")
            );
        }

        @Test
        @DisplayName("Le constructeur complet initialise fidèlement tous les champs")
        void testConstructeurCompletNominal() {
            assertAll(
                () -> assertEquals(1, utilisateur.getId()),
                () -> assertEquals("Arthur", utilisateur.getPseudo()),
                () -> assertEquals("arthur@camelot.fr", utilisateur.getEmail()),
                () -> assertEquals("excalibur123", utilisateur.getPassword()),
                () -> assertFalse(utilisateur.isOnline()),
                () -> assertFalse(utilisateur.isAdmin()),
                () -> assertSame(royaume, utilisateur.getRoyaume())
            );
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -99 })
        @DisplayName("Le constructeur complet rejette un id <= 0 via IllegalArgumentException")
        void testConstructeurCompletRejetteIdInvalide(int idInvalide) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Utilisateur(idInvalide, "Arthur", "arthur@camelot.fr", "excalibur123", false, false, royaume));
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   ", "\t\n" })
        @DisplayName("Le constructeur complet rejette un pseudo vide ou blanc via IllegalArgumentException")
        void testConstructeurCompletRejettePseudoBlanc(String pseudoInvalide) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Utilisateur(1, pseudoInvalide, "arthur@camelot.fr", "excalibur123", false, false, royaume));
        }

        @Test
        @DisplayName("Le constructeur complet rejette un pseudo null via IllegalArgumentException")
        void testConstructeurCompletRejettePseudoNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Utilisateur(1, null, "arthur@camelot.fr", "excalibur123", false, false, royaume));
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   ", "\t" })
        @DisplayName("Le constructeur complet rejette un email ou un mot de passe blanc via IllegalArgumentException")
        void testConstructeurCompletRejetteEmailEtPasswordBlancs(String chaineBlanche) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Utilisateur(1, "Arthur", chaineBlanche, "excalibur123", false, false, royaume));
            assertThrows(IllegalArgumentException.class,
                    () -> new Utilisateur(1, "Arthur", "arthur@camelot.fr", chaineBlanche, false, false, royaume));
        }
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
            Royaume camelot = new Royaume(2, "Camelot");
            utilisateur.setRoyaume(camelot);

            assertSame(camelot, utilisateur.getRoyaume());

            utilisateur.setRoyaume(null);
            assertNull(utilisateur.getRoyaume());
        }
    }

    // =========================================================================
    // 4. Validation des mutateurs et invariants
    // =========================================================================

    @Nested
    @DisplayName("Validation des mutateurs et des invariants")
    class InvariantsSettersTests {

        @ParameterizedTest
        @ValueSource(strings = { "", "   ", "\t" })
        @DisplayName("setPseudo rejette les chaînes blanches via IllegalArgumentException")
        void testSetPseudoRejetteBlanc(String pseudoInvalide) {
            assertThrows(IllegalArgumentException.class, () -> utilisateur.setPseudo(pseudoInvalide));
            assertEquals("Arthur", utilisateur.getPseudo());
        }

        @Test
        @DisplayName("setPseudo rejette null via IllegalArgumentException")
        void testSetPseudoRejetteNull() {
            assertThrows(IllegalArgumentException.class, () -> utilisateur.setPseudo(null));
            assertEquals("Arthur", utilisateur.getPseudo());
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   ", "\t" })
        @DisplayName("setEmail rejette les chaînes blanches via IllegalArgumentException")
        void testSetEmailRejetteBlanc(String emailInvalide) {
            assertThrows(IllegalArgumentException.class, () -> utilisateur.setEmail(emailInvalide));
            assertEquals("arthur@camelot.fr", utilisateur.getEmail());
        }

        @Test
        @DisplayName("setEmail rejette null via IllegalArgumentException")
        void testSetEmailRejetteNull() {
            assertThrows(IllegalArgumentException.class, () -> utilisateur.setEmail(null));
            assertEquals("arthur@camelot.fr", utilisateur.getEmail());
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   ", "\t" })
        @DisplayName("setPassword rejette les chaînes blanches via IllegalArgumentException")
        void testSetPasswordRejetteBlanc(String mdpInvalide) {
            assertThrows(IllegalArgumentException.class, () -> utilisateur.setPassword(mdpInvalide));
            assertEquals("excalibur123", utilisateur.getPassword());
        }

        @Test
        @DisplayName("setPassword rejette null via IllegalArgumentException")
        void testSetPasswordRejetteNull() {
            assertThrows(IllegalArgumentException.class, () -> utilisateur.setPassword(null));
            assertEquals("excalibur123", utilisateur.getPassword());
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -50 })
        @DisplayName("setId rejette les identifiants <= 0 via IllegalArgumentException")
        void testSetIdRejetteNonPositif(int idInvalide) {
            assertThrows(IllegalArgumentException.class, () -> utilisateur.setId(idInvalide));
            assertEquals(1, utilisateur.getId());
        }
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
