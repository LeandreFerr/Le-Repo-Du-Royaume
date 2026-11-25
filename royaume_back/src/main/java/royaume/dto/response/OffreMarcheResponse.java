package royaume.dto.response;

import java.time.LocalDateTime;

<<<<<<< HEAD
import royaume.dto.QuantiteRessourceDTO;
=======
import royaume.model.QuantiteRessource;
>>>>>>> Modul_6_pikachu
import royaume.model.StatutOffre;

public record OffreMarcheResponse(
    Integer id,
    LocalDateTime datePublication,
    StatutOffre statut,
    Integer vendeurId,
    String vendeurNom,
<<<<<<< HEAD
    QuantiteRessourceDTO propose,
    QuantiteRessourceDTO demande
=======
    QuantiteRessource propose,
    QuantiteRessource demande
>>>>>>> Modul_6_pikachu
) {}