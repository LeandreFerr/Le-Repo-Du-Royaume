package royaume.model;

import java.util.Objects;

public abstract class Population {

	// =========================================================================
	// Attributs
	// =========================================================================

	protected Integer id;
	protected String nom;
	protected String titreSouverain;
	protected Double attraction;
	protected String capaciteSpeciale;

	// =========================================================================
	// Constructeurs
	// =========================================================================

	public Population() {}

	public Population(Integer id, String nom, String titreSouverain, Double attraction) {
		this(id, nom, titreSouverain, attraction, null);
	}

	public Population(Integer id, String nom, String titreSouverain, Double attraction, String capaciteSpeciale) {
		checkId(id);
		checkNom(nom);
		checkTitreSouverain(titreSouverain);
		checkAttraction(attraction);
		checkCapaciteSpeciale(capaciteSpeciale);

		this.id = id;
		this.nom = nom;
		this.titreSouverain = titreSouverain;
		this.attraction = attraction;
		this.capaciteSpeciale = capaciteSpeciale;
	}

	// =========================================================================
	// Combat et modificateurs passifs
	// =========================================================================

	public abstract double getAvantageCombatContre(Population adversaire);

	public double getBonusAttaque() {
		return 1.0;
	}

	public double getTauxPillage() {
		return 0.0;
	}

	public double getMalusCombatAdversaire() {
		return 1.0;
	}

	public double getMalusProductionAdversaire() {
		return 1.0;
	}

	public double getBonusProduction(Ressource ressource) {
		return 1.0;
	}

	public int getBonusCroissancePopulation() {
		return 0;
	}

	// =========================================================================
	// Capacité spéciale
	// =========================================================================

	public abstract String getDescriptionCapacite();

	public void activerCapaciteSpecial() {
		System.out.println("Vous activez votre capacité : " + (capaciteSpeciale != null ? capaciteSpeciale : "inconnue"));
	}

	// =========================================================================
	// Getters & Setters
	// =========================================================================

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		checkId(id);
		this.id = id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		checkNom(nom);
		this.nom = nom;
	}

	public String getTitreSouverain() {
		return titreSouverain;
	}

	public void setTitreSouverain(String titreSouverain) {
		checkTitreSouverain(titreSouverain);
		this.titreSouverain = titreSouverain;
	}

	public Double getAttraction() {
		return attraction;
	}

	public void setAttraction(Double attraction) {
		checkAttraction(attraction);
		this.attraction = attraction;
	}

	public String getCapaciteSpeciale() {
		return capaciteSpeciale;
	}

	public void setCapaciteSpeciale(String capaciteSpeciale) {
		checkCapaciteSpeciale(capaciteSpeciale);
		this.capaciteSpeciale = capaciteSpeciale;
	}

	// =========================================================================
	// Validations des invariants
	// =========================================================================

	private void checkId(Integer id) {
		if (id != null && id <= 0) {
			throw new IllegalArgumentException("L'id ne peut pas être inférieur ou égal à 0.");
		}
	}

	private void checkNom(String nom) {
		if (nom == null || nom.isBlank()) {
			throw new IllegalArgumentException("Le nom de la population ne peut pas être vide ou null.");
		}
	}

	private void checkTitreSouverain(String titreSouverain) {
		if (titreSouverain == null || titreSouverain.isBlank()) {
			throw new IllegalArgumentException("Le titre du souverain ne peut pas être vide ou null.");
		}
	}

	private void checkAttraction(Double attraction) {
		if (attraction == null) {
			throw new IllegalArgumentException("Le taux d'attraction ne peut pas être null.");
		}
		if (Double.isNaN(attraction) || Double.isInfinite(attraction)) {
			throw new IllegalArgumentException("Le taux d'attraction doit être un nombre fini.");
		}
		if (attraction < 0.0) {
			throw new IllegalArgumentException("Le taux d'attraction ne peut pas être négatif.");
		}
	}

	private void checkCapaciteSpeciale(String capaciteSpeciale) {
		if (capaciteSpeciale != null && capaciteSpeciale.isBlank()) {
			throw new IllegalArgumentException("La capacité spéciale ne peut pas être une chaîne blanche.");
		}
	}

	// =========================================================================
	// Object
	// =========================================================================

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Population that = (Population) o;
		return Objects.equals(id, that.id) && Objects.equals(nom, that.nom);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, nom);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", nom=" + nom + ", titreSouverain=" + titreSouverain
				+ ", attraction=" + attraction + ", capaciteSpeciale=" + capaciteSpeciale + "]";
	}
}
