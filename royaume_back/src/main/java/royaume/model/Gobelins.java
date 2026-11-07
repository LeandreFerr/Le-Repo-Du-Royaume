package royaume.model;

public class Gobelins extends Population {

	public Gobelins() {
		setCapaciteSpeciale("voler");
	}

	public Gobelins(Integer id, String nom, String titreSouverain, Double attraction) {
		super(id, nom, titreSouverain, attraction, "voler");
	}

	@Override
	public double getAvantageCombatContre(Population adversaire) {
		if (adversaire instanceof Canards) {
			return 1.20;
		}
		if (adversaire instanceof Princesses) {
			return 0.80;
		}
		return 1.00;
	}

	@Override
	public double getTauxPillage() {
		return 0.05;
	}

	@Override
	public String getDescriptionCapacite() {
		return "Voler : Pille 5% des réserves ennemies après raid. Fort contre les Canards, vulnérable aux Princesses.";
	}

	@Override
	public void activerCapaciteSpecial() {
		System.out.println("Les Gobelins volent des ressources !");
	}
}
