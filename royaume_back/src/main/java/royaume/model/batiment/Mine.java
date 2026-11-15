package royaume.model.batiment;
import java.util.List;

import royaume.config.ConfigurationPalliersBatiment;
import royaume.model.QuantiteRessource;

/*
 * [REVIEW TICKET 1] Voir le commentaire détaillé dans BatimentProduction.java :
 * Selon le nouvel UML (suite à la réunion pour le valider avec Jordan), 
 * cette classe peut être supprimée au profit d'une classe
 * BatimentProduction concrète 
 * (ce qui corrige aussi le bug `typeRessource == null`
 * causé par l'appel à super(...) sans Ressource.OR).
 */
public class Mine extends BatimentProduction {

    private int productionOrHeure = 2;

    Mine(Integer id, String nom, int niveau,
         int pv, int pvMax, boolean construction,
         int productionOrHeure) {

        super(id, nom, niveau, pv, pvMax, construction);
        this.productionOrHeure = productionOrHeure;
    }

    public int produireOr() {
        if (!estOperationnel()) {
            return 0;
        }

        return productionOrHeure * niveau;
    }

    @Override
    public int calculerProduction() {
        return produireOr();
    }

    @Override
    public List<QuantiteRessource> getCoutAmelioration() {
    	 int niveauSuivant = getNiveau() + 1;
         
         List<PallierBatiment> paliers = ConfigurationPalliersBatiment.getPaliers(Mine.class);
         
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