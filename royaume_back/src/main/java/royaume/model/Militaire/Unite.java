package royaume.model.Militaire;

public class Unite {
    private final TypeTroupe type;
    //private int pointsDeVie;
    //private int x, y;

    public Unite(TypeTroupe type) {
        this.type = type;
        //this.pointsDeVie = type.getPointsDeVieMax();
    }

    public int getVitesse() {
        return type.getVitesse();   // on lit la valeur depuis l'enum
    }

    public TypeTroupe getType() { return type; }
}