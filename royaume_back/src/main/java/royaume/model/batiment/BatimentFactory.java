package royaume.model.batiment;

import royaume.model.Royaume;

public class BatimentFactory {

    private BatimentFactory() {
    }

    public static Chateau creerChateauInitial(Royaume royaume) {

        Chateau chateau = new Chateau(
            1, "chateau", 1, 9000, 9000, false
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
            id, nom, 1, 200, 200, true,
            capaciteLogement, bonusCroissance
        );

        royaume.ajouterBatiment(maison);

        return maison;
    }
/*
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
    }*/
    

    public static Mine creerMine(
            Royaume royaume,
            Integer id,
            String nom,
            int productionOrHeure) {

        Mine mine = new Mine(
            id, nom, 1, 2500, 2500, false,
            productionOrHeure
        );

        royaume.ajouterBatiment(mine);

        return mine;
    }

    public static Carriere creerCarriere(
            Royaume royaume,
            Integer id,
            String nom,
            int productionPierreHeure) {

        Carriere carriere = new Carriere(
            id, nom, 1, 4000, 4000, false,
            productionPierreHeure
        );

        royaume.ajouterBatiment(carriere);

        return carriere;
    }

    public static Scierie creerScierie(
            Royaume royaume,
            Integer id,
            String nom,
            int productionBoisHeure) {

        Scierie scierie = new Scierie(
            id, nom, 1, 3500, 3500, false,
            productionBoisHeure
        );

        royaume.ajouterBatiment(scierie);

        return scierie;
    }

    public static Taverne creerTaverne(
            Royaume royaume,
            Integer id,
            String nom,
            int productionNourritureHeure) {

        Taverne taverne = new Taverne(
            id, nom, 1, 7000, 7000, false,
            productionNourritureHeure
        );

        royaume.ajouterBatiment(taverne);

        return taverne;
    }
}
