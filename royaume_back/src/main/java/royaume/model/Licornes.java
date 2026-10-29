package royaume.model;

public class Licornes extends Population {

    public String capaciteSpeciale;

    public Licornes(Integer id, String nom, String titreSouverain, Double attraction) {
        super(id, nom, titreSouverain, attraction);
        this.capaciteSpeciale = "peter";
    }

    @Override
    public void activerCapaciteSpecial() {
        System.out.println("Les licornes font des pets arc-en-ciel !");
        // Stat attaque et defense ennemis * 0.90
    }


}