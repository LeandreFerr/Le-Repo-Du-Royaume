package royaume.model.batiment;
import java.util.List;

import royaume.model.QuantiteRessource;

public class Carriere extends BatimentProduction {

    private int productionPierreHeure;

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
        return List.of();
    }

    @Override
    public int getTempsConstructionBase() {
        return 180;
    }
}