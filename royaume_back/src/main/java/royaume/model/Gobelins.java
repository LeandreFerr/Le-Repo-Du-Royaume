package royaume.model;

public class Gobelins extends Population {

    public String capaciteSpeciale;

    public Gobelins(Integer id, String nom, String titreSouverain, Double attraction) {
        super(id, nom, titreSouverain, attraction);
        this.capaciteSpeciale = "tirer";
    }

    @Override
    public void activerCapaciteSpecial() {
        System.out.println("Les Gobelins lancent des fleches !");
        // choisi un royaume, vole 5% des ressources
        // que or ?  choisir une ressource precise ?
        // 10% ressource unique OU 2% all ressource
    }


}