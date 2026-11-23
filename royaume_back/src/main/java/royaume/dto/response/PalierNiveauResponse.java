package royaume.dto.response;

import java.util.List;

import royaume.model.QuantiteRessource;

public record PalierNiveauResponse(Integer id, int niveau, int populationRequise, List<QuantiteRessource> prerequis){}
