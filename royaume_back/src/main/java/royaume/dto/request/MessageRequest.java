package royaume.dto.request;


public record MessageRequest(String contenu, 

		Integer partieId, Integer expediteurId, Integer destinataireId) {}
