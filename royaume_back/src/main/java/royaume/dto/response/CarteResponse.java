package royaume.dto.response;

import java.util.List;

public record CarteResponse(
        Integer id,
        int largeur,
        int hauteur,
        int centreX,
        int centreY,
        Integer partieId,
        List<EmplacementCarteResponse> emplacements,
        EmplacementCarteResponse emplacementMarche,
        String archetype,
        String seed
) {

}
