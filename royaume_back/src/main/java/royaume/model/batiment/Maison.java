package royaume.model.batiment;
import java.util.List;

import royaume.config.ConfigurationPalliersBatiment;
import royaume.model.QuantiteRessource;
import royaume.model.Royaume;

public class Maison extends Batiment {

    
    //private static double bonusCroissance = 0.0; // Le bonus global -> déplacer vers Royaume sinon c'est commun a tout les royaumes
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

    @Override
    public int getTempsConstructionBase() {
        return 120;
    }
}