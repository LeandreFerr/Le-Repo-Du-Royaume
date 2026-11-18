package royaume.model.batiment;

import java.util.List;

import royaume.config.ConfigurationPalliersBatiment;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;


public class BatimentProduction extends Batiment{

	
	private Ressource typeRessource;
	private int productionBaseHeure;  
	
	
	
	
	
	BatimentProduction(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax,
			boolean enConstruction, Ressource typeRessource, int productionBaseHeure) {
		super(id, nom, niveau, pointsDeVie, pointsDeVieMax, enConstruction);
		this.typeRessource = typeRessource;
		this.productionBaseHeure = productionBaseHeure;
	}



	/*
    BatimentProduction(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax,
			boolean enConstruction, Ressource typeRessource, int productionBaseHeure) {
		super(id, nom, niveau, pointsDeVie, pointsDeVieMax, enConstruction);
		this.typeRessource = typeRessource;
		this.productionBaseHeure = productionBaseHeure;
	}
	
	

    BatimentProduction(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax,
			boolean enConstruction) {
		super(id, nom, niveau, pointsDeVie, pointsDeVieMax, enConstruction);
	}*/



	public Ressource getTypeRessource() {
		return typeRessource;
	}



	public void setTypeRessource(Ressource typeRessource) {
		this.typeRessource = typeRessource;
	}



	public int getProductionBaseHeure() {
		return productionBaseHeure;
	}



	public void setProductionBaseHeure(int productionBaseHeure) {
		this.productionBaseHeure = productionBaseHeure;
	}



	public int calculerProduction() {
		// calcule le rendement effectif basé sur le niveau et renvoie 0 si non opérationnel
		 if (!estOperationnel()) {
	            return 0;
	        }

	        return productionBaseHeure * niveau;   // Constante * niveau
	    	
	}												
	

	@Override
	public List<QuantiteRessource> getCoutAmelioration() {
	
	    int niveauSuivant = getNiveau() + 1;
	
	    List<PallierBatiment> paliers =
	        ConfigurationPalliersBatiment.getPaliersProduction(
	            getTypeRessource()
	        );
	
	    for (PallierBatiment palier : paliers) {
	        if (palier.getNiveau() == niveauSuivant) {
	            return palier.getCout();
	        }
	    }
	
	    return List.of();
	}


    @Override
    public int getTempsConstructionBase() {
        return 180;
    }
	
}
