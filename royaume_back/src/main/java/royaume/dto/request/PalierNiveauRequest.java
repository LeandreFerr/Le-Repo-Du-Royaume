package royaume.dto.request;

import java.util.List;

import royaume.model.QuantiteRessource;

public record PalierNiveauRequest(
	    int niveau,
	    int populationRequise,
	    List<QuantiteRessource> prerequis
	) {}

	