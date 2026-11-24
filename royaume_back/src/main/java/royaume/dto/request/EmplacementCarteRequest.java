package royaume.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmplacementCarteRequest(

    @NotNull 
    @Positive 
    Integer royaumeId
    
) {

}
