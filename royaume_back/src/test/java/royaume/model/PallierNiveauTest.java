
package royaume.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import royaume.config.ConfigurationPalliers;

public class PallierNiveauTest {

    private Royaume royaume;
    private List<PallierNiveau> paliers;

    @BeforeEach
    void setUp() {

        paliers = ConfigurationPalliers.getPaliers();

        List<QuantiteRessource> ressources = new ArrayList<>();

        ressources.add(new QuantiteRessource(1000, Ressource.OR));
        ressources.add(new QuantiteRessource(1000, Ressource.PIERRE));
        ressources.add(new QuantiteRessource(1000, Ressource.BOIS));
        ressources.add(new QuantiteRessource(7000, Ressource.NOURRITURE));

        royaume = new Royaume(
            1, "Gondor", 1,
            500, 500, 0, 100,
            false, ressources
        );
    }

    // -------------------- Configuration des paliers --------------------

    @Test
    @DisplayName("La configuration contient 9 paliers")
    void testConfigurationPaliers() {

        assertNotNull(paliers);
        assertEquals(9, paliers.size());
    }

    
    
    @Test
    @DisplayName("Les paliers permettent de passer du niveau 1 au niveau 10")
    void testNiveauxPaliers2() {

        for (int i = 0; i < paliers.size(); i++) {
            assertEquals(
                i + 2, //  indice 0 dans la liste palliers c'est conditions-passer-au-niveau-2
                paliers.get(i).getNiveau(),
                "Le palier doit correspondre au niveau " + (i + 2)
            );
        }

        assertEquals(9, paliers.size());
    }

    @Test
    @DisplayName("La liste des paliers est non modifiable")
    void testListePaliersImmuable() {

        assertThrows(
            UnsupportedOperationException.class,
            () -> paliers.add(null)
        );
    }

    // -------------------- Gestion des ressources --------------------

    @Test
    @DisplayName("aAssezDeRessources retourne true si le stock est suffisant")
    void testAssezDeRessources() {

        assertTrue(royaume.aAssezDeRessources(Ressource.OR, 500));
    }

    @Test
    @DisplayName("aAssezDeRessources retourne false si le stock est insuffisant")
    void testRessourcesInsuffisantes() {

        assertFalse(royaume.aAssezDeRessources(Ressource.OR, 1500));
    }

    @Test
    @DisplayName("aAssezDeRessources retourne false si la ressource est absente")
    void testRessourceAbsente() {

        royaume.getRessources().removeIf(
            r -> r.getRessource() == Ressource.OR
        );

        assertFalse(royaume.aAssezDeRessources(Ressource.OR, 100));
    }

    @Test
    @DisplayName("crediterRessources augmente le stock")
    void testCrediterRessources() {

        royaume.crediterRessources(Ressource.OR, 500);

        assertEquals(1500, royaume.getRessources().get(0).getQuantite());
    }

    @Test
    @DisplayName("debiterRessources retire correctement les ressources")
    void testDebiterRessources() {

        boolean resultat = royaume.debiterRessources(Ressource.OR, 300);

        assertTrue(resultat);
        assertEquals(700, royaume.getRessources().get(0).getQuantite());
    }

    @Test
    @DisplayName("debiterRessources refuse un débit supérieur au stock")
    void testDebiterRessourcesInsuffisantes() {

        boolean resultat = royaume.debiterRessources(Ressource.OR, 2000);

        assertFalse(resultat);
        assertEquals(1000, royaume.getRessources().get(0).getQuantite());
    }

    // -------------------- Coût des ressources --------------------

    @Test
    @DisplayName("getCoutRessource retourne les coûts du premier palier")
    void testCoutRessourcesPremierPalier() {

        assertEquals(100,
            PallierNiveau.getCoutRessource(Ressource.OR, royaume, paliers));

        assertEquals(80,
            PallierNiveau.getCoutRessource(Ressource.PIERRE, royaume, paliers));

        assertEquals(60,
            PallierNiveau.getCoutRessource(Ressource.BOIS, royaume, paliers));

        assertEquals(90,
            PallierNiveau.getCoutRessource(Ressource.NOURRITURE, royaume, paliers));
    }

    @Test
    @DisplayName("getCoutRessource retourne les coûts du deuxième palier")
    void testCoutRessourcesDeuxiemePalier() {

        royaume.setNiveau(2);

        assertEquals(120,
            PallierNiveau.getCoutRessource(Ressource.OR, royaume, paliers));

        assertEquals(100,
            PallierNiveau.getCoutRessource(Ressource.PIERRE, royaume, paliers));

        assertEquals(100,
            PallierNiveau.getCoutRessource(Ressource.BOIS, royaume, paliers));

        assertEquals(1300,
            PallierNiveau.getCoutRessource(Ressource.NOURRITURE, royaume, paliers));
    }

    @Test
    @DisplayName("getCoutRessource retourne 0 au niveau maximum")
    void testCoutRessourceNiveauMaximum() {

        royaume.setNiveau(10);

        assertEquals(0,
            PallierNiveau.getCoutRessource(Ressource.OR, royaume, paliers));
    }

    @Test
    @DisplayName("getCoutRessource retourne 0 si la ressource est absente du palier")
    void testCoutRessourceAbsente() {

        List<PallierNiveau> palierPersonnalise = List.of(
            new PallierNiveau(
                1, 2, 100,
                List.of(new QuantiteRessource(100, Ressource.OR))
            )
        );

        assertEquals(0,
            PallierNiveau.getCoutRessource(
                Ressource.BOIS, royaume, palierPersonnalise
            ));
    }

    // -------------------- Ressources manquantes --------------------

    @Test
    @DisplayName("getRessourceManquante retourne 0 si le stock est suffisant")
    void testAucuneRessourceManquante() {

        assertEquals(0,
            PallierNiveau.getRessourceManquante(Ressource.OR, royaume, paliers));

        assertEquals(0,
            PallierNiveau.getRessourceManquante(Ressource.PIERRE, royaume, paliers));
    }

    @Test
    @DisplayName("getRessourceManquante calcule la quantité manquante")
    void testRessourcesManquantes() {

        royaume.getRessources().get(0).setQuantite(50);

        int manqueOr = PallierNiveau.getRessourceManquante(
            Ressource.OR, royaume, paliers
        );

        assertEquals(50, manqueOr);
    }

    @Test
    @DisplayName("getRessourceManquante retourne le coût total si le stock est nul")
    void testRessourceManquanteStockNul() {

        royaume.getRessources().removeIf(
            r -> r.getRessource() == Ressource.OR
        );

        assertEquals(100,
            PallierNiveau.getRessourceManquante(
                Ressource.OR, royaume, paliers
            ));
    }

    // -------------------- Conditions de montée de niveau --------------------

    @Test
    @DisplayName("peutMonterDeNiveau retourne true si toutes les conditions sont réunies")
    void testPeutMonterDeNiveau() {

        assertTrue(royaume.peutMonterDeNiveau(paliers));
    }

    @Test
    @DisplayName("peutMonterDeNiveau retourne false si la population est insuffisante")
    void testPopulationInsuffisante() {

        royaume.setPopulationDisponible(10);
        royaume.setPopulationOuvriere(10);
        royaume.setPopulationMilitaire(0);
        royaume.setPopulationEnEntrainement(0);

        assertFalse(royaume.peutMonterDeNiveau(paliers));
    }

    @Test
    @DisplayName("peutMonterDeNiveau retourne false si une ressource est insuffisante")
    void testRessourceInsuffisantePourMonter() {

        royaume.getRessources().get(0).setQuantite(10);

        assertFalse(royaume.peutMonterDeNiveau(paliers));
    }

    @Test
    @DisplayName("peutMonterDeNiveau retourne false au niveau maximum")
    void testPeutMonterNiveauMaximum() {

        royaume.setNiveau(10);

        assertFalse(royaume.peutMonterDeNiveau(paliers));
    }

    @Test
    @DisplayName("peutMonterDeNiveau retourne false si aucun palier suivant existe")
    void testPalierSuivantAbsent() {

        assertFalse(
            royaume.peutMonterDeNiveau(new ArrayList<>())
        );
    }

    // -------------------- Amélioration du niveau --------------------

    @Test
    @DisplayName("ameliorerNiveau fait passer le royaume du niveau 1 au niveau 2")
    void testAmeliorerNiveau() {

        royaume.ameliorerNiveau(paliers);

        assertEquals(2, royaume.getNiveau());
    }

    @Test
    @DisplayName("ameliorerNiveau débite les ressources nécessaires")
    void testAmeliorerNiveauDebiteRessources() {

        royaume.ameliorerNiveau(paliers);

        assertEquals(900, royaume.getRessources().get(0).getQuantite());
        assertEquals(920, royaume.getRessources().get(1).getQuantite());
        assertEquals(940, royaume.getRessources().get(2).getQuantite());
        assertEquals(6910, royaume.getRessources().get(3).getQuantite());
    }

    @Test
    @DisplayName("ameliorerNiveau ne modifie rien si les conditions ne sont pas réunies")
    void testAmeliorerNiveauRefuse() {

        royaume.getRessources().get(0).setQuantite(10);

        royaume.ameliorerNiveau(paliers);

        assertEquals(1, royaume.getNiveau());
        assertEquals(10, royaume.getRessources().get(0).getQuantite());
    }

    @Test
    @DisplayName("Un royaume évolue successivement jusqu'à ne plus remplir les conditions")
    void testEvolutionSuccessive() {

        while (royaume.peutMonterDeNiveau(paliers)) {
            royaume.ameliorerNiveau(paliers);
        }

        assertTrue(royaume.getNiveau() <= 10);
        assertFalse(royaume.peutMonterDeNiveau(paliers));
    }
    
    //______________________ VERIF bonne quantité du bon type ____________
    

@Test
@DisplayName("ameliorerNiveau débite les bonnes quantités de chaque type indépendent de ressource")
void testAmeliorerNiveauDebiteBonnesRessources() {

    // Given : stocks initiaux
    int orInitial = 1000;
    int pierreInitiale = 1000;
    int boisInitial = 1000;
    int nourritureInitiale = 7000;

    // Coûts du palier niveau 2
    int coutOr = 100;
    int coutPierre = 80;
    int coutBois = 60;
    int coutNourriture = 90;

    // When
    royaume.ameliorerNiveau(paliers);

    // Then : vérification de chaque stock
    assertEquals(orInitial - coutOr,
        royaume.getRessources().stream()
            .filter(r -> r.getRessource() == Ressource.OR)
            .findFirst()
            .get()
            .getQuantite());

    assertEquals(pierreInitiale - coutPierre,
        royaume.getRessources().stream()
            .filter(r -> r.getRessource() == Ressource.PIERRE)
            .findFirst()
            .get()
            .getQuantite());

    assertEquals(boisInitial - coutBois,
        royaume.getRessources().stream()
            .filter(r -> r.getRessource() == Ressource.BOIS)
            .findFirst()
            .get()
            .getQuantite());

    assertEquals(nourritureInitiale - coutNourriture,
        royaume.getRessources().stream()
            .filter(r -> r.getRessource() == Ressource.NOURRITURE)
            .findFirst()
            .get()
            .getQuantite());

    // Vérification du niveau atteint
    assertEquals(2, royaume.getNiveau());
}

}
