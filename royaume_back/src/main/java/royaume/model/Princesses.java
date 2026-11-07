package royaume.model;

public class Princesses extends Population {

	public Princesses() {
		setCapaciteSpeciale("roter du feu");
	}

	public Princesses(Integer id, String nom, String titreSouverain, Double attraction) {
		super(id, nom, titreSouverain, attraction, "roter du feu");
	}

	@Override
	public double getAvantageCombatContre(Population adversaire) {
		if (adversaire instanceof Gobelins) {
			return 1.20;
		}
		if (adversaire instanceof Licornes) {
			return 0.80;
		}
		return 1.00;
	}

	@Override
	public double getMalusProductionAdversaire() {
		return 0.70;
	}

	@Override
	public String getDescriptionCapacite() {
		return "Roter : Le rot de flammes incendie les installations rivales, réduisant leur production de 30%. Fort contre les Gobelins, vulnérable aux Licornes.";
	}

	@Override
	public void activerCapaciteSpecial() {
		System.out.println("Les Princesses rotent des flammes !");
	}
}
