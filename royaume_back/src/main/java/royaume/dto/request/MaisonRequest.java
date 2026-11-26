package royaume.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MaisonRequest(
    @NotNull Integer royaumeId,
    @NotNull String nom,
    @Positive int ouvriersAssignes
) {}