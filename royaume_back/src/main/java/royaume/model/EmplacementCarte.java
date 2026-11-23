package royaume.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class EmplacementCarte {

    // =========================================================================
    // Attributs
    // =========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int numero;
    private int coordX;
    private int coordY;
    private boolean occupe = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carte_id")
    private Carte carte;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "royaume_id", unique = true)
    private Royaume royaume;

    // =========================================================================
    // Constructeurs
    // =========================================================================

    public EmplacementCarte() {
    }

    public EmplacementCarte(
            int numero,
            int coordX,
            int coordY,
            Carte carte) {

        this.id = null;
        this.numero = numero;
        this.coordX = coordX;
        this.coordY = coordY;
        this.occupe = false;
        this.carte = carte;
    }

    public EmplacementCarte(
            Integer id,
            int numero,
            int coordX,
            int coordY,
            boolean occupe,
            Carte carte) {

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
            throw new IllegalArgumentException("Aucun royaume à assigner.");
        }

        if (occupe) {
            throw new IllegalStateException("L'emplacement " + numero + " est déjà occupé.");
        }

        if (royaume.getEmplacement() != null) {
            throw new IllegalStateException("Le royaume est déjà placé sur l'emplacement " + royaume.getEmplacement().getNumero() + ".");
        }

        this.royaume = royaume;
        this.occupe = true;

        royaume.setEmplacement(this);
    }

    public void liberer() {
        Royaume ancienRoyaume = this.royaume;

        this.royaume = null;
        this.occupe = false;

        if (ancienRoyaume != null && ancienRoyaume.getEmplacement() == this) {

            ancienRoyaume.setEmplacement(null);
        }
    }

    // =========================================================================
    // Getters et setters
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
    // Représentation
    // =========================================================================

    @Override
    public String toString() {
        return "EmplacementCarte [id=" + id
                + ", numero=" + numero
                + ", coordX=" + coordX
                + ", coordY=" + coordY
                + ", occupe=" + occupe
                + ", royaume=" + royaume
                + "]";
    }
}