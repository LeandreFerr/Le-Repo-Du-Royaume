package royaume.model;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="offre_marche")
public class OffreMarche {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@NotNull
	@DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm")
	@Column(name="date_publication", nullable=false)
	private LocalDateTime datePublication;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name="statut-offre",nullable=false)
	private StatutOffre statut = StatutOffre.DISPONIBLE;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name="vendeur_id", nullable=false)
	private Royaume vendeur;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "marche_id", nullable = true)
	private Marche marche;
	
	@NotNull
	@Valid
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "quantite", column = @Column(name = "propose_quantite", nullable = false)),
		@AttributeOverride(name = "ressource", column = @Column(name = "propose_ressource", nullable = false))
	})
	private QuantiteRessource propose;
	
	@NotNull
	@Valid
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "quantite", column = @Column(name = "demande_quantite", nullable = false)),
		@AttributeOverride(name = "ressource", column = @Column(name = "demande_ressource", nullable = false))
	})
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
