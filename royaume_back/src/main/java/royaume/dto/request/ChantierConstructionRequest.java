package royaume.dto.request;

public record ChantierConstructionRequest(Integer batimentId,
		Integer royaumeId,
		int ouvriersMinimum,
		int ouvriersAssignes,
		int tempsBaseSecondes,
        boolean amelioration) {

}
