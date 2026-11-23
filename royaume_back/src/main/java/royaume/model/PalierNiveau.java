package royaume.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "palier_niveau")
public class PalierNiveau {

	// =========================================================================
	// Attributs
	// =========================================================================

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@NotNull
	@Min(2)
	@Max(10)
	@Column(nullable = false, unique = true)
	private int niveau;
	@NotNull
	@PositiveOrZero
	@Column(nullable = false)
	private int populationRequise;
	@NotNull
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "json")
	private List<QuantiteRessource> prerequis = new ArrayList<>();

	// =========================================================================
	// Constructeurs
	// =========================================================================

	public PalierNiveau() {}

	public PalierNiveau(Integer id, int niveau, int populationRequise, List<QuantiteRessource> prerequis) {
		
		setPrerequis(prerequis);
		this.id = id;
		this.niveau = niveau;
		this.populationRequise = populationRequise;
	}

	// =========================================================================
	// Méthodes utilitaires statiques
	// =========================================================================

	public static int getCoutRessource(Ressource ressourceRecherchee, Royaume royaume, List<PalierNiveau> paliers) {
		if (royaume == null || paliers == null || ressourceRecherchee == null) {
			return 0;
		}

		int niveauActuel = royaume.getNiveau();
		PalierNiveau palierSuivant = paliers.stream()
				.filter(p -> p != null && p.getNiveau() == niveauActuel + 1)
				.findFirst()
				.orElse(null);

		if (palierSuivant == null || palierSuivant.getPrerequis() == null) {
			return 0;
		}

		int quantiteNecessaire = palierSuivant.getPrerequis().stream()
				.filter(r -> r != null && r.getRessource() == ressourceRecherchee)
				.mapToInt(QuantiteRessource::getQuantite)
				.findFirst()
				.orElse(0);

		System.out.println("Ressource : " + ressourceRecherchee + " | Quantité nécessaire : " + quantiteNecessaire);
		return quantiteNecessaire;
	}

	public static int getRessourceManquante(Ressource ressourceRecherchee, Royaume royaume, List<PalierNiveau> paliers) {
		if (royaume == null || paliers == null || ressourceRecherchee == null) {
			return 0;
		}

		int quantiteNecessaire = getCoutRessource(ressourceRecherchee, royaume, paliers);
		if (quantiteNecessaire <= 0) {
			return 0;
		}

		int quantitePossedee = royaume.getRessources().stream()
				.filter(r -> r != null && r.getRessource() == ressourceRecherchee)
				.mapToInt(QuantiteRessource::getQuantite)
				.sum();

		int quantiteManquante = Math.max(0, quantiteNecessaire - quantitePossedee);
		System.out.println("Ressource : " + ressourceRecherchee + " | Quantité manquante : " + quantiteManquante);
		return quantiteManquante;
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

	public int getNiveau() {
		return niveau;
	}

	public void setNiveau(int niveau) {
		this.niveau = niveau;
	}

	public int getPopulationRequise() {
		return populationRequise;
	}

	public void setPopulationRequise(int populationRequise) {
		this.populationRequise = populationRequise;
	}

	public List<QuantiteRessource> getPrerequis() {
		if (prerequis == null) {
			return Collections.emptyList();
		}
		return prerequis.stream()
				.map(q -> new QuantiteRessource(q.getQuantite(), q.getRessource()))
				.toList();
	}

	public void setPrerequis(List<QuantiteRessource> prerequis) {
		if (prerequis == null) {
			this.prerequis = new ArrayList<>();
			return;
		}
		for (QuantiteRessource qr : prerequis) {
			if (qr == null) {
				throw new IllegalArgumentException("Les prérequis ne peuvent pas contenir d'élément null.");
			}
		}
		long nbDistinctes = prerequis.stream()
				.map(QuantiteRessource::getRessource)
				.distinct()
				.count();
		if (nbDistinctes != prerequis.size()) {
			throw new IllegalArgumentException("Un palier ne peut pas contenir plusieurs prérequis pour une même ressource.");
		}
		this.prerequis = prerequis.stream()
				.map(q -> new QuantiteRessource(q.getQuantite(), q.getRessource()))
				.toList();
	}

	// =========================================================================
	// Equals, HashCode, ToString
	// =========================================================================

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof PalierNiveau that)) return false;
		return niveau == that.niveau && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, niveau);
	}

	@Override
	public String toString() {
		return "PalierNiveau [id=" + id + ", niveau=" + niveau + ", populationRequise=" + populationRequise
				+ ", prerequis=" + prerequis + "]";
	}
}
