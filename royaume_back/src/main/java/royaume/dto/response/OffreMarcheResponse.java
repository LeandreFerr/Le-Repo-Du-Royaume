package royaume.dto.response;

import java.time.LocalDateTime;

import royaume.model.QuantiteRessource;
import royaume.model.StatutOffre;

public record OffreMarcheResponse(
    Integer id,
    LocalDateTime datePublication,
    StatutOffre statut,
    Integer vendeurId,
    String vendeurNom,
    QuantiteRessource propose,
    QuantiteRessource demande
) {}