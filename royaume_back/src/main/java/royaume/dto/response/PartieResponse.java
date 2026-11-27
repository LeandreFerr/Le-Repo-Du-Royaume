package royaume.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import royaume.model.StatutPartie;
import royaume.model.TypeVictoire;

public record PartieResponse(Integer id, String nom,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
		LocalDateTime dateCreation,
		double vitesseJeu, int maxJoueurs,
		Integer vainqueurId, String vainqueurPseudo, TypeVictoire typeVictoire, 
		List<RoyaumeResponse> royaumes, StatutPartie statut) {

}
