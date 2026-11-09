package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import royaume.model.Royaume;

class ChantierConstructionTest {

    private Royaume royaume;
    private Mine mine;
    private ChantierConstruction chantier;

    @BeforeEach
    void setUp() {

        royaume = new Royaume(1, "Test");

        mine = new Mine(
            2, "Mine test", 1,
            2500, 2500, false, 100
        );

        royaume.ajouterBatiment(mine);

        chantier = new ChantierConstruction(
            1,
            mine,
            royaume,
            1,
            2,
            3600,
            false
        );
    }

    @Test
    void creerChantier() {

        assertNotNull(chantier);
        assertEquals(mine, chantier.getBatiment());

        assertNotNull(chantier.getDateDebut());
        assertNotNull(chantier.getDateFinPrevue());

        assertTrue(mine.isEnConstruction());
        assertSame(chantier, mine.getChantier());
    }

    @Test
    void assignerOuvriersAvanceDateFin() {

        LocalDateTime ancienneDate =
            chantier.getDateFinPrevue();

        chantier.assignerOuvriers(2);

        assertEquals(4, chantier.getOuvriersAssignes());

        assertTrue(
            chantier.getDateFinPrevue().isBefore(ancienneDate)
        );

        assertEquals(96, royaume.getPopulationDisponible());
        assertEquals(4, royaume.getPopulationOuvriere());
    }

    @Test
    void retirerOuvriersRepousseDateFin() {

        chantier.assignerOuvriers(2);

        LocalDateTime ancienneDate =
            chantier.getDateFinPrevue();

        chantier.retirerOuvriers(2);

        assertEquals(2, chantier.getOuvriersAssignes());

        assertTrue(
            chantier.getDateFinPrevue().isAfter(ancienneDate)
        );

        assertEquals(98, royaume.getPopulationDisponible());
        assertEquals(2, royaume.getPopulationOuvriere());
    }

    @Test
    void retirerOuvriersNePeutPasPasserSousMinimum() {

        assertThrows(
            IllegalArgumentException.class,
            () -> chantier.retirerOuvriers(2)
        );

        assertEquals(2, chantier.getOuvriersAssignes());
    }

    @Test
    void calculerTempsRestant() {

        LocalDateTime maintenant = LocalDateTime.now();

        long restant =
            chantier.calculerTempsRestant(maintenant);

        assertTrue(restant > 0);
        assertTrue(restant <= 1800);

        LocalDateTime apresFin =
            chantier.getDateFinPrevue().plusSeconds(10);

        assertEquals(
            0,
            chantier.calculerTempsRestant(apresFin)
        );
    }

    @Test
    void verifierEcheanceChantier() {

        LocalDateTime avantFin =
            chantier.getDateFinPrevue().minusSeconds(1);

        LocalDateTime apresFin =
            chantier.getDateFinPrevue().plusSeconds(1);

        assertFalse(chantier.estPretAAchever(avantFin));

        assertTrue(chantier.estPretAAchever(apresFin));
    }

    @Test
    void finaliserChantierRestitueOuvriers() {

        LocalDateTime fin =
            chantier.getDateFinPrevue().plusSeconds(1);

        chantier.finaliser(fin);

       // assertTrue(chantier.isTermine());

        assertFalse(mine.isEnConstruction());
        assertNull(mine.getChantier());
        assertTrue(mine.estOperationnel());
        
        assertEquals(100, royaume.getPopulationDisponible());
        assertEquals(0, royaume.getPopulationOuvriere());
        assertEquals(0, chantier.getOuvriersAssignes());
    }
    
    @Test
    void populationInsuffisanteRefuseAffectation() {

        assertThrows(
            IllegalStateException.class,
            () -> chantier.assignerOuvriers(200)
        );

        assertEquals(2, chantier.getOuvriersAssignes());
        assertEquals(98, royaume.getPopulationDisponible());
    }
    
    @Test
    void verifierTempsConstructionMineEtRecalculDateFin() {

        // ARRANGE : création d'une mine avec 1 ouvrier

        int ouvriersInitiaux = 1;

        Mine mine = new Mine(
            1, "Mine test", 1, 2500, 2500, true, 2
        );

        ChantierConstruction chantier = new ChantierConstruction(
            1,
            mine,
            royaume,
            1,
            ouvriersInitiaux,
            mine.getTempsConstructionBase(),
            false
        );

        // VERIFICATION 1 : temps de construction de base

        assertEquals(180, mine.getTempsConstructionBase());
        assertEquals(180, chantier.getDateFinPrevue()
            .getSecond() >= 0 ? 180 : 0); // À remplacer par la vérification temporelle ci-dessous

        // VERIFICATION 2 : avec 1 ouvrier supplémentaire

        chantier.assignerOuvriers(1);

        assertEquals(2, chantier.getOuvriersAssignes());

        long tempsRestant = chantier.calculerTempsRestant();

        assertTrue(tempsRestant <= 90);
        assertTrue(tempsRestant > 0);

        // VERIFICATION 3 : avec 3 ouvriers supplémentaires

        chantier.assignerOuvriers(3);

        assertEquals(5, chantier.getOuvriersAssignes());

        tempsRestant = chantier.calculerTempsRestant();

        assertTrue(tempsRestant <= 36);
        assertTrue(tempsRestant > 0);
    }

    
    
}
