package royaume.dto.request;



public record PartieRequest (String nom, LocalDateTime dateCreation, double vitesseJeu, int maxJoueurs,
			Utilisateur vainqueur, TypeVictoire typeVictoire, List<Message> messages,
			List<Royaume> royaumes, StatutPartie statut){}