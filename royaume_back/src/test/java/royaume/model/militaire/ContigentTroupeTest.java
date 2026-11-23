package royaume.model.militaire;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ContingentTroupeTest {

    @Test
    void ajouter_incrementeLaQuantite() {
        ContingentTroupe c = new ContingentTroupe(TypeTroupe.LANCIER, 5);
        c.ajouter(3);
        assertEquals(8, c.getQuantite());
    }

    @Test
    void retirer_decrementeLaQuantite() {
        ContingentTroupe c = new ContingentTroupe(TypeTroupe.LANCIER, 5);
        c.retirer(2);
        assertEquals(3, c.getQuantite());
    }

    @Test
    void retirer_toutLeStock_donneZero() {
        ContingentTroupe c = new ContingentTroupe(TypeTroupe.ARCHER, 5);
        c.retirer(5);
        assertEquals(0, c.getQuantite());
    }

    @Test
    void retirer_plusQueLeStock_estRefuse_etNeChangeRien() {
        ContingentTroupe c = new ContingentTroupe(TypeTroupe.ARCHER, 5);
        assertThrows(IllegalArgumentException.class, () -> c.retirer(6));
        assertEquals(5, c.getQuantite());
    }

    @Test
    void quantiteNegative_estRefusee() {
        ContingentTroupe c = new ContingentTroupe(TypeTroupe.ARCHER, 5);
        assertThrows(IllegalArgumentException.class, () -> c.ajouter(-1));
        assertThrows(IllegalArgumentException.class, () -> c.retirer(-1));
    }
}