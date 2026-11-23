package royaume.dto.response;

import java.util.List;

public record MarcheResponse(
    Integer id,
    String nom,
    List<OffreMarcheResponse> offres
) {}