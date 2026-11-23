package royaume.dto.response;

import java.time.LocalDateTime;

import royaume.dto.QuantiteRessourceDTO;
import royaume.model.StatutEchange;

public record PropositionEchangeResponse(Integer id, 
		LocalDateTime dateCreation, 
		StatutEchange statut, 
		RoyaumeResponse expediteur,
		RoyaumeResponse destinataire, 
		QuantiteRessourceDTO offerte, 
		QuantiteRessourceDTO demandee) {

}
