package royaume.model;

import java.util.List;

public class Chateau extends Batiment{
	
	private int niveauPalierDebloque;
	private double bonusDefenseRoyaume;
	
	
	
	public Chateau(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax, boolean enConstruction,
			int niveauPalierDebloque, double bonusDefenseRoyaume) {
		super(id, nom, niveau, pointsDeVie, pointsDeVieMax, enConstruction);
		this.niveauPalierDebloque = niveauPalierDebloque;
		this.bonusDefenseRoyaume = bonusDefenseRoyaume;
	}

	
	
	public Chateau(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax, boolean enConstruction) {
		super(id, nom, niveau, pointsDeVie, pointsDeVieMax, enConstruction);
	}


	
	public int getNiveauPalierDebloque() {
		return niveauPalierDebloque;
	}



	public void setNiveauPalierDebloque(int niveauPalierDebloque) {
		this.niveauPalierDebloque = niveauPalierDebloque;
	}



	public double getBonusDefenseRoyaume() {
		return bonusDefenseRoyaume;
	}



	public void setBonusDefenseRoyaume(double bonusDefenseRoyaume) {
		this.bonusDefenseRoyaume = bonusDefenseRoyaume;
	}
	//__________________________________________________
	
	
	public void debloquerPalier() {
		
		// void (met à jour le niveau de technologie/palier accessible
		 
	}
	
	public boolean verifierConditionsVictoire() {
		
		//(renvoie true si le niveau atteint 10)
		
	return false;
	}
	
	public void alerteNiveau10() {
		
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
