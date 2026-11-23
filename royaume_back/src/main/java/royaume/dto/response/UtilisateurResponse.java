package royaume.dto.response;

public record UtilisateurResponse(
    Integer id,
    String pseudo,
    String email,
    boolean online,
    boolean admin,
    Integer royaumeId,
    String royaumeNom
) {}