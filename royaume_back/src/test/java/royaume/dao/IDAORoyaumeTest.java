
package royaume.dao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import royaume.model.Canards;
import royaume.model.Population;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

@DataJpaTest
@DisplayName("Tests d'intégration JPA — IDAORoyaume")
class IDAORoyaumeTest {

    @Autowired
    private IDAORoyaume daoRoyaume;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Enregistre et relit un Royaume avec ses stocks JSON")
    void testSauvegardeEtRelectureRoyaumeAvecRessourcesJson() {

        // =====================================================
        // GIVEN : création d'un peuple
        // =====================================================

        Population peuple = new Canards(
                null,
                "Canards",
                "Archiduc",
                0.05
        );

        entityManager.persist(peuple);
        entityManager.flush();
        assertNotNull(peuple.getId());


        // =====================================================
        // GIVEN : création d'un royaume
        // =====================================================

        Royaume royaume = new Royaume(
                null,
                "Avalon",
                peuple,
                3,
                250,
                50,
                20,
                30,
                false,
                List.of(
                    new QuantiteRessource(150, Ressource.BOIS),
                    new QuantiteRessource(80, Ressource.PIERRE),
                    new QuantiteRessource(45, Ressource.OR),
                    new QuantiteRessource(200, Ressource.NOURRITURE)
                )
        );

        // =====================================================
        // WHEN : sauvegarde en base de données
        // =====================================================

        Royaume royaumeSauvegarde = daoRoyaume.saveAndFlush(royaume);

        assertNotNull(royaumeSauvegarde.getId());

        // =====================================================
        // WHEN : relecture depuis la base
        // =====================================================

        entityManager.clear();

        Royaume royaumeRelu = daoRoyaume.findById(
                royaumeSauvegarde.getId()
        ).orElseThrow();

        // =====================================================
        // THEN : vérification des données
        // =====================================================

        assertNotNull(royaumeRelu);

        

        assertAll(

            // Identité
            () -> assertEquals(
                    royaumeSauvegarde.getId(),
                    royaumeRelu.getId()
            ),

            () -> assertEquals("Avalon", royaumeRelu.getNom()),

            // Population et caractéristiques
            () -> assertEquals(
                    "Canards",
                    royaumeRelu.getPeuple().getNom()
            ),

            () -> assertEquals(3, royaumeRelu.getNiveau()),

            () -> assertEquals(
                    250,
                    royaumeRelu.getPopulationDisponible()
            ),

            () -> assertEquals(
                    50,
                    royaumeRelu.getPopulationOuvriere()
            ),

            () -> assertEquals(
                    20,
                    royaumeRelu.getPopulationEnEntrainement()
            ),

            () -> assertEquals(
                    30,
                    royaumeRelu.getPopulationMilitaire()
            ),

            () -> assertFalse(royaumeRelu.isEnRuine()),

            // Stocks JSON
            () -> assertNotNull(royaumeRelu.getRessources()),

            () -> assertEquals(
                    4,
                    royaumeRelu.getRessources().size()
            ),

            () -> assertEquals(
                    150,
                    getStock(royaumeRelu, Ressource.BOIS)
            ),

            () -> assertEquals(
                    80,
                    getStock(royaumeRelu, Ressource.PIERRE)
            ),

            () -> assertEquals(
                    45,
                    getStock(royaumeRelu, Ressource.OR)
            ),

            () -> assertEquals(
                    200,
                    getStock(royaumeRelu, Ressource.NOURRITURE)
            ),

            // Vérification métier après relecture
            () -> assertTrue(
                    royaumeRelu.aAssezDeRessources(
                            Ressource.BOIS, 150
                    )
            ),

            () -> assertTrue(
                    royaumeRelu.aAssezDeRessources(
                            Ressource.OR, 45
                    )
            ),

            // Vérification de la méthode personnalisée du DAO
            () -> assertTrue(
                    daoRoyaume.existsByNom("Avalon")
            )
        );
    }

    /**
     * Retourne le stock d'une ressource dans un Royaume.
     */
    private int getStock(Royaume royaume, Ressource ressource) {

        return royaume.getRessources()
                .stream()
                .filter(r -> r.getRessource() == ressource)
                .mapToInt(QuantiteRessource::getQuantite)
                .sum();
    }
}
