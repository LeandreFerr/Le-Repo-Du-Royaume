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
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests unitaires rigoureux - Classe Partie")
public class PartieTest {

    private Partie partie;
    private Royaume royaume1;
    private Royaume royaume2;
    private Royaume royaume3;
    private Utilisateur joueur1;
    private Utilisateur joueur2;
    private Utilisateur joueurEtranger;

    @BeforeEach
    void setUp() {
        partie = new Partie();
        partie.setId(1);
        partie.setNom("Bataille des Cinq Armées");

        royaume1 = new Royaume(1, "Gondor");
        royaume2 = new Royaume(2, "Rohan");
        royaume3 = new Royaume(3, "Mordor");

        joueur1 = new Utilisateur(1, "Aragorn", "aragorn@gondor.fr", "mdp1", true, false, royaume1);
        joueur2 = new Utilisateur(2, "Theoden", "theoden@rohan.fr", "mdp2", true, false, royaume2);

        Royaume royaumeEtranger = new Royaume(99, "Harad");
        joueurEtranger = new Utilisateur(99, "Sauron", "sauron@mordor.fr", "anneau", true, false, royaumeEtranger);
    }

    // =========================================================================
    // 1. État initial & Constructeurs
    // =========================================================================

    @Nested
    @DisplayName("État initial et constructeurs")
    class EtatInitialEtConstructeursTests {

        @Test
        @DisplayName("Le constructeur par défaut initialise une partie saine EN_ATTENTE")
        void testEtatInitialParDefaut() {
            // Given / When
            Partie nouvelle = new Partie();

            // Then
            assertAll(
                () -> assertEquals(StatutPartie.EN_ATTENTE, nouvelle.getStatut()),
                () -> assertEquals(1.0, nouvelle.getVitesseJeu()),
                () -> assertEquals(10, nouvelle.getMaxJoueurs()),
                () -> assertNull(nouvelle.getVainqueur()),
                () -> assertNull(nouvelle.getTypeVictoire()),
                () -> assertNotNull(nouvelle.getRoyaumes()),
                () -> assertTrue(nouvelle.getRoyaumes().isEmpty()),
                () -> assertNotNull(nouvelle.getMessages()),
                () -> assertTrue(nouvelle.getMessages().isEmpty())
            );
        }

        @Test
        @DisplayName("Le constructeur complet initialise fidèlement tous les champs")
        void testConstructeurCompletNominal() {
            LocalDateTime date = LocalDateTime.of(2026, 11, 5, 10, 0);
            List<Royaume> listeR = new ArrayList<>(List.of(royaume1, royaume2));
            List<Message> listeM = new ArrayList<>();

            Partie p = new Partie(10, "Guerre de l'Anneau", date, 2.0, 4, joueur1,
                    TypeVictoire.CHATEAU_NIVEAU_10, listeM, listeR, StatutPartie.TERMINEE);

            assertAll(
                () -> assertEquals(10, p.getId()),
                () -> assertEquals("Guerre de l'Anneau", p.getNom()),
                () -> assertEquals(date, p.getDateCreation()),
                () -> assertEquals(2.0, p.getVitesseJeu()),
                () -> assertEquals(4, p.getMaxJoueurs()),
                () -> assertSame(joueur1, p.getVainqueur()),
                () -> assertEquals(TypeVictoire.CHATEAU_NIVEAU_10, p.getTypeVictoire()),
                () -> assertEquals(2, p.getRoyaumes().size()),
                () -> assertEquals(StatutPartie.TERMINEE, p.getStatut())
            );
        }

        @Test
        @DisplayName("Le constructeur complet protège contre les listes ou statuts null")
        void testConstructeurCompletNullSafety() {
            Partie p = new Partie(1, "Salon Robuste", null, 1.0, 8, null, null, null, null, null);

            assertAll(
                () -> assertNotNull(p.getMessages(), "La liste des messages ne doit pas être null"),
                () -> assertTrue(p.getMessages().isEmpty()),
                () -> assertNotNull(p.getRoyaumes(), "La liste des royaumes ne doit pas être null"),
                () -> assertTrue(p.getRoyaumes().isEmpty()),
                () -> assertEquals(StatutPartie.EN_ATTENTE, p.getStatut(), "Le statut par défaut doit être EN_ATTENTE")
            );
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 1, -1, -10 })
        @DisplayName("Le constructeur complet rejette un maxJoueurs < 2 via IllegalArgumentException")
        void testConstructeurCompletRejetteMaxJoueursInvalide(int maxInvalide) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Partie(1, "Invalide", null, 1.0, maxInvalide, null, null, null, null, null));
        }

        @ParameterizedTest
        @ValueSource(doubles = { 0.0, -0.5, -2.0, Double.NaN, Double.POSITIVE_INFINITY })
        @DisplayName("Le constructeur complet rejette une vitesse <= 0 ou non finie via IllegalArgumentException")
        void testConstructeurCompletRejetteVitesseInvalide(double vitesseInvalide) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Partie(1, "Invalide", null, vitesseInvalide, 4, null, null, null, null, null));
        }
    }

    // =========================================================================
    // 2. Inscriptions & Désinscriptions de royaumes
    // =========================================================================

    @Nested
    @DisplayName("Inscriptions et désinscriptions de royaumes")
    class InscriptionsEtDesinscriptionsTests {

        @Test
        @DisplayName("inscrireRoyaume ajoute les royaumes avec succès et met à jour le décompte")
        void testInscrireRoyaumeNominal() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);

            assertAll(
                () -> assertEquals(2, partie.getRoyaumes().size()),
                () -> assertEquals(2, partie.getNJoueurs()),
                () -> assertTrue(partie.getRoyaumes().contains(royaume1)),
                () -> assertTrue(partie.getRoyaumes().contains(royaume2))
            );
        }

        @Test
        @DisplayName("inscrireRoyaume lève IllegalArgumentException si le royaume est null")
        void testInscrireRoyaumeRejetteNull() {
            assertThrows(IllegalArgumentException.class, () -> partie.inscrireRoyaume(null));
            assertTrue(partie.getRoyaumes().isEmpty());
        }

        @Test
        @DisplayName("inscrireRoyaume lève IllegalArgumentException si le royaume est déjà en ruine")
        void testInscrireRoyaumeRejetteRoyaumeEnRuine() {
            Royaume ruine = new Royaume(4, "Ruines d'Osgiliath");
            ruine.setEnRuine(true);

            assertThrows(IllegalArgumentException.class, () -> partie.inscrireRoyaume(ruine),
                    "Un royaume déjà en ruine ne peut pas s'inscrire à une nouvelle partie");
            assertTrue(partie.getRoyaumes().isEmpty());
        }

        @Test
        @DisplayName("inscrireRoyaume lève IllegalArgumentException si le royaume est déjà inscrit (doublon)")
        void testInscrireRoyaumeRejetteDoublon() {
            partie.inscrireRoyaume(royaume1);

            assertThrows(IllegalArgumentException.class, () -> partie.inscrireRoyaume(royaume1),
                    "Un même royaume ne peut pas s'inscrire deux fois dans la partie");
            assertEquals(1, partie.getRoyaumes().size());
        }

        @Test
        @DisplayName("inscrireRoyaume lève IllegalStateException si la capacité maxJoueurs est atteinte")
        void testInscrireRoyaumeRejetteCapaciteAtteinte() {
            partie.setMaxJoueurs(2);
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);

            assertThrows(IllegalStateException.class, () -> partie.inscrireRoyaume(royaume3),
                    "Une inscription sur un salon plein doit lever IllegalStateException");
            assertEquals(2, partie.getRoyaumes().size());
        }

        @Test
        @DisplayName("inscrireRoyaume lève IllegalStateException si la partie est déjà EN_COURS ou TERMINEE")
        void testInscrireRoyaumeRejetteSiPasEnAttente() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();

            // Tentative en cours
            assertThrows(IllegalStateException.class, () -> partie.inscrireRoyaume(royaume3),
                    "Rejoindre une partie EN_COURS doit lever IllegalStateException");

            // Tentative terminée
            partie.terminer(joueur1, TypeVictoire.CHATEAU_NIVEAU_10);
            assertThrows(IllegalStateException.class, () -> partie.inscrireRoyaume(royaume3),
                    "Rejoindre une partie TERMINEE doit lever IllegalStateException");
        }

        @Test
        @DisplayName("desinscrireRoyaume retire le royaume lorsque la partie est EN_ATTENTE")
        void testDesinscrireRoyaumeNominal() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);

            partie.desinscrireRoyaume(royaume1);

            assertAll(
                () -> assertEquals(1, partie.getRoyaumes().size()),
                () -> assertFalse(partie.getRoyaumes().contains(royaume1)),
                () -> assertTrue(partie.getRoyaumes().contains(royaume2))
            );
        }

        @Test
        @DisplayName("desinscrireRoyaume lève IllegalStateException si la partie est déjà EN_COURS")
        void testDesinscrireRoyaumeRejetteSiEnCours() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();

            assertThrows(IllegalStateException.class, () -> partie.desinscrireRoyaume(royaume1),
                    "Impossible de se désinscrire d'une partie déjà en cours");
            assertEquals(2, partie.getRoyaumes().size());
        }

        @Test
        @DisplayName("desinscrireRoyaume lève IllegalArgumentException si le royaume est null ou non inscrit")
        void testDesinscrireRoyaumeRejetteInvalide() {
            partie.inscrireRoyaume(royaume1);

            assertThrows(IllegalArgumentException.class, () -> partie.desinscrireRoyaume(null));
            assertThrows(IllegalArgumentException.class, () -> partie.desinscrireRoyaume(royaume2));
            assertEquals(1, partie.getRoyaumes().size());
        }
    }

    // =========================================================================
    // 3. Démarrage de la partie (demarrer)
    // =========================================================================

    @Nested
    @DisplayName("Démarrage de la partie")
    class DemarragePartieTests {

        @Test
        @DisplayName("demarrer bascule le statut à EN_COURS dès qu'au moins 2 royaumes sont inscrits")
        void testDemarrerNominal() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);

            partie.demarrer();

            assertEquals(StatutPartie.EN_COURS, partie.getStatut());
        }

        @Test
        @DisplayName("demarrer lève IllegalStateException s'il y a moins de 2 royaumes inscrits")
        void testDemarrerRejetteSiMoinsDeDeuxRoyaumes() {
            // 0 royaume
            assertThrows(IllegalStateException.class, () -> partie.demarrer(),
                    "Démarrer sans royaume doit lever IllegalStateException");

            // 1 royaume
            partie.inscrireRoyaume(royaume1);
            assertThrows(IllegalStateException.class, () -> partie.demarrer(),
                    "Démarrer avec 1 seul royaume doit lever IllegalStateException");

            assertEquals(StatutPartie.EN_ATTENTE, partie.getStatut());
        }

        @Test
        @DisplayName("demarrer lève IllegalStateException si la partie est déjà EN_COURS ou TERMINEE")
        void testDemarrerRejetteSiStatutInapproprié() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();

            // Déjà EN_COURS
            assertThrows(IllegalStateException.class, () -> partie.demarrer());

            // TERMINEE
            partie.terminer(joueur1, TypeVictoire.CHATEAU_NIVEAU_10);
            assertThrows(IllegalStateException.class, () -> partie.demarrer());
            assertEquals(StatutPartie.TERMINEE, partie.getStatut());
        }
    }

    // =========================================================================
    // 4. Forfaits & Joueurs actifs (declarerForfait, getNJoueurs)
    // =========================================================================

    @Nested
    @DisplayName("Gestion des forfaits et comptage des joueurs actifs")
    class ForfaitsEtJoueursActifsTests {

        @Test
        @DisplayName("declarerForfait passe le royaume en ruine et décrémente getNJoueurs")
        void testDeclarerForfaitNominal() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();
            assertEquals(2, partie.getNJoueurs());

            partie.declarerForfait(royaume2);

            assertAll(
                () -> assertTrue(royaume2.isEnRuine(), "Le royaume ayant déclaré forfait doit passer en ruine"),
                () -> assertFalse(royaume1.isEnRuine()),
                () -> assertEquals(2, partie.getRoyaumes().size(), "Le royaume reste présent dans la partie"),
                () -> assertEquals(1, partie.getNJoueurs(), "Seul 1 joueur actif restant")
            );
        }

        @Test
        @DisplayName("declarerForfait lève IllegalStateException si la partie n'est pas EN_COURS")
        void testDeclarerForfaitRejetteSiPasEnCours() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);

            assertThrows(IllegalStateException.class, () -> partie.declarerForfait(royaume1),
                    "Pas de forfait possible tant que la partie est EN_ATTENTE");
            assertFalse(royaume1.isEnRuine());
        }

        @Test
        @DisplayName("declarerForfait lève IllegalArgumentException si le royaume est null ou externe")
        void testDeclarerForfaitRejetteInvalide() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();

            assertThrows(IllegalArgumentException.class, () -> partie.declarerForfait(null));
            assertThrows(IllegalArgumentException.class, () -> partie.declarerForfait(royaume3));
            assertEquals(2, partie.getNJoueurs());
        }

        @Test
        @DisplayName("declarerForfait lève IllegalStateException si le royaume est déjà en ruine")
        void testDeclarerForfaitRejetteRoyaumeDejaEnRuine() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();
            partie.declarerForfait(royaume2);

            assertThrows(IllegalStateException.class, () -> partie.declarerForfait(royaume2),
                    "Un royaume déjà en ruine ne peut pas re-déclarer forfait");
        }
    }

    // =========================================================================
    // 5. Conditions de victoire & Fin de partie (terminer)
    // =========================================================================

    @Nested
    @DisplayName("Fin de partie et attribution de la victoire")
    class FinDePartieEtVictoireTests {

        @Test
        @DisplayName("terminer affecte le vainqueur, le type de victoire et passe le statut à TERMINEE")
        void testTerminerNominal() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();

            partie.terminer(joueur1, TypeVictoire.CHATEAU_NIVEAU_10);

            assertAll(
                () -> assertEquals(StatutPartie.TERMINEE, partie.getStatut()),
                () -> assertSame(joueur1, partie.getVainqueur()),
                () -> assertEquals(TypeVictoire.CHATEAU_NIVEAU_10, partie.getTypeVictoire())
            );
        }

        @Test
        @DisplayName("terminer fonctionne après forfait de l'adversaire (ABANDON_ADVERSAIRES)")
        void testTerminerApresForfaitAdversaire() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();
            partie.declarerForfait(royaume2);

            partie.terminer(joueur1, TypeVictoire.ABANDON_ADVERSAIRES);

            assertAll(
                () -> assertEquals(StatutPartie.TERMINEE, partie.getStatut()),
                () -> assertSame(joueur1, partie.getVainqueur()),
                () -> assertEquals(TypeVictoire.ABANDON_ADVERSAIRES, partie.getTypeVictoire())
            );
        }

        @Test
        @DisplayName("terminer lève IllegalStateException si la partie est EN_ATTENTE ou déjà TERMINEE")
        void testTerminerRejetteMauvaisStatut() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);

            // EN_ATTENTE
            assertThrows(IllegalStateException.class,
                    () -> partie.terminer(joueur1, TypeVictoire.DOMINATION_MILITAIRE));

            partie.demarrer();
            partie.terminer(joueur1, TypeVictoire.CHATEAU_NIVEAU_10);

            // Déjà TERMINEE
            assertThrows(IllegalStateException.class,
                    () -> partie.terminer(joueur2, TypeVictoire.DOMINATION_MILITAIRE));
            assertSame(joueur1, partie.getVainqueur(), "Le premier vainqueur doit être préservé");
        }

        @Test
        @DisplayName("terminer lève IllegalArgumentException si vainqueur ou typeVictoire est null")
        void testTerminerRejetteArgumentsNull() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();

            assertThrows(IllegalArgumentException.class, () -> partie.terminer(null, TypeVictoire.CHATEAU_NIVEAU_10));
            assertThrows(IllegalArgumentException.class, () -> partie.terminer(joueur1, null));
            assertEquals(StatutPartie.EN_COURS, partie.getStatut());
        }

        @Test
        @DisplayName("terminer lève IllegalArgumentException si le joueur est étranger à la partie")
        void testTerminerRejetteVainqueurEtranger() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();

            assertThrows(IllegalArgumentException.class,
                    () -> partie.terminer(joueurEtranger, TypeVictoire.DOMINATION_MILITAIRE),
                    "Un joueur étranger à la partie ne peut pas être déclaré vainqueur");
            assertEquals(StatutPartie.EN_COURS, partie.getStatut());
            assertNull(partie.getVainqueur());
        }

        @Test
        @DisplayName("terminer lève IllegalArgumentException si le joueur n'a aucun royaume rattaché")
        void testTerminerRejetteJoueurSansRoyaume() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();

            Utilisateur sansRoyaume = new Utilisateur(15, "SansTerre", "sans@terre.fr", "mdp", true, false, null);

            assertThrows(IllegalArgumentException.class,
                    () -> partie.terminer(sansRoyaume, TypeVictoire.CHATEAU_NIVEAU_10),
                    "Un joueur sans royaume ne peut pas remporter la partie");
            assertEquals(StatutPartie.EN_COURS, partie.getStatut());
        }

        @Test
        @DisplayName("terminer lève IllegalStateException si le royaume du vainqueur est en ruine")
        void testTerminerRejetteVainqueurEnRuine() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();
            partie.declarerForfait(royaume1);

            assertThrows(IllegalStateException.class,
                    () -> partie.terminer(joueur1, TypeVictoire.CHATEAU_NIVEAU_10),
                    "Un royaume en ruine ne peut pas être déclaré vainqueur");
            assertEquals(StatutPartie.EN_COURS, partie.getStatut());
        }

        @Test
        @DisplayName("terminer lève IllegalStateException si tous les royaumes sont en ruine (getNJoueurs == 0)")
        void testTerminerRejetteSiAucunSurvivant() {
            partie.inscrireRoyaume(royaume1);
            partie.inscrireRoyaume(royaume2);
            partie.demarrer();
            partie.declarerForfait(royaume1);
            partie.declarerForfait(royaume2);
            assertEquals(0, partie.getNJoueurs());

            assertThrows(IllegalStateException.class,
                    () -> partie.terminer(joueur1, TypeVictoire.CHATEAU_NIVEAU_10),
                    "Une partie où tous les royaumes sont ruinés n'a aucun vainqueur valide");
        }
    }

    // =========================================================================
    // 6. Messagerie intégrée (ajouterMessage)
    // =========================================================================

    @Nested
    @DisplayName("Messagerie de partie")
    class MessagerieTests {

        @Test
        @DisplayName("ajouterMessage enregistre le message et lie la relation vers la partie")
        void testAjouterMessageNominal() {
            Message msg = new Message();
            msg.setId(1);
            msg.setContenu("Bonne chance à tous !");
            msg.setExpediteur(joueur1);

            partie.ajouterMessage(msg);

            assertAll(
                () -> assertEquals(1, partie.getMessages().size()),
                () -> assertSame(partie, msg.getPartie(), "La relation retour vers Partie doit être auto-renseignée")
            );
        }

        @Test
        @DisplayName("ajouterMessage lève IllegalArgumentException si le message est null")
        void testAjouterMessageRejetteNull() {
            assertThrows(IllegalArgumentException.class, () -> partie.ajouterMessage(null));
            assertTrue(partie.getMessages().isEmpty());
        }

        @Test
        @DisplayName("ajouterMessage lève IllegalArgumentException si le message est déjà présent")
        void testAjouterMessageRejetteDoublon() {
            Message msg = new Message();
            msg.setId(1);
            msg.setContenu("Message unique");
            partie.ajouterMessage(msg);

            assertThrows(IllegalArgumentException.class, () -> partie.ajouterMessage(msg));
            assertEquals(1, partie.getMessages().size());
        }
    }

    // =========================================================================
    // 7. Encapsulation défensive des collections
    // =========================================================================

    @Nested
    @DisplayName("Encapsulation défensive des listes")
    class EncapsulationListesTests {

        @Test
        @DisplayName("getRoyaumes retourne une liste protégée contre l'altération directe")
        void testGetRoyaumesEstProtegeeContreMutation() {
            partie.inscrireRoyaume(royaume1);

            List<Royaume> vue = partie.getRoyaumes();

            assertThrows(UnsupportedOperationException.class, () -> vue.add(royaume2),
                    "Ajouter directement à la liste exposée doit lever UnsupportedOperationException");
            assertThrows(UnsupportedOperationException.class, () -> vue.clear(),
                    "Vider directement la liste exposée doit lever UnsupportedOperationException");
            assertEquals(1, partie.getRoyaumes().size());
        }

        @Test
        @DisplayName("getMessages retourne une liste protégée contre l'altération directe")
        void testGetMessagesEstProtegeeContreMutation() {
            Message msg = new Message();
            msg.setId(1);
            msg.setContenu("Test");
            partie.ajouterMessage(msg);

            List<Message> vue = partie.getMessages();

            assertThrows(UnsupportedOperationException.class, () -> vue.add(new Message()),
                    "Ajouter directement aux messages doit lever UnsupportedOperationException");
            assertThrows(UnsupportedOperationException.class, () -> vue.clear());
            assertEquals(1, partie.getMessages().size());
        }
    }

    // =========================================================================
    // 8. Validation des Setters et Configuration
    // =========================================================================

    @Nested
    @DisplayName("Validation de la configuration et des setters")
    class ConfigurationEtSettersTests {

        @ParameterizedTest
        @ValueSource(ints = { 0, 1, -1, -50 })
        @DisplayName("setMaxJoueurs rejette toute valeur < 2 via IllegalArgumentException sans altérer l'état")
        void testSetMaxJoueursValidation(int maxInvalide) {
            partie.setMaxJoueurs(8);

            assertThrows(IllegalArgumentException.class, () -> partie.setMaxJoueurs(maxInvalide));
            assertEquals(8, partie.getMaxJoueurs());
        }

        @ParameterizedTest
        @ValueSource(doubles = { 0.0, -1.0, -5.5, Double.NaN, Double.POSITIVE_INFINITY })
        @DisplayName("setVitesseJeu rejette toute valeur <= 0 ou non finie via IllegalArgumentException")
        void testSetVitesseJeuValidation(double vitesseInvalide) {
            partie.setVitesseJeu(1.5);

            assertThrows(IllegalArgumentException.class, () -> partie.setVitesseJeu(vitesseInvalide));
            assertEquals(1.5, partie.getVitesseJeu());
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   ", "\t\n" })
        @DisplayName("setNom rejette une chaîne vide ou blanche via IllegalArgumentException")
        void testSetNomValidationChaineBlanche(String nomInvalide) {
            partie.setNom("Valide");

            assertThrows(IllegalArgumentException.class, () -> partie.setNom(nomInvalide));
            assertEquals("Valide", partie.getNom());
        }

        @Test
        @DisplayName("setNom rejette null via IllegalArgumentException")
        void testSetNomRejetteNull() {
            partie.setNom("Valide");

            assertThrows(IllegalArgumentException.class, () -> partie.setNom(null));
            assertEquals("Valide", partie.getNom());
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -99 })
        @DisplayName("setId rejette les identifiants <= 0 via IllegalArgumentException")
        void testSetIdValidation(int idInvalide) {
            partie.setId(5);

            assertThrows(IllegalArgumentException.class, () -> partie.setId(idInvalide));
            assertEquals(5, partie.getId());
        }

        @Test
        @DisplayName("setStatut(null) replace par le statut par défaut EN_ATTENTE")
        void testSetStatutNullSafe() {
            partie.setStatut(null);
            assertEquals(StatutPartie.EN_ATTENTE, partie.getStatut());
        }

        @Test
        @DisplayName("setRoyaumes(null) et setMessages(null) réinitialisent vers des listes saines vides")
        void testSetCollectionsNullSafe() {
            partie.setRoyaumes(null);
            partie.setMessages(null);

            assertNotNull(partie.getRoyaumes());
            assertTrue(partie.getRoyaumes().isEmpty());
            assertNotNull(partie.getMessages());
            assertTrue(partie.getMessages().isEmpty());
        }
    }
}
