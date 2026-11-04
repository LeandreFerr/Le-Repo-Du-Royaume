package royaume.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.Chateau;

public class Royaume {

	private Integer id;
	private String nom;
	private int niveau = 1;
	private int populationDisponible = 100;
	private int populationOuvriere = 0;
	private int populationEnEntrainement = 0;
	private int populationMilitaire = 0;
	private boolean enRuine = false;

	
	// Ressources automatiquement offerte à la création du Royaume (niveau1, début de partie) :
	
	private List<QuantiteRessource> ressources = new ArrayList<>(List.of(
		    new QuantiteRessource(40, Ressource.BOIS),
		    new QuantiteRessource(40, Ressource.PIERRE),
		    new QuantiteRessource(20, Ressource.OR),
		    new QuantiteRessource(100, Ressource.NOURRITURE)
		));
	
	// Batiments automatiquement offerte à la création -> Factory  :
	
		private List<Batiment> batiments = new ArrayList<>(List.of(
			    //new Chateau(1, "chateau", 1, 50, 50, false) //pourrait être en construction -> true 
			));


	// ---------------- CONSTRUCTEURS --------------------------

	public Royaume() {}


	public Royaume(Integer id, String nom) {
		this.id = id;
		this.nom = nom;
		BatimentFactory.creerChateauInitial(this);
	}


	public Royaume(Integer id, String nom, int niveau, int populationDisponible, int populationOuvriere,
			int populationEnEntrainement, int populationMilitaire, boolean enRuine,
			List<QuantiteRessource> ressources, List<Batiment> batiments) {
		this.id = id;
		this.nom = nom;
		this.niveau = niveau;
		this.populationDisponible = populationDisponible;
		this.populationOuvriere = populationOuvriere;
		this.populationEnEntrainement = populationEnEntrainement;
		this.populationMilitaire = populationMilitaire;
		this.enRuine = enRuine;
		this.ressources = ressources;
		this.batiments = batiments;
		BatimentFactory.creerChateauInitial(this);

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
		BatimentFactory.creerChateauInitial(this);

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

	
	public boolean peutMonterDeNiveau(List<PallierNiveau> paliers) {

	    PallierNiveau palierSuivant = paliers.stream().filter(p -> p.getNiveau() == this.niveau + 1).findFirst().orElse(null);

	    if (palierSuivant == null) {
	        return false;}

	    int popTotale = populationDisponible + populationOuvriere + populationMilitaire + populationEnEntrainement;

	    if (popTotale < palierSuivant.getPopulationRequise()) {
	        return false;}

	    for (QuantiteRessource requis : palierSuivant.getPrerequis()) {
	        if (!aAssezDeRessources(
	                requis.getRessource(),
	                requis.getQuantite())) {
	            return false;}
	    }
	    return true;
	}

	
	public void ameliorerNiveau(List<PallierNiveau> paliers) {

	    if (!peutMonterDeNiveau(paliers)) {
	        System.out.println(
	                "Ressources ou population insuffisantes pour monter de niveau.");
	        return;}

	    int niveauSuivant = this.niveau + 1;

	    PallierNiveau palierSuivant = paliers.stream().filter(p -> p.getNiveau() == niveauSuivant).findFirst().orElse(null);

	    // Débiter les ressources
	    
	    for (QuantiteRessource requis : palierSuivant.getPrerequis()) {
	        boolean debitReussi = debiterRessources(
	                requis.getRessource(),
	                requis.getQuantite());

	        if (!debitReussi) {
	            throw new IllegalStateException("Impossible de débiter les ressources requises.");}}

	    this.niveau = niveauSuivant;
	    System.out.println("Le royaume atteint le niveau " + this.niveau);
	}
	
	//_____________________________________________________
	
	
	public void ajouterBatiment(Batiment batiment) {

	    if (batiment == null) {
	        throw new IllegalArgumentException(
	            "Le bâtiment ne peut pas être null"
	        );
	    }

	    // Les maisons peuvent être présentes en plusieurs exemplaires

	    // Un seul château par royaume
	    if (batiment instanceof Chateau) {

	        boolean chateauExiste = batiments.stream()
	            .anyMatch(b -> b instanceof Chateau);

	        if (chateauExiste) {
	            throw new IllegalArgumentException(
	                "Un château existe déjà dans ce royaume"
	            );
	        }
	    }

	    // Un seul bâtiment de production par ressource
	    if (batiment instanceof BatimentProduction production) {

	        boolean ressourceExiste = batiments.stream()
	            .filter(BatimentProduction.class::isInstance)
	            .map(BatimentProduction.class::cast)
	            .anyMatch(b ->
	                b.getTypeRessource() == production.getTypeRessource()
	            );

	        if (ressourceExiste) {
	            throw new IllegalArgumentException(
	                "Un bâtiment produit déjà cette ressource dans ce royaume"
	            );
	        }
	    }

	    batiments.add(batiment);
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


	public List<Batiment> getBatiments() {
        return Collections.unmodifiableList(batiments);
	}


	/*public void setBatiments(List<Batiment> batiments) {
		this.batiments = batiments;
	}*/ // C'est FACTORY 


	@Override
	public String toString() {
		return "Royaume [id=" + id + ", nom=" + nom + ", niveau=" + niveau + ", populationDisponible="
				+ populationDisponible + ", populationOuvriere=" + populationOuvriere + ", populationEnEntrainement="
				+ populationEnEntrainement + ", populationMilitaire=" + populationMilitaire + ", enRuine=" + enRuine
				+ ", ressources=" + ressources + "]";
	}

}
