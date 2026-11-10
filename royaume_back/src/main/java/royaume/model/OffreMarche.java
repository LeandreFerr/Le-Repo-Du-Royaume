package royaume.model;

import java.time.LocalDateTime;

public class OffreMarche {

	private Integer id;
	private LocalDateTime datePublication;
	private StatutOffre statut;
	
	private Royaume vendeur;
	private QuantiteRessource propose;
	private QuantiteRessource demande;
	
	
	
	public OffreMarche(Integer id, LocalDateTime datePublication, StatutOffre statut, Royaume vendeur,
			QuantiteRessource propose, QuantiteRessource demande) {
		this.id = id;
		this.datePublication = datePublication;
		this.statut = statut;
		this.vendeur = vendeur;
		this.propose = propose;
		this.demande = demande;
	}

	public OffreMarche() {}
	
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LocalDateTime getDatePublication() {
		return datePublication;
	}

	public void setDatePublication(LocalDateTime datePublication) {
		this.datePublication = datePublication;
	}

	public StatutOffre getStatut() {
		return statut;
	}

	public void setStatut(StatutOffre statut) {
		this.statut = statut;
	}

	public Royaume getVendeur() {
		return vendeur;
	}

	public void setVendeur(Royaume vendeur) {
		this.vendeur = vendeur;
	}

	public QuantiteRessource getPropose() {
		return propose;
	}

	public void setPropose(QuantiteRessource propose) {
		this.propose = propose;
	}

	public QuantiteRessource getDemande() {
		return demande;
	}

	public void setDemande(QuantiteRessource demande) {
		this.demande = demande;
	}
	
	
}
