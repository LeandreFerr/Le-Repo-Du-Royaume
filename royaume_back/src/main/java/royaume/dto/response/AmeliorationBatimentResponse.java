
package royaume.dto.response;

import java.util.List;
import royaume.model.QuantiteRessource;

public record AmeliorationBatimentResponse(
    Integer batimentId,
    int niveauActuel,
    int niveauSuivant,
    int niveauChateauRequis,
    List<QuantiteRessource> coutAmelioration,
    int tempsConstructionSecondes,
    int ouvriersMinimum
) {}
