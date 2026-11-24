package royaume.dto.response;

import java.time.LocalDateTime;

public record MessageResponse(Integer id, 
		String contenu, 
		LocalDateTime dateEnvoi, 
		boolean lu, 
		Integer partieId,
		Integer expediteurId,
		String expediteurPseudo, 
		Integer destinataireId,
		String destinatairePseudo ){}