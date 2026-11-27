package royaume.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import royaume.dto.QuantiteRessourceDTO;
import royaume.model.StatutOffre;

public record OffreMarcheResponse(
    Integer id,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    LocalDateTime datePublication,
    StatutOffre statut,
    Integer vendeurId,
    String vendeurNom,
    QuantiteRessourceDTO propose,
    QuantiteRessourceDTO demande) {}