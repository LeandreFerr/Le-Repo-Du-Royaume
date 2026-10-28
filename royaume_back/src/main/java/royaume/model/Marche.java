package royaume.model;

import java.time.LocalDateTime;
import java.util.List;

public class Marche {

	private Integer id;
	private String nom;
	private static List<OffreMarche> offres;

	public Marche(Integer id, String nom, List<OffreMarche> offres) {
		this.id = id;
		this.nom = nom;
		this.offres = offres;
	}

	public Marche() {}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public List<OffreMarche> getOffres() {
		return offres;
	}
	public void setOffres(List<OffreMarche> offres) {
		this.offres = offres;
	}


	// Methode publier offre qui va débiter le sock du vendeur pour consigner la marchandise et ajoute l'offre à la liste

	public OffreMarche publierOffre(Royaume vendeur, QuantiteRessource aVendre, QuantiteRessource aRecevoir){

		// permet de sélectionner la même ressource que celle qu'on propose et de la débiter
		vendeur.getRessources().stream()
		.filter(ressource -> ressource.getRessource() == aVendre.getRessource())
		.findFirst()
		.ifPresent((QuantiteRessource ressourceStock) -> {
			ressourceStock.retirer(aVendre.getQuantite()); 
		});

		OffreMarche annonce = new OffreMarche(id, LocalDateTime.now(), StatutOffre.DISPONIBLE, vendeur, aVendre, aRecevoir);
		
		this.offres.add(annonce);

		return annonce;
	}

	// Retirer l'offre restitue la marchandise et passe en annulee

	public void retirerOffre (OffreMarche offre){

		QuantiteRessource quantiteProposee = offre.getPropose();
		int quantite = quantiteProposee.getQuantite();
		Royaume vendeur = offre.getVendeur();
		Ressource type = quantiteProposee.getRessource();

		vendeur.getRessources().stream()
		.filter(ressourceStock -> ressourceStock.getRessource() == type)
		.findFirst()
		.ifPresent((QuantiteRessource ressourceStock) -> {
			ressourceStock.ajouter(quantite); 
		});

		offre.setStatut(StatutOffre.ANNULEE);
		
		
	}


	// Executer échange vérifie que l'acheteur a les ressources demandées, effectue le transfert croisé et passe l'offre en CONCLUE

	public void executerEchange (OffreMarche offre, Royaume acheteur) {

		if (offre.getStatut() != StatutOffre.DISPONIBLE) {
			throw new IllegalStateException("Cette offre n'est plus disponible.");
		}

		QuantiteRessource quantiteProposee = offre.getPropose();
		int qteProposee = quantiteProposee.getQuantite();
		Royaume vendeur = offre.getVendeur();
		Ressource typePropose = quantiteProposee.getRessource();  

		QuantiteRessource quantiteDemandee = offre.getDemande();
		int qteDemandee = quantiteDemandee.getQuantite();
		Ressource typeDemande = quantiteDemandee.getRessource();

		boolean acheteurPossedeTout = acheteur.getRessources().stream()
				.filter(ressourceStock -> ressourceStock.getRessource() == typeDemande)
				.findFirst()
				.map(ressourceStock -> ressourceStock.getQuantite() >= qteDemandee)
				.orElse(false);

		if (!acheteurPossedeTout) {
			throw new IllegalStateException("L'acheteur n'a pas assez de ressources pour cet échange.");
		}

		
		acheteur.getRessources().stream()
		.filter(ressourceStock -> ressourceStock.getRessource() == typeDemande)
		.findFirst()
		.ifPresent((QuantiteRessource ressourceStock) -> {
			ressourceStock.retirer(qteDemandee);
		});

		
		vendeur.getRessources().stream()
		.filter(ressourceStock -> ressourceStock.getRessource() == typeDemande)
		.findFirst()
		.ifPresent((QuantiteRessource ressourceStock) -> {
			ressourceStock.ajouter(qteDemandee);
		});

		
		acheteur.getRessources().stream()
		.filter(ressourceStock -> ressourceStock.getRessource() == typePropose)
		.findFirst()
		.ifPresent((QuantiteRessource ressourceStock) -> {
			ressourceStock.ajouter(qteProposee);
		});

	
		offre.setStatut(StatutOffre.CONCLUE);
		
		
	}

}

