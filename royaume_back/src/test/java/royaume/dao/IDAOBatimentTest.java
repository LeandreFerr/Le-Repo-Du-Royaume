
package royaume.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import royaume.model.peuple.Licornes;
import royaume.model.Royaume;
import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.Maison;

@DataJpaTest
@DisplayName("Tests d'intégration JPA — IDAOBatiment")
class IDAOBatimentTest {
	

	@Autowired
	private IDAORoyaume royaumeDAO;


	@Autowired
	private IDAOChantierConstruction chantierDAO;
	
    @Autowired
    private IDAOBatiment dao;

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

    private Maison creerMaison(Royaume royaume, String nom) {

        return BatimentFactory.creerMaison(
                royaume,
                null,
                nom,
                1
        );
    }

    // ---------------------------------------------------------
    // SAVE
    // ---------------------------------------------------------

    @Test
    @DisplayName("save() : enregistrer un bâtiment")
    void testSaveBatiment() {

        Royaume royaume = creerRoyaume();

        Maison maison = creerMaison(royaume, "Maison test");

        Batiment resultat = dao.save(maison);

        entityManager.flush();

        assertThat(resultat.getId()).isNotNull();
        assertThat(resultat.getNom()).isEqualTo("Maison test");
    }

    // ---------------------------------------------------------
    // FIND BY ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("findById() : retrouver un bâtiment par son identifiant")
    void testFindById() {

        Royaume royaume = creerRoyaume();

        Maison maison = creerMaison(royaume, "Maison test");

        dao.save(maison);

        entityManager.flush();

        Integer id = maison.getId();

        entityManager.clear();

        Optional<Batiment> resultat = dao.findById(id);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getNom()).isEqualTo("Maison test");
        assertThat(resultat.get()).isInstanceOf(Maison.class);
    }

    // ---------------------------------------------------------
    // FIND ALL
    // ---------------------------------------------------------

    @Test
    @DisplayName("findAll() : récupérer tous les bâtiments")
    void testFindAll() {

        Royaume royaume = creerRoyaume();

        Maison maison1 = creerMaison(royaume, "Maison test");
        Maison maison2 = creerMaison(royaume, "Deuxieme maison");

        dao.save(maison1);
        dao.save(maison2);

        entityManager.flush();
        entityManager.clear();

        assertThat(dao.findAll())
                .hasSize(2)
                .extracting(Batiment::getNom)
                .containsExactlyInAnyOrder(
                        "Maison test",
                        "Deuxieme maison"
                );
    }

    // ---------------------------------------------------------
    // EXISTS BY ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("existsById() : vérifier l'existence d'un bâtiment")
    void testExistsById() {

        Royaume royaume = creerRoyaume();

        Maison maison = creerMaison(royaume, "Maison test");

        dao.save(maison);

        entityManager.flush();

        assertThat(dao.existsById(maison.getId())).isTrue();
        assertThat(dao.existsById(999999)).isFalse();
    }

    // ---------------------------------------------------------
    // DELETE BY ID
    // ---------------------------------------------------------
    @Test
    @DisplayName("retirerBatiment() : supprimer une maison et son chantier")
    void testDeleteById() {

        // =====================================================
        // ARRANGE : création du royaume
        // =====================================================

        Royaume royaume = creerRoyaume();

        entityManager.persist(royaume);
        entityManager.flush();

        // =====================================================
        // ARRANGE : création via la Factory
        // =====================================================

        Maison maison = BatimentFactory.creerMaison(
                royaume,
                null,
                "Maison test",
                1
        );

        // Persistance de la maison et du chantier par cascade
        entityManager.persist(maison);
        entityManager.flush();

        Integer batimentId = maison.getId();
        Integer chantierId = maison.getChantier().getId();

        // Vérifications avant suppression
        assertThat(dao.existsById(batimentId)).isTrue();
        assertThat(chantierId).isNotNull();

        // =====================================================
        // ACT : suppression métier
        // =====================================================

        royaume.retirerBatiment(maison);

        entityManager.flush();
        entityManager.clear();

        // =====================================================
        // ASSERT : vérifications en base
        // =====================================================

        // Le bâtiment doit avoir disparu
        assertThat(dao.existsById(batimentId)).isFalse();

        // Son chantier doit également avoir disparu
        assertThat(chantierDAO.existsById(chantierId)).isFalse();

        // Le royaume doit toujours exister
        assertThat(royaumeDAO.existsById(royaume.getId())).isTrue();
    }
    // ---------------------------------------------------------
    // RELATION AVEC ROYAUME
    // ---------------------------------------------------------

    @Test
    @DisplayName("Vérifier la relation entre un bâtiment et son royaume")
    void testRelationBatimentRoyaume() {

        Royaume royaume = creerRoyaume();

        Maison maison = creerMaison(royaume, "Maison test");

        dao.save(maison);

        entityManager.flush();

        Integer id = maison.getId();

        entityManager.clear();

        Batiment resultat = dao.findById(id).orElseThrow();

        assertThat(resultat.getRoyaume()).isNotNull();
        assertThat(resultat.getRoyaume().getNom())
                .isEqualTo("Royaume test");
    }

    // ---------------------------------------------------------
    // HERITAGE JOINED
    // ---------------------------------------------------------

    @Test
    @DisplayName("Vérifier la persistance d'une sous-classe de Batiment")
    void testHeritageJoined() {

        Royaume royaume = creerRoyaume();

        Maison maison = creerMaison(royaume, "Maison test");

        dao.save(maison);

        entityManager.flush();

        Integer id = maison.getId();

        entityManager.clear();

        Batiment resultat = dao.findById(id).orElseThrow();

        assertThat(resultat).isInstanceOf(Maison.class);
        assertThat(resultat.getId()).isEqualTo(id);
    }
}
