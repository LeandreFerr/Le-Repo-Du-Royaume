package royaume.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import royaume.model.Ressource;

public record BatimentProductionRequest(
    @NotNull Integer royaumeId,
    @NotNull String nom,
    @Positive int pointsDeVie,
    @NotNull Ressource typeRessource,
    @PositiveOrZero int productionBaseHeure,
    @Positive int ouvriersAssignes
) {}