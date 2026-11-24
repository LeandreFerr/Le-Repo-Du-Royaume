package royaume.dto.request;

public record ChantierConstructionRequest(Integer batimentId,
		Integer royaumeId,
		int ouvriersAssignes,
        boolean amelioration) {

}
