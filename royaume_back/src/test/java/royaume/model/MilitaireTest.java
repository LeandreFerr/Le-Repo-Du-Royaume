package royaume.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import royaume.model.Militaire.*;
import royaume.model.Peuple.*;

public class MilitaireTest {

    private static final Population PEUPLE_TEST = new Canards(1, "Canards", "Archiduc", 0.05);
    private Royaume royaume;

    @BeforeEach
    void setUp() {
        List<QuantiteRessource> listeRessources = new ArrayList<>(List.of(
                new QuantiteRessource(100, Ressource.BOIS),
                new QuantiteRessource(100, Ressource.PIERRE),
                new QuantiteRessource(100, Ressource.OR),
                new QuantiteRessource(100, Ressource.NOURRITURE)
        ));

        royaume = new Royaume(1, "TPT", PEUPLE_TEST, 1, 100, 0, 0, 0, false, listeRessources);
    }

    @Test
    void recruterArcherReussit() {
        assertTrue(royaume.recruterTroupe(TypeTroupe.ARCHER));

        assertEquals(99, royaume.getPopulationDisponible());
        assertEquals(1, royaume.getPopulationEnEntrainement());
    }

    @Test
    void recruterArcherDeduitLesRessources() {
        royaume.recruterTroupe(TypeTroupe.ARCHER);

        // Archer : 10 or, 10 bois, 10 nourriture
        // On vérifie via aAssezDeRessources : il reste exactement 90
        assertTrue(royaume.aAssezDeRessources(Ressource.OR, 90));
        assertFalse(royaume.aAssezDeRessources(Ressource.OR, 91));
        assertTrue(royaume.aAssezDeRessources(Ressource.BOIS, 90));
        assertFalse(royaume.aAssezDeRessources(Ressource.BOIS, 91));
        assertTrue(royaume.aAssezDeRessources(Ressource.NOURRITURE, 90));
        // La pierre n'est pas utilisée par l'archer
        assertTrue(royaume.aAssezDeRessources(Ressource.PIERRE, 100));
        assertFalse(royaume.aAssezDeRessources(Ressource.PIERRE, 101));
    }

    @Test
    void recruterSansRessourcesEchoue() {
        Royaume pauvre = new Royaume(2, "Pauvre", PEUPLE_TEST);   // stocks à 0

        assertFalse(pauvre.recruterTroupe(TypeTroupe.ARCHER));
        assertEquals(0, pauvre.getPopulationEnEntrainement());
    }
}