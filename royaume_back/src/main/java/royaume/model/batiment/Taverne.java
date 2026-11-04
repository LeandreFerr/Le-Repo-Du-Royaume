package royaume.model.batiment;

import java.util.List;

import royaume.model.QuantiteRessource;

public class Taverne extends BatimentProduction {

    private int productionNourritureHeure = 50;

    Taverne(Integer id, String nom, int niveau,
            int pv, int pvMax, boolean construction,
            int productionNourritureHeure) {

        super(id, nom, niveau, pv, pvMax, construction);
        this.productionNourritureHeure = productionNourritureHeure;
    }

    public int produireNourriture() {
        if (!estOperationnel()) {
            return 0;
        }

        return productionNourritureHeure * niveau;
    }

    @Override
    public int calculerProduction() {
        return produireNourriture();
    }

    @Override
    public List<QuantiteRessource> getCoutAmelioration() {
        return List.of();
    }

    @Override
    public int getTempsConstructionBase() {
        return 180;
    }

    public int getProductionNourritureHeure() {
        return productionNourritureHeure;
    }
}