package royaume.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import royaume.model.Carte;
import royaume.model.EmplacementCarte;

@DataJpaTest
class IDAOEmplacementCarteTest {

    @Autowired
    private IDAOEmplacementCarte daoEmplacementCarte;

    @Autowired
    private IDAOCarte daoCarte;

    @Test
    void testPersistanceEmplacementCarte() {
        // Given
        Carte carte = new Carte();

        Carte carteSauvegardee = daoCarte.saveAndFlush(carte);

        EmplacementCarte emplacement =
                new EmplacementCarte(
                        1,
                        25,
                        50,
                        carteSauvegardee);

        // When
        EmplacementCarte sauvegarde = daoEmplacementCarte.saveAndFlush(emplacement);

        // Then
        assertNotNull(sauvegarde.getId());

        Optional<EmplacementCarte> resultat = daoEmplacementCarte.findById(sauvegarde.getId());

        assertTrue(resultat.isPresent());
        assertEquals(1, resultat.get().getNumero());
        assertEquals(25, resultat.get().getCoordX());
        assertEquals(50, resultat.get().getCoordY());
        assertFalse(resultat.get().isOccupe());
    }

    @Test
    void testPersistanceEmplacementAvecCarte() {
        // Given
        Carte carte = new Carte();

        Carte carteSauvegardee = daoCarte.saveAndFlush(carte);

        EmplacementCarte emplacement =
                new EmplacementCarte(
                        1,
                        25,
                        50,
                        carteSauvegardee);

        // When
        EmplacementCarte sauvegarde = daoEmplacementCarte.saveAndFlush(emplacement);

        // Then
        Optional<EmplacementCarte> resultat = daoEmplacementCarte.findById(sauvegarde.getId());

        assertTrue(resultat.isPresent());
        assertNotNull(resultat.get().getCarte());

        assertEquals(carteSauvegardee.getId(), resultat.get().getCarte().getId());
    }

    @Test
    void testPersistanceEmplacementGenereParCarte() {
        // Given
        Carte carte = new Carte();

        carte.genererEmplacements(1);

        // When
        Carte carteSauvegardee = daoCarte.saveAndFlush(carte);

        EmplacementCarte emplacement = carteSauvegardee.getEmplacements().get(0);

        // Then
        assertNotNull(carteSauvegardee.getId());
        assertNotNull(emplacement.getId());

        Optional<EmplacementCarte> resultat = daoEmplacementCarte.findById(emplacement.getId());

        assertTrue(resultat.isPresent());
        assertEquals(1, resultat.get().getNumero());
        assertEquals(50, resultat.get().getCoordX());
        assertEquals(10, resultat.get().getCoordY());

        assertEquals(carteSauvegardee.getId(), resultat.get().getCarte().getId());
    }

    @Test
    void testModificationEmplacementCarte() {
        // Given
        Carte carte = new Carte();

        Carte carteSauvegardee = daoCarte.saveAndFlush(carte);

        EmplacementCarte emplacement =
                new EmplacementCarte(
                        1,
                        25,
                        50,
                        carteSauvegardee);

        EmplacementCarte sauvegarde = daoEmplacementCarte.saveAndFlush(emplacement);

        Integer idEmplacement = sauvegarde.getId();

        // When
        sauvegarde.setNumero(5);
        sauvegarde.setCoordX(77);
        sauvegarde.setCoordY(88);

        daoEmplacementCarte.saveAndFlush(sauvegarde);

        // Then
        Optional<EmplacementCarte> resultat = daoEmplacementCarte.findById(idEmplacement);

        assertTrue(resultat.isPresent());
        assertEquals(5, resultat.get().getNumero());
        assertEquals(77, resultat.get().getCoordX());
        assertEquals(88, resultat.get().getCoordY());
    }

    @Test
    void testSuppressionEmplacementCarte() {
        // Given
        Carte carte = new Carte();

        Carte carteSauvegardee = daoCarte.saveAndFlush(carte);

        EmplacementCarte emplacement =
                new EmplacementCarte(
                        1,
                        25,
                        50,
                        carteSauvegardee);

        EmplacementCarte sauvegarde = daoEmplacementCarte.saveAndFlush(emplacement);

        Integer idEmplacement = sauvegarde.getId();

        // When
        daoEmplacementCarte.delete(sauvegarde);
        daoEmplacementCarte.flush();

        // Then
        Optional<EmplacementCarte> resultat = daoEmplacementCarte.findById(idEmplacement);

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testCoordonneesConserveesApresRechargement() {
        // Given
        Carte carte = new Carte();

        Carte carteSauvegardee = daoCarte.saveAndFlush(carte);

        EmplacementCarte emplacement =
                new EmplacementCarte(
                        4,
                        80,
                        20,
                        carteSauvegardee);

        EmplacementCarte sauvegarde = daoEmplacementCarte.saveAndFlush(emplacement);

        Integer idEmplacement = sauvegarde.getId();

        // When
        Optional<EmplacementCarte> resultat = daoEmplacementCarte.findById(idEmplacement);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals(4, resultat.get().getNumero());
        assertEquals(80, resultat.get().getCoordX());
        assertEquals(20, resultat.get().getCoordY());
    }

    @Test
    void testEtatLibreConserveApresPersistance() {
        // Given
        Carte carte = new Carte();

        Carte carteSauvegardee = daoCarte.saveAndFlush(carte);

        EmplacementCarte emplacement =
                new EmplacementCarte(
                        1,
                        25,
                        50,
                        carteSauvegardee);

        // When
        EmplacementCarte sauvegarde = daoEmplacementCarte.saveAndFlush(emplacement);

        // Then
        Optional<EmplacementCarte> resultat = daoEmplacementCarte.findById(sauvegarde.getId());

        assertTrue(resultat.isPresent());
        assertFalse(resultat.get().isOccupe());
    }

    @Test
    void testPlusieursEmplacementsPourUneCarte() {
        // Given
        Carte carte = new Carte();

        carte.genererEmplacements(3);

        // When
        Carte carteSauvegardee = daoCarte.saveAndFlush(carte);

        // Then
        assertEquals(3, carteSauvegardee.getEmplacements().size());

        for (EmplacementCarte emplacement : carteSauvegardee.getEmplacements()) {

            assertNotNull(emplacement.getId());

            assertEquals(carteSauvegardee.getId(), emplacement.getCarte().getId());

            assertTrue(daoEmplacementCarte.existsById(emplacement.getId()));
        }
    }
}