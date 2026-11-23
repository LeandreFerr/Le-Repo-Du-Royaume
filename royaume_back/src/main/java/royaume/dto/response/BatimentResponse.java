
package royaume.dto.response;

import royaume.model.Ressource;

public record BatimentResponse(

    // Attributs communs
    Integer id,
    String nom,
    int niveau,
    int pointsDeVie,
    int pointsDeVieMax,
    boolean enConstruction,
    int montantReparationEnCours,

    // Relation
    Integer royaumeId,

    // BatimentProduction
    Ressource typeRessource,
    Integer productionBaseHeure,

    // Maison
    Double bonusPropre,

    // Chateau
    Integer niveauPalierDebloque,
    Double bonusDefenseRoyaume

) {}
