
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatimentProductionTest {

    private BatimentProduction mine;

    @BeforeEach
    void setUp() {
        mine = new Mine(
            1,
            "Mine d'or",
            1,
            50,
            50,
            false,
            100
        );
    }

    @Test
    void calculerProduction_RetourneProductionDeBase() {

        assertEquals(100, mine.calculerProduction());
    }

    @Test
    void calculerProduction_AugmenteAvecLeNiveau() {

        mine.ameliorer();

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
