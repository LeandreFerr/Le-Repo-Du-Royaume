package royaume.dto.request;

import royaume.dto.QuantiteRessourceDTO;

public record OffreMarcheRequest(
    Integer vendeurId,
    QuantiteRessourceDTO propose,
    QuantiteRessourceDTO demande
) {}