package royaume.model.batiment;

import java.util.List;

import royaume.model.QuantiteRessource;

public class Chateau extends Batiment{
	
	private static int niveauPalierDebloque;
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
		this.niveauPalierDebloque = niveau;
		this.bonusDefenseRoyaume = 0.0;
	}


	
	public static int getNiveauPalierDebloque() {
		return niveauPalierDebloque;
	}



	public static void setNiveauPalierDebloque(int niveauPalierDebloque) {
		Chateau.niveauPalierDebloque = niveauPalierDebloque;
	}



	public double getBonusDefenseRoyaume() {
		return bonusDefenseRoyaume;
	}



	public void setBonusDefenseRoyaume(double bonusDefenseRoyaume) {
		this.bonusDefenseRoyaume = bonusDefenseRoyaume;
	}
	
	
	//__________________________________________________   // Chateau passe au niveau 2 si on est au niveau 2 
	
	// Si pv du chateau arrive à zero ?  
	
	//Si on te detruit ton chateau quel que soit ton niveau de Royaume alors : enRuine = true; (et Chateau(PV) = 0)

	
	// si tu es en ruine alors les autres peuvent te piquer tes ressources + population  ?? 
	
	
	
	//____________________
	
	// FAIRE L'INVERSE : Si on passe au niveau 2 le chateau passe au niveau 2 automatiquement (ses PVmax n'augmente pas)
	
	// Remplacée par améliorer
	
	/*public void synchroniserAvecNiveauRoyaume(int nouveauNiveauRoyaume) {
        this.niveau = nouveauNiveauRoyaume;
        debloquerPalier();
    }*/
	
	@Override
	public void ameliorer() {
	
		niveau++;
	    debloquerPalier();}
	
	
	public void debloquerPalier() {
		//ex :  Si t'es niveau 2 bah tu débloque le niveau 2 pour tout les batiments (chateau inclu)
		  niveauPalierDebloque = niveau;	 
	}  
	

	
	//__________________________
	public boolean verifierConditionsVictoire() {
	
        return niveau == 10;  	//(renvoie true si le niveau atteint 10)
	}
	
	public void alerteNiveau10() {
		if (verifierConditionsVictoire()) {
            System.out.println("Vous avez dead ça ! Niveau 10 atteint - End Game");
        }
	}
	
	
	
	//__________________________ 

// On n'améliore pas le chateau, on passe seulement au niveau supérieur et c'est ça qui améliore le chateau donc ça renvoie une liste vide et temps de construction = 0
  
	@Override
	public List<QuantiteRessource> getCoutAmelioration(){
	// coût nécessaire pour lancer le niveau suivant 
		
        return List.of();
	}
	
	@Override
	public int getTempsConstructionBase() {
	//  durée de base en secondes
		
		 return 0;	}
}
