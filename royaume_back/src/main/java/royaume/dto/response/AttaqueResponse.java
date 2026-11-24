package royaume.dto.response;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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

    LocalDateTime dateDepart,
    LocalDateTime dateImpact,
    LocalDateTime dateRetourPrevue,   // null tant que l'attaque n'est pas gagnée
    Duration tempsTrajet,
    boolean victoireAttaquant,
    StatutAttaque statut
) {}