package royaume.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import royaume.dao.IDAOPartie;
import royaume.dao.IDAORoyaume;
import royaume.model.Partie;
import royaume.model.Royaume;

@Service
public class PartieService {
	
	private final IDAOPartie partieDAO;
	private final IDAORoyaume royaumeDAO;
	
	public PartieService(IDAOPartie partieDAO,IDAORoyaume royaumeDAO) {
		this.partieDAO = partieDAO;
		this.royaumeDAO = royaumeDAO;
	}
	
	
	@Transactional
	public Partie inscrireRoyaumes(Integer partieId, List<Integer> royaumeIds) {
	    Partie partie = partieDAO.findById(partieId)
	            .orElseThrow(() -> new RuntimeException("Partie introuvable : " + partieId));

	    for (Integer royaumeId : royaumeIds) {
	        Royaume royaume = royaumeDAO.findById(royaumeId)
	                .orElseThrow(() -> new RuntimeException("Royaume introuvable : " + royaumeId));
	        if (!partie.getRoyaumes().contains(royaume)) {
	            partie.inscrireRoyaume(royaume);
	        }
	    }

	    return partieDAO.save(partie);
	}

}
