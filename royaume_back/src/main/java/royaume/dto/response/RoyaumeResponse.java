package royaume.dto.response;

import java.util.List;

import royaume.dto.QuantiteRessourceDTO;

public record RoyaumeResponse(
    Integer id,
    String nom,
    String peuple,
    int niveau,
    int populationDisponible,
    int populationOuvriere,
    int populationEnEntrainement,
    int populationMilitaire,
    boolean enRuine,
    double bonusCroissance,
    List<QuantiteRessourceDTO> ressources,
    List<ContingentTroupeResponse> garnison, // ajout pour militaire
    List<BatimentResponse> batiments,
    Integer x,
    Integer y
) {}