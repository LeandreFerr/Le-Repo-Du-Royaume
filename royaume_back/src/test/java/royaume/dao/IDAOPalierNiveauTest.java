package royaume.dao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import royaume.model.PalierNiveau;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;

@DataJpaTest
@DisplayName("Tests d'intégration JPA — PalierNiveauIDAO")
class IDAOPalierNiveauTest {

    @Autowired
    private PalierNiveauIDAO palierNiveauIDAO;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Enregistre et relit un PalierNiveau avec ses prérequis JSON")
    void testSauvegardeEtRelecturePalierAvecPrerequisJson() {
        PalierNiveau palier = new PalierNiveau(
                null,
                2,
                150,
                List.of(
                        new QuantiteRessource(100, Ressource.BOIS),
                        new QuantiteRessource(50, Ressource.PIERRE)
                )
        );

        PalierNiveau sauvegarde = palierNiveauIDAO.saveAndFlush(palier);
        assertNotNull(sauvegarde.getId());

        entityManager.clear();

        PalierNiveau relu = palierNiveauIDAO.findById(sauvegarde.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(sauvegarde.getId(), relu.getId()),
                () -> assertEquals(2, relu.getNiveau()),
                () -> assertEquals(150, relu.getPopulationRequise()),
                () -> assertNotNull(relu.getPrerequis()),
                () -> assertEquals(2, relu.getPrerequis().size()),
                () -> assertTrue(relu.getPrerequis().contains(new QuantiteRessource(100, Ressource.BOIS))),
                () -> assertTrue(relu.getPrerequis().contains(new QuantiteRessource(50, Ressource.PIERRE)))
        );
    }

    @Test
    @DisplayName("findByNiveau retrouve le palier demandé ou retourne empty")
    void testFindByNiveau() {
        PalierNiveau palier = new PalierNiveau(
                null,
                3,
                250,
                List.of(new QuantiteRessource(200, Ressource.OR))
        );
        palierNiveauIDAO.saveAndFlush(palier);
        entityManager.clear();

        Optional<PalierNiveau> trouve = palierNiveauIDAO.findByNiveau(3);
        Optional<PalierNiveau> absent = palierNiveauIDAO.findByNiveau(9);

        assertTrue(trouve.isPresent());
        assertEquals(3, trouve.get().getNiveau());
        assertEquals(250, trouve.get().getPopulationRequise());
        assertTrue(absent.isEmpty());
    }

    @Test
    @DisplayName("findAllByOrderByNiveauAsc retourne les paliers triés par niveau croissant")
    void testFindAllByOrderByNiveauAsc() {
        palierNiveauIDAO.save(new PalierNiveau(null, 5, 500, List.of()));
        palierNiveauIDAO.save(new PalierNiveau(null, 2, 150, List.of()));
        palierNiveauIDAO.save(new PalierNiveau(null, 4, 350, List.of()));
        palierNiveauIDAO.flush();
        entityManager.clear();

        List<PalierNiveau> paliers = palierNiveauIDAO.findAllByOrderByNiveauAsc();

        assertEquals(List.of(2, 4, 5), paliers.stream().map(PalierNiveau::getNiveau).toList());
    }
}
