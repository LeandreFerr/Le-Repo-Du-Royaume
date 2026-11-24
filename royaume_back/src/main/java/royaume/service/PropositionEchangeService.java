package royaume.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import royaume.dao.IDAOPropositionEchange;
import royaume.dao.IDAORoyaume;
import royaume.model.PropositionEchange;
import royaume.model.QuantiteRessource;
import royaume.model.Royaume;
import royaume.model.StatutEchange;

@Service
@Transactional
public class PropositionEchangeService {
	
	private final IDAORoyaume royaumeDAO;
	private final IDAOPropositionEchange propositionEchangeDAO;
	
	public PropositionEchangeService(IDAORoyaume royaumeDAO, IDAOPropositionEchange propositionEchangeDAO) {
		this.royaumeDAO = royaumeDAO;
		this.propositionEchangeDAO = propositionEchangeDAO;
	}
	
	// =========================================
    // RECUPERATION DU ROYAUME ET DU MARCHE PAR ID
    // =========================================

    private Royaume recupererRoyaume(Integer id) {
        return royaumeDAO.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Royaume introuvable : " + id
            ));
    }
    
    // =========================================
    // PROPOSITION ECHANGE
    // =========================================
    
    public PropositionEchange creerProposition(Integer royaumeIdExpediteur, Integer royaumeIdDestinataire, QuantiteRessource offerte, QuantiteRessource demandee) {
    	Royaume expediteur = recupererRoyaume(royaumeIdExpediteur);
    	Royaume destinataire = recupererRoyaume(royaumeIdDestinataire);
    	
    	PropositionEchange echange = new PropositionEchange(null,
				LocalDateTime.now(),
				StatutEchange.EN_ATTENTE,
				expediteur,
				destinataire,
				offerte,
				demandee
		);
    	
    	return propositionEchangeDAO.save(echange);
    	
    }
    
    public PropositionEchange accepterProposition(Integer propositionId) {
    	PropositionEchange echange = propositionEchangeDAO.findById(propositionId)
    			.orElseThrow(() -> new RuntimeException("Proposition introuvable : " + propositionId));

    	echange.accepter();

    	if (echange.getStatut() == StatutEchange.ACCEPTE) {
    		royaumeDAO.save(echange.getExpediteur());
    		royaumeDAO.save(echange.getDestinataire());
    	}

    	return propositionEchangeDAO.save(echange);
    }
    
    
    

}
