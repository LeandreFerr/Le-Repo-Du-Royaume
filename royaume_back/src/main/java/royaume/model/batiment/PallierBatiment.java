package royaume.model.batiment;

import java.util.List;

import royaume.model.QuantiteRessource;

public class PallierBatiment {

    private int niveau;
    private List<QuantiteRessource> cout;

    public PallierBatiment(int niveau, List<QuantiteRessource> cout) {
        this.niveau = niveau;
        this.cout = cout;
    }

    public int getNiveau() {
        return niveau;
    }

    public List<QuantiteRessource> getCout() {
        return cout;
    }
}