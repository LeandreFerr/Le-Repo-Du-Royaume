package royaume.model.Peuple;
import royaume.model.Ressource;

public class Teuteus extends Population {

	public Teuteus() {
		setCapaciteSpeciale("manger crottes de nez");
	}

	public Teuteus(Integer id, String nom, String titreSouverain, Double attraction) {
		super(id, nom, titreSouverain, attraction, "manger crottes de nez");
	}

	@Override
	public double getAvantageCombatContre(Population adversaire) {
		if (adversaire instanceof Licornes) {
			return 1.20;
		}
		if (adversaire instanceof Canards) {
			return 0.80;
		}
		return 1.00;
	}

	@Override
	public double getBonusProduction(Ressource ressource) {
		if (ressource == Ressource.NOURRITURE) {
			return 1.50;
		}
		return 1.00;
	}

	@Override
	public int getBonusCroissancePopulation() {
		return 2;
	}

	@Override
	public String getDescriptionCapacite() {
		return "Manger crottes de nez : Booste la production de nourriture de 50% et accorde +2 habitants par cycle. Fort contre les Licornes, vulnérable aux Canards.";
	}

	@Override
	public void activerCapaciteSpecial() {
		System.out.println("Les Teuteus mangent leur crottes de nez !");
	}
}
