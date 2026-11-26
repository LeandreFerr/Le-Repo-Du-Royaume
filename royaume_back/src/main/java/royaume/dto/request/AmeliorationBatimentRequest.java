package royaume.dto.request;

import jakarta.validation.constraints.Positive;

public record AmeliorationBatimentRequest(
    @Positive int ouvriersAssignes
) {}