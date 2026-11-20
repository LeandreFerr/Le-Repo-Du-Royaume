
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import royaume.config.ConfigurationPaliers;
import royaume.model.Peuple.Canards;
import royaume.model.PalierNiveau;
import royaume.model.Peuple.Population;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

class ChateauTest {

    private static final Population PEUPLE_TEST = new Canards(1, "Canards", "Archiduc", 0.05);

    private Chateau chateau;
    private Maison maison;
    private Royaume royaume;

    @BeforeEach
    void setUp() {
    	
      	List<QuantiteRessource> ressources = List.of(
    			new QuantiteRessource(10000, Ressource.OR),
    			new QuantiteRessource(10000, Ressource.PIERRE),
    			new QuantiteRessource(10000, Ressource.BOIS),
    			new QuantiteRessource(50000, Ressource.NOURRITURE)
    			);

    	royaume = new Royaume(1, "FarFarAway", PEUPLE_TEST, 1, 500, 500, 200, 100, false, ressources);
        
    	chateau = royaume.getChateau();
    	chateau.debloquerPalier();
    	
    	//Maison.setBonusCroissance(0.0); Maintenant le bonus Global est géré par Royaume direct
    	maison = new Maison(2, "Maison Test", 1, 100, 100, false);
    	
 
    }

    @Test
    void verifierConditionsVictoire_RetourneFalseDuNiveau1AuNiveau9() {

        for (int niveau = 1; niveau <= 9; niveau++) {

            assertFalse(
                chateau.verifierConditionsVictoire(),
                "La victoire ne doit pas être possible au niveau " + niveau
            );

            chateau.ameliorer();
        }
    }

    @Test
    void verifierConditionsVictoire_RetourneTrueAuNiveau10() {

        for (int i = 1; i < 10; i++) {
            chateau.ameliorer();
        }

        assertEquals(10, chateau.getNiveau());
        assertTrue(chateau.verifierConditionsVictoire());
    }

    @Test
    void debloquerPalier_MetAJourLeNiveauDebloque() {

        chateau.ameliorer();

        chateau.debloquerPalier();  // LA 

        assertEquals(2, chateau.getNiveauPalierDebloque());
    }
    
   
    @Test
    void testInterdictionDepasserPalierChateau() {

        Exception exception = assertThrows(
            IllegalStateException.class,
            () -> maison.ameliorer(1, royaume)
        );

        assertEquals(
            "Impossible d'améliorer : le niveau du bâtiment ne peut pas dépasser le niveau du château.",
            exception.getMessage()
        );

        assertEquals(1, maison.getNiveau());
        assertFalse(maison.isEnConstruction());
        assertEquals(null, maison.getChantier());
    }
    ///////////////////////////////////////
    /// 
    /// ///////////////////////////////////
    /// 
    /// 
    ///     C4EST LA le fonctionnement ameliorer / finaliser ////

@Test
void testAmeliorerBatimentApresAmeliorationChateau() {

    chateau.ameliorer();
    chateau.debloquerPalier();

    // Première amélioration : niveau 1 vers niveau 2
    assertDoesNotThrow(() -> {
        maison.ameliorer(1, royaume);
    });

    // Finalisation du premier chantier
    maison.getChantier().finaliser(
        maison.getChantier().getDateFinPrevue()
    );

    assertEquals(2, maison.getNiveau());

    // Deuxième amélioration : interdite car le palier est à 2
    Exception exception = assertThrows(
        IllegalStateException.class,
        () -> maison.ameliorer(1, royaume)
    );

    assertEquals(
        "Impossible d'améliorer : le niveau du bâtiment ne peut pas dépasser le niveau du château.",
        exception.getMessage()
    ); 
    assertEquals(2, maison.getNiveau());
    assertFalse(maison.isEnConstruction());
    assertEquals(null, maison.getChantier());
}

///
/// 
/// 
/// ////////////////////////////////////////////////////////

/*
    @Test
    void testAmeliorerBatimentApresAmeliorationChateau() {
    
        chateau.ameliorer(); 
        chateau.debloquerPalier();
        assertDoesNotThrow(() -> {
            maison.ameliorer(1,royaume);
        });
        // [REVIEW TICKET 2] Une fois ameliorer(Royaume) renommée en acheverAmelioration(Royaume),
        // vous pourrez ici finaliser le chantier comme dans vos autres tests :
        // maison.getChantier().finaliser(maison.getChantier().getDateFinPrevue());
        
       // maison.acheverAmelioration(royaume);
       maison.getChantier().finaliser(maison.getChantier().getDateFinPrevue());
        
        assertEquals(2, maison.getNiveau());

        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
          //  maison.acheverAmelioration(royaume);
          maison.getChantier().finaliser(maison.getChantier().getDateFinPrevue());

        });

        assertEquals("Impossible d'améliorer : le niveau du bâtiment ne peut pas dépasser le niveau du château.", exception.getMessage());
        assertEquals(2, maison.getNiveau());
        assertFalse(maison.isEnConstruction());
        assertEquals(null, maison.getChantier());
        
    }
    */
    @Test
    void testLeNiveauDuChateauMonteAvecLeRoyaume() {
  
        List<PalierNiveau> paliers = ConfigurationPaliers.getPaliers();
        assertEquals(1, royaume.getNiveau());
        assertEquals(1, royaume.getChateau().getNiveau());

 
        if (royaume.peutMonterDeNiveau(paliers)) {
            royaume.ameliorerNiveau(paliers);
        }

        assertEquals(royaume.getNiveau(), royaume.getChateau().getNiveau());
        assertEquals(royaume.getNiveau(), royaume.getChateau().getNiveauPalierDebloque());
    }
    
    @Test
    void chateauEnRuineSiPv0() {
    	Chateau chateauHS = royaume.getChateau();
    	chateauHS.setPointsDeVie(0);
    	assertEquals(0, chateauHS.getPointsDeVie(), "Les points de vie du château doivent être à 0");
        assertTrue(royaume.estEnRuine(), "Le royaume doit être en ruine (enRuine = true)");
    }
    	
    }

