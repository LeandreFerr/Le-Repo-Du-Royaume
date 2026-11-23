package royaume.dto;

import java.util.List;

import royaume.model.QuantiteRessource;

public record PalierNiveauDTO(
	    int niveau,
	    int populationRequise,
	    List<QuantiteRessource> prerequis
	) {}