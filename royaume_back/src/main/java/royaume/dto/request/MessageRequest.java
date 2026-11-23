package royaume.dto.request;

import java.time.LocalDateTime;

import royaume.model.Partie;
import royaume.model.Utilisateur;

public record MessageRequest(String contenu, 
		LocalDateTime dateEnvoi, 
		boolean lu, 
		Integer PartieId, 
		Utilisateur expediteur, 
		Utilisateur destinataire){}