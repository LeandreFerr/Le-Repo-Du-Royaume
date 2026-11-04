package royaume.model.batiment;

import java.util.List;

import royaume.model.QuantiteRessource;

public class Chateau extends Batiment{
	
	private int niveauPalierDebloque;
	private double bonusDefenseRoyaume;
	
	
    // Constructeur en privée, on ne doit passer que par Factory !

    Chateau(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax, boolean enConstruction,
			int niveauPalierDebloque, double bonusDefenseRoyaume) {
		super(id, nom, niveau, pointsDeVie, pointsDeVieMax, enConstruction);
		this.niveauPalierDebloque = niveauPalierDebloque;
		this.bonusDefenseRoyaume = bonusDefenseRoyaume;
	}

	//   this.niveauPalierDebloque = niveau;
    //   this.bonusDefenseRoyaume = 0.0;   --> A VERIF
	
	 Chateau(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax, boolean enConstruction) {
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
		  niveauPalierDebloque = niveau;
		 
	}
	
	//__________________________
	public boolean verifierConditionsVictoire() {
		
		//(renvoie true si le niveau atteint 10)
        return niveau == 10;
	}
	
	public void alerteNiveau10() {
		if (verifierConditionsVictoire()) {
            System.out.println("Vous avez dead ça ! Niveau 10 atteint - End Game");
        }
	}
	//__________________________


	@Override
	public List<QuantiteRessource> getCoutAmelioration(){
	// coût nécessaire pour lancer le niveau suivant 
		
        return List.of();
	}
	
	@Override
	public int getTempsConstructionBase() {
	//  durée de base en secondes
		
		 return 300;	}
}
