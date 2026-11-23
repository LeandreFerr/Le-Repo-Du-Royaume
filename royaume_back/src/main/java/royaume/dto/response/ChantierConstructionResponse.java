package royaume.dto.response;

import royaume.dto.request.BatimentResponse;

public record ChantierConstructionResponse(Integer id,
		BatimentResponse batiment,
        RoyaumeResponse royaume,
        int ouvriersMinimum,
        int ouvriersAssignes,
        int tempsBaseSecondes,
        boolean amelioration) {

}
