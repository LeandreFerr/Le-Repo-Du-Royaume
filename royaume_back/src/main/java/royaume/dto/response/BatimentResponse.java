
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
/*package royaume.dto.response;

import java.util.List;

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
    Double bonusDefenseRoyaume,

    // Prévisualisation
    Integer niveauSuivant,
    List<RessourceCoutResponse> coutAmelioration,
    Integer tempsBaseSecondes,
    Long tempsRestantSecondes,
    Integer pointsDeVieRecuperables

) {} */