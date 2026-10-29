package royaume.model;

public class Teuteus extends Population {

    public String capaciteSpeciale;

    public Teuteus(Integer id, String nom, String titreSouverain, Double attraction, String capaciteSpeciale) {
        super(id, nom, titreSouverain, attraction);
        this.capaciteSpeciale = capaciteSpeciale;
    }

    @Override
    public void activerCapaciteSpecial() {
        System.out.println("Les Teuteus mangent leur crottes de nez!");
    }


}