
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

class BatimentProductionTest {

    private BatimentProduction mine;
    private Royaume royaume;
    
    @BeforeEach
    void setUp() {

        List<QuantiteRessource> ressources = new ArrayList<>();

        ressources.add(new QuantiteRessource(1000, Ressource.OR));
        ressources.add(new QuantiteRessource(1000, Ressource.PIERRE));
        ressources.add(new QuantiteRessource(1000, Ressource.BOIS));
        ressources.add(new QuantiteRessource(7000, Ressource.NOURRITURE));

        royaume = new Royaume(
            1, "Gondor", 1,
            500, 500, 0, 100,
            false, ressources
        );
    	
    	//Passe au niveau 2 le chateau :
        
        Chateau chateau = royaume.getChateau();
        chateau.ameliorer();
        
        
        mine = new Mine(
            1,
            "Mine d'or",
            1,
            50,
            50,
            false,
            100
        );
        
        royaume.ajouterBatiment(mine);

    }

    @Test
    void calculerProduction_RetourneProductionDeBase() {

        assertEquals(100, mine.calculerProduction());
    }

    @Test
    void calculerProduction_AugmenteAvecLeNiveau() {

        mine.ameliorer(2, royaume);

        mine.getChantier().finaliser(
        	    mine.getChantier().getDateFinPrevue()
        	);
        
        assertEquals(200, mine.calculerProduction());
    }

    @Test
    void calculerProduction_RetourneZeroSiBatimentDetruit() {

        mine.subirDegats(50);

        assertEquals(0, mine.calculerProduction());
    }

    @Test
    void calculerProduction_RetourneZeroSiEnConstruction() {

        mine.setEnConstruction(true);

        assertEquals(0, mine.calculerProduction());
    }
}
