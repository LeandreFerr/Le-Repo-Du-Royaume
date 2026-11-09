package royaume.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EmplacementCarteTest {

    private Carte carte;
    private EmplacementCarte emplacement;
    private Royaume royaume;

    @BeforeEach
    void setUp() {
        carte = new Carte();

        emplacement = new EmplacementCarte(
                1,
                25,
                50,
                carte);

        royaume = new Royaume();
    }

    @Test
    void constructeurSimplifieDoitCreerUnEmplacementLibre() {
        assertNull(emplacement.getId());
        assertEquals(1, emplacement.getNumero());
        assertEquals(25, emplacement.getCoordX());
        assertEquals(50, emplacement.getCoordY());
        assertFalse(emplacement.isOccupe());
        assertSame(carte, emplacement.getCarte());
        assertNull(emplacement.getRoyaume());
    }

    @Test
    void assignerRoyaumeDoitOccuperLEmplacement() {
        emplacement.assignerRoyaume(royaume);

        assertTrue(emplacement.isOccupe());
        assertSame(royaume, emplacement.getRoyaume());
    }

    @Test
    void assignerRoyaumeDoitAussiRenseignerLEmplacementDuRoyaume() {
        emplacement.assignerRoyaume(royaume);

        assertSame(emplacement, royaume.getEmplacement());
    }

    @Test
    void assignerRoyaumeNullDoitLeverUneException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> emplacement.assignerRoyaume(null));

        assertEquals(
                "Aucun royaume à assigner.",
                exception.getMessage());

        assertFalse(emplacement.isOccupe());
        assertNull(emplacement.getRoyaume());
    }

    @Test
    void assignerUnDeuxiemeRoyaumeDoitLeverUneException() {
        Royaume deuxiemeRoyaume = new Royaume();

        emplacement.assignerRoyaume(royaume);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> emplacement.assignerRoyaume(deuxiemeRoyaume));

        assertEquals(
                "L'emplacement 1 est déjà occupé.",
                exception.getMessage());

        assertSame(royaume, emplacement.getRoyaume());
        assertTrue(emplacement.isOccupe());
        assertNull(deuxiemeRoyaume.getEmplacement());
    }

    @Test
    void libererDoitRendreLEmplacementDisponible() {
        emplacement.assignerRoyaume(royaume);

        emplacement.liberer();

        assertFalse(emplacement.isOccupe());
        assertNull(emplacement.getRoyaume());
    }

    @Test
    void libererDoitRetirerLEmplacementDuRoyaume() {
        emplacement.assignerRoyaume(royaume);

        emplacement.liberer();

        assertNull(royaume.getEmplacement());
    }

    @Test
    void libererUnEmplacementDejaLibreNeDoitPasLeverException() {
        assertDoesNotThrow(() -> emplacement.liberer());

        assertFalse(emplacement.isOccupe());
        assertNull(emplacement.getRoyaume());
    }
}
