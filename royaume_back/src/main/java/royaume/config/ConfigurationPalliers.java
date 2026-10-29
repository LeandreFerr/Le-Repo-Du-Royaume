package royaume.config;

import java.util.List;

import royaume.model.PallierNiveau;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;

public final class ConfigurationPalliers {
	

	    private ConfigurationPalliers() {
	        // Empêche l'instanciation de cette classe
	    }

	    private static final List<PallierNiveau> PALIERS = List.of(

	        new PallierNiveau(1, 2, 100, List.of(
	            new QuantiteRessource(100, Ressource.OR),
	            new QuantiteRessource(80, Ressource.PIERRE),
	            new QuantiteRessource(60, Ressource.BOIS),
	            new QuantiteRessource(90, Ressource.NOURRITURE)
	        )),

	        new PallierNiveau(2, 3, 200, List.of(
	            new QuantiteRessource(120, Ressource.OR),
	            new QuantiteRessource(100, Ressource.PIERRE),
	            new QuantiteRessource(100, Ressource.BOIS),
	            new QuantiteRessource(1300, Ressource.NOURRITURE)
	        )),

	        new PallierNiveau(3, 4, 300, List.of(
	            new QuantiteRessource(200, Ressource.OR),
	            new QuantiteRessource(150, Ressource.PIERRE),
	            new QuantiteRessource(150, Ressource.BOIS),
	            new QuantiteRessource(2000, Ressource.NOURRITURE)
	        )),

	        new PallierNiveau(4, 5, 400, List.of(
	            new QuantiteRessource(300, Ressource.OR),
	            new QuantiteRessource(250, Ressource.PIERRE),
	            new QuantiteRessource(250, Ressource.BOIS),
	            new QuantiteRessource(2800, Ressource.NOURRITURE)
	        )),

	        new PallierNiveau(5, 6, 500, List.of(
	            new QuantiteRessource(400, Ressource.OR),
	            new QuantiteRessource(350, Ressource.PIERRE),
	            new QuantiteRessource(350, Ressource.BOIS),
	            new QuantiteRessource(3600, Ressource.NOURRITURE)
	        )),

	        new PallierNiveau(6, 7, 600, List.of(
	            new QuantiteRessource(550, Ressource.OR),
	            new QuantiteRessource(500, Ressource.PIERRE),
	            new QuantiteRessource(500, Ressource.BOIS),
	            new QuantiteRessource(4400, Ressource.NOURRITURE)
	        )),

	        new PallierNiveau(7, 8, 700, List.of(
	            new QuantiteRessource(700, Ressource.OR),
	            new QuantiteRessource(650, Ressource.PIERRE),
	            new QuantiteRessource(650, Ressource.BOIS),
	            new QuantiteRessource(5200, Ressource.NOURRITURE)
	        )),

	        new PallierNiveau(8, 9, 800, List.of(
	            new QuantiteRessource(850, Ressource.OR),
	            new QuantiteRessource(800, Ressource.PIERRE),
	            new QuantiteRessource(800, Ressource.BOIS),
	            new QuantiteRessource(6100, Ressource.NOURRITURE)
	        )),

	        new PallierNiveau(9, 10, 1000, List.of(
	            new QuantiteRessource(1000, Ressource.OR),
	            new QuantiteRessource(1000, Ressource.PIERRE),
	            new QuantiteRessource(1000, Ressource.BOIS),
	            new QuantiteRessource(7000, Ressource.NOURRITURE)
	        ))
	    );

	    public static List<PallierNiveau> getPaliers() {
	        return PALIERS;
	    }
	
}
