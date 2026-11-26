package royaume.dto.response;

import java.time.LocalDateTime;

public record TickerResponse(
    Integer partieId,
    LocalDateTime tempsCourant,
    double vitesseSimulation
) {
} 


