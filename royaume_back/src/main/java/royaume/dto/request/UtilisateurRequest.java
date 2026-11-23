package royaume.dto.request;

public record UtilisateurRequest(
    String pseudo,
    String email,
    String password
) {}