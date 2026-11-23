package royaume.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import royaume.dao.IDAOCarte;
import royaume.dao.IDAOEmplacementCarte;
import royaume.model.Carte;
import royaume.model.EmplacementCarte;

@DataJpaTest
class IDAOCarteTest {

    @Autowired
    private IDAOCarte daoCarte;

    @Autowired
    private IDAOEmplacementCarte daoEmplacementCarte;

    @Test
    void testPersistanceCarte() {
        // Given
        Carte carte = new Carte();

        carte.setLargeur(200);
        carte.setHauteur(120);
        carte.setCentreX(100);
        carte.setCentreY(60);

        // When
        Carte sauvegardee = daoCarte.saveAndFlush(carte);

        // Then
        assertNotNull(sauvegardee.getId());

        Optional<Carte> resultat = daoCarte.findById(sauvegardee.getId());

        assertTrue(resultat.isPresent());
        assertEquals(200, resultat.get().getLargeur());
        assertEquals(120, resultat.get().getHauteur());
        assertEquals(100, resultat.get().getCentreX());
        assertEquals(60, resultat.get().getCentreY());
        assertNull(resultat.get().getPartie());
    }

    @Test
    void testPersistanceCarteAvecValeursParDefaut() {
        // Given
        Carte carte = new Carte();

        // When
        Carte sauvegardee = daoCarte.saveAndFlush(carte);

        // Then
        assertNotNull(sauvegardee.getId());

        Optional<Carte> resultat = daoCarte.findById(sauvegardee.getId());

        assertTrue(resultat.isPresent());
        assertEquals(100, resultat.get().getLargeur());
        assertEquals(100, resultat.get().getHauteur());
        assertEquals(50, resultat.get().getCentreX());
        assertEquals(50, resultat.get().getCentreY());
        assertTrue(resultat.get().getEmplacements().isEmpty());
    }

    @Test
    void testModificationCarte() {
        // Given
        Carte carte = new Carte();
        Carte sauvegardee = daoCarte.saveAndFlush(carte);
        Integer idCarte = sauvegardee.getId();

        // When
        sauvegardee.setLargeur(300);
        sauvegardee.setHauteur(180);
        sauvegardee.setCentreX(150);
        sauvegardee.setCentreY(90);

        daoCarte.saveAndFlush(sauvegardee);

        // Then
        Optional<Carte> resultat = daoCarte.findById(idCarte);

        assertTrue(resultat.isPresent());
        assertEquals(300, resultat.get().getLargeur());
        assertEquals(180, resultat.get().getHauteur());
        assertEquals(150, resultat.get().getCentreX());
        assertEquals(90, resultat.get().getCentreY());
    }

    @Test
    void testSuppressionCarte() {
        // Given
        Carte carte = new Carte();
        Carte sauvegardee = daoCarte.saveAndFlush(carte);

        Integer idCarte = sauvegardee.getId();

        // When
        daoCarte.delete(sauvegardee);
        daoCarte.flush();

        // Then
        Optional<Carte> resultat = daoCarte.findById(idCarte);

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testPersistanceDesEmplacementsEnCascade() {
        // Given
        Carte carte = new Carte();

        carte.genererEmplacements(3);

        // When
        Carte sauvegardee = daoCarte.saveAndFlush(carte);

        // Then
        assertNotNull(sauvegardee.getId());
        assertEquals(3, sauvegardee.getEmplacements().size());

        for (EmplacementCarte emplacement : sauvegardee.getEmplacements()) {

            assertNotNull(emplacement.getId());
            assertSame(sauvegardee, emplacement.getCarte());
        }

        assertEquals(3, daoEmplacementCarte.count());
    }

    @Test
    void testRechargementCarteAvecEmplacements() {
        // Given
        Carte carte = new Carte();

        carte.genererEmplacements(3);

        Carte sauvegardee = daoCarte.saveAndFlush(carte);

        Integer idCarte = sauvegardee.getId();

        // When
        Optional<Carte> resultat =
                daoCarte.findById(idCarte);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals(
                3,
                resultat.get().getEmplacements().size());

        for (EmplacementCarte emplacement :
                resultat.get().getEmplacements()) {

            assertNotNull(emplacement.getId());
            assertNotNull(emplacement.getCarte());

            assertEquals(
                    idCarte,
                    emplacement.getCarte().getId());
        }
    }

    @Test
    void testSuppressionEmplacementAvecOrphanRemoval() {
        // Given
        Carte carte = new Carte();

        carte.genererEmplacements(3);

        Carte sauvegardee =
                daoCarte.saveAndFlush(carte);

        EmplacementCarte emplacementSupprime =
                sauvegardee.getEmplacements().get(0);

        Integer idEmplacement =
                emplacementSupprime.getId();

        // When
        sauvegardee.supprimerEmplacement(
                emplacementSupprime);

        daoCarte.saveAndFlush(sauvegardee);

        // Then
        Optional<EmplacementCarte> resultat = daoEmplacementCarte.findById(idEmplacement);

        assertTrue(resultat.isEmpty());

        Optional<Carte> carteRechargee = daoCarte.findById(sauvegardee.getId());

        assertTrue(carteRechargee.isPresent());

        assertEquals(2, carteRechargee.get().getEmplacements().size());
    }

    @Test
    void testRegenerationEmplacementsAvecOrphanRemoval() {
        // Given
        Carte carte = new Carte();

        carte.genererEmplacements(3);

        Carte sauvegardee = daoCarte.saveAndFlush(carte);

        Integer ancienId = sauvegardee.getEmplacements().get(0).getId();

        // When
        sauvegardee.genererEmplacements(2);

        daoCarte.saveAndFlush(sauvegardee);

        // Then
        Optional<EmplacementCarte> ancienEmplacement = daoEmplacementCarte.findById(ancienId);

        assertTrue(ancienEmplacement.isEmpty());

        Optional<Carte> carteRechargee = daoCarte.findById(sauvegardee.getId());

        assertTrue(carteRechargee.isPresent());

        assertEquals(2, carteRechargee.get().getEmplacements().size());

        for (EmplacementCarte emplacement : carteRechargee.get().getEmplacements()) {

            assertNotNull(emplacement.getId());
        }
    }

    @Test
    void testSuppressionCarteSupprimeSesEmplacementsEnCascade() {
        // Given
        Carte carte = new Carte();

        carte.genererEmplacements(3);

        Carte sauvegardee = daoCarte.saveAndFlush(carte);

        Integer idCarte = sauvegardee.getId();

        Integer idPremierEmplacement = sauvegardee.getEmplacements().get(0).getId();

        Integer idDeuxiemeEmplacement = sauvegardee.getEmplacements().get(1).getId();

        Integer idTroisiemeEmplacement = sauvegardee.getEmplacements().get(2).getId();

        // When
        daoCarte.delete(sauvegardee);
        daoCarte.flush();

        // Then
        assertFalse(daoCarte.existsById(idCarte));

        assertFalse(daoEmplacementCarte.existsById(idPremierEmplacement));

        assertFalse(daoEmplacementCarte.existsById(idDeuxiemeEmplacement));

        assertFalse(daoEmplacementCarte.existsById(idTroisiemeEmplacement));
    }

    @Test
    void testEmplacementMarcheNonPersiste() {
        // Given
        Carte carte = new Carte();

        carte.genererEmplacements(3);

        // When
        Carte sauvegardee = daoCarte.saveAndFlush(carte);

        // Then
        assertNull(sauvegardee.getEmplacementMarche().getId());

        assertFalse(sauvegardee.getEmplacements().contains(sauvegardee.getEmplacementMarche()));

        assertEquals(3,daoEmplacementCarte.count());
    }
}
