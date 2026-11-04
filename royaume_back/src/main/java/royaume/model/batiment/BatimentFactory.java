package royaume.model.batiment;

import royaume.model.Ressource;
import royaume.model.Royaume;

public class BatimentFactory {

    private BatimentFactory() {
    }

    public static Chateau creerChateauInitial(Royaume royaume) {

        Chateau chateau = new Chateau(
            1, "chateau", 1, 50, 50, false
        );

        royaume.ajouterBatiment(chateau);

        return chateau;
    }

    public static Maison creerMaison(
            Royaume royaume,
            Integer id,
            String nom,
            int capaciteLogement,
            double bonusCroissance) {

        Maison maison = new Maison(
            id, nom, 1, 50, 50, true,
            capaciteLogement, bonusCroissance
        );

        royaume.ajouterBatiment(maison);

        return maison;
    }

    public static BatimentProduction creerBatimentProduction(
            Royaume royaume,
            Integer id,
            String nom,
            Ressource ressource,
            int productionBaseHeure) {

        BatimentProduction batiment = new BatimentProduction(
            id, nom, 1, 50, 50, true,
            ressource, productionBaseHeure
        );

        royaume.ajouterBatiment(batiment);

        return batiment;
    }
}
