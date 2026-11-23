package royaume.dto.request;

public record MessageRequest(String contenu, LocalDateTime dateEnvoi, boolean lu, Partie partie, Utilisateur expediteur, Utilisateur destinataire){}