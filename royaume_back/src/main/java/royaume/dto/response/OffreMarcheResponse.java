package royaume.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import royaume.model.StatutOffre;
import royaume.model.QuantiteRessource;

public record OffreMarcheResponse(
    Integer id,
    LocalDateTime datePublication,
    StatutOffre statut,
    Integer vendeurId,
    String vendeurNom,
    List<QuantiteRessource> propose,
    List<QuantiteRessource> demande
) {}