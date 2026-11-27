package royaume.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import royaume.dto.QuantiteRessourceDTO;
import royaume.model.StatutEchange;

public record PropositionEchangeResponse(Integer id, 
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
		LocalDateTime dateCreation, 
		StatutEchange statut, 
		Integer expediteurId,
		String expediteurNom,
		Integer destinataireId, 
		String destinataireNom,
		QuantiteRessourceDTO offerte, 
		QuantiteRessourceDTO demandee) {

}
