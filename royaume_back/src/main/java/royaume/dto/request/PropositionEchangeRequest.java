package royaume.dto.request;



import royaume.dto.QuantiteRessourceDTO;
public record PropositionEchangeRequest(
    Integer expediteurId,
    Integer destinataireId,
    QuantiteRessourceDTO offerte,
    QuantiteRessourceDTO demandee
) {}