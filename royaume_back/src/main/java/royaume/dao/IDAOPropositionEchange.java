package royaume.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import royaume.model.PropositionEchange;
import royaume.model.Royaume;
import royaume.model.StatutEchange;

public interface IDAOPropositionEchange extends JpaRepository<PropositionEchange, Integer> {
	
	@Query("Find by Destinataire and Statut")
	List<PropositionEchange> findByDestinataireeAndStatut(Royaume destinataire, StatutEchange statut);
}
