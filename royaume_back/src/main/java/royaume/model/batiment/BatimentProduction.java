package royaume.model.batiment;

import java.util.List;

import royaume.config.ConfigurationPalliersBatiment;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;


/*
 * ============================================================================
 * [REVIEW TICKET 1 - Arbre des bâtiments]
 * ============================================================================
 * Déjà je tiens à dire que vous avez hyper bien bossé
 *
 * 1) Note UML (my bad) :
 *    Vous aviez l'ancienne version de l'UML, je n'avais pas dû vous envoyer
 *    la nouvelle version ! Sur le nouvel UML, BatimentProduction est une classe
 *    CONCRÈTE (pas abstract), et il n'y a plus les 4 sous-classes Mine, Carriere,
 *    Scierie et Taverne (qui dupliquent quasiment le même code).
 *    Je l'avais modifié suite à la réunion avec Jordan pour valider le diagramme.
 *
 * 2) Bug actuel causé par les 4 sous-classes :
 *    Mine, Carriere, Scierie et Taverne appellent le constructeur à 6 paramètres
 *    `super(id, nom, niveau, pv, pvMax, construction)` qui laisse `typeRessource = null`.
 *    Du coup, dans Royaume.ajouterBatiment(), le test :
 *      `b.getTypeRessource() == production.getTypeRessource()`
 *    compare `null == null` (true) et EMPÊCHE de construire une Carrière/Scierie/Taverne
 *    si le royaume possède déjà une Mine !
 *
 * 3) A corriger (alignement nouvel UML) :
 *    - Rendre BatimentProduction concrète (retirer `abstract`) et supprimer le
 *      constructeur à 6 paramètres pour obliger à renseigner `typeRessource` et
 *      `productionBaseHeure`.
 *    - Déplacer getCoutAmelioration() et getTempsConstructionBase() (180s)
 *      directement ici dans BatimentProduction.
 *    - Supprimer Mine.java, Carriere.java, Scierie.java et Taverne.java
 *      (ce sera en plus beaucoup plus simple à mapper en JPA juste après !).
 * 
 * ============================================================================
 */
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
