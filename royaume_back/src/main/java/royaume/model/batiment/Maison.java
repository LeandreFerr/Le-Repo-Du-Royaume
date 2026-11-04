package royaume.model.batiment;
import java.util.List;

import royaume.model.QuantiteRessource;

public class Maison extends Batiment {

    private int capaciteLogement;
    private double bonusCroissance = 0.3; // ex: taux de croissance de la pop augmente ?

    
    
    // Constructeur en privée, dans le package, car on ne doit passer que par Factory !
    
     Maison(Integer id, String nom, int niveau,
            int pv, int pvMax, boolean construction,
            int capaciteLogement, double bonusCroissance) {

        super(id, nom, niveau, pv, pvMax, construction);
        this.capaciteLogement = capaciteLogement;
        this.bonusCroissance = bonusCroissance;
    }

    public int getCapaciteLogement() {
        return capaciteLogement * niveau;
    }

    @Override
    public List<QuantiteRessource> getCoutAmelioration() {
        return List.of();
    }

    @Override
    public int getTempsConstructionBase() {
        return 120;
    }
}