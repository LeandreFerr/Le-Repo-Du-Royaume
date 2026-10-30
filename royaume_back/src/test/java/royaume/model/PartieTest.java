package royaume.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PartieTest {

    private Partie partie;
    private Royaume royaume1;
    private Royaume royaume2;
    private Royaume royaume3;
    private Utilisateur joueur1;
    private Utilisateur joueur2;

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
    }

    // -------------------- État initial --------------------

    @Test
    @DisplayName("Une nouvelle partie est EN_ATTENTE avec vitesse 1.0, maxJoueurs 10 et listes vides")
    void testEtatInitialParDefaut() {
        // Given / When
        Partie nouvellePartie = new Partie();

        // Then
        assertEquals(StatutPartie.EN_ATTENTE, nouvellePartie.getStatut());
        assertEquals(1.0, nouvellePartie.getVitesseJeu());
        assertEquals(10, nouvellePartie.getMaxJoueurs());
        assertNull(nouvellePartie.getVainqueur());
        assertNull(nouvellePartie.getTypeVictoire());
        assertNotNull(nouvellePartie.getRoyaumes());
        assertTrue(nouvellePartie.getRoyaumes().isEmpty());
        assertNotNull(nouvellePartie.getMessages());
        assertTrue(nouvellePartie.getMessages().isEmpty());
    }

    // -------------------- Inscription de royaumes --------------------

    @Test
    @DisplayName("inscrireRoyaume ajoute un royaume si la partie est EN_ATTENTE et non complète")
    void testInscrireRoyaumeNominal() {
        // Given / When
        boolean ajout1 = partie.inscrireRoyaume(royaume1);
        boolean ajout2 = partie.inscrireRoyaume(royaume2);

        // Then
        assertTrue(ajout1);
        assertTrue(ajout2);
        assertEquals(2, partie.getRoyaumes().size());
        assertEquals(2, partie.getNJoueurs());
        assertTrue(partie.getRoyaumes().contains(royaume1));
        assertTrue(partie.getRoyaumes().contains(royaume2));
    }

    @Test
    @DisplayName("inscrireRoyaume refuse un doublon déjà présent dans la partie")
    void testInscrireRoyaumeRefuseDoublon() {
        // Given
        partie.inscrireRoyaume(royaume1);

        // When
        boolean doublon = partie.inscrireRoyaume(royaume1);

        // Then
        assertFalse(doublon, "Un même royaume ne doit pas pouvoir être inscrit deux fois");
        assertEquals(1, partie.getRoyaumes().size());
    }

    @Test
    @DisplayName("inscrireRoyaume refuse un ajout lorsque maxJoueurs est atteint")
    void testInscrireRoyaumeRefuseSiCapaciteMaxAtteinte() {
        // Given
        partie.setMaxJoueurs(2);
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);

        // When
        boolean ajoutSurnombre = partie.inscrireRoyaume(royaume3);

        // Then
        assertFalse(ajoutSurnombre, "L'inscription doit être refusée quand le salon est complet");
        assertEquals(2, partie.getRoyaumes().size());
        assertFalse(partie.getRoyaumes().contains(royaume3));
    }

    @Test
    @DisplayName("inscrireRoyaume refuse un ajout quand la partie est EN_COURS ou TERMINEE")
    void testInscrireRoyaumeRefuseSiPasEnAttente() {
        // Given : partie démarrée (EN_COURS)
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);
        partie.demarrer();
        assertEquals(StatutPartie.EN_COURS, partie.getStatut());

        // When / Then (EN_COURS)
        assertFalse(partie.inscrireRoyaume(royaume3), "Impossible de rejoindre une partie EN_COURS");
        assertEquals(2, partie.getRoyaumes().size());

        // Given : partie terminée (TERMINEE)
        partie.terminer(joueur1, TypeVictoire.CHATEAU_NIVEAU_10);
        assertEquals(StatutPartie.TERMINEE, partie.getStatut());

        // When / Then (TERMINEE)
        assertFalse(partie.inscrireRoyaume(royaume3), "Impossible de rejoindre une partie TERMINEE");
        assertEquals(2, partie.getRoyaumes().size());
    }

    @Test
    @DisplayName("inscrireRoyaume refuse une référence null sans corrompre la liste")
    void testInscrireRoyaumeRefuseNull() {
        // When
        boolean resultat = partie.inscrireRoyaume(null);

        // Then
        assertFalse(resultat, "Un royaume null ne doit pas pouvoir être inscrit");
        assertTrue(partie.getRoyaumes().isEmpty());
    }

    // -------------------- Désinscription & Démarrage --------------------

    @Test
    @DisplayName("desinscrireRoyaume retire le royaume lorsque la partie est EN_ATTENTE")
    void testDesinscrireRoyaumeEnAttente() {
        // Given
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);

        // When
        partie.desinscrireRoyaume(royaume1);

        // Then
        assertEquals(1, partie.getRoyaumes().size());
        assertFalse(partie.getRoyaumes().contains(royaume1));
        assertTrue(partie.getRoyaumes().contains(royaume2));
    }

    @Test
    @DisplayName("desinscrireRoyaume ne supprime pas le royaume si la partie est déjà EN_COURS")
    void testDesinscrireRoyaumeIgnoreSiEnCours() {
        // Given
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);
        partie.demarrer();

        // When
        partie.desinscrireRoyaume(royaume1);

        // Then
        assertEquals(2, partie.getRoyaumes().size(), "Un royaume en jeu ne doit pas disparaître de la carte");
    }

    @Test
    @DisplayName("demarrer bascule le statut à EN_COURS dès qu'au moins 2 royaumes sont inscrits")
    void testDemarrerNominal() {
        // Given
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);

        // When
        partie.demarrer();

        // Then
        assertEquals(StatutPartie.EN_COURS, partie.getStatut());
    }

    @Test
    @DisplayName("demarrer maintient la partie EN_ATTENTE s'il y a moins de 2 royaumes")
    void testDemarrerRefuseSiMoinsDeDeuxRoyaumes() {
        // Given / When : 0 royaume
        partie.demarrer();
        assertEquals(StatutPartie.EN_ATTENTE, partie.getStatut());

        // Given / When : 1 seul royaume
        partie.inscrireRoyaume(royaume1);
        partie.demarrer();

        // Then
        assertEquals(StatutPartie.EN_ATTENTE, partie.getStatut(), "Il faut au moins 2 royaumes pour démarrer");
    }

    @Test
    @DisplayName("demarrer ne modifie pas une partie déjà TERMINEE")
    void testDemarrerIgnoreSiTerminee() {
        // Given
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);
        partie.demarrer();
        partie.terminer(joueur1, TypeVictoire.CHATEAU_NIVEAU_10);

        // When
        partie.demarrer();

        // Then
        assertEquals(StatutPartie.TERMINEE, partie.getStatut());
    }

    // -------------------- Forfaits & Joueurs actifs --------------------

    @Test
    @DisplayName("declarerForfait passe le royaume en ruine et décrémente getNJoueurs si EN_COURS")
    void testDeclarerForfaitEnCours() {
        // Given
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);
        partie.demarrer();
        assertEquals(2, partie.getNJoueurs());

        // When
        partie.declarerForfait(royaume2);

        // Then
        assertTrue(royaume2.isEnRuine(), "Le royaume ayant déclaré forfait doit passer en ruine");
        assertFalse(royaume1.isEnRuine());
        assertEquals(2, partie.getRoyaumes().size(), "Le royaume en ruine reste présent dans la partie");
        assertEquals(1, partie.getNJoueurs(), "Seul 1 royaume actif doit être comptabilisé");
    }

    @Test
    @DisplayName("declarerForfait est ignoré si la partie n'est pas EN_COURS ou si le royaume est externe/null")
    void testDeclarerForfaitCasLimites() {
        // Given : partie encore EN_ATTENTE
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);

        // When : tentative de forfait avant démarrage
        partie.declarerForfait(royaume1);

        // Then
        assertFalse(royaume1.isEnRuine(), "Pas de forfait possible tant que la partie est EN_ATTENTE");

        // Given : partie démarrée, mais royaume3 non inscrit
        partie.demarrer();

        // When : forfait sur null et sur un royaume étranger
        partie.declarerForfait(null);
        partie.declarerForfait(royaume3);

        // Then
        assertFalse(royaume3.isEnRuine(), "Une partie ne doit pas ruiner un royaume qui ne lui appartient pas");
        assertEquals(2, partie.getNJoueurs());
    }

    // -------------------- Fin de partie & Conditions de victoire --------------------

    @Test
    @DisplayName("terminer affecte le vainqueur, le type de victoire et passe le statut à TERMINEE")
    void testTerminerNominal() {
        // Given
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);
        partie.demarrer();

        // When
        partie.terminer(joueur1, TypeVictoire.CHATEAU_NIVEAU_10);

        // Then
        assertEquals(StatutPartie.TERMINEE, partie.getStatut());
        assertSame(joueur1, partie.getVainqueur());
        assertEquals(TypeVictoire.CHATEAU_NIVEAU_10, partie.getTypeVictoire());
    }

    @Test
    @DisplayName("terminer fonctionne lorsqu'il ne reste qu'un seul royaume actif après forfait adverse")
    void testTerminerApresForfaitAdversaire() {
        // Given
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);
        partie.demarrer();
        partie.declarerForfait(royaume2);
        assertEquals(1, partie.getNJoueurs());

        // When
        partie.terminer(joueur1, TypeVictoire.ABANDON_ADVERSAIRES);

        // Then
        assertEquals(StatutPartie.TERMINEE, partie.getStatut());
        assertSame(joueur1, partie.getVainqueur());
        assertEquals(TypeVictoire.ABANDON_ADVERSAIRES, partie.getTypeVictoire());
    }

    @Test
    @DisplayName("terminer est refusé si la partie est EN_ATTENTE ou déjà TERMINEE")
    void testTerminerRefuseMauvaisStatut() {
        // Given : EN_ATTENTE
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);

        // When
        partie.terminer(joueur1, TypeVictoire.DOMINATION_MILITAIRE);

        // Then
        assertEquals(StatutPartie.EN_ATTENTE, partie.getStatut());
        assertNull(partie.getVainqueur());
        assertNull(partie.getTypeVictoire());

        // Given : partie terminée une première fois avec joueur1
        partie.demarrer();
        partie.terminer(joueur1, TypeVictoire.CHATEAU_NIVEAU_10);

        // When : tentative d'écraser le vainqueur avec joueur2
        partie.terminer(joueur2, TypeVictoire.DOMINATION_MILITAIRE);

        // Then
        assertSame(joueur1, partie.getVainqueur(), "Le vainqueur d'une partie terminée ne doit plus changer");
        assertEquals(TypeVictoire.CHATEAU_NIVEAU_10, partie.getTypeVictoire());
    }

    @Test
    @DisplayName("terminer refuse un vainqueur null ou un typeVictoire null")
    void testTerminerRefuseArgumentsNull() {
        // Given
        partie.inscrireRoyaume(royaume1);
        partie.inscrireRoyaume(royaume2);
        partie.demarrer();

        // When : vainqueur null
        partie.terminer(null, TypeVictoire.CHATEAU_NIVEAU_10);

        // Then
        assertEquals(StatutPartie.EN_COURS, partie.getStatut());
        assertNull(partie.getVainqueur());

        // When : typeVictoire null
        partie.terminer(joueur1, null);

        // Then
        assertEquals(StatutPartie.EN_COURS, partie.getStatut(), "Une victoire sans TypeVictoire doit être refusée");
        assertNull(partie.getVainqueur());
        assertNull(partie.getTypeVictoire());
    }

    // -------------------- Constructeur complet & Messagerie --------------------

    @Test
    @DisplayName("Le constructeur complet et les setters protègent contre des collections ou un statut null")
    void testConstructeurCompletEtSettersNullSafe() {
        // Given / When
        Partie partieAvecNulls = new Partie(2, "Salon Robuste", null, 1.5, 8, null, null, null, null, null);

        // Then
        assertNotNull(partieAvecNulls.getMessages());
        assertTrue(partieAvecNulls.getMessages().isEmpty());
        assertNotNull(partieAvecNulls.getRoyaumes());
        assertTrue(partieAvecNulls.getRoyaumes().isEmpty());
        assertEquals(StatutPartie.EN_ATTENTE, partieAvecNulls.getStatut());

        // When : passage de null aux setters
        partieAvecNulls.setMessages(null);
        partieAvecNulls.setRoyaumes(null);
        partieAvecNulls.setStatut(null);

        // Then
        assertNotNull(partieAvecNulls.getMessages());
        assertNotNull(partieAvecNulls.getRoyaumes());
        assertEquals(StatutPartie.EN_ATTENTE, partieAvecNulls.getStatut());
    }

    @Test
    @DisplayName("ajouterMessage enregistre le message, lie la partie et rejette null ou un doublon")
    void testAjouterMessage() {
        // Given
        Message msg = new Message();
        msg.setId(1);
        msg.setContenu("Prêts à lancer ?");
        msg.setExpediteur(joueur1);

        // When
        boolean premierAjout = partie.ajouterMessage(msg);
        boolean ajoutDoublon = partie.ajouterMessage(msg);
        boolean ajoutNull = partie.ajouterMessage(null);

        // Then
        assertTrue(premierAjout);
        assertFalse(ajoutDoublon, "Un même message ne doit pas être ajouté deux fois");
        assertFalse(ajoutNull, "Un message null doit être refusé");
        assertEquals(1, partie.getMessages().size());
        assertSame(partie, msg.getPartie(), "La relation retour vers Partie doit être renseignée automatiquement");
    }
}

