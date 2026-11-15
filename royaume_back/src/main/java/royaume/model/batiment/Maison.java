package royaume.model.batiment;
import java.util.List;

import royaume.config.ConfigurationPalliersBatiment;
import royaume.model.QuantiteRessource;
import royaume.model.Royaume;

public class Maison extends Batiment {

 // Le bonus global -> déplacer vers Royaume + pas en static :  sinon c'est commun a tout les royaumes
    //private static double bonusCroissance = 0.0;  
	
    private double bonusPropre =0.0;
    
    // Constructeur en privée, dans le package, car on ne doit passer que par Factory !
    
     Maison(Integer id, String nom, int niveau,
            int pv, int pvMax, boolean construction) {

        super(id, nom, niveau, pv, pvMax, construction);
       
       // Maison.bonusCroissance += 0.12;  -> avec retirerBonus et ajouterBonus pour si maison detruite
    }
     
     
     /////////////////////  A faire à chaque fois que la maison change de niveau + a la creation
     public void actualiserBonus(Royaume royaume) {

    	    if (royaume == null) {
    	        throw new IllegalArgumentException("Royaume null");
    	    }

    	    double nouveauBonus = 0.0;

    	    if (estOperationnel()) {
    	        nouveauBonus = calculerBonusPropre();
    	    }

    	    // Retirer l'ancien bonus de CE royaume
    	    
    	    royaume.retirerBonusCroissance(this.bonusPropre);

    	    // Mémoriser le nouveau bonus propre à cette maison
    	    
    	    this.bonusPropre = nouveauBonus;

    	    // Ajouter le nouveau bonus à CE royaume
    	    
    	    royaume.ajouterBonusCroissance(this.bonusPropre);
    	}

     /////////////////////

     public double calculerBonusPropre() {
    	    return 0.12 * getNiveau();
    	}

    
     public double getBonusPropre() {
    	    return bonusPropre;
    	}
     
   




	



	/*
	 * [REVIEW TICKET 1 - Réponse question Trello]
	 * Renvoyer `List.of()` quand le bâtiment est déjà au niveau max (10) et bloquer
	 * avec une exception dans ameliorer() c'est nickel, on laisse tel quel !
	 */
	@Override
    public List<QuantiteRessource> getCoutAmelioration() {
    	
    	int niveauSuivant = getNiveau() + 1;
         
         List<PallierBatiment> paliers = ConfigurationPalliersBatiment.getPaliers(Maison.class);
         
         for (PallierBatiment palier : paliers) {
             if (palier.getNiveau() == niveauSuivant) {
                 return palier.getCout();
             }
         }
         return List.of();
    }

    /*
    * [REVIEW TICKET 1 - Réponse question Trello]
    * Pour le temps de construction (fixe vs évolutif selon le niveau) :
    * Laisser le temps fixe c'est plus simple à mettre en place,
    * mais le faire évoluer avec le niveau (ex: 120 * niveau ou un truc du genre), 
    * c'est ce qu'on voit en général dans les jeux du style. Vous pouvez faire 
    * comme vous le sentez en vrai.
    */
    @Override
    public int getTempsConstructionBase() {
        return 120;
    }
}