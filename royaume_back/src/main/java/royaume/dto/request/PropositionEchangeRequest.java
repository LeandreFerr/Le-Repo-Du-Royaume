package royaume.dto.request;

import java.time.LocalDateTime;

import royaume.dto.QuantiteRessourceDTO;
import royaume.model.StatutEchange;

public record PropositionEchangeRequest(LocalDateTime dateCreation, 
		StatutEchange statut, 
		Integer expediteurId,
		Integer destinataireId, 
		QuantiteRessourceDTO offerte, 
		QuantiteRessourceDTO demandee) {

}
