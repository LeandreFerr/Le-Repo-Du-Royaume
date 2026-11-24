package royaume.dto.request;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import royaume.model.militaire.TypeTroupe;

public record LancerAttaqueRequest(

    @NotNull
    Integer attaquantId,

    @NotNull
    Integer defenseurId,

    // Correspond à la signature de Royaume.lancerAttaque
    @NotEmpty
    Map<@NotNull TypeTroupe, @NotNull @Positive Integer> troupes,

    @Positive
    double distance

) {}