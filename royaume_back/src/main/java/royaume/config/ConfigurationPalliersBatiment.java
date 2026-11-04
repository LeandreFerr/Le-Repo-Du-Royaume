
package royaume.config;

import java.util.List;
import java.util.Map;

import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.batiment.Batiment;
import royaume.model.batiment.Carriere;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;
import royaume.model.batiment.Mine;
import royaume.model.batiment.PallierBatiment;
import royaume.model.batiment.Scierie;
import royaume.model.batiment.Taverne;


// PVmax depend du niveau du batiment cf. ameliorer() dans Batiment 

//calculerPointsDeVieMax()
//getCoutAmelioration();

public final class ConfigurationPalliersBatiment {

    private ConfigurationPalliersBatiment() {
    }

    private static final Map<Class<? extends Batiment>, List<PallierBatiment>> PALIERS =
        Map.of(

            // ---------------- MAISON ----------------
            Maison.class, List.of(
                new PallierBatiment(2, cout(10, 10, 10, 1)),
                new PallierBatiment(3, cout(30, 30, 30, 3)),
                new PallierBatiment(4, cout(50, 50, 50, 5)),
                new PallierBatiment(5, cout(70, 70, 70, 7)),
                new PallierBatiment(6, cout(90, 90, 90, 9)),
                new PallierBatiment(7, cout(120, 120, 120, 12)),
                new PallierBatiment(8, cout(150, 150, 150, 15)),
                new PallierBatiment(9, cout(175, 175, 175, 18)),
                new PallierBatiment(10, cout(200, 200, 200, 20))
            ),

            // ---------------- MINE ----------------
            Mine.class, List.of(
                new PallierBatiment(2, cout(100, 80, 60, 40)),
                new PallierBatiment(3, cout(180, 140, 100, 80)),
                new PallierBatiment(4, cout(280, 220, 160, 130)),
                new PallierBatiment(5, cout(400, 320, 240, 200)),
                new PallierBatiment(6, cout(550, 450, 350, 300)),
                new PallierBatiment(7, cout(750, 600, 500, 420)),
                new PallierBatiment(8, cout(1000, 800, 650, 550)),
                new PallierBatiment(9, cout(1300, 1050, 850, 750)),
                new PallierBatiment(10, cout(1700, 1400, 1150, 1000))
            ),

            // ---------------- CARRIERE ----------------
            Carriere.class, List.of(
                new PallierBatiment(2, cout(80, 60, 100, 40)),
                new PallierBatiment(3, cout(140, 100, 180, 80)),
                new PallierBatiment(4, cout(220, 160, 280, 130)),
                new PallierBatiment(5, cout(320, 240, 400, 200)),
                new PallierBatiment(6, cout(450, 350, 550, 300)),
                new PallierBatiment(7, cout(600, 500, 750, 420)),
                new PallierBatiment(8, cout(800, 650, 1000, 550)),
                new PallierBatiment(9, cout(1050, 850, 1300, 750)),
                new PallierBatiment(10, cout(1400, 1150, 1700, 1000))
            ),

            // ---------------- SCIERIE ----------------
            Scierie.class, List.of(
                new PallierBatiment(2, cout(60, 100, 80, 40)),
                new PallierBatiment(3, cout(100, 180, 140, 80)),
                new PallierBatiment(4, cout(160, 280, 220, 130)),
                new PallierBatiment(5, cout(240, 400, 320, 200)),
                new PallierBatiment(6, cout(350, 550, 450, 300)),
                new PallierBatiment(7, cout(500, 750, 600, 420)),
                new PallierBatiment(8, cout(650, 1000, 800, 550)),
                new PallierBatiment(9, cout(850, 1300, 1050, 750)),
                new PallierBatiment(10, cout(1150, 1700, 1400, 1000))
            ),

            // ---------------- TAVERNE ----------------
            Taverne.class, List.of(
                new PallierBatiment(2, cout(100, 100, 80, 60)),
                new PallierBatiment(3, cout(180, 180, 140, 120)),
                new PallierBatiment(4, cout(280, 280, 220, 200)),
                new PallierBatiment(5, cout(400, 400, 320, 300)),
                new PallierBatiment(6, cout(550, 550, 450, 420)),
                new PallierBatiment(7, cout(750, 750, 600, 600)),
                new PallierBatiment(8, cout(1000, 1000, 800, 850)),
                new PallierBatiment(9, cout(1300, 1300, 1050, 1150)),
                new PallierBatiment(10, cout(1700, 1700, 1400, 1500))
            )
/*
            // ---------------- CHATEAU ----------------   // cf. ça augmente tout seul avec le niveau du Royaume
            Chateau.class, List.of(
                new PallierBatiment(2, cout(150, 150, 150, 100)),
                new PallierBatiment(3, cout(250, 250, 250, 200)),
                new PallierBatiment(4, cout(400, 400, 400, 350)),
                new PallierBatiment(5, cout(600, 600, 600, 500)),
                new PallierBatiment(6, cout(850, 850, 850, 750)),
                new PallierBatiment(7, cout(1150, 1150, 1150, 1000)),
                new PallierBatiment(8, cout(1500, 1500, 1500, 1300)),
                new PallierBatiment(9, cout(1900, 1900, 1900, 1700)),
                new PallierBatiment(10, cout(2400, 2400, 2400, 2200))
            )*/
        );

    /*
     * Ordre des paramètres :
     * bois, pierre, nourriture, or
     */
    
  //__________________________________________________

    private static List<QuantiteRessource> cout(
            int bois, int pierre, int nourriture, int or) {

        return List.of(
            new QuantiteRessource(bois, Ressource.BOIS),
            new QuantiteRessource(pierre, Ressource.PIERRE),
            new QuantiteRessource(nourriture, Ressource.NOURRITURE),
            new QuantiteRessource(or, Ressource.OR)
        );
    }
//__________________________________________________
    
    public static List<PallierBatiment> getPaliers(
            Class<? extends Batiment> type) {

        return PALIERS.getOrDefault(type, List.of());
    }
}
