package royaume.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="offre_marche")
public class OffreMarche {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@Column(nullable=false)
	private LocalDateTime datePublication;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
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
