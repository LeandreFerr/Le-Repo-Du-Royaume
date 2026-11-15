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
 * causé par l'appel à super(...) sans Ressource.BOIS).
 */
public class Scierie extends BatimentProduction {

    private int productionBoisHeure = 24;

    Scierie(Integer id, String nom, int niveau,
            int pv, int pvMax, boolean construction,
            int productionBoisHeure) {

        super(id, nom, niveau, pv, pvMax, construction);
        this.productionBoisHeure = productionBoisHeure;
    }

    public int produireBois() {
        if (!estOperationnel()) {
            return 0;
        }

        return productionBoisHeure * niveau;
    }

    @Override
    public int calculerProduction() {
        return produireBois();
    }

    @Override
    public List<QuantiteRessource> getCoutAmelioration() {
    	 int niveauSuivant = getNiveau() + 1;
         
         List<PallierBatiment> paliers = ConfigurationPalliersBatiment.getPaliers(Scierie.class);
         
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

    public int getProductionBoisHeure() {
        return productionBoisHeure;
    }
}