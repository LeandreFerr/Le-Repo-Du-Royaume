package royaume.dto.request;

import java.util.List;
import royaume.model.QuantiteRessource;

public record OffreMarcheRequest(
    Integer vendeurId,
    List<QuantiteRessource> propose,
    List<QuantiteRessource> demande
) {}