package royaume.dto.request;

import java.time.LocalDateTime;

import royaume.model.Partie;
import royaume.model.Utilisateur;

public record MessageRequest(String contenu, LocalDateTime dateEnvoi, boolean lu, Partie partie, Utilisateur expediteur, Utilisateur destinataire){}