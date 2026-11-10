
package royaume.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PropositionEchangeTest {

    private PropositionEchange proposition;
    private Royaume expediteur;
    private Royaume destinataire;

    private QuantiteRessource orExpediteur;
    private QuantiteRessource boisExpediteur;
    private QuantiteRessource pierreDestinataire;
    private QuantiteRessource nourritureDestinataire;
//____________________________________________________________________
    
    @BeforeEach
    void setUp() {

        // Ressources de l'expéditeur
        orExpediteur = new QuantiteRessource(500, Ressource.OR);
        boisExpediteur = new QuantiteRessource(300, Ressource.BOIS);

        List<QuantiteRessource> ressourcesExpediteur = new ArrayList<>();
        ressourcesExpediteur.add(orExpediteur);
        ressourcesExpediteur.add(boisExpediteur);
        ressourcesExpediteur.add(new QuantiteRessource(0, Ressource.PIERRE));
        ressourcesExpediteur.add(new QuantiteRessource(100, Ressource.NOURRITURE));

        // Ressources du destinataire
        pierreDestinataire = new QuantiteRessource(400, Ressource.PIERRE);
        nourritureDestinataire = new QuantiteRessource(800, Ressource.NOURRITURE);

        List<QuantiteRessource> ressourcesDestinataire = new ArrayList<>();
        ressourcesDestinataire.add(new QuantiteRessource(0, Ressource.OR));
        ressourcesDestinataire.add(pierreDestinataire);
        ressourcesDestinataire.add(new QuantiteRessource(0, Ressource.BOIS));
        ressourcesDestinataire.add(nourritureDestinataire);

        // Création des royaumes
        Population peupleExpediteur = new Canards(1, "Canards", "Archiduc", 0.05);
        expediteur = new Royaume(
            1, "Gondor", peupleExpediteur, 1,
            100, 0, 0, 0,
            false, ressourcesExpediteur
        );

        Population peupleDestinataire = new Princesses(2, "Princesses", "Reine", 0.10);
        destinataire = new Royaume(
            2, "Rohan", peupleDestinataire, 1,
            100, 0, 0, 0,
            false, ressourcesDestinataire
        );

        // Proposition d'échange
        proposition = new PropositionEchange(
            1,
            LocalDateTime.now(),
            StatutEchange.EN_ATTENTE,
            expediteur,
            destinataire,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );
    }
  //____________________________________________________________________

    // ==========================================================
    // État initial
    // ==========================================================

    @Test
    @DisplayName("Une nouvelle proposition est EN_ATTENTE par défaut")
    void testEtatInitial() {

        // Given / When
        PropositionEchange nouvelleProposition = new PropositionEchange();

        // Then
        assertEquals(
            StatutEchange.EN_ATTENTE,
            nouvelleProposition.getStatut()
        );

        assertNull(nouvelleProposition.getId());
        assertNull(nouvelleProposition.getDateCreation());
        assertNull(nouvelleProposition.getExpediteur());
        assertNull(nouvelleProposition.getDestinataire());
        assertNull(nouvelleProposition.getOfferte());
        assertNull(nouvelleProposition.getDemandee());
    }

    @Test
    @DisplayName("Le constructeur complet initialise correctement la proposition")
    void testConstructeurComplet() {

        assertEquals(1, proposition.getId());
        assertNotNull(proposition.getDateCreation());
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());

        assertSame(expediteur, proposition.getExpediteur());
        assertSame(destinataire, proposition.getDestinataire());

        assertEquals(Ressource.OR, proposition.getOfferte().getRessource());
        assertEquals(100, proposition.getOfferte().getQuantite());

        assertEquals(Ressource.PIERRE, proposition.getDemandee().getRessource());
        assertEquals(150, proposition.getDemandee().getQuantite());
    }

    // ==========================================================
    // Acceptation d'une proposition
    // ==========================================================

    @Test
    @DisplayName("accepter réalise correctement l'échange bilatéral de ressources")
    void testAccepterEchangeNominal() {

        // Given
        // L'expéditeur possède 500 OR et offre 100 OR.
        // Le destinataire possède 400 PIERRE et offre 150 PIERRE.

        // When
        proposition.accepter();

        // Then
        assertEquals(StatutEchange.ACCEPTE, proposition.getStatut());

        // Vérification du stock de l'expéditeur
        assertEquals(400, getStock(expediteur, Ressource.OR));
        assertEquals(150, expediteur.getRessources().stream()
            .filter(r -> r.getRessource() == Ressource.PIERRE)
            .mapToInt(QuantiteRessource::getQuantite)
            .sum());

        // Vérification du stock du destinataire
        assertEquals(250, getStock(destinataire, Ressource.PIERRE));
        assertEquals(100, destinataire.getRessources().stream()
            .filter(r -> r.getRessource() == Ressource.OR)
            .mapToInt(QuantiteRessource::getQuantite)
            .sum());
    }

    @Test
    @DisplayName("accepter refuse l'échange si l'expéditeur ne possède pas assez de ressources")
    void testAccepterRefuseSiExpediteurInsuffisant() {

        // Given
        expediteur.debiterRessources(Ressource.OR, 450); // Stock passe à 50 < 100 requis

        int orAvant = getStock(expediteur, Ressource.OR);
        int pierreAvant = getStock(destinataire, Ressource.PIERRE);

        // When
        proposition.accepter();

        // Then
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());

        assertEquals(orAvant, getStock(expediteur, Ressource.OR));
        assertEquals(pierreAvant, getStock(destinataire, Ressource.PIERRE));

        assertEquals(0, destinataire.getRessources().stream()
            .filter(r -> r.getRessource() == Ressource.OR)
            .mapToInt(QuantiteRessource::getQuantite)
            .sum());
    }

    @Test
    @DisplayName("accepter refuse l'échange si le destinataire ne possède pas assez de ressources")
    void testAccepterRefuseSiDestinataireInsuffisant() {

        // Given
        destinataire.debiterRessources(Ressource.PIERRE, 300); // Stock passe à 100 < 150 requis

        int orAvant = getStock(expediteur, Ressource.OR);
        int pierreAvant = getStock(destinataire, Ressource.PIERRE);

        // When
        proposition.accepter();

        // Then
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());

        assertEquals(orAvant, getStock(expediteur, Ressource.OR));
        assertEquals(pierreAvant, getStock(destinataire, Ressource.PIERRE));
    }

    @Test
    @DisplayName("accepter refuse l'échange si les deux royaumes manquent de ressources")
    void testAccepterRefuseSiDeuxRoyaumesInsuffisants() {

        // Given
        expediteur.debiterRessources(Ressource.OR, 450);
        destinataire.debiterRessources(Ressource.PIERRE, 300);

        // When
        proposition.accepter();

        // Then
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());

        assertEquals(50, getStock(expediteur, Ressource.OR));
        assertEquals(100, getStock(destinataire, Ressource.PIERRE));
    }

    @Test
    @DisplayName("accepter ne fait rien si l'expéditeur est null")
    void testAccepterExpediteurNull() {

        // Given
        proposition.setExpediteur(null);

        // When
        proposition.accepter();

        // Then
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());
        assertEquals(400, getStock(destinataire, Ressource.PIERRE));
    }

    @Test
    @DisplayName("accepter ne fait rien si le destinataire est null")
    void testAccepterDestinataireNull() {

        // Given
        proposition.setDestinataire(null);

        // When
        proposition.accepter();

        // Then
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());
        assertEquals(500, getStock(expediteur, Ressource.OR));
    }

    @Test
    @DisplayName("accepter ne fait rien si la ressource offerte est null")
    void testAccepterOfferteNull() {

        // Given
        proposition.setOfferte(null);

        // When
        proposition.accepter();

        // Then
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());
        assertEquals(500, getStock(expediteur, Ressource.OR));
    }

    @Test
    @DisplayName("accepter ne fait rien si la ressource demandée est null")
    void testAccepterDemandeeNull() {

        // Given
        proposition.setDemandee(null);

        // When
        proposition.accepter();

        // Then
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());
        assertEquals(500, getStock(expediteur, Ressource.OR));
    }

    @Test
    @DisplayName("accepter ne modifie rien si la proposition est déjà ACCEPTEE")
    void testAccepterPropositionDejaAcceptee() {

        // Given
        proposition.accepter();

        int orApresPremierEchange = getStock(expediteur, Ressource.OR);
        int pierreApresPremierEchange = getStock(destinataire, Ressource.PIERRE);

        // When
        proposition.accepter();

        // Then
        assertEquals(StatutEchange.ACCEPTE, proposition.getStatut());

        assertEquals(orApresPremierEchange, getStock(expediteur, Ressource.OR));
        assertEquals(pierreApresPremierEchange, getStock(destinataire, Ressource.PIERRE));
    }

    // ==========================================================
    // Refus d'une proposition
    // ==========================================================

    @Test
    @DisplayName("refuser passe le statut à REFUSE sans modifier les stocks des royaumes")
    void testRefuser() {

        // Given
        int orAvant = getStock(expediteur, Ressource.OR);
        int pierreAvant = getStock(destinataire, Ressource.PIERRE);

        // When
        proposition.refuser();

        // Then
        assertEquals(StatutEchange.REFUSE, proposition.getStatut());

        assertEquals(orAvant, getStock(expediteur, Ressource.OR));
        assertEquals(pierreAvant, getStock(destinataire, Ressource.PIERRE));
    }

   
    @Test
    @DisplayName("refuser ne modifie pas une proposition déjà ACCEPTEE")
    void testRefuserPropositionAcceptee() {

        // Given
        proposition.accepter();

        int orApresEchange = getStock(expediteur, Ressource.OR);

        // When
        proposition.refuser();

        // Then
        assertEquals(StatutEchange.ACCEPTE, proposition.getStatut());

        assertEquals(
            orApresEchange,
            getStock(expediteur, Ressource.OR),
            "Le stock d'or ne doit pas changer après le refus d'une proposition déjà acceptée"
        );
    }
    @Test
    @DisplayName("refuser ne modifie pas une proposition déjà ANNULEE")
    void testRefuserPropositionAnnulee() {

        // Given
        proposition.annuler();

        // When
        proposition.refuser();

        // Then
        assertEquals(StatutEchange.ANNULE, proposition.getStatut());
        assertEquals(500, getStock(expediteur, Ressource.OR));
        assertEquals(400, getStock(destinataire, Ressource.PIERRE));
    }

    // ==========================================================
    // Annulation d'une proposition
    // ==========================================================

    @Test
    @DisplayName("annuler passe le statut à ANNULE")
    void testAnnuler() {

        // When
        proposition.annuler();

        // Then
        assertEquals(StatutEchange.ANNULE, proposition.getStatut());

        assertEquals(500, getStock(expediteur, Ressource.OR));
        assertEquals(400, getStock(destinataire, Ressource.PIERRE));
    }

    @Test
    @DisplayName("annuler ne modifie pas une proposition déjà ACCEPTEE")
    void testAnnulerPropositionAcceptee() {

        // Given
        proposition.accepter();

        // When
        proposition.annuler();

        // Then
        assertEquals(StatutEchange.ACCEPTE, proposition.getStatut());
        assertEquals(400, getStock(expediteur, Ressource.OR));
        assertEquals(250, getStock(destinataire, Ressource.PIERRE));
    }

    @Test
    @DisplayName("annuler ne modifie pas une proposition déjà REFUSEE")
    void testAnnulerPropositionRefusee() {

        // Given
        proposition.refuser();

        // When
        proposition.annuler();

        // Then
        assertEquals(StatutEchange.REFUSE, proposition.getStatut());
        assertEquals(500, getStock(expediteur, Ressource.OR));
        assertEquals(400, getStock(destinataire, Ressource.PIERRE));
    }

    // ==========================================================
    // Constructeurs et setters
    // ==========================================================

    @Test
    @DisplayName("Les setters modifient correctement les attributs de la proposition")
    void testSetters() {

        // Given
        LocalDateTime nouvelleDate = LocalDateTime.of(2026, 5, 10, 14, 30);

        // When
        proposition.setId(10);
        proposition.setDateCreation(nouvelleDate);
        proposition.setStatut(StatutEchange.REFUSE);

        // Then
        assertEquals(10, proposition.getId());
        assertEquals(nouvelleDate, proposition.getDateCreation());
        assertEquals(StatutEchange.REFUSE, proposition.getStatut());
    }

    // ==========================================================
    // Sécurité métier & Invariants
    // ==========================================================

    @Test
    @DisplayName("accepter refuse l'échange si l'expéditeur et le destinataire sont le même royaume (auto-échange)")
    void testAccepterRefuseAutoEchange() {
        // Given : proposition pointant sur le même royaume
        proposition.setDestinataire(expediteur);

        // When
        proposition.accepter();

        // Then
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());
        assertEquals(500, getStock(expediteur, Ressource.OR));
    }

    @Test
    @DisplayName("accepter refuse l'échange si l'un des royaumes est en ruine")
    void testAccepterRefuseSiRoyaumeEnRuine() {
        // Given : Expéditeur en ruine
        expediteur.setEnRuine(true);
        proposition.accepter();
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());

        // Given : Destinataire en ruine
        expediteur.setEnRuine(false);
        destinataire.setEnRuine(true);
        proposition.accepter();
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());
    }

    @Test
    @DisplayName("accepter refuse l'échange si la quantité offerte ou demandée est non positive")
    void testAccepterRefuseQuantitesInvalides() {
        // Given : Quantité offerte nulle
        proposition.setOfferte(new QuantiteRessource(0, Ressource.OR));
        proposition.accepter();
        assertEquals(StatutEchange.EN_ATTENTE, proposition.getStatut());
    }

    @Test
    @DisplayName("Equals et HashCode respectent le contrat POJO")
    void testEqualsEtHashCode() {
        PropositionEchange p1 = new PropositionEchange();
        p1.setId(5);
        PropositionEchange p2 = new PropositionEchange();
        p2.setId(5);
        PropositionEchange p3 = new PropositionEchange();
        p3.setId(6);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    private int getStock(Royaume royaume, Ressource ressource) {
        return royaume.getRessources().stream()
            .filter(r -> r.getRessource() == ressource)
            .mapToInt(QuantiteRessource::getQuantite)
            .findFirst()
            .orElse(0);
    }
}
