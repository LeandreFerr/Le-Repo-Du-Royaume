package royaume.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ReparationBatimentRequest(
    @PositiveOrZero int montant,
   @Positive int ouvriersAssignes
) {}