package royaume.dto.response;

import java.util.List;
import royaume.dto.QuantiteRessourceDTO;

public record AmeliorerRoyaumeResponse(

    int niveauActuel,
    PalierNiveauResponse prochainPalier,

    int populationDisponible,
    int populationRequise,
    boolean populationSuffisante,

    List<RessourcesAmeliorerRoyaumeResponse> ressources,

    boolean ameliorationPossible

) {}