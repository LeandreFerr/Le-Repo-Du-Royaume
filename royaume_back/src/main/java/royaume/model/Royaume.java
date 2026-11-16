package royaume.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "royaume")
public class Royaume {

	// =========================================================================
	// Attributs
	// =========================================================================

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@NotBlank
	@Size(min=2, max=50)
	private String nom;
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "peuple", nullable = false)
	private Population peuple;
	@Min(1)
	@Max(10)
	@NotNull
	private int niveau = 1;
	@PositiveOrZero
	@NotNull
	private int populationDisponible = 100;
	@PositiveOrZero
	@NotNull
	private int populationOuvriere = 0;
	@PositiveOrZero
	@NotNull
	private int populationEnEntrainement = 0;
	@PositiveOrZero
	@NotNull
	private int populationMilitaire = 0;
	
	@NotNull
	private boolean enRuine = false;
	
	@NotNull
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "ressources", columnDefinition = "json", nullable = false)
	@Valid
	private List<@NotNull @Valid QuantiteRessource> ressources = creerRessourcesParDefaut();

	// =========================================================================
	// Constructeurs
	// =========================================================================

	public Royaume() {}

	public Royaume(Integer id, String nom, Population peuple) {
		validerPeuple(peuple);
		this.id = id;
		this.nom = nom;
		this.peuple = peuple;
	}

	public Royaume(Integer id, String nom, Population peuple, int niveau, int populationDisponible,
			int populationOuvriere, int populationEnEntrainement, int populationMilitaire, boolean enRuine,
			List<QuantiteRessource> ressources) {
		
		validerPeuple(peuple);
		this.id = id;
		this.nom = nom;
		this.peuple = peuple;
		this.niveau = niveau;
		this.populationDisponible = populationDisponible;
		this.populationOuvriere = populationOuvriere;
		this.populationEnEntrainement = populationEnEntrainement;
		this.populationMilitaire = populationMilitaire;
		this.enRuine = enRuine;
		setRessources(ressources);
	}

	// =========================================================================
	// Méthodes métier
	// =========================================================================

	public void crediterRessources(Ressource ressource, int quantite) {
		if (ressource == null) {
			throw new IllegalArgumentException("La ressource ne peut pas être null.");
		}
		if (quantite <= 0) {
			throw new IllegalArgumentException("La quantité à créditer doit être strictement positive.");
		}
		ressources.stream()
			.filter(r -> r.getRessource() == ressource)
			.findFirst()
			.ifPresentOrElse(
				r -> r.ajouter(quantite),
				() -> ressources.add(new QuantiteRessource(quantite, ressource))
			);
	}

	public boolean aAssezDeRessources(Ressource ressource, int quantite) {
		if (ressource == null) {
			throw new IllegalArgumentException("La ressource ne peut pas être null.");
		}
		if (quantite < 0) {
			throw new IllegalArgumentException("La quantité ne peut pas être négative.");
		}
		if (quantite == 0) {
			return true;
		}
		return ressources.stream()
			.filter(r -> r.getRessource() == ressource)
			.anyMatch(r -> r.getQuantite() >= quantite);
	}

	public boolean debiterRessources(Ressource ressource, int quantite) {
		if (ressource == null) {
			throw new IllegalArgumentException("La ressource ne peut pas être null.");
		}
		if (quantite <= 0) {
			throw new IllegalArgumentException("La quantité à débiter doit être strictement positive.");
		}
		if (!aAssezDeRessources(ressource, quantite)) {
			return false;
		}
		ressources.stream()
			.filter(r -> r.getRessource() == ressource)
			.findFirst()
			.ifPresent(r -> r.retirer(quantite));
		return true;
	}

	public void appliquerCroissance(double taux) {
		if (!Double.isFinite(taux) || taux < -1.0) {
			throw new IllegalArgumentException("Le taux de croissance doit être un nombre fini supérieur ou égal à -1.0.");
		}
		long resultat = Math.round(populationDisponible * (1.0 + taux));
		populationDisponible = Math.toIntExact(resultat);
	}

	public boolean peutMonterDeNiveau(List<PalierNiveau> paliers) {
		if (this.enRuine) {
			return false;
		}
		if (paliers == null || paliers.isEmpty()) {
			return false;
		}
		if (this.niveau >= 10) {
			return false;
		}

		int niveauSuivant = this.niveau + 1;
		PalierNiveau palierSuivant = paliers.stream()
				.filter(p -> p != null && p.getNiveau() == niveauSuivant)
				.findFirst()
				.orElse(null);

		if (palierSuivant == null) {
			return false;
		}

		int popTotale = populationDisponible + populationOuvriere + populationMilitaire + populationEnEntrainement;
		if (popTotale < palierSuivant.getPopulationRequise()) {
			return false;
		}

		if (palierSuivant.getPrerequis() != null) {
			for (QuantiteRessource requis : palierSuivant.getPrerequis()) {
				if (requis != null && !aAssezDeRessources(requis.getRessource(), requis.getQuantite())) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean ameliorerNiveau(List<PalierNiveau> paliers) {
		if (!peutMonterDeNiveau(paliers)) {
			System.out.println("Ressources ou population insuffisantes pour monter de niveau.");
			return false;
		}

		int niveauSuivant = this.niveau + 1;
		PalierNiveau palierSuivant = paliers.stream()
				.filter(p -> p != null && p.getNiveau() == niveauSuivant)
				.findFirst()
				.orElse(null);

		if (palierSuivant == null) {
			return false;
		}

		if (palierSuivant.getPrerequis() != null) {
			for (QuantiteRessource requis : palierSuivant.getPrerequis()) {
				if (requis != null) {
					boolean debitReussi = debiterRessources(requis.getRessource(), requis.getQuantite());
					if (!debitReussi) {
						throw new IllegalStateException("Impossible de débiter les ressources requises : " + requis.getRessource());
					}
				}
			}
		}

		setNiveau(niveauSuivant);
		System.out.println("Le royaume atteint le niveau " + this.niveau);
		return true;
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

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public Population getPeuple() {
		return peuple;
	}

	public void setPeuple(Population peuple) {
		if (this.peuple != null) {
			throw new UnsupportedOperationException("Le peuple d'un royaume est définitif et ne peut pas être modifié.");
		}
		validerPeuple(peuple);
		this.peuple = peuple;
	}

	public Population getPopulation() {
		return getPeuple();
	}

	public void setPopulation(Population population) {
		setPeuple(population);
	}

	public int getNiveau() {
		return niveau;
	}

	public void setNiveau(int niveau) {
		this.niveau = niveau;
	}

	public int getPopulationDisponible() {
		return populationDisponible;
	}

	public void setPopulationDisponible(int populationDisponible) {
		this.populationDisponible = populationDisponible;
	}

	public int getPopulationOuvriere() {
		return populationOuvriere;
	}

	public void setPopulationOuvriere(int populationOuvriere) {
		this.populationOuvriere = populationOuvriere;
	}

	public int getPopulationEnEntrainement() {
		return populationEnEntrainement;
	}

	public void setPopulationEnEntrainement(int populationEnEntrainement) {
		this.populationEnEntrainement = populationEnEntrainement;
	}

	public int getPopulationMilitaire() {
		return populationMilitaire;
	}

	public void setPopulationMilitaire(int populationMilitaire) {
		this.populationMilitaire = populationMilitaire;
	}

	public boolean isEnRuine() {
		return enRuine;
	}

	public void setEnRuine(boolean enRuine) {
		this.enRuine = enRuine;
	}

	public List<QuantiteRessource> getRessources() {
		return ressources.stream()
				.map(r -> new QuantiteRessource(r.getQuantite(), r.getRessource()))
				.toList();
	}

	public void setRessources(List<QuantiteRessource> ressources) {
		validerRessources(ressources);
		this.ressources = creerRessourcesParDefaut();
		if (ressources != null) {
			for (QuantiteRessource qr : ressources) {
				this.ressources.stream()
					.filter(r -> r.getRessource() == qr.getRessource())
					.findFirst()
					.ifPresent(r -> r.setQuantite(qr.getQuantite()));
			}
		}
	}

	// =========================================================================
	// Méthodes helpers & Validations
	// =========================================================================

	private static List<QuantiteRessource> creerRessourcesParDefaut() {
		return new ArrayList<>(List.of(
			new QuantiteRessource(0, Ressource.PIERRE),
			new QuantiteRessource(0, Ressource.BOIS),
			new QuantiteRessource(0, Ressource.OR),
			new QuantiteRessource(0, Ressource.NOURRITURE)
		));
	}

	

	private void validerPeuple(Population peuple) {
		
		if (!(peuple instanceof Canards
				|| peuple instanceof Gobelins
				|| peuple instanceof Licornes
				|| peuple instanceof Princesses
				|| peuple instanceof Teuteus)) {
			throw new IllegalArgumentException("Le peuple du royaume doit être l'un des 5 peuples disponibles (Canards, Gobelins, Licornes, Princesses, Teuteus).");
		}
	}

	
	

	private void validerRessources(List<QuantiteRessource> initiales) {
		if (initiales != null) {
			
			long nbDistinctes = initiales.stream()
					.map(QuantiteRessource::getRessource)
					.distinct()
					.count();
			if (nbDistinctes != initiales.size()) {
				throw new IllegalArgumentException("Il ne peut pas y avoir plusieurs quantiteRessource d'un même type de ressource.");
			}
		}
	}

	// =========================================================================
	// Représentation
	// =========================================================================

	@Override
	public String toString() {
		return "Royaume [id=" + id + ", nom=" + nom + ", peuple=" + (peuple != null ? peuple.getNom() : null)
				+ ", niveau=" + niveau + ", populationDisponible=" + populationDisponible + ", populationOuvriere="
				+ populationOuvriere + ", populationEnEntrainement=" + populationEnEntrainement
				+ ", populationMilitaire=" + populationMilitaire + ", enRuine=" + enRuine
				+ ", ressources=" + ressources + "]";
	}
}
