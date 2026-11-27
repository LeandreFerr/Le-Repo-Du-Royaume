package royaume.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record MessageResponse(Integer id, 
		String contenu, 
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
		LocalDateTime dateEnvoi, 
		boolean lu, 
		Integer partieId,
		Integer expediteurId,
		String expediteurPseudo, 
		Integer destinataireId,
		String destinatairePseudo ){}