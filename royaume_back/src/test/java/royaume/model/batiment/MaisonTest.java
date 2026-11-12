
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import royaume.model.QuantiteRessource;
import royaume.model.Ressource;

class MaisonTest {

    private Maison maison;

    @BeforeEach
    void setUp() {
    	
    	Maison.setBonusCroissance(0.0);
    	maison = new Maison(1,"Maison paysanne", 1,50,50,false);
    	
    	}

    
    @Test
    void getCoutAmelioration_NiveauSuivant_RetourneLesCoutsDeLaConfiguration() {
        // Cout maison de niveau 1 passe au niveau 2
        List<QuantiteRessource> couts = maison.getCoutAmelioration();

        assertNotNull(couts);
        assertFalse(couts.isEmpty(), "Le niveau 2 doit avoir un coût d'amélioration");

        boolean contientBois = couts.stream()
                .anyMatch(r -> r.getRessource() == Ressource.BOIS && r.getQuantite() == 10);
        assertTrue(contientBois, "Le coût du niveau 2 doit inclure 10 de bois");
    }

    @Test
    void getCoutAmelioration_NiveauMax_RetourneListeVide() {
        // On monte la maison au niveau max (10)
        for (int i = maison.getNiveau(); i < 10; i++) {
            maison.ameliorer();
        }

        List<QuantiteRessource> couts = maison.getCoutAmelioration();

        assertNotNull(couts);
        assertTrue(couts.isEmpty(), "Il ne doit pas y avoir de coût au-delà du niveau max");
    }
    
    @Test
    void testBonusCroissanceSAdditionneAvecPlusieursMaisons() {
    	
    	//On a déjà une maison dans le set up donc on doit être à 0.12
    	// 0,0001 permet d'éviter les faux échecs liés aux arrondis
    	assertEquals(0.12,Maison.getBonusCroissance(), 0.0001);
    	
    	// 2. Création d'une deuxième maison 
        new Maison(2, "Maison paysanne 2", 1, 50, 50, false);
        assertEquals(0.24, Maison.getBonusCroissance(), 0.0001);

        // 3. Création d'une troisième maison 
        new Maison(3, "Maison bourgeoise", 1, 100, 100, false);
        assertEquals(0.36, Maison.getBonusCroissance(), 0.0001);
    }
}
