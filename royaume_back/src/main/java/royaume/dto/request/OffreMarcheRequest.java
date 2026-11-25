package royaume.dto.request;

import royaume.dto.QuantiteRessourceDTO;
import royaume.model.QuantiteRessource;

public record OffreMarcheRequest(
    Integer vendeurId,
    QuantiteRessource propose,
    QuantiteRessource demande
) {}