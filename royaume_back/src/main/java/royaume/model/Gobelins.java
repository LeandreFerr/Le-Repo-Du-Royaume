package royaume.model;

public class Gobelins extends Population {

    public String capaciteSpeciale;

    public Gobelins(Integer id, String nom, String titreSouverain, Double attraction, String capaciteSpeciale) {
        super(id, nom, titreSouverain, attraction);
        this.capaciteSpeciale = capaciteSpeciale;
    }

    @Override
    public void activerCapaciteSpecial() {
        System.out.println("Les Gobelins lancent des fleches !");
    }


}