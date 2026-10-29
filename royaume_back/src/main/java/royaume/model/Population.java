package royaume.model;

public abstract class Population {

    protected Integer id;
    protected String nom;
    protected String titreSouverain;
    protected Double attraction; // Remplacer par "attraction" pitié
    // je trouve ca bizarre d'avoir un ratio pour charmer
    // je croyais de base que c'etait un ration bonus pour la population


    public Population(Integer id, String nom, String titreSouverain, Double attraction){
        this.id = id;
        this.nom = nom;
        this.titreSouverain = titreSouverain;
        this.attraction = attraction;
    }

    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public String getTitreSouverain() {
        return titreSouverain;
    }

    public void setTitreSouverain(String titreSouverain) {
        this.titreSouverain = titreSouverain;
    }


    public Double getAttraction() {
        return attraction;
    }

    public void setAttraction(Double attraction) {
        this.attraction = attraction;
    }

    @Override
    public String toString() {
        return "Population [id=" + id + ", nom=" + nom + ", titreSouverain=" + titreSouverain + ", attraction="
                + attraction + "]";
    }



    public void activerCapaciteSpecial(){
        System.out.println("Vous activer votre capa");

    }


}
