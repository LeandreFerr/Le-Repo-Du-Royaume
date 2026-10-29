package royaume.model;

public class Teuteus extends Population {

    public String capaciteSpeciale;

    public Teuteus(Integer id, String nom, String titreSouverain, Double attraction) {
        super(id, nom, titreSouverain, attraction);
        this.capaciteSpeciale = "manger";
    }

    @Override
    public void activerCapaciteSpecial() {
        System.out.println("Les Teuteus mangent leur crottes de nez!");
        // booste la production de nourriture 
        // Et
        // + 1ou2 en population
    }


}