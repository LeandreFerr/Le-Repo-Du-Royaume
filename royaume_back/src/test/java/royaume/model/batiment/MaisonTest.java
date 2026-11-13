
package royaume.model.batiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

class MaisonTest {

    private Maison maison;
    private Royaume royaume;

    @BeforeEach
    void setUp() {

        royaume = new Royaume(1, "Royaume test");

        maison = new Maison(
            1,
            "Maison paysanne",
            1,
            50,
            50,
            false
        );

        royaume.ajouterBatiment(maison);

        // Initialiser le bonus de la maison
        maison.actualiserBonus(royaume);
    }

    @Test
    void getCoutAmelioration_NiveauSuivant_RetourneLesCoutsDeLaConfiguration() {

        List<QuantiteRessource> couts = maison.getCoutAmelioration();

        assertNotNull(couts);
        assertFalse(couts.isEmpty());

        boolean contientBois = couts.stream()
            .anyMatch(r ->
                r.getRessource() == Ressource.BOIS
                && r.getQuantite() == 10
            );

        assertTrue(contientBois);
    }

    @Test
    void getCoutAmelioration_NiveauMax_RetourneListeVide() {

        for (int i = maison.getNiveau(); i < 10; i++) {
            maison.ameliorer();
        }

        assertTrue(maison.getCoutAmelioration().isEmpty());
    }

    // -----------------------------------------
    // TESTS DU BONUS DE CROISSANCE
    // -----------------------------------------

    @Test
    void actualiserBonus_AjouteBonusMaisonAuRoyaume() {

        assertEquals(0.12, royaume.getBonusCroissance(), 0.0001);
    }

    @Test
    void actualiserBonus_AjouteBonusDePlusieursMaisons() {

        Maison maison2 = new Maison(
            2, "Maison 2", 1, 50, 50, false
        );

        Maison maison3 = new Maison(
            3, "Maison 3", 1, 50, 50, false
        );

        royaume.ajouterBatiment(maison2);
        royaume.ajouterBatiment(maison3);

        maison2.actualiserBonus(royaume);
        maison3.actualiserBonus(royaume);

        assertEquals(0.36, royaume.getBonusCroissance(), 0.0001);
    }

    @Test
    void actualiserBonus_NeDoitPasAjouterDeuxFoisLeMemeBonus() {

        maison.actualiserBonus(royaume);

        assertEquals(0.12, royaume.getBonusCroissance(), 0.0001);
    }

    @Test
    void actualiserBonus_AugmenteBonusApresAmeliorationMaison() {

        // Bonus initial : 0.12

        maison.ameliorer();

        maison.actualiserBonus(royaume);

        // Niveau 2 : 0.24

        assertEquals(0.24, royaume.getBonusCroissance(), 0.0001);
    }

    // -----------------------------------------
    // TESTS DE RETRAIT ET DESTRUCTION
    // -----------------------------------------

    @Test
    void retirerBatiment_RetireBonusMaisonDuRoyaume() {

        assertEquals(0.12, royaume.getBonusCroissance(), 0.0001);

        royaume.retirerBatiment(maison);

        assertEquals(0.0, royaume.getBonusCroissance(), 0.0001);

        assertFalse(royaume.getBatiments().contains(maison));
    }

    @Test
    void detruireMaison_RetireAutomatiquementSonBonus() {

        maison.subirDegats(50);

        assertEquals(0, maison.getPointsDeVie());

        assertFalse(royaume.getBatiments().contains(maison));

        assertEquals(0.0, royaume.getBonusCroissance(), 0.0001);
    }

    @Test
    void detruireUneMaison_NeRetirePasLeBonusDesAutresMaisons() {

        Maison maison2 = new Maison(
            2, "Maison 2", 1, 50, 50, false
        );

        royaume.ajouterBatiment(maison2);
        maison2.actualiserBonus(royaume);

        assertEquals(0.24, royaume.getBonusCroissance(), 0.0001);

        // Détruire uniquement la première maison
        maison.subirDegats(50);

        // Le bonus de la deuxième maison doit rester
        assertEquals(0.12, royaume.getBonusCroissance(), 0.0001);

        assertTrue(royaume.getBatiments().contains(maison2));
        assertFalse(royaume.getBatiments().contains(maison));
    }

    @Test
    void bonusCroissance_EstIndependantPourChaqueRoyaume() {

        Royaume royaume2 = new Royaume(2, "Deuxième royaume");

        Maison maison2 = new Maison(
            2, "Maison autre royaume", 1, 50, 50, false
        );

        royaume2.ajouterBatiment(maison2);
        maison2.actualiserBonus(royaume2);

        assertEquals(0.12, royaume.getBonusCroissance(), 0.0001);
        assertEquals(0.12, royaume2.getBonusCroissance(), 0.0001);

        // Détruire une maison dans le premier royaume
        maison.subirDegats(50);

        assertEquals(0.0, royaume.getBonusCroissance(), 0.0001);

        // Le deuxième royaume n'est pas affecté
        assertEquals(0.12, royaume2.getBonusCroissance(), 0.0001);
    }
}