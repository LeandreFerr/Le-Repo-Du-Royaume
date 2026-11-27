
package royaume.dto.response;

import royaume.model.Ressource;

public record BatimentResponse(

    Integer id,
    String nom,
    int niveau,
    int pointsDeVie,
    int pointsDeVieMax,
    boolean enConstruction,
    int montantReparationEnCours,

    Integer royaumeId,

    Ressource typeRessource,
    Integer productionBaseHeure,

    Double bonusPropre,

    Integer niveauPalierDebloque,
    Double bonusDefenseRoyaume

) {}
