package royaume.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import royaume.model.OffreMarche;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;
import royaume.model.StatutOffre;
import royaume.model.peuple.Canards;
import royaume.model.peuple.Population;

@DataJpaTest
public class IDAOOffreMarcheTest {
	
	@Autowired
	private IDAOOffreMarche daoOffreMarche;

	@Autowired
	private IDAORoyaume daoRoyaume;

	@Autowired
	private EntityManager entityManager;
	
	private Royaume vendeur;

	@BeforeEach
	void setUp() {
		Population peuple = new Canards(null, "Canards", "Archiduc", 0.05);
		entityManager.persist(peuple);
		entityManager.flush();
		vendeur = daoRoyaume.saveAndFlush(new Royaume(null, "Vendeur", peuple));
	}
	
	@Test
	@DisplayName("Valide le find by statut")
	void testFindByStatut() {
		
		// Given
		OffreMarche offreDispo = new OffreMarche(null,LocalDateTime.now(), StatutOffre.DISPONIBLE, vendeur,new QuantiteRessource(100, Ressource.OR), 
	            new QuantiteRessource(50, Ressource.PIERRE) );
		
		OffreMarche offreAnnulee = new OffreMarche(null,LocalDateTime.now(), StatutOffre.ANNULEE, vendeur,new QuantiteRessource(10, Ressource.OR), 
	            new QuantiteRessource(100, Ressource.PIERRE) );
		
		OffreMarche offreDispobis = new OffreMarche(null,LocalDateTime.now(), StatutOffre.DISPONIBLE, vendeur,new QuantiteRessource(100, Ressource.NOURRITURE), 
	            new QuantiteRessource(50, Ressource.BOIS) );
		
		daoOffreMarche.save(offreDispo);
		daoOffreMarche.save(offreAnnulee);
		daoOffreMarche.save(offreDispobis);
		
		// When
		List<OffreMarche> resultats = daoOffreMarche.findByStatut(StatutOffre.DISPONIBLE);
		
		// Then
		assertThat(resultats).hasSize(2);
        assertThat(resultats.get(0).getStatut()).isEqualTo(StatutOffre.DISPONIBLE);
        assertThat(resultats.get(1).getStatut()).isEqualTo(StatutOffre.DISPONIBLE);
        
	}
	
	@Test
	@DisplayName("Valide le find by statut et ressource proposee")
	void testFindByStatutAndProposeRessource() {
		
		// Given
				OffreMarche offreDispo = new OffreMarche(null,LocalDateTime.now(), StatutOffre.DISPONIBLE, vendeur,new QuantiteRessource(100, Ressource.OR), 
			            new QuantiteRessource(50, Ressource.PIERRE) );
				
				OffreMarche offreAnnulee = new OffreMarche(null,LocalDateTime.now(), StatutOffre.ANNULEE, vendeur,new QuantiteRessource(10, Ressource.OR), 
			            new QuantiteRessource(100, Ressource.PIERRE) );
				
				OffreMarche offreDispobis = new OffreMarche(null,LocalDateTime.now(), StatutOffre.DISPONIBLE, vendeur,new QuantiteRessource(100, Ressource.NOURRITURE), 
			            new QuantiteRessource(50, Ressource.BOIS) );
				
				daoOffreMarche.save(offreDispo);
				daoOffreMarche.save(offreAnnulee);
				daoOffreMarche.save(offreDispobis);
				
		// When
		List<OffreMarche> resultats = daoOffreMarche.findByStatutAndProposeRessource(StatutOffre.DISPONIBLE,Ressource.OR);
			
		// Then
		assertThat(resultats).hasSize(1);
		assertThat(resultats.get(0).getStatut()).isEqualTo(StatutOffre.DISPONIBLE);
		assertThat(resultats.get(0).getPropose().getRessource()).isEqualTo(Ressource.OR);
		assertThat(resultats.get(0).getPropose().getQuantite()).isEqualTo(100);
		
	}
	
	}


