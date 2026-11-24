package royaume.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import royaume.model.StatutPartie;
import royaume.model.TypeVictoire;

public record PartieResponse(Integer id, String nom, LocalDateTime dateCreation, double vitesseJeu, int maxJoueurs,
		Integer vainqueurId, String vainqueurPseudo, TypeVictoire typeVictoire, 
		List<RoyaumeResponse> royaumes, StatutPartie statut) {

}
