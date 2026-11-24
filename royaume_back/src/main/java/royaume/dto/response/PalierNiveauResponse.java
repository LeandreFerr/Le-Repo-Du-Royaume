package royaume.dto.response;

import java.util.List;

import royaume.dto.QuantiteRessourceDTO;

public record PalierNiveauResponse(Integer id, int niveau, int populationRequise, List<QuantiteRessourceDTO> prerequis){}
