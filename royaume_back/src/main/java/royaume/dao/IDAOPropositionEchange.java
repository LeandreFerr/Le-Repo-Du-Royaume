package royaume.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import royaume.model.PropositionEchange;
import royaume.model.Royaume;
import royaume.model.StatutEchange;

public interface IDAOPropositionEchange extends JpaRepository<PropositionEchange, Integer> {
	
	List<PropositionEchange> findByDestinataireAndStatut(Royaume destinataire, StatutEchange statut);
}
