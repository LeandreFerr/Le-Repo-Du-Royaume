package royaume.model;

public class Licornes extends Population {

    public String capaciteSpeciale;

    public Licornes(Integer id, String nom, String titreSouverain, Double attraction, String capaciteSpeciale) {
        super(id, nom, titreSouverain, attraction);
        this.capaciteSpeciale = capaciteSpeciale;
    }

    @Override
    public void activerCapaciteSpecial() {
        System.out.println("Les licornes font des pets arc-en-ciel !");
    }


}