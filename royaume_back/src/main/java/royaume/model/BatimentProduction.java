package royaume.model;

import java.util.List;


public class BatimentProduction extends Batiment{

	
	private Ressource typeRessource;
	private int productionBaseHeure;
	
	
	
	
	
	public BatimentProduction(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax,
			boolean enConstruction, Ressource typeRessource, int productionBaseHeure) {
		super(id, nom, niveau, pointsDeVie, pointsDeVieMax, enConstruction);
		this.typeRessource = typeRessource;
		this.productionBaseHeure = productionBaseHeure;
	}
	
	

	public BatimentProduction(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax,
			boolean enConstruction) {
		super(id, nom, niveau, pointsDeVie, pointsDeVieMax, enConstruction);
	}



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
		return 2;
	}
	
	
	@Override
	public List<QuantiteRessource> getCoutAmelioration(){
	// coût nécessaire pour lancer le niveau suivant 
		
		// return List<QuantiteRessource> ;
	}
	
	@Override
	public int getTempsConstructionBase() {
	//  durée de base en secondes ou ticks
		return 2;
	}
}
