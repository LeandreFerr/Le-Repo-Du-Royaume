package royaume.model.batiment;

import royaume.model.Royaume;

/*
 * [REVIEW TICKET 1] Super idée de centraliser la création des bâtiments
 * dans BatimentFactory avec des constructeurs au niveau du package et 
 * l'ajout automatique au Royaume !
 */
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
    
    
    /*
     * [REVIEW TICKET 1] On peut retirer les paramètres `int capaciteLogement` et
     * `double bonusCroissance` de cette signature : ils ne sont plus utilisés dans
     * la méthode (on retire capaciteLogement -> la Maison agit comme un générateur 
     * de population, et le bonus est déjà calculé par niveau).
     */
    public static Maison creerMaison(
            Royaume royaume,
            Integer id,
            String nom,
            int capaciteLogement,
            double bonusCroissance,
            int ouvriers) {

        Maison maison = new Maison(
            id, nom, 1, 200, 200, true // enConstruction
            
        );

        royaume.ajouterBatiment(maison);

        new ChantierConstruction(
            id,
            maison,
            royaume,
            1,
            ouvriers,
            maison.getTempsConstructionBase(),
            false // premier chantier à la création du batiment 
        );

        return maison;
    }
/*

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
    
    */
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
    
    ////////////////////////////////////

    /*
     * [REVIEW TICKET 1] Avec BatimentProduction concret (cf. BatimentProduction.java),
     * vous pouvez soit utiliser une méthode unique creerBatimentProduction(..., Ressource typeRessource, ...),
     * soit garder creerMine/creerCarriere/creerScierie/creerTaverne en leur faisant
     * instancier et retourner un `new BatimentProduction(..., Ressource.OR, ...)`
     * Mais pour plus de modularité, j'ai une préférence pour la méthode creerBatimentProduction
     * (sans oublier de bien passer la Ressource correspondante pour corriger le bug null !).
     */
    public static Mine creerMine(
            Royaume royaume,
            Integer id,
            String nom,
            int productionOrHeure,
            int ouvriers) {

        Mine mine = new Mine(
                id, nom, 1, 2500, 2500, true,
                productionOrHeure
            );
        royaume.ajouterBatiment(mine);
        new ChantierConstruction(
            id,
            mine,
            royaume,
            1,
            ouvriers,
            mine.getTempsConstructionBase(),
            false
        );

        return mine;
    }
    
    /*
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
    }*/
    ////////////////////////////////////
    public static Carriere creerCarriere(
            Royaume royaume,
            Integer id,
            String nom,
            int productionPierreHeure,
            int ouvriers) {

        Carriere carriere = new Carriere(
                id, nom, 1, 4000, 4000, true,
                productionPierreHeure
            );
        royaume.ajouterBatiment(carriere);
        
        new ChantierConstruction(
            id,
            carriere,
            royaume,
            1,
            ouvriers,
            carriere.getTempsConstructionBase(),
            false
        );

        return carriere;
    }
    
    /*
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
    }*/
///////////////////////////////////////////////
    public static Scierie creerScierie(
            Royaume royaume,
            Integer id,
            String nom,
            int productionBoisHeure,
            int ouvriers) {

        Scierie scierie = new Scierie(
                id, nom, 1, 3500, 3500, true,
                productionBoisHeure
            );
        
        royaume.ajouterBatiment(scierie);
        
        new ChantierConstruction(
            id,
            scierie,
            royaume,
            1,
            ouvriers,
            scierie.getTempsConstructionBase(),
            false
        );

        return scierie;
    }
    /*
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
    }*/
    
    ////////////:://///////////////////////////////////////

    public static Taverne creerTaverne(
            Royaume royaume,
            Integer id,
            String nom,
            int productionNourritureHeure,
            int ouvriers) {

        Taverne taverne = new Taverne(
                id, nom, 1, 7000, 7000, true,
                productionNourritureHeure
            );
        
        royaume.ajouterBatiment(taverne);

        new ChantierConstruction(
            id,
            taverne,
            royaume,
            1,
            ouvriers,
            taverne.getTempsConstructionBase(),
            false
        );

        return taverne;
    }
}
    /*
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
}*/
