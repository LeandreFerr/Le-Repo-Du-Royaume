package royaume.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import royaume.model.militaire.TypeTroupe;

public record EntrainerTroupeRequest(

    @NotNull
    Integer royaumeId,

    @NotNull
    TypeTroupe type,

    @Positive
    int quantite

) {}