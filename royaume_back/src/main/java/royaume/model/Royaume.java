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

	private List<QuantiteRessource> ressources = new ArrayList<>(); // final ou pas final ? 

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


	public void crediterRessource(Ressource ressource, int quantite) {
		if (ressource == null || quantite <= 0) {
			return;
		}

		for (QuantiteRessource qr : ressources) {
			if (qr.getRessource() == ressource) {
				qr.ajouter(quantite);
				return;
			}
		}
	}

	public boolean aAssezRessource(Ressource ressource, int quantite) {
		if (ressource == null || quantite <= 0) {
			return false;
		}
		for (QuantiteRessource qr : ressources) {
			if (qr.getRessource() == ressource) {
				return qr.getQuantite() >= quantite; }
		}
		return false;
	}


	public boolean debiterRessource(Ressource ressource, int quantite) {
		if (ressource == null || quantite <= 0) {
			return false;}

		if (!aAssezRessource(ressource, quantite)) {
			return false;}

		for (QuantiteRessource qr : ressources) {
			if (qr.getRessource() == ressource) {
				qr.retirer(quantite);
				return true;}
		}
		return false;
	}



}
