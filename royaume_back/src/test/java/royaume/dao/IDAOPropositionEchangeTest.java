package royaume.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import royaume.model.PropositionEchange;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;
import royaume.model.StatutEchange;

@DataJpaTest
public class IDAOPropositionEchangeTest {
	
	@Autowired
	private IDAOPropositionEchange daoPropositionEchange;
	
	private Royaume destinataire;
	private Royaume destinataireDeux;
	private Royaume expediteur;
	
	@Test
	@DisplayName("Valide le find by destinataire et statut")
	void testFindByDestinataireAndStatut() {
		
		// Given 
		
		PropositionEchange propositionUn = new PropositionEchange(null, LocalDateTime.now(),StatutEchange.EN_ATTENTE,expediteur, destinataire,new QuantiteRessource(10, Ressource.OR),new QuantiteRessource(100, Ressource.PIERRE) );
		
		PropositionEchange propositionDeux = new PropositionEchange(null, LocalDateTime.now(),StatutEchange.EN_ATTENTE,expediteur, destinataireDeux,new QuantiteRessource(10, Ressource.BOIS),new QuantiteRessource(50, Ressource.NOURRITURE) );
		
		PropositionEchange propositionTrois = new PropositionEchange(null, LocalDateTime.now(),StatutEchange.ANNULE,expediteur, destinataire,new QuantiteRessource(10, Ressource.OR),new QuantiteRessource(100, Ressource.PIERRE) );
		
		daoPropositionEchange.save(propositionUn);
		daoPropositionEchange.save(propositionDeux);
		daoPropositionEchange.save(propositionTrois);
		
		// When 
		List <PropositionEchange> resultats = daoPropositionEchange.findByDestinataireAndStatut(destinataireDeux, StatutEchange.EN_ATTENTE);
		
		
		// Then 
		assertThat(resultats).hasSize(1);
		assertThat(resultats.get(1).getOfferte().getRessource()).isEqualTo(Ressource.BOIS);
		assertThat(resultats.get(1).getDemandee().getQuantite()).isEqualTo(50);
	}
	
}

