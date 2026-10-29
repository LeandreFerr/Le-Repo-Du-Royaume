package royaume.model;

public class Princesses extends Population {

    public String capaciteSpeciale;

    public Princesses(Integer id, String nom, String titreSouverain, Double attraction, String capaciteSpeciale) {
        super(id, nom, titreSouverain, attraction);
        this.capaciteSpeciale = capaciteSpeciale;
    }

    @Override
    public void activerCapaciteSpecial() {
        System.out.println("Les Princesses rotent des flammes !");
    }


}