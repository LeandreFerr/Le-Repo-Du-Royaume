package royaume.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RoyaumeTest {

    private Royaume royaume;
    private QuantiteRessource bois;
    private QuantiteRessource pierre;
    private Population peuple;

    @BeforeEach
    void setUp() {
        
        pierre = new QuantiteRessource(50, Ressource.PIERRE);
        bois = new QuantiteRessource(100,Ressource.BOIS);
        
        peuple = new Canards(1, "Corentin", "seigneur", 1.0);

        List<QuantiteRessource> listeRessources = new ArrayList<>(List.of(bois, pierre));
        royaume = new Royaume(1, "TPT", peuple, 1, 100, 0, 0, 0, false, listeRessources);
    }

    @Test
    @DisplayName("aAssezDeRessources retourne true si quantité suffisante, false sinon")
    void testAAssezDeRessources() {
        // Quantité pile égale ou inférieure au stock
        assertTrue(royaume.aAssezDeRessources(Ressource.BOIS, 50));
        assertTrue(royaume.aAssezDeRessources(Ressource.BOIS, 100));

        // Quantité supérieure au stock
        assertFalse(royaume.aAssezDeRessources(Ressource.BOIS, 101));

        // Ressource inexistante dans le stock (ex: OR)
        assertFalse(royaume.aAssezDeRessources(Ressource.OR, 10));
    }

    @Test
    @DisplayName("crediterRessources incrémente la ressource ciblée sans modifier les autres")
    void testCrediterRessources() {
        royaume.crediterRessources(Ressource.BOIS, 25);

        assertEquals(125, bois.getQuantite(), "Le bois doit être augmenté de 25");
        assertEquals(50, pierre.getQuantite(), "La pierre ne doit pas être modifiée");
    }

    @Test
    @DisplayName("debiterRessources retire le stock si suffisant, ou refuse sans modifier")
    void testDebiterRessources() {
        // Cas succès
        assertTrue(royaume.debiterRessources(Ressource.BOIS, 30));
        assertEquals(70, bois.getQuantite());

        // Cas échec (stock insuffisant)
        assertFalse(royaume.debiterRessources(Ressource.BOIS, 100));
        assertEquals(70, bois.getQuantite(), "Le stock ne doit pas avoir bougé");
    }

    @Test
    @DisplayName("appliquerCroissance augmente la populationDisponible selon le taux")
    void testAppliquerCroissance() {
        // Base : 100 habitants, croissance de +10% -> 110
        royaume.appliquerCroissance(0.10);
        assertEquals(110, royaume.getPopulationDisponible());

        // Test de l'arrondi : 110 avec +5% -> 115.5 arrondi à 116
        royaume.appliquerCroissance(0.05);
        assertEquals(116, royaume.getPopulationDisponible());
    }
}
