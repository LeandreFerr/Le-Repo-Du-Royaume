
package royaume.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;

class RoyaumeTestBatiment {

    private Royaume royaume;

    @BeforeEach
    void setUp() {
        royaume = new Royaume(1, "Royaume test");
    }

    @Test
    void constructeur_CreeAutomatiquementLeChateauInitial() {

        assertEquals(1, royaume.getBatiments().size());

        assertTrue(
            royaume.getBatiments().get(0) instanceof Chateau
        );
    }

    @Test
    void creerMaison_AjouteAutomatiquementLaMaisonAuRoyaume() {

        Maison maison = BatimentFactory.creerMaison(
            royaume,
            2,
            "Maison paysanne",
            10,
            0.3
        );

        assertTrue(royaume.getBatiments().contains(maison));
        assertEquals(2, royaume.getBatiments().size());
    }

    @Test
    void creerPlusieursMaisons_EstAutorise() {

        Maison maison1 = BatimentFactory.creerMaison(
            royaume, 2, "Maison 1", 10, 0.3
        );

        Maison maison2 = BatimentFactory.creerMaison(
            royaume, 3, "Maison 2", 10, 0.3
        );

        assertEquals(3, royaume.getBatiments().size());

        assertTrue(royaume.getBatiments().contains(maison1));
        assertTrue(royaume.getBatiments().contains(maison2));
    }

    @Test
    void creerBatimentProduction_AjouteAutomatiquementLaMine() {

        BatimentProduction mine =
            BatimentFactory.creerBatimentProduction(
                royaume,
                2,
                "Mine d'or",
                Ressource.OR,
                100
            );

        assertTrue(royaume.getBatiments().contains(mine));
        assertEquals(2, royaume.getBatiments().size());
    }

    @Test
    void ajouterBatiment_RefuseUnDeuxiemeBatimentProductionDeMemeClasse() {

        BatimentFactory.creerBatimentProduction(
            royaume, 2, "Mine d'or", Ressource.OR, 100
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> BatimentFactory.creerBatimentProduction(
                royaume, 3, "Mine d'or", Ressource.OR, 80
            )
        );
    }

    @Test
    void ajouterBatiment_RefuseUnDeuxiemeChateau() {

        assertThrows(
            IllegalArgumentException.class,
            () -> BatimentFactory.creerChateauInitial(royaume)
        );
    }

    @Test
    void getBatiments_NePermetPasDeModifierDirectementLaListe() {

        assertThrows(
            UnsupportedOperationException.class,
            () -> royaume.getBatiments().clear()
        );
    }
}
