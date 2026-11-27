package royaume.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import royaume.model.militaire.TypeTroupe;

public record SessionEntrainementResponse(
    Integer id,
    TypeTroupe typeTroupe,
    int quantite,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    LocalDateTime dateDebut,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    LocalDateTime dateFinPrevue,
    boolean finalisee,

    // Relation
    Integer royaumeId
) {}