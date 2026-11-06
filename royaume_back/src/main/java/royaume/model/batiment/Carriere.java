package royaume.model.batiment;
import java.util.List;

import royaume.config.ConfigurationPalliersBatiment;
import royaume.model.QuantiteRessource;

public class Carriere extends BatimentProduction {

    private int productionPierreHeure = 30; // #nb. Pierre /heure

    Carriere(Integer id, String nom, int niveau,
             int pv, int pvMax, boolean construction,
             int productionPierreHeure) {

        super(id, nom, niveau, pv, pvMax, construction);
        this.productionPierreHeure = productionPierreHeure;
    }

    public int produirePierre() {
        if (!estOperationnel()) {
            return 0;
        }

        return productionPierreHeure * niveau;
    }

    @Override
    public int calculerProduction() {
        return produirePierre();
    }

    @Override
    public List<QuantiteRessource> getCoutAmelioration() {
        int niveauSuivant = getNiveau() + 1;
        
        List<PallierBatiment> paliers = ConfigurationPalliersBatiment.getPaliers(Carriere.class);
        
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