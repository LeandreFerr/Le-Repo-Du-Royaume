package royaume.dto.response;

public record MessageResponse(Integer id, String contenu, LocalDateTime dateEnvoi, boolean lu, Partie partie,
			Utilisateur expediteur, Utilisateur destinataire){}