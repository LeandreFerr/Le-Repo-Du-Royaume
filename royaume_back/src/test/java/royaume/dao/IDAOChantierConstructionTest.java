
package royaume.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import royaume.model.Licornes;
import royaume.model.Royaume;
import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.ChantierConstruction;
import royaume.model.batiment.Maison;

@DataJpaTest
@DisplayName("Tests d'intégration JPA — IDAOChantierConstruction")
class IDAOChantierConstructionTest {

    @Autowired
    private IDAOChantierConstruction dao;

    @Autowired
    private IDAOBatiment batimentDAO;
    
    @Autowired
    private EntityManager entityManager;

    // ---------------------------------------------------------
    // MÉTHODES UTILITAIRES
    // ---------------------------------------------------------

    private Royaume creerRoyaume() {

        Licornes peuple = new Licornes(
                null,
                "Licornes",
                "Reine",
                1.0
        );

        entityManager.persist(peuple);

        Royaume royaume = new Royaume();
        royaume.setNom("Royaume test");
        royaume.setPeuple(peuple);

        entityManager.persist(royaume);

        return royaume;
    }

    private ChantierConstruction creerChantier(
            Royaume royaume,
            String nomMaison) {

        // 1. Création de la maison et de son chantier via la Factory
        Maison maison = BatimentFactory.creerMaison(
                royaume,
                null,
                nomMaison,
                1
        );

        // 2. Persistance de la maison
        // La cascade ALL de Batiment persiste également son chantier.
        entityManager.persist(maison);

        entityManager.flush();

        // 3. Retourner le chantier créé par la Factory
        return maison.getChantier();
    }

    // ---------------------------------------------------------
    // SAVE
    // ---------------------------------------------------------

    @Test
    @DisplayName("save() : enregistrer un chantier")
    void testSaveChantier() {

        Royaume royaume = creerRoyaume();

        ChantierConstruction chantier =
                creerChantier(royaume, "Maison test");

        ChantierConstruction resultat = dao.save(chantier);

        entityManager.flush();

        assertThat(resultat.getId()).isNotNull();
        assertThat(resultat.getBatiment()).isNotNull();
        assertThat(resultat.getBatiment().getNom())
                .isEqualTo("Maison test");
        assertThat(resultat.getOuvriersAssignes()).isEqualTo(1);
    }

    // ---------------------------------------------------------
    // FIND BY ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("findById() : retrouver un chantier par son identifiant")
    void testFindById() {

        Royaume royaume = creerRoyaume();

        ChantierConstruction chantier =
                creerChantier(royaume, "Maison test");

        dao.save(chantier);

        entityManager.flush();

        Integer id = chantier.getId();

        entityManager.clear();

        Optional<ChantierConstruction> resultat = dao.findById(id);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getOuvriersAssignes()).isEqualTo(1);
        assertThat(resultat.get().getBatiment().getNom())
                .isEqualTo("Maison test");
    }

    // ---------------------------------------------------------
    // FIND ALL
    // ---------------------------------------------------------

    @Test
    @DisplayName("findAll() : récupérer tous les chantiers")
    void testFindAll() {

        Royaume royaume = creerRoyaume();

        ChantierConstruction chantier1 =
                creerChantier(royaume, "Premiere maison");

        ChantierConstruction chantier2 =
                creerChantier(royaume, "Deuxieme maison");

        dao.save(chantier1);
        dao.save(chantier2);

        entityManager.flush();
        entityManager.clear();

        assertThat(dao.findAll()).hasSize(2);
    }

    // ---------------------------------------------------------
    // EXISTS BY ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("existsById() : vérifier l'existence d'un chantier")
    void testExistsById() {

        Royaume royaume = creerRoyaume();

        ChantierConstruction chantier =
                creerChantier(royaume, "Maison test");

        dao.save(chantier);

        entityManager.flush();

        assertThat(dao.existsById(chantier.getId())).isTrue();
        assertThat(dao.existsById(999999)).isFalse();
    }

    // ---------------------------------------------------------
    // DELETE BY ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("Supprimer un chantier sans supprimer son bâtiment")
    void testDeleteById() {

        // Arrange
        Royaume royaume = creerRoyaume();

        Maison maison = BatimentFactory.creerMaison(
                royaume,
                null,
                "Maison test",
                1
        );

        entityManager.persist(maison);
        entityManager.flush();

        ChantierConstruction chantier = maison.getChantier();

        Integer chantierId = chantier.getId();
        Integer batimentId = maison.getId();

        assertThat(dao.existsById(chantierId)).isTrue();

        // Act : suppression par orphanRemoval
        maison.setChantier(null);

        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(dao.existsById(chantierId)).isFalse();

        assertThat(batimentDAO.existsById(batimentId)).isTrue();

        Batiment batimentRecharge = batimentDAO.findById(batimentId)
                .orElseThrow();

        assertThat(batimentRecharge.getChantier()).isNull();
    }
    @Test
    @DisplayName("Finaliser un chantier supprime le chantier mais conserve le bâtiment")
    void testFinaliserChantierSupprimeChantierMaisConserveBatiment() {

        // Arrange
        Royaume royaume = creerRoyaume();
        entityManager.persist(royaume);
        entityManager.flush();

        Maison maison = BatimentFactory.creerMaison(
                royaume,
                null,
                "Maison test",
                1
        );

        entityManager.persist(maison);
        entityManager.flush();

        ChantierConstruction chantier = maison.getChantier();

        Integer chantierId = chantier.getId();
        Integer batimentId = maison.getId();

        // On utilise une date postérieure à la fin prévue
        LocalDateTime dateFin = chantier.getDateFinPrevue().plusSeconds(1);

        // Act
        chantier.finaliser(dateFin);

        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(dao.existsById(chantierId)).isFalse();

        assertThat(batimentDAO.existsById(batimentId)).isTrue();

        Batiment batimentRecharge = batimentDAO.findById(batimentId)
                .orElseThrow();

        assertThat(batimentRecharge.getChantier()).isNull();
        assertThat(batimentRecharge.isEnConstruction()).isFalse();
    }
    // ---------------------------------------------------------
    // RELATION CHANTIER -> BATIMENT
    // ---------------------------------------------------------

    @Test
    @DisplayName("Vérifier la relation entre chantier et bâtiment")
    void testRelationChantierBatiment() {

        Royaume royaume = creerRoyaume();

        ChantierConstruction chantier =
                creerChantier(royaume, "Maison test");

        dao.save(chantier);

        entityManager.flush();

        Integer id = chantier.getId();

        entityManager.clear();

        ChantierConstruction resultat =
                dao.findById(id).orElseThrow();

        assertThat(resultat.getBatiment()).isNotNull();
        assertThat(resultat.getBatiment().getNom())
                .isEqualTo("Maison test");
    }

    // ---------------------------------------------------------
    // RELATION INVERSE BATIMENT -> CHANTIER
    // ---------------------------------------------------------

    @Test
    @DisplayName("Vérifier que le bâtiment retrouve son chantier")
    void testRelationInverseBatimentChantier() {

        Royaume royaume = creerRoyaume();

        ChantierConstruction chantier =
                creerChantier(royaume, "Maison test");

        Integer idChantier = chantier.getId();
        Integer idBatiment = chantier.getBatiment().getId();

        dao.save(chantier);

        entityManager.flush();
        entityManager.clear();

        Maison maison = entityManager.find(Maison.class, idBatiment);

        assertThat(maison).isNotNull();
        assertThat(maison.getChantier()).isNotNull();
        assertThat(maison.getChantier().getId())
                .isEqualTo(idChantier);
    }
}
