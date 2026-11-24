package royaume.dto.response;

import java.time.LocalDateTime;

import royaume.model.militaire.TypeTroupe;

public record SessionEntrainementResponse(
    Integer id,
    TypeTroupe typeTroupe,
    int quantite,
    LocalDateTime dateDebut,
    LocalDateTime dateFinPrevue,
    boolean finalisee,

    // Relation
    Integer royaumeId
) {}