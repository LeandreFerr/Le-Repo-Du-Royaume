
package royaume.dto.response;

public record ReparationBatimentResponse(
    Integer batimentId,
    int pointsDeVieActuels,
    int pointsDeVieMax,
    int tempsReparationSecondes,
    int ouvriersMinimum
) {}
