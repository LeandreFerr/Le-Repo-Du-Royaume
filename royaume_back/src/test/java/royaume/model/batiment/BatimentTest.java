package royaume.model.batiment;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;


// C'est une classe concrète créée uniquement pour les tests (sinon abstraite !!!! )

class BatimentTest {

    private Batiment batiment;
    private Royaume royaume;

    
    static class BatimentTestable extends Batiment {

        BatimentTestable() {
            super(1, "Batiment test", 1, 50, 50, false);
        }

        @Override
        public List<QuantiteRessource> getCoutAmelioration() {
            return List.of();
        }

        @Override
        public int getTempsConstructionBase() {
            return 60;
        }
    }

    @BeforeEach
    void setUp() {
        batiment = new BatimentTestable();
        royaume = new Royaume(2, "Test");
        Chateau chateau = royaume.getChateau();
        if (chateau != null) {
            chateau.ameliorer(); // Monte le château au niveau 2 pour débloquer le palier 2
        }
    }

    @Test
    void subirDegats_DiminueCorrectementLesPV() {

        batiment.subirDegats(20);

        assertEquals(30, batiment.getPointsDeVie());
    }

    @Test
    void subirDegats_NeDoitPasDescendreSousZero() {

        batiment.subirDegats(100);

        assertEquals(0, batiment.getPointsDeVie());
    }

    /*@Test
    void reparer_AugmenteLesPV() {

        batiment.subirDegats(30);

        batiment.reparer(20);

        assertEquals(40, batiment.getPointsDeVie());
    }

    @Test
    void reparer_NeDoitPasDepasserLesPVMax() {

        batiment.reparer(100);

        assertEquals(100, batiment.getPointsDeVie());
    }*/
    
    @Test
    void reparer_LanceLeChantierSansModifierInstantaneDesPV() {
        batiment.subirDegats(30); // Disons qu'il passe de 50 à 20 PV
        int pvAvant = batiment.getPointsDeVie();

        // On lance la réparation (ex: avec 2 ouvriers et le royaume)
        batiment.reparer(30,2, royaume);

        // Les PV ne doivent PAS avoir changé immédiatement
        assertEquals(pvAvant, batiment.getPointsDeVie());
        // Mais le bâtiment est bien en construction
        assertTrue(batiment.isEnConstruction());
        assertNotNull(batiment.getChantier());
    }

   /* @Test
    void reparer_AugmenteLesPV_ApresFinalisation() {
        batiment.subirDegats(30); // 20 PV restants

        batiment.reparer(30,2, royaume);

        // On simule que le temps est écoulé en trichant un peu sur la date de fin prévue du chantier,
        // ou en appelant directement finaliser avec une date future pour les tests unitaires :
        ChantierConstruction chantier = batiment.getChantier();
        LocalDateTime futur = chantier.getDateFinPrevue().plusSeconds(1);

        // Si vous avez une méthode pour appliquer la réparation à la fin (ou via finaliser) :
        // On simule la finalisation qui restitue les PV :
        chantier.finaliser(futur);
        
        // Supposons que finaliser remette au max (ou applique le montant prévu) :
        assertEquals(batiment.getPointsDeVieMax(), batiment.getPointsDeVie());
        assertFalse(batiment.isEnConstruction());
    }*/
    
    @Test
    void reparer_AugmenteLesPV_ApresFinalisation() {
        batiment.subirDegats(30); // 20 PV restants

        batiment.reparer(30, 2, royaume);

        ChantierConstruction chantier = batiment.getChantier();
        LocalDateTime futur = chantier.getDateFinPrevue().plusSeconds(1);

        // Finalise le chantier (ce qui déclenche batiment.verifierEtFinaliserReparation())
        chantier.finaliser(futur);
        
        // Assertions
        assertEquals(batiment.getPointsDeVieMax(), batiment.getPointsDeVie()); // Doit donner 50
        assertFalse(batiment.isEnConstruction());
    }

    @Test
    void reparer_NeDoitPasDepasserLesPVMax() {
        // Même si le bâtiment n'a presque rien perdu, la réparation/finalisation 
        // ne doit jamais dépasser le plafond des points de vie max
        batiment.subirDegats(5); // Il lui manque juste 5 PV

        batiment.reparer(10,2, royaume);

        ChantierConstruction chantier = batiment.getChantier();
        LocalDateTime futur = chantier.getDateFinPrevue().plusSeconds(1);

        chantier.finaliser(futur);

        assertEquals(batiment.getPointsDeVieMax(), batiment.getPointsDeVie());
    }

    @Test
    void estOperationnel_RetourneTrueSiBatimentEnBonEtat() {

        assertTrue(batiment.estOperationnel());
    }

    @Test
    void estOperationnel_RetourneFalseSiBatimentDetruit() {

        batiment.subirDegats(50);

        assertFalse(batiment.estOperationnel());
    }

    @Test
    void estOperationnel_RetourneFalseSiEnConstruction() {

        batiment.setEnConstruction(true);

        assertFalse(batiment.estOperationnel());
    }

    @Test
    void ameliorer_IncrementeLeNiveauEtLesPVMax() {

        batiment.ameliorer();

        assertEquals(2, batiment.getNiveau());
        assertEquals(100, batiment.getPointsDeVieMax());
    }
    
    
    @Test
    void getCoutAmelioration_Carriere_RetourneCoutsConfig() {
    	Carriere carriere = new Carriere(1, "Carrière", 1, 50, 100, false, 30);
    	List<QuantiteRessource> couts = carriere.getCoutAmelioration();
    	assertNotNull(couts);
        assertFalse(couts.isEmpty(), "La carrière de niveau 1 doit avoir un coût pour passer au niveau 2");
        boolean contientBois = couts.stream()
                .anyMatch(r -> r.getRessource() == Ressource.BOIS && r.getQuantite() == 80);
        assertTrue(contientBois, "Le coût pour le niveau 2 de la carrière doit inclure 80 de bois");
    	
    }
    
    void getTempsConstruction() {
    	Carriere carriere = new Carriere(1, "Carrière", 1, 50, 100, false, 30);
    	int temps= carriere.getTempsConstructionBase();
    	assertEquals(180,temps);
    	
    }
}
