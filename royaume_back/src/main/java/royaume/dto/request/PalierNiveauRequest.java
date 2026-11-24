package royaume.dto.request;

import java.util.List;

import royaume.dto.QuantiteRessourceDTO;

public record PalierNiveauRequest(
	    int niveau,
	    int populationRequise,
	    List<QuantiteRessourceDTO> prerequis
	) {}

	