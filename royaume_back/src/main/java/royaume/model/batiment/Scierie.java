package royaume.model.batiment;

import java.util.List;

import royaume.model.QuantiteRessource;

public class Scierie extends BatimentProduction {

    private int productionBoisHeure;

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