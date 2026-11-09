package royaume.model.Peuple;

public class Canards extends Population {

	public Canards() {
		setCapaciteSpeciale("danse des canards");
	}

	public Canards(Integer id, String nom, String titreSouverain, Double attraction) {
		super(id, nom, titreSouverain, attraction, "danse des canards");
	}

	@Override
	public double getAvantageCombatContre(Population adversaire) {
		if (adversaire instanceof Teuteus) {
			return 1.20;
		}
		if (adversaire instanceof Gobelins) {
			return 0.80;
		}
		return 1.00;
	}

	@Override
	public double getBonusAttaque() {
		return 1.25;
	}

	@Override
	public String getDescriptionCapacite() {
		return "Danser : Confère un bonus d'attaque offensif de 25%. Fort contre les Teuteus, vulnérable aux Gobelins.";
	}

	@Override
	public void activerCapaciteSpecial() {
		System.out.println("Les Canards dansent sous la pluie !");
	}
}
