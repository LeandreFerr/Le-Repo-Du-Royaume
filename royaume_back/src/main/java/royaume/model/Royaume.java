package royaume.model;

import java.util.ArrayList;
import java.util.List;

public class Royaume {

	private Integer id;
	private String nom;
	private int niveau = 1;
	private int populationDisponible = 100;
	private int populationOuvriere = 0;
	private int populationEnEntrainement = 0;
	private int populationMilitaire = 0;
	private boolean enRuine = false;

	private List<QuantiteRessource> ressources = new ArrayList<>();


	// ---------------- CONSTRUCTEURS --------------------------

	public Royaume() {}


	public Royaume(Integer id, String nom) {
		this.id = id;
		this.nom = nom;
	}


	public Royaume(Integer id, String nom, int niveau, int populationDisponible, int populationOuvriere,
			int populationEnEntrainement, int populationMilitaire, boolean enRuine,
			List<QuantiteRessource> ressources) {
		this.id = id;
		this.nom = nom;
		this.niveau = niveau;
		this.populationDisponible = populationDisponible;
		this.populationOuvriere = populationOuvriere;
		this.populationEnEntrainement = populationEnEntrainement;
		this.populationMilitaire = populationMilitaire;
		this.enRuine = enRuine;
		this.ressources = ressources;
	}

	// ------------------------- Méthodes métier -------------------------------


	public void crediterRessources(Ressource ressource, int quantite) {
		ressources.stream()
			.filter(r -> r.getRessource() == ressource)
			.findFirst()
			.ifPresent(r -> r.ajouter(quantite));
	}


	public boolean aAssezDeRessources(Ressource ressource, int quantite) {
		return ressources.stream()
			.filter(r -> r.getRessource() == ressource)
			.anyMatch(r -> r.getQuantite()>= quantite);
	}

	public boolean debiterRessources(Ressource ressource, int quantite) {
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
		// A voir si on l'utilise étant donné qu'on a le bâtiment "maison"
		populationDisponible = (int) Math.round(populationDisponible * (1 + taux));
	}



	// -------------------------Getters / Setters ------------------------------




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
		return ressources;
	}


	public void setRessources(List<QuantiteRessource> ressources) {
		this.ressources = ressources;
	}


	@Override
	public String toString() {
		return "Royaume [id=" + id + ", nom=" + nom + ", niveau=" + niveau + ", populationDisponible="
				+ populationDisponible + ", populationOuvriere=" + populationOuvriere + ", populationEnEntrainement="
				+ populationEnEntrainement + ", populationMilitaire=" + populationMilitaire + ", enRuine=" + enRuine
				+ ", ressources=" + ressources + "]";
	}

}
