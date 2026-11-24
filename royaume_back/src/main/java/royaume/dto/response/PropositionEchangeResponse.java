package royaume.dto.response;

import java.time.LocalDateTime;

import royaume.dto.QuantiteRessourceDTO;
import royaume.model.StatutEchange;

public record PropositionEchangeResponse(Integer id, 
		LocalDateTime dateCreation, 
		StatutEchange statut, 
		Integer expediteurId,
		String expediteurNom,
		Integer destinataireId, 
		String destinataireNom,
		QuantiteRessourceDTO offerte, 
		QuantiteRessourceDTO demandee) {

}
