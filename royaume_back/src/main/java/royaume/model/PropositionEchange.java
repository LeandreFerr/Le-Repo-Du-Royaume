package royaume.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class PropositionEchange {

	private Integer id;
	private LocalDateTime dateCreation;
	private StatutEchange statut = StatutEchange.EN_ATTENTE;

	private Royaume expediteur;
	private Royaume destinataire;
	private QuantiteRessource offerte;
	private QuantiteRessource demandee;

	public PropositionEchange() {}

	public PropositionEchange(Integer id, LocalDateTime dateCreation, StatutEchange statut, Royaume expediteur,
			Royaume destinataire, QuantiteRessource offerte, QuantiteRessource demandee) {
		this.id = id;
		this.dateCreation = dateCreation;
		this.statut = (statut != null) ? statut : StatutEchange.EN_ATTENTE;
		this.expediteur = expediteur;
		this.destinataire = destinataire;
		this.offerte = offerte;
		this.demandee = demandee;
	}

	// =========================================================================
	// Méthodes métier
	// =========================================================================

	public void annuler() {
		if (this.statut == StatutEchange.EN_ATTENTE) {
			this.statut = StatutEchange.ANNULE;
		}
	}

	public void refuser() {
		if (this.statut == StatutEchange.EN_ATTENTE) {
			this.statut = StatutEchange.REFUSE;
		}
	}

	public void accepter() {
		if (this.statut != StatutEchange.EN_ATTENTE) {
			return;
		}

		if (expediteur == null || destinataire == null
				|| offerte == null || demandee == null) {
			return;
		}

		// Règle anti-triche : auto-échange interdit
		if (expediteur == destinataire || (expediteur.getId() != null && expediteur.getId().equals(destinataire.getId()))) {
			return;
		}

		// Règle métier : les royaumes en ruine ne peuvent pas commercer
		if (expediteur.isEnRuine() || destinataire.isEnRuine()) {
			return;
		}

		// Règle métier : quantités strictement positives
		if (offerte.getQuantite() <= 0 || demandee.getQuantite() <= 0) {
			return;
		}

		// Vérification que les deux royaumes ont assez de ressources
		if (!expediteur.aAssezDeRessources(offerte.getRessource(), offerte.getQuantite())
				|| !destinataire.aAssezDeRessources(demandee.getRessource(), demandee.getQuantite())) {
			return;
		}

		boolean debitExpediteur = expediteur.debiterRessources(offerte.getRessource(), offerte.getQuantite());
		boolean debitDestinataire = destinataire.debiterRessources(demandee.getRessource(), demandee.getQuantite());

		// Rollback automatique si un débit échoue
		if (!debitExpediteur || !debitDestinataire) {
			if (debitExpediteur) {
				expediteur.crediterRessources(offerte.getRessource(), offerte.getQuantite());
			}
			if (debitDestinataire) {
				destinataire.crediterRessources(demandee.getRessource(), demandee.getQuantite());
			}
			return;
		}

		// Échange bilatéral des ressources
		expediteur.crediterRessources(demandee.getRessource(), demandee.getQuantite());
		destinataire.crediterRessources(offerte.getRessource(), offerte.getQuantite());
		this.statut = StatutEchange.ACCEPTE;
	}

	// =========================================================================
	// Getters & Setters
	// =========================================================================

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

	// =========================================================================
	// Contrat POJO : equals, hashCode & toString
	// =========================================================================

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PropositionEchange that = (PropositionEchange) o;
		if (id != null && that.id != null) {
			return Objects.equals(id, that.id);
		}
		return Objects.equals(dateCreation, that.dateCreation)
				&& statut == that.statut
				&& Objects.equals(expediteur, that.expediteur)
				&& Objects.equals(destinataire, that.destinataire)
				&& Objects.equals(offerte, that.offerte)
				&& Objects.equals(demandee, that.demandee);
	}

	@Override
	public int hashCode() {
		return (id != null) ? Objects.hashCode(id) : Objects.hash(dateCreation, statut, expediteur, destinataire, offerte, demandee);
	}

	@Override
	public String toString() {
		return "PropositionEchange [id=" + id + ", dateCreation=" + dateCreation + ", statut=" + statut
				+ ", expediteur=" + (expediteur != null ? expediteur.getNom() : "null")
				+ ", destinataire=" + (destinataire != null ? destinataire.getNom() : "null")
				+ ", offerte=" + offerte + ", demandee=" + demandee + "]";
	}
}
