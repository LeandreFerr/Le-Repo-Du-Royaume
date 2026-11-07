package royaume.model;

public class Licornes extends Population {

	public Licornes() {
		setCapaciteSpeciale("peter des arcs en ciels");
	}

	public Licornes(Integer id, String nom, String titreSouverain, Double attraction) {
		super(id, nom, titreSouverain, attraction, "peter des arcs en ciels");
	}

	@Override
	public double getAvantageCombatContre(Population adversaire) {
		if (adversaire instanceof Princesses) {
			return 1.20;
		}
		if (adversaire instanceof Teuteus) {
			return 0.80;
		}
		return 1.00;
	}

	@Override
	public double getMalusCombatAdversaire() {
		return 0.90;
	}

	@Override
	public String getDescriptionCapacite() {
		return "Peter : Les pets arc-en-ciel réduisent les stats de combat de l'adversaire de 10%. Fort contre les Princesses, vulnérable aux Teuteus.";
	}

	@Override
	public void activerCapaciteSpecial() {
		System.out.println("Les licornes font des pets arc-en-ciel !");
	}
}
