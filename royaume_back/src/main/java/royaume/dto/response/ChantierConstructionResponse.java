package royaume.dto.response;

public record ChantierConstructionResponse(Integer id,
		BatimentResponse batiment,
        RoyaumeResponse royaume,
        int ouvriersMinimum,
        int ouvriersAssignes,
        int tempsBaseSecondes,
        boolean amelioration) {

}
