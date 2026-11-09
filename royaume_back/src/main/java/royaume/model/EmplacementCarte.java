package royaume.model;

public class EmplacementCarte {

    // =========================================================================
    // Attributs
    // =========================================================================

    private Integer id;
    private int numero;
    private int coordX;
    private int coordY;
    private boolean occupe = false;

    private Carte carte;
    private Royaume royaume;

    

    // =========================================================================
    // Constructeurs
    // =========================================================================

    public EmplacementCarte() {}

    public EmplacementCarte(int numero,int coordX,int coordY,Carte carte) {
        this.id = null;
        this.numero = numero;
        this.coordX = coordX;
        this.coordY = coordY;   // Constructeur simplifié pour la méthode genererEmplacement() de classe Carte
        this.occupe = false;
        this.carte = carte;
    }

    public EmplacementCarte(Integer id, int numero, int coordX, int coordY, boolean occupe, Carte carte) {
        this.id = id;
        this.numero = numero;
        this.coordX = coordX;
        this.coordY = coordY;
        this.occupe = occupe;
        this.carte = carte;
    }

    // =========================================================================
    // Méthodes métier
    // =========================================================================

    public void assignerRoyaume(Royaume royaume) {
        if (royaume == null) {
        throw new IllegalArgumentException(
                "Aucun royaume à assigner."
        );
        }

        if (occupe) {
        throw new IllegalStateException(
                "L'emplacement " + numero + " est déjà occupé."
        );
        }

    this.royaume = royaume;
    this.occupe = true;
    royaume.setEmplacement(this);
    }

    public void liberer() {

        if (this.royaume != null) {
        this.royaume.setEmplacement(null);
        }

    this.royaume = null;
    this.occupe = false;
    }

    // =========================================================================
    // Getters & Setters
    // =========================================================================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getCoordX() {
        return coordX;
    }

    public void setCoordX(int coordX) {
        this.coordX = coordX;
    }

    public int getCoordY() {
        return coordY;
    }

    public void setCoordY(int coordY) {
        this.coordY = coordY;
    }

    public boolean isOccupe() {
        return occupe;
    }

    public void setOccupe(boolean occupe) {
        this.occupe = occupe;
    }

    public Carte getCarte() {
        return carte;
    }

    public void setCarte(Carte carte) {
        this.carte = carte;
    }

    public Royaume getRoyaume() {
    return royaume;
    }

    // =========================================================================
    // Méthodes helpers & Validations
    // =========================================================================


    // =========================================================================
    // Représentation
    // =========================================================================

    @Override
    public String toString() {
        return "EmplacementCarte [id=" + id + ", numero=" + numero + ", coordX=" + coordX + ", coordY=" + coordY
                + ", occupe=" + occupe + ", royaume=" + royaume  + "]";
    }

}
