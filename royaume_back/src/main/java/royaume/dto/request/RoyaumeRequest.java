package royaume.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RoyaumeRequest(
    @NotBlank String nom,
    @NotNull @Positive Integer peupleId,
    @NotNull @Positive Integer partieId,
    @NotNull @Positive Integer utilisateurId
) {}