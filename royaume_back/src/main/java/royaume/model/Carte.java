package royaume.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Carte {

    // =========================================================================
    // Attributs
    // =========================================================================

    private int largeur = 100;
    private int hauteur = 100;
    private int centreX = 50;
    private int centreY = 50;

    private Partie partie;

    private List<EmplacementCarte> emplacements = new ArrayList<>();

    // =========================================================================
    // Constructeurs
    // =========================================================================

    public Carte() {
    }

    public Carte(int largeur, int hauteur, int centreX, int centreY, Partie partie,
            List<EmplacementCarte> emplacements) {
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.centreX = centreX;
        this.centreY = centreY;
        this.partie = partie;
        this.emplacements = emplacements != null ? emplacements : new ArrayList<>();
    }

    // =========================================================================
    // Méthodes métier
    // =========================================================================

    public double calculerDistanceEntre(Royaume r1, Royaume r2) {

        if (r1 == null || r2 == null) {
            throw new IllegalArgumentException(
                    "Les deux royaumes doivent être renseignés.");
        }
        return calculerDistance(
                r1.getX(),
                r1.getY(),
                r2.getX(),
                r2.getY());
    }

    public Optional<EmplacementCarte> getPremierEmplacementLibre() {
        return emplacements.stream().filter(e -> !e.isOccupe()).findFirst();
    }

    public void genererEmplacements(int nombre) {
        if (nombre < 1 || nombre > 10) {
            throw new IllegalArgumentException(
                    "L'emplacement : " + nombre
                            + "n'est pas disponible. Le nombre d'emplacements doit être compris entre 1 et 10 ");
        }

        emplacements.clear();

        int espacementX = largeur / (nombre + 1);

        for (int i = 1; i <= nombre; i++) {
            EmplacementCarte emplacement = new EmplacementCarte(i, i * espacementX, hauteur / 2, this);
            emplacements.add(emplacement);
        }
    }

    // Méthode non présente sur le ticket, qui permet de placer le royaume et de
    // l'assigner sur un emplacement
    // CAD -> liaison entre emplacementCarte - Carte - Royaume

    public EmplacementCarte placerRoyaume(Royaume royaume) {

        if (royaume == null) {
            throw new IllegalArgumentException(
                    "Le royaume à placer ne peut pas être null.");
        }

        EmplacementCarte emplacement = getPremierEmplacementLibre()
                .orElseThrow(() -> new IllegalStateException(
                        "Aucun emplacement libre sur la carte."));

        emplacement.assignerRoyaume(royaume);

        return emplacement;
    }

    // =========================================================================
    // Getters & Setters
    // =========================================================================

    public int getLargeur() {
        return largeur;
    }

    public void setLargeur(int largeur) {
        this.largeur = largeur;
    }

    public int getHauteur() {
        return hauteur;
    }

    public void setHauteur(int hauteur) {
        this.hauteur = hauteur;
    }

    public int getCentreX() {
        return centreX;
    }

    public void setCentreX(int centreX) {
        this.centreX = centreX;
    }

    public int getCentreY() {
        return centreY;
    }

    public void setCentreY(int centreY) {
        this.centreY = centreY;
    }

    public Partie getPartie() {
        return partie;
    }

    public void setPartie(Partie partie) {
        this.partie = partie;
    }

    public List<EmplacementCarte> getEmplacements() {
        return emplacements;
    }

    public void setEmplacements(List<EmplacementCarte> emplacements) {
        this.emplacements = emplacements != null ? emplacements : new ArrayList<>();
    }

    // =========================================================================
    // Méthodes helpers & Validations
    // =========================================================================

    private double calculerDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(
                Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    // =========================================================================
    // Représentation
    // =========================================================================

    @Override
    public String toString() {
        return "Carte [largeur=" + largeur + ", hauteur=" + hauteur + ", centreX=" + centreX + ", centreY=" + centreY
                + ", partie=" + partie + ", emplacements=" + emplacements + "]";
    }

}
