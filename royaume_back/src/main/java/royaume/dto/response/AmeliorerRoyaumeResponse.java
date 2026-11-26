package royaume.dto.response;

import java.util.List;

public record AmeliorerRoyaumeResponse(

    // Niveaux
    int niveauActuel,
    PalierNiveauResponse prochainPalier,

    // Population
    int populationDisponible,
    int populationRequise,
    int populationManquante,
    boolean populationSuffisante,

    // Ressources
    List<RessourcesAmeliorerRoyaumeResponse> ressources,

    // Décision métier globale
    boolean ameliorationPossible

) {}
