
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChateauTest {

    private Chateau chateau;

    @BeforeEach
    void setUp() {
        chateau = new Chateau(
            1,
            "Chateau",
            1,
            50,
            50,
            false
        );
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

        chateau.debloquerPalier();

        assertEquals(2, chateau.getNiveauPalierDebloque());
    }
}
