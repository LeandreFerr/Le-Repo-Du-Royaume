package royaume.model.batiment;

import royaume.model.Ressource;
import royaume.model.Royaume;


public class BatimentFactory {

    private BatimentFactory() {
    }

    public static Chateau creerChateauInitial(Royaume royaume) {

        Chateau chateau = new Chateau(
            null, "chateau", 1, 9000, 9000, false
        );

        royaume.ajouterBatiment(chateau);

        return chateau;
    }
    
    
    public static Maison creerMaison(
            Royaume royaume,
            Integer id,
            String nom,
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

    
    ////////////////////////////////////
    public static BatimentProduction creerBatimentProduction(
            Royaume royaume,
            Integer id,
            String nom,
            int pointsDeVie,
            Ressource typeRessource,
            int productionBaseHeure,
            int ouvriers) {


        BatimentProduction batiment = new BatimentProduction(
            id,
            nom,
            1,
            pointsDeVie,
            pointsDeVie,
            true,
            typeRessource,
            productionBaseHeure
        );

        royaume.ajouterBatiment(batiment);

        new ChantierConstruction(
            id,
            batiment,
            royaume,
            1,
            ouvriers,
            batiment.getTempsConstructionBase(),
            false
        );

        return batiment;
    }
    
}
