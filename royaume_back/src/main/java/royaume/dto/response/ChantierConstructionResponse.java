package royaume.dto.response;

import java.time.LocalDateTime;

public record ChantierConstructionResponse(Integer id,
		Integer batimentId,
		Integer royaumeId,
		LocalDateTime dateDebut,
		LocalDateTime dateFinPrevue,
        int ouvriersMinimum,
        int ouvriersAssignes,
        int tempsBaseSecondes,
        boolean amelioration) {

}
