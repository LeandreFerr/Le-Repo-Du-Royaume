package royaume.model.batiment;
import java.util.List;

import royaume.config.ConfigurationPalliersBatiment;
import royaume.model.QuantiteRessource;

public class Maison extends Batiment {

    
    private static double bonusCroissance = 0.0; // ex: taux de croissance de la pop augmente ?

    
    // Constructeur en privée, dans le package, car on ne doit passer que par Factory !
    
     Maison(Integer id, String nom, int niveau,
            int pv, int pvMax, boolean construction) {

        super(id, nom, niveau, pv, pvMax, construction);
       
        Maison.bonusCroissance += 0.12;
    }
     
     


    public static double getBonusCroissance() {

    	return bonusCroissance;
	}




	 public static void setBonusCroissance(double bonusCroissance) {
		 Maison.bonusCroissance = bonusCroissance;
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