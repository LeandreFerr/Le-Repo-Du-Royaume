package royaume.model;

public class Canards extends Population {

    public String capaciteSpeciale;

    public Canards(Integer id, String nom, String titreSouverain, Double attraction) {
        super(id, nom, titreSouverain, attraction);
        this.capaciteSpeciale = "danser";
    }

    @Override
    public void activerCapaciteSpecial() {
        System.out.println("Les Canards dansent sous la pluie !");

    }


}