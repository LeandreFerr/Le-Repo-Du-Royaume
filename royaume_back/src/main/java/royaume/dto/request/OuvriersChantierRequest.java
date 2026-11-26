package royaume.dto.request;

import jakarta.validation.constraints.Positive;

public record OuvriersChantierRequest(
    @Positive int nombre
) {}