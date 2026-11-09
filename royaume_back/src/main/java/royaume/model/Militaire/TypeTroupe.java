package royaume.model.Militaire;

import royaume.model.Ressource;
import java.util.Map;



public enum TypeTroupe {    
    
    ARCHER(1, "Archer", 20, 5, 10,
           Map.of(Ressource.OR, 10, Ressource.BOIS, 10, Ressource.NOURRITURE, 10)),
    CAVALIER(2, "Cavalier", 15, 10, 15,
             Map.of(Ressource.OR, 10, Ressource.PIERRE, 10, Ressource.NOURRITURE, 20)),
    LANCIER(3, "Lancier", 15, 15, 10,
            Map.of(Ressource.OR, 10, Ressource.BOIS, 5, Ressource.NOURRITURE, 10)),
    SPECIAL(4, "Special", 20, 20, 20,
            Map.of(Ressource.OR, 30, Ressource.PIERRE, 10, Ressource.BOIS, 10));

    private int id;
    private String nom;
    
    private int attaque;
    private int defense;
    private int vitesse;
    private final Map<Ressource, Integer> cout;    
    
    private TypeTroupe(int id, String nom, int attaque, int defense, int vitesse, Map<Ressource, Integer> cout) {
        this.id = id;
        this.nom = nom;
        this.attaque = attaque;
        this.defense = defense;
        this.vitesse = vitesse;
        this.cout = cout;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public int getAttaque() {
        return attaque;
    }

    public int getDefense() {
        return defense;
    }

    public int getVitesse() {   
        return vitesse;
    }

    public Map<Ressource, Integer> getCout() {
        return cout;
    }


}