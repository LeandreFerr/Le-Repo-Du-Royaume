package royaume.dto.response;

import java.time.LocalDateTime;

import royaume.dto.QuantiteRessourceDTO;
import royaume.model.StatutOffre;

public record OffreMarcheResponse(
    Integer id,
    LocalDateTime datePublication,
    StatutOffre statut,
    Integer vendeurId,
    String vendeurNom,
    QuantiteRessourceDTO propose,
    QuantiteRessourceDTO demande) {}