
package royaume.dto.response;

import java.util.List;

import royaume.dto.QuantiteRessourceDTO;
import royaume.model.Ressource;

public record ModeleBatimentResponse(

    String id,
    String nom,
    String description,
    String icone,
    String type,

    int niveauRoyaumeRequis,
    int ouvriersMinimum,
    int tempsConstructionSecondes,

    int pointsDeVie,
    Ressource typeRessource,
    int productionBaseHeure,

    List<QuantiteRessourceDTO> coutConstruction

) {}
