package royaume.model;

public class Princesses extends Population {

    public String capaciteSpeciale;

    public Princesses(Integer id, String nom, String titreSouverain, Double attraction) {
        super(id, nom, titreSouverain, attraction);
        this.capaciteSpeciale = "roter";
    }

    @Override
    public void activerCapaciteSpecial() {
        System.out.println("Les Princesses rotent des flammes !");
        // vise un royaume, incendie, production *0.7 pendant x temps
    }


}