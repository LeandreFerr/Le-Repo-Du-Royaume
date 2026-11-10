package royaume.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Marche {

	private Integer id;
	private String nom;
	private final List<OffreMarche> offres = new ArrayList<>();

	public Marche() {}

	public Marche(Integer id, String nom) {
		this.id = id;
		this.nom = nom;
	}

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
		return Collections.unmodifiableList(offres);
	}

	// =========================================================================
	// Méthodes métier du Marché
	// =========================================================================

	public OffreMarche publierOffre(Royaume vendeur, QuantiteRessource aVendre, QuantiteRessource aRecevoir) {
		if (vendeur == null || aVendre == null || aRecevoir == null) {
			throw new IllegalArgumentException("Le vendeur et les ressources ne peuvent pas être null.");
		}
		if (aVendre.getQuantite() <= 0 || aRecevoir.getQuantite() <= 0) {
			throw new IllegalArgumentException("Les quantités à vendre et à recevoir doivent être strictement positives.");
		}
		if (vendeur.isEnRuine()) {
			throw new IllegalStateException("Un royaume en ruine ne peut pas publier d'offre sur le marché.");
		}
		if (!vendeur.aAssezDeRessources(aVendre.getRessource(), aVendre.getQuantite())) {
			throw new IllegalStateException("Le vendeur n'a pas assez de ressources pour publier cette offre.");
		}

		vendeur.debiterRessources(aVendre.getRessource(), aVendre.getQuantite());

		OffreMarche annonce = new OffreMarche(null, LocalDateTime.now(), StatutOffre.DISPONIBLE, vendeur, aVendre, aRecevoir);
		this.offres.add(annonce);
		return annonce;
	}

	public void retirerOffre(OffreMarche offre) {
		if (offre == null) {
			throw new IllegalArgumentException("L'offre ne peut pas être null.");
		}
		if (offre.getStatut() != StatutOffre.DISPONIBLE) {
			throw new IllegalStateException("Seule une offre disponible peut être retirée du marché.");
		}

		QuantiteRessource quantiteProposee = offre.getPropose();
		int quantite = quantiteProposee.getQuantite();
		Royaume vendeur = offre.getVendeur();
		Ressource type = quantiteProposee.getRessource();

		vendeur.crediterRessources(type, quantite);
		offre.setStatut(StatutOffre.ANNULEE);
	}

	public void executerEchange(OffreMarche offre, Royaume acheteur) {
		if (offre == null || acheteur == null) {
			throw new IllegalArgumentException("L'offre et l'acheteur ne peuvent pas être null.");
		}
		if (offre.getStatut() != StatutOffre.DISPONIBLE) {
			throw new IllegalStateException("Cette offre n'est plus disponible.");
		}

		Royaume vendeur = offre.getVendeur();
		if (vendeur == acheteur || (vendeur != null && vendeur.getId() != null && vendeur.getId().equals(acheteur.getId()))) {
			throw new IllegalStateException("Un vendeur ne peut pas acheter sa propre offre.");
		}
		if (acheteur.isEnRuine()) {
			throw new IllegalStateException("Un royaume en ruine ne peut pas commercer.");
		}

		QuantiteRessource quantiteProposee = offre.getPropose();
		int qteProposee = quantiteProposee.getQuantite();
		Ressource typePropose = quantiteProposee.getRessource();

		QuantiteRessource quantiteDemandee = offre.getDemande();
		int qteDemandee = quantiteDemandee.getQuantite();
		Ressource typeDemande = quantiteDemandee.getRessource();

		if (!acheteur.aAssezDeRessources(typeDemande, qteDemandee)) {
			throw new IllegalStateException("L'acheteur n'a pas assez de ressources pour cet échange.");
		}

		boolean debitReussi = acheteur.debiterRessources(typeDemande, qteDemandee);
		if (!debitReussi) {
			throw new IllegalStateException("Échec du débit des ressources chez l'acheteur.");
		}

		vendeur.crediterRessources(typeDemande, qteDemandee);
		acheteur.crediterRessources(typePropose, qteProposee);

		offre.setStatut(StatutOffre.CONCLUE);
	}
}
