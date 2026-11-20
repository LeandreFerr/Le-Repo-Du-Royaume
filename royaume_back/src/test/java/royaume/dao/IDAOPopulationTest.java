package royaume.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import royaume.model.Peuple.Canards;
import royaume.model.Peuple.Population;

@DataJpaTest
	class IDAOPopulationTest {

	    @Autowired
	    private IDAOPopulation populationRepository;

	    @Autowired
	    private EntityManager entityManager;

	    @Test
	    void testSauvegardeEtRelectureCanards() {

	        Canards canards = new Canards(
	                null,
	                "Canards",
	                "Archiduc",
	                0.05
	        );

	        Population sauvegardee = populationRepository.saveAndFlush(canards);

	        assertNotNull(sauvegardee.getId());

	        entityManager.clear();

	        Population relue = populationRepository
	                .findById(sauvegardee.getId())
	                .orElseThrow();

	        assertEquals(Canards.class, relue.getClass());
	        assertEquals("Canards", relue.getNom());
	        
	        Population trouvee = populationRepository
	                .findByNom("Canards")
	                .orElseThrow();

	        assertEquals(Canards.class, trouvee.getClass());
	    }
	}