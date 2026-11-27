package royaume.dto.response;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import royaume.dto.QuantiteRessourceDTO;
import royaume.model.militaire.StatutAttaque;

public record AttaqueResponse(
    Integer id,

    // Relations
    Integer attaquantId,
    String attaquantNom,
    Integer defenseurId,
    String defenseurNom,

    List<ContingentTroupeResponse> troupesEnvoyees,
    List<QuantiteRessourceDTO> butin,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    LocalDateTime dateDepart,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    LocalDateTime dateImpact,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    LocalDateTime dateRetourPrevue,   // null tant que l'attaque n'est pas gagnée
    Duration tempsTrajet,
    boolean victoireAttaquant,
    StatutAttaque statut
) {}