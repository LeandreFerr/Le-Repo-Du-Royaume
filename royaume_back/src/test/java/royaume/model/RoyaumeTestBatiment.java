
package royaume.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.ChantierConstruction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;
import royaume.model.batiment.Mine;

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

        Chateau chateau = (Chateau) royaume.getBatiments().get(0);

        assertFalse(chateau.isEnConstruction());
        assertNull(chateau.getChantier());
    }

    @Test
    void creerMaison_AjouteMaisonEtChantierAuRoyaume() {

        Maison maison = BatimentFactory.creerMaison(
            royaume,
            2,
            "Maison paysanne",
            10,
            0.3,
            3
        );

        assertTrue(royaume.getBatiments().contains(maison));
        assertEquals(2, royaume.getBatiments().size());

        assertTrue(maison.isEnConstruction());
        assertNotNull(maison.getChantier());

        assertEquals(3, maison.getChantier().getOuvriersAssignes());

        assertEquals(97, royaume.getPopulationDisponible());
        assertEquals(3, royaume.getPopulationOuvriere());
    }

    @Test
    void creerPlusieursMaisons_EstAutorise() {

        Maison maison1 = BatimentFactory.creerMaison(
            royaume, 2, "Maison 1", 10, 0.3, 2
        );

        Maison maison2 = BatimentFactory.creerMaison(
            royaume, 3, "Maison 2", 10, 0.3, 2
        );

        assertEquals(3, royaume.getBatiments().size());

        assertTrue(royaume.getBatiments().contains(maison1));
        assertTrue(royaume.getBatiments().contains(maison2));

        assertTrue(maison1.isEnConstruction());
        assertTrue(maison2.isEnConstruction());

        assertNotNull(maison1.getChantier());
        assertNotNull(maison2.getChantier());

        assertEquals(96, royaume.getPopulationDisponible());
        assertEquals(4, royaume.getPopulationOuvriere());
    }

    @Test
    void creerBatimentProduction_AjouteAutomatiquementLaMine() {

        BatimentProduction mine =
            BatimentFactory.creerMine(
                royaume,
                2,
                "Mine d'or",
                100,
                3
            );

        assertTrue(royaume.getBatiments().contains(mine));
        assertEquals(2, royaume.getBatiments().size());

        assertTrue(mine.isEnConstruction());
        assertNotNull(mine.getChantier());

        assertEquals(3, mine.getChantier().getOuvriersAssignes());

        assertEquals(97, royaume.getPopulationDisponible());
        assertEquals(3, royaume.getPopulationOuvriere());
    }

    @Test
    void ajouterBatiment_RefuseUnDeuxiemeBatimentProductionDeMemeClasse() {

        BatimentFactory.creerMine(
            royaume, 2, "Mine d'or", 100, 2
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> BatimentFactory.creerMine(
                royaume, 3, "Deuxième mine", 80, 2
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

    @Test
    void creerMine_ChantierEstBienAssocieAuBatiment() {

        Mine mine = BatimentFactory.creerMine(
            royaume, 2, "Mine test", 100, 4
        );

        ChantierConstruction chantier = mine.getChantier();

        assertNotNull(chantier);
        assertSame(mine, chantier.getBatiment());
        assertSame(royaume, chantier.getRoyaume());

        assertTrue(mine.isEnConstruction());
        assertFalse(chantier.isTermine());
    }
}
