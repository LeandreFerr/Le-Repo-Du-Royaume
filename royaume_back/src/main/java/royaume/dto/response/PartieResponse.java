package royaume.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import royaume.model.Message;
import royaume.model.Royaume;
import royaume.model.StatutPartie;
import royaume.model.TypeVictoire;
import royaume.model.Utilisateur;

public record PartieResponse(Integer id, String nom, LocalDateTime dateCreation, double vitesseJeu, int maxJoueurs,
		Utilisateur vainqueur, TypeVictoire typeVictoire, List<Message> messages,
		List<Royaume> royaumes, StatutPartie statut) {

}
