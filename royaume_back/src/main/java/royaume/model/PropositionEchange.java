package royaume.model;

import java.time.LocalDateTime;

public class PropositionEchange {
	private Integer id;
	private LocalDateTime dateCreation;
	private StatutEchange statut;

	private Royaume expediteur;
	private Royaume destinataire;
	private QuantiteRessource offerte;
	private QuantiteRessource demandee;

	public PropositionEchange(Integer id, LocalDateTime dateCreation, StatutEchange statut, Royaume expediteur,
			Royaume destinataire, QuantiteRessource offerte, QuantiteRessource demandee) {
		this.id = id;
		this.dateCreation = dateCreation;
		this.statut = statut;
		this.expediteur = expediteur;
		this.destinataire = destinataire;
		this.offerte = offerte;
		this.demandee = demandee;
	}

	public PropositionEchange() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LocalDateTime getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(LocalDateTime dateCreation) {
		this.dateCreation = dateCreation;
	}

	public StatutEchange getStatut() {
		return statut;
	}

	public void setStatut(StatutEchange statut) {
		this.statut = statut;
	}

	public Royaume getExpediteur() {
		return expediteur;
	}

	public void setExpediteur(Royaume expediteur) {
		this.expediteur = expediteur;
	}

	public Royaume getDestinataire() {
		return destinataire;
	}

	public void setDestinataire(Royaume destinataire) {
		this.destinataire = destinataire;
	}

	public QuantiteRessource getOfferte() {
		return offerte;
	}

	public void setOfferte(QuantiteRessource offerte) {
		this.offerte = offerte;
	}

	public QuantiteRessource getDemandee() {
		return demandee;
	}

	public void setDemandee(QuantiteRessource demandee) {
		this.demandee = demandee;
	}

	//____________________________________________________________________

	public void annuler() {
		if (this.statut == StatutEchange.EN_ATTENTE) {
			this.statut = StatutEchange.ANNULE;
		}
	}
	// Variante possible : 
	/*public void annuler(PropositionEchange proposition) {
    if (proposition.getStatut() == StatutEchange.EN_ATTENTE) {
        proposition.setStatut(StatutEchange.ANNULE);
    }
	}*/


	public void accepter() {
		if (this.statut != StatutEchange.EN_ATTENTE) {
			return;
		}

		if (expediteur == null || destinataire == null
				|| offerte == null || demandee == null) {
			return;
		}

		// Verif -> les deux royaumes ont assez de ressources :

		if (!expediteur.aAssezRessource(offerte.getRessource(), offerte.getQuantite())
				|| !destinataire.aAssezRessource(demandee.getRessource(), demandee.getQuantite())) {
			return;
		}

		boolean debitExpediteur = expediteur.debiterRessource(offerte.getRessource(), offerte.getQuantite());
		boolean debitDestinataire = destinataire.debiterRessource(demandee.getRessource(), demandee.getQuantite());

		// Si un débit échoue, annule le débit déjà effectué 
		//   (au cas où une autre opération aurait changer les choses entre temps) 

		if (!debitExpediteur || !debitDestinataire) {
			if (debitExpediteur) {
				expediteur.crediterRessource(offerte.getRessource(), offerte.getQuantite());
			}

			if (debitDestinataire) {
				destinataire.crediterRessource(demandee.getRessource(), demandee.getQuantite());
			}

			return;
		}

		expediteur.crediterRessource(demandee.getRessource(), demandee.getQuantite());
		destinataire.crediterRessource(offerte.getRessource(), offerte.getQuantite());
		this.statut = StatutEchange.ACCEPTE;
	}



	public void refuser() {
		if (this.statut == StatutEchange.EN_ATTENTE) {
			this.statut = StatutEchange.REFUSE;}
	}
}
