package royaume.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ChantierConstructionResponse(Integer id,
		Integer batimentId,
		Integer royaumeId,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
		LocalDateTime dateDebut,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
		LocalDateTime dateFinPrevue,
        int ouvriersMinimum,
        int ouvriersAssignes,
        int tempsBaseSecondes,
        boolean amelioration) {

}
