
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
