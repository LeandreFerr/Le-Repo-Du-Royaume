package royaume.dto.request;

<<<<<<< HEAD
import royaume.dto.QuantiteRessourceDTO;

public record OffreMarcheRequest(
    Integer vendeurId,
    QuantiteRessourceDTO propose,
    QuantiteRessourceDTO demande
=======
import royaume.model.QuantiteRessource;

public record OffreMarcheRequest(
    Integer vendeurId,
    QuantiteRessource propose,
    QuantiteRessource demande
>>>>>>> Modul_6_pikachu
) {}