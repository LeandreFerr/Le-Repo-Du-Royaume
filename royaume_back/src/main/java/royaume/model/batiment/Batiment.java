package royaume.model.batiment;

import java.util.List;

import royaume.model.QuantiteRessource;

public abstract class Batiment {
	
	protected Integer id;
	protected String nom;
	protected int niveau = 1;
	protected int pointsDeVie;
	protected int pointsDeVieMax;
	protected boolean enConstruction =false; // A passer à true dans Chantier mais là init à false.
	
	public Batiment(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax, boolean enConstruction) {
		this.id = id;
		this.nom = nom;
		this.niveau = niveau;
		this.pointsDeVie = pointsDeVie;
		this.pointsDeVieMax = pointsDeVieMax;
		this.enConstruction = enConstruction;
	}

	public Batiment() {
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

	public int getPointsDeVie() {
		return pointsDeVie;
	}

	public void setPointsDeVie(int pointsDeVie) {
		this.pointsDeVie = pointsDeVie;
	}

	public int getPointsDeVieMax() {
		return pointsDeVieMax;
	}

	public void setPointsDeVieMax(int pointsDeVieMax) {
		this.pointsDeVieMax = pointsDeVieMax;
	}

	public boolean isEnConstruction() {
		return enConstruction;
	}

	public void setEnConstruction(boolean enConstruction) {
		this.enConstruction = enConstruction;
	}
	
	//___________________________________________________________________
	
	public void subirDegats(int degats) {
		
		// diminue les points de vie sans descendre sous 0 :
		
		if (degats < 0) {
            throw new IllegalArgumentException("Dégâts négatifs interdits");
        }
        pointsDeVie = Math.max(0, pointsDeVie - degats);
	}
	
	
	public void reparer(int montant) {
		
		// augmente les PV sans dépasser pointsDeVieMax :
		
		if (montant < 0) {
            throw new IllegalArgumentException("Réparation négative interdite");
        }
        pointsDeVie = Math.min(pointsDeVieMax, pointsDeVie + montant);
	}
	
	
	public boolean estOperationnel() {
		
		// renvoie true si pointsDeVie > 0 AND !enConstruction : 
		
        return pointsDeVie > 0 && !enConstruction;
	}
	
	//______________________
	
	public void ameliorer() {
		
		// augmente le niveau et recalcule les pointsDeVieMax
		// Si amélioration : Retrouve le max de point de vie
		
		niveau++;
        pointsDeVieMax = calculerPointsDeVieMax();
        pointsDeVie = pointsDeVieMax;
	}
	
	protected int calculerPointsDeVieMax() {
        return 50 * niveau;
    }
	//______________________


	
	public abstract List<QuantiteRessource> getCoutAmelioration();
		// coût nécessaire pour lancer le niveau suivant 
	
	
	public abstract int getTempsConstructionBase() ;
	//  durée de base en secondes ou ticks
	
}
