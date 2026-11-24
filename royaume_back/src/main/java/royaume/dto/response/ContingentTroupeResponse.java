package royaume.dto.response;

import royaume.model.militaire.TypeTroupe;

public record ContingentTroupeResponse(
    TypeTroupe type,
    int quantite
) {}