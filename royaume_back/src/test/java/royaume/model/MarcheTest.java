package royaume.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MarcheTest {

    private Marche marche;
    private Royaume vendeur;
    private Royaume acheteur;

    private QuantiteRessource orVendeur;
    private QuantiteRessource pierreVendeur;

    private QuantiteRessource orAcheteur;
    private QuantiteRessource pierreAcheteur;
//__________________________________________________________
    @BeforeEach
    void setUp() {

        // Initialisation d'une nouvelle instance de marché
        marche = new Marche(1, "Marché central");

        // Ressources du vendeur
        orVendeur = new QuantiteRessource(500, Ressource.OR);
        pierreVendeur = new QuantiteRessource(0, Ressource.PIERRE);

        Population peupleVendeur = new Canards(1, "Canards", "Archiduc", 0.05);
        vendeur = new Royaume(
            1, "Gondor", peupleVendeur, 1,
            100, 0, 0, 0, false,
            new ArrayList<>(List.of(
                new QuantiteRessource(300, Ressource.BOIS),
                pierreVendeur,
                orVendeur,
                new QuantiteRessource(100, Ressource.NOURRITURE)
            ))
        );

        // Ressources de l'acheteur
        orAcheteur = new QuantiteRessource(0, Ressource.OR);
        pierreAcheteur = new QuantiteRessource(400, Ressource.PIERRE);

        Population peupleAcheteur = new Princesses(2, "Princesses", "Reine", 0.10);
        acheteur = new Royaume(
            2, "Rohan", peupleAcheteur, 1,
            100, 0, 0, 0, false,
            new ArrayList<>(List.of(
                new QuantiteRessource(200, Ressource.BOIS),
                pierreAcheteur,
                orAcheteur,
                new QuantiteRessource(500, Ressource.NOURRITURE)
            ))
        );
    }

//___________________________________________________________________________
    
    // ==========================================================
    // Publication d'une offre
    // ==========================================================

    @Test
    @DisplayName("publierOffre débite les ressources du vendeur et crée une offre DISPONIBLE")
    void testPublierOffreNominal() {

        // Given
        QuantiteRessource propose = new QuantiteRessource(100, Ressource.OR);
        QuantiteRessource demande = new QuantiteRessource(150, Ressource.PIERRE);

        // When
        OffreMarche offre = marche.publierOffre(vendeur, propose, demande);

        // Then
        assertNotNull(offre);

        assertEquals(StatutOffre.DISPONIBLE, offre.getStatut());
        assertEquals(vendeur, offre.getVendeur());

        assertEquals(400, getStock(vendeur, Ressource.OR),
            "Les 100 OR proposés doivent être débités immédiatement");

        assertEquals(100, offre.getPropose().getQuantite());
        assertEquals(Ressource.OR, offre.getPropose().getRessource());

        assertEquals(150, offre.getDemande().getQuantite());
        assertEquals(Ressource.PIERRE, offre.getDemande().getRessource());

        assertEquals(1, marche.getOffres().size());
        assertTrue(marche.getOffres().contains(offre));
    }


    @Test
    @DisplayName("publierOffre refuse la publication si le vendeur ne possède pas assez de ressources")
    void testPublierOffreStockInsuffisant() {

        // Given
        QuantiteRessource propose = new QuantiteRessource(600, Ressource.OR);
        QuantiteRessource demande = new QuantiteRessource(150, Ressource.PIERRE);

        int stockInitial = getStock(vendeur, Ressource.OR);

        // When / Then
        assertThrows(
            IllegalStateException.class,
            () -> marche.publierOffre(vendeur, propose, demande)
        );

        assertEquals(stockInitial, getStock(vendeur, Ressource.OR),
            "Le stock ne doit pas être modifié si la publication échoue");

        assertTrue(marche.getOffres().isEmpty(),
            "Aucune offre ne doit être créée");
    }


    // ==========================================================
    // Retrait d'une offre
    // ==========================================================

    @Test
    @DisplayName("retirerOffre restitue les ressources au vendeur et passe le statut à ANNULEE")
    void testRetirerOffreNominal() {

        // Given
        QuantiteRessource propose = new QuantiteRessource(100, Ressource.OR);
        QuantiteRessource demande = new QuantiteRessource(150, Ressource.PIERRE);

        OffreMarche offre = marche.publierOffre(vendeur, propose, demande);

        assertEquals(400, getStock(vendeur, Ressource.OR));

        // When
        marche.retirerOffre(offre);

        // Then
        assertEquals(StatutOffre.ANNULEE, offre.getStatut());

        assertEquals(500, getStock(vendeur, Ressource.OR),
            "Les 100 OR consignés doivent être restitués au vendeur");
    }


    // ==========================================================
    // Exécution d'un échange
    // ==========================================================

    @Test
    @DisplayName("executerEchange réalise correctement le transfert bilatéral de ressources")
    void testExecuterEchangeNominal() {

        // Given
        // Le vendeur possède 500 OR et propose 100 OR.
        // L'acheteur possède 400 PIERRE et doit en donner 150.

        QuantiteRessource propose = new QuantiteRessource(100, Ressource.OR);
        QuantiteRessource demande = new QuantiteRessource(150, Ressource.PIERRE);

        OffreMarche offre = marche.publierOffre(vendeur, propose, demande);

        // Vérification du débit lors de la publication
        assertEquals(400, getStock(vendeur, Ressource.OR));

        // When
        marche.executerEchange(offre, acheteur);

        // Then
        assertEquals(StatutOffre.CONCLUE, offre.getStatut());

        // Vérification du stock du vendeur
        assertEquals(400, getStock(vendeur, Ressource.OR),
            "Le vendeur a déjà consigné les 100 OR lors de la publication");

        assertEquals(150, getStock(vendeur, Ressource.PIERRE),
            "Le vendeur doit recevoir les 150 PIERRE de l'acheteur");

        // Vérification du stock de l'acheteur
        assertEquals(250, getStock(acheteur, Ressource.PIERRE),
            "L'acheteur doit payer les 150 PIERRE demandées");

        assertEquals(100, getStock(acheteur, Ressource.OR),
            "L'acheteur doit recevoir les 100 OR proposés par le vendeur");
    }


    @Test
    @DisplayName("executerEchange échoue si l'acheteur ne possède pas assez de ressources")
    void testExecuterEchangeStockInsuffisant() {

        // Given
        QuantiteRessource propose = new QuantiteRessource(100, Ressource.OR);
        QuantiteRessource demande = new QuantiteRessource(500, Ressource.PIERRE);

        OffreMarche offre = marche.publierOffre(vendeur, propose, demande);

        // Stocks après publication
        int orVendeurAvant = getStock(vendeur, Ressource.OR);
        int pierreVendeurAvant = getStock(vendeur, Ressource.PIERRE);

        int orAcheteurAvant = getStock(acheteur, Ressource.OR);
        int pierreAcheteurAvant = getStock(acheteur, Ressource.PIERRE);

        // When / Then
        assertThrows(
            IllegalStateException.class,
            () -> marche.executerEchange(offre, acheteur)
        );

        // Le statut reste DISPONIBLE
        assertEquals(StatutOffre.DISPONIBLE, offre.getStatut());

        // Aucun stock ne doit avoir été modifié
        assertEquals(orVendeurAvant, getStock(vendeur, Ressource.OR));
        assertEquals(pierreVendeurAvant, getStock(vendeur, Ressource.PIERRE));

        assertEquals(orAcheteurAvant, getStock(acheteur, Ressource.OR));
        assertEquals(pierreAcheteurAvant, getStock(acheteur, Ressource.PIERRE));
    }


    @Test
    @DisplayName("executerEchange refuse une offre déjà conclue")
    void testExecuterEchangeOffreDejaConclue() {

        // Given
        OffreMarche offre = marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );

        marche.executerEchange(offre, acheteur);

        int orAcheteurAvant = getStock(acheteur, Ressource.OR);
        int pierreAcheteurAvant = getStock(acheteur, Ressource.PIERRE);

        // When / Then
        assertThrows(
            IllegalStateException.class,
            () -> marche.executerEchange(offre, acheteur)
        );

        assertEquals(StatutOffre.CONCLUE, offre.getStatut());

        assertEquals(orAcheteurAvant, getStock(acheteur, Ressource.OR));
        assertEquals(pierreAcheteurAvant, getStock(acheteur, Ressource.PIERRE));
    }


    // ==========================================================
    // Constructeurs et setters
    // ==========================================================

    @Test
    @DisplayName("Les setters modifient correctement les attributs d'une offre")
    void testSettersOffre() {

        // Given
        OffreMarche offre = new OffreMarche();

        LocalDateTime date = LocalDateTime.of(2026, 5, 10, 14, 30);

        // When
        offre.setId(10);
        offre.setDatePublication(date);
        offre.setStatut(StatutOffre.DISPONIBLE);
        offre.setVendeur(vendeur);
        offre.setPropose(new QuantiteRessource(100, Ressource.OR));
        offre.setDemande(new QuantiteRessource(150, Ressource.PIERRE));

        // Then
        assertEquals(10, offre.getId());
        assertEquals(date, offre.getDatePublication());
        assertEquals(StatutOffre.DISPONIBLE, offre.getStatut());

        assertEquals(vendeur, offre.getVendeur());

        assertEquals(100, offre.getPropose().getQuantite());
        assertEquals(Ressource.OR, offre.getPropose().getRessource());

        assertEquals(150, offre.getDemande().getQuantite());
        assertEquals(Ressource.PIERRE, offre.getDemande().getRessource());
    }
    // ==========================================================
    // Bon fonctionnement de la liste "offres"
    // ==========================================================

    
    @Test
    @DisplayName("publierOffre ajoute correctement une offre à la liste du marché")
    void testPublierOffreAjouteALaListe() {

        // Given
        int nombreOffresAvant = marche.getOffres().size();

        // When
        OffreMarche offre = marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );

        // Then
        assertNotNull(offre);
        assertEquals(nombreOffresAvant + 1, marche.getOffres().size());
        assertTrue(marche.getOffres().contains(offre));
        assertEquals(StatutOffre.DISPONIBLE, offre.getStatut());
    }
    
    @Test
    @DisplayName("Une offre annulée reste dans la liste avec le statut ANNULEE")
    void testOffreAnnuleeConserveeDansLaListe() {

        // Given
        OffreMarche offre = marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );

        int nombreOffresAvant = marche.getOffres().size();

        // When
        marche.retirerOffre(offre);

        // Then
        assertEquals(nombreOffresAvant, marche.getOffres().size());

        assertTrue(marche.getOffres().contains(offre));

        assertEquals(StatutOffre.ANNULEE, offre.getStatut());
    }
    
    @Test
    @DisplayName("Une offre conclue reste dans la liste avec le statut CONCLUE")
    void testOffreConclueConserveeDansLaListe() {

        // Given
        OffreMarche offre = marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );

        int nombreOffresAvant = marche.getOffres().size();

        // When
        marche.executerEchange(offre, acheteur);

        // Then
        assertEquals(nombreOffresAvant, marche.getOffres().size());

        assertTrue(marche.getOffres().contains(offre));

        assertEquals(StatutOffre.CONCLUE, offre.getStatut());
    }
    
    @Test
    @DisplayName("La liste conserve toutes les offres après plusieurs publications, annulations et échanges")
    void testHistoriqueCompletDesOffres() {

        // Given : création de trois offres

        OffreMarche offre1 = marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );

        OffreMarche offre2 = marche.publierOffre(
            vendeur,
            new QuantiteRessource(50, Ressource.OR),
            new QuantiteRessource(100, Ressource.PIERRE)
        );

        OffreMarche offre3 = marche.publierOffre(
            vendeur,
            new QuantiteRessource(80, Ressource.BOIS),
            new QuantiteRessource(50, Ressource.PIERRE)
        );

        // Vérification des publications
        assertEquals(3, marche.getOffres().size());

        // When : annulation de la première offre
        marche.retirerOffre(offre1);

        // When : exécution de la deuxième offre
        marche.executerEchange(offre2, acheteur);

        // La troisième offre reste disponible

        // Then : les trois offres sont toujours présentes
        assertEquals(3, marche.getOffres().size());

        assertTrue(marche.getOffres().contains(offre1));
        assertTrue(marche.getOffres().contains(offre2));
        assertTrue(marche.getOffres().contains(offre3));

        // Vérification des statuts
        assertEquals(StatutOffre.ANNULEE, offre1.getStatut());
        assertEquals(StatutOffre.CONCLUE, offre2.getStatut());
        assertEquals(StatutOffre.DISPONIBLE, offre3.getStatut());

        // Vérification qu'aucune offre n'a été supprimée
        assertEquals(3, marche.getOffres().size());
    }

    // ==========================================================
    // Sécurité métier, Invariants & Architecture
    // ==========================================================

    @Test
    @DisplayName("retirerOffre lève une exception et ne rembourse pas si l'offre est déjà annulée ou conclue")
    void testRetirerOffreAntiDuplication() {
        // Given : Offre publiée puis annulée
        OffreMarche offre = marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );
        marche.retirerOffre(offre);
        assertEquals(500, getStock(vendeur, Ressource.OR));

        // When & Then : Tentative de ré-annulation (anti-duplication glitch)
        assertThrows(IllegalStateException.class, () -> marche.retirerOffre(offre));
        assertEquals(500, getStock(vendeur, Ressource.OR),
            "Le vendeur ne doit pas être crédité une seconde fois");

        // Cas offre conclue
        OffreMarche offre2 = marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );
        marche.executerEchange(offre2, acheteur);
        assertThrows(IllegalStateException.class, () -> marche.retirerOffre(offre2));
    }

    @Test
    @DisplayName("executerEchange interdit à un vendeur d'acheter sa propre offre")
    void testExecuterEchangeAutoAchatInterdit() {
        // Given
        OffreMarche offre = marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );

        // When & Then
        assertThrows(IllegalStateException.class, () -> marche.executerEchange(offre, vendeur));
    }

    @Test
    @DisplayName("Un royaume en ruine ne peut ni publier d'offre ni exécuter d'échange")
    void testReglesRoyaumeEnRuine() {
        // Given : Vendeur en ruine
        vendeur.setEnRuine(true);
        assertThrows(IllegalStateException.class, () -> marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        ));

        // Acheteur en ruine
        vendeur.setEnRuine(false);
        OffreMarche offre = marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );
        acheteur.setEnRuine(true);
        assertThrows(IllegalStateException.class, () -> marche.executerEchange(offre, acheteur));
    }

    @Test
    @DisplayName("getOffres retourne une vue non modifiable")
    void testListeOffresImmuableDepuisExterieur() {
        assertThrows(UnsupportedOperationException.class, () -> marche.getOffres().add(null));
    }

    @Test
    @DisplayName("Deux instances distinctes de Marché possèdent des listes d'offres isolées et indépendantes")
    void testInstancesMarcheIndependantes() {
        Marche autreMarche = new Marche(2, "Marché secondaire");
        marche.publierOffre(
            vendeur,
            new QuantiteRessource(100, Ressource.OR),
            new QuantiteRessource(150, Ressource.PIERRE)
        );

        assertEquals(1, marche.getOffres().size());
        assertEquals(0, autreMarche.getOffres().size(),
            "Le deuxième marché ne doit pas contenir les offres du premier (non-statique)");
    }

    private int getStock(Royaume royaume, Ressource ressource) {
        return royaume.getRessources().stream()
            .filter(r -> r.getRessource() == ressource)
            .mapToInt(QuantiteRessource::getQuantite)
            .findFirst()
            .orElse(0);
    }
}