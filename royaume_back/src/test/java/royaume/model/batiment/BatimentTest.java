package royaume.model.batiment;


import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import royaume.model.QuantiteRessource;
import royaume.model.Ressource;


// C'est une classe concrète créée uniquement pour les tests (sinon abstraite !!!! )

class BatimentTest {

    private Batiment batiment;

    
    static class BatimentTestable extends Batiment {

        BatimentTestable() {
            super(1, "Batiment test", 1, 50, 100, false);
        }

        @Override
        public List<QuantiteRessource> getCoutAmelioration() {
            return List.of();
        }

        @Override
        public int getTempsConstructionBase() {
            return 60;
        }
    }

    @BeforeEach
    void setUp() {
        batiment = new BatimentTestable();
    }

    @Test
    void subirDegats_DiminueCorrectementLesPV() {

        batiment.subirDegats(20);

        assertEquals(30, batiment.getPointsDeVie());
    }

    @Test
    void subirDegats_NeDoitPasDescendreSousZero() {

        batiment.subirDegats(100);

        assertEquals(0, batiment.getPointsDeVie());
    }

    @Test
    void reparer_AugmenteLesPV() {

        batiment.subirDegats(30);

        batiment.reparer(20);

        assertEquals(40, batiment.getPointsDeVie());
    }

    @Test
    void reparer_NeDoitPasDepasserLesPVMax() {

        batiment.reparer(100);

        assertEquals(100, batiment.getPointsDeVie());
    }

    @Test
    void estOperationnel_RetourneTrueSiBatimentEnBonEtat() {

        assertTrue(batiment.estOperationnel());
    }

    @Test
    void estOperationnel_RetourneFalseSiBatimentDetruit() {

        batiment.subirDegats(50);

        assertFalse(batiment.estOperationnel());
    }

    @Test
    void estOperationnel_RetourneFalseSiEnConstruction() {

        batiment.setEnConstruction(true);

        assertFalse(batiment.estOperationnel());
    }

    @Test
    void ameliorer_IncrementeLeNiveauEtLesPVMax() {

        batiment.ameliorer();

        assertEquals(2, batiment.getNiveau());
        assertEquals(100, batiment.getPointsDeVieMax());
    }
    
    
    @Test
    void getCoutAmelioration_Carriere_RetourneCoutsConfig() {
    	Carriere carriere = new Carriere(1, "Carrière", 1, 50, 100, false, 30);
    	List<QuantiteRessource> couts = carriere.getCoutAmelioration();
    	assertNotNull(couts);
        assertFalse(couts.isEmpty(), "La carrière de niveau 1 doit avoir un coût pour passer au niveau 2");
        boolean contientBois = couts.stream()
                .anyMatch(r -> r.getRessource() == Ressource.BOIS && r.getQuantite() == 80);
        assertTrue(contientBois, "Le coût pour le niveau 2 de la carrière doit inclure 80 de bois");
    	
    }
    
    void getTempsConstruction() {
    	Carriere carriere = new Carriere(1, "Carrière", 1, 50, 100, false, 30);
    	int temps= carriere.getTempsConstructionBase();
    	assertEquals(180,temps);
    	
    }
}
