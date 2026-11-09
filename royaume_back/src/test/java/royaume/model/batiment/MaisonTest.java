
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
        maison = new Maison(
            1,
            "Maison paysanne",
            1,
            50,
            50,
            false,
            10,
            0.3
        );
    }

    @Test
    void getCapaciteLogement_RetourneCapaciteDeBase() {

        assertEquals(10, maison.getCapaciteLogement());
    }

    @Test
    void getCapaciteLogement_AugmenteAvecLeNiveau() {

        maison.ameliorer();

        assertEquals(20, maison.getCapaciteLogement());

        maison.ameliorer();

        assertEquals(30, maison.getCapaciteLogement());
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
    
}
