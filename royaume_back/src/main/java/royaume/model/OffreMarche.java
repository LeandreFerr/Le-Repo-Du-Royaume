package royaume.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class OffreMarche {

	private Integer id;
	private LocalDateTime datePublication;
	private StatutOffre statut = StatutOffre.DISPONIBLE;

	private Royaume vendeur;
	private QuantiteRessource propose;
	private QuantiteRessource demande;

	public OffreMarche() {}

	public OffreMarche(Integer id, LocalDateTime datePublication, StatutOffre statut, Royaume vendeur,
			QuantiteRessource propose, QuantiteRessource demande) {
		this.id = id;
		this.datePublication = datePublication;
		this.statut = (statut != null) ? statut : StatutOffre.DISPONIBLE;
		this.vendeur = vendeur;
		this.propose = propose;
		this.demande = demande;
	}

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OffreMarche that = (OffreMarche) o;
		if (id != null && that.id != null) {
			return Objects.equals(id, that.id);
		}
		return Objects.equals(datePublication, that.datePublication)
				&& statut == that.statut
				&& Objects.equals(vendeur, that.vendeur)
				&& Objects.equals(propose, that.propose)
				&& Objects.equals(demande, that.demande);
	}

	@Override
	public int hashCode() {
		return (id != null) ? Objects.hashCode(id) : Objects.hash(datePublication, statut, vendeur, propose, demande);
	}

	@Override
	public String toString() {
		return "OffreMarche [id=" + id + ", datePublication=" + datePublication + ", statut=" + statut
				+ ", vendeur=" + (vendeur != null ? vendeur.getNom() : "null")
				+ ", propose=" + propose + ", demande=" + demande + "]";
	}
}
