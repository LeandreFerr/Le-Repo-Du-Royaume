package royaume.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CarteRequest(

    @NotNull
    @Positive
    Integer partieId,

    @Min(value = 1)
    @Max(value = 10)
    int nombreEmplacements,

    String archetype,

    String seed

) {

    public CarteRequest(Integer partieId, int nombreEmplacements) {
        this(partieId, nombreEmplacements, null, null);
    }
}
