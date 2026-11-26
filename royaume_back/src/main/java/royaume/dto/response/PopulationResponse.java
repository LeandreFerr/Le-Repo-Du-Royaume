package royaume.dto.response;

public record PopulationResponse(
    Integer id,
    String nom,
    String titreSouverain,
    Double attraction,
    String capaciteSpeciale,
    String descriptionCapacite
) {}