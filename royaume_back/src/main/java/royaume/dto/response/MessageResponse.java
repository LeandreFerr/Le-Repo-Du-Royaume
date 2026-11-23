package royaume.dto.response;

import java.time.LocalDateTime;

import royaume.model.Partie;
import royaume.model.Utilisateur;

public record MessageResponse(Integer id, String contenu, LocalDateTime dateEnvoi, boolean lu, Partie partie,
			Utilisateur expediteur, Utilisateur destinataire){}