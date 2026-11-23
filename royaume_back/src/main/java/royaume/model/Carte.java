package royaume.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;

@Entity
public class Carte {

    // =========================================================================
    // Attributs
    // =========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int largeur = 100;
    private int hauteur = 100;
    private int centreX = 50;
    private int centreY = 50;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partie_id", unique = true)
    private Partie partie;

    @OneToMany(mappedBy = "carte", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmplacementCarte> emplacements = new ArrayList<>();

    @Transient
    private final EmplacementCarte emplacementMarche =
            new EmplacementCarte(0, 50, 50, this);

    // =========================================================================
    // Constructeurs
    // =========================================================================

    public Carte() {
    }

    public Carte(
            int largeur,
            int hauteur,
            int centreX,
            int centreY,
            Partie partie,
            List<EmplacementCarte> emplacements) {

        this.largeur = largeur;
        this.hauteur = hauteur;
        this.centreX = centreX;
        this.centreY = centreY;
        this.partie = partie;

        setEmplacements(emplacements);
    }

    // =========================================================================
    // Méthodes métier
    // =========================================================================

    public double calculerDistanceEntre(Royaume r1, Royaume r2) {
        if (r1 == null || r2 == null) {
            throw new IllegalArgumentException("Les deux royaumes doivent être renseignés.");
        }

        return calculerDistance(
                r1.getX(),
                r1.getY(),
                r2.getX(),
                r2.getY());
    }

    public Optional<EmplacementCarte> getPremierEmplacementLibre() {
        return emplacements.stream()
                .filter(emplacement -> !emplacement.isOccupe())
                .findFirst();
    }

    public void genererEmplacements(int nombre) {
        if (nombre < 1 || nombre > 10) {
            throw new IllegalArgumentException("Le nombre d'emplacements (" + nombre + ") doit être compris entre 1 et 10.");
        }

        int rayon = Math.min(largeur, hauteur) / 2 - 10;

        if (rayon <= 0) {
            throw new IllegalStateException("La carte est trop petite pour générer les emplacements.");
        }

        for (EmplacementCarte emplacement :
                new ArrayList<>(emplacements)) {

            supprimerEmplacement(emplacement);
        }

        double angleEntreEmplacements = 2 * Math.PI / nombre;
        double angleInitial = -Math.PI / 2;

        for (int i = 0; i < nombre; i++) {
            double angle = angleInitial + i * angleEntreEmplacements;

            int x = centreX + (int) Math.round(rayon * Math.cos(angle));
            int y = centreY + (int) Math.round(rayon * Math.sin(angle));

            EmplacementCarte emplacement = new EmplacementCarte(
                            i + 1,
                            x,
                            y,
                            this);

            ajouterEmplacement(emplacement);
        }
    }

    // Surcharge de la méthode pour générer les emplacements avec un décalage entre
    // [-4;4]

    public void genererEmplacements(
            int nombre,
            boolean avecDecalageAleatoire) {

        genererEmplacements(nombre);

        if (!avecDecalageAleatoire) {
            return;
        }

        for (EmplacementCarte emplacement : emplacements) {
            emplacement.setCoordX( emplacement.getCoordX() + genererDecalageAleatoire());
            emplacement.setCoordY( emplacement.getCoordY()+ genererDecalageAleatoire());
        }
    }

    public EmplacementCarte placerRoyaume(Royaume royaume) {
        if (royaume == null) {
            throw new IllegalArgumentException("Le royaume à placer ne peut pas être null.");
        }

        EmplacementCarte emplacement = getPremierEmplacementLibre()
        .orElseThrow(() -> new IllegalStateException("Aucun emplacement libre sur la carte."));

        emplacement.assignerRoyaume(royaume);

        return emplacement;
    }

    
    public void ajouterEmplacement(EmplacementCarte emplacement) {
        if (emplacement == null) {
            throw new IllegalArgumentException("L'emplacement ne peut pas être null.");
        }

        if (!emplacements.contains(emplacement)) {
            emplacements.add(emplacement);
        }

        emplacement.setCarte(this);
    }

    public void supprimerEmplacement(EmplacementCarte emplacement) {
        if (emplacement == null) {
            return;
        }

        if (!emplacements.contains(emplacement)) {
            return;
        }

        emplacement.liberer();
        emplacements.remove(emplacement);
        emplacement.setCarte(null);
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

    public int getLargeur() {
        return largeur;
    }

    public void setLargeur(int largeur) {
        checkDimension(largeur, "largeur");
        this.largeur = largeur;
    }

    public int getHauteur() {
        return hauteur;
    }

    public void setHauteur(int hauteur) {
        checkDimension(hauteur, "hauteur");
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

    public void setEmplacements(
            List<EmplacementCarte> nouveauxEmplacements) {

        for (EmplacementCarte emplacement :
                new ArrayList<>(this.emplacements)) {

            supprimerEmplacement(emplacement);
        }

        if (nouveauxEmplacements == null) {
            return;
        }

        List<EmplacementCarte> copie =
                new ArrayList<>(nouveauxEmplacements);

        for (EmplacementCarte emplacement : copie) {
            ajouterEmplacement(emplacement);
        }
    }

    public EmplacementCarte getEmplacementMarche() {
        return emplacementMarche;
    }

    // =========================================================================
    // Méthodes helpers et validations
    // =========================================================================

    private double calculerDistance(
            int x1,
            int y1,
            int x2,
            int y2) {

        return Math.hypot(
                (double) x2 - x1,
                (double) y2 - y1);
    }

    private static void checkDimension(
            int valeur,
            String nom) {

        if (valeur <= 0) {
            throw new IllegalArgumentException("La " + nom + " de la carte doit être strictement positive.");
        }
    }

    private int genererDecalageAleatoire() {
        int valeur =  ThreadLocalRandom.current().nextInt(2, 5);

        return ThreadLocalRandom.current().nextBoolean()
                ? valeur
                : -valeur;
    }

    // =========================================================================
    // Représentation
    // =========================================================================

    @Override
    public String toString() {
        return "Carte [id=" + id
                + ", largeur=" + largeur
                + ", hauteur=" + hauteur
                + ", centreX=" + centreX
                + ", centreY=" + centreY
                + ", partie=" + partie
                + ", emplacements=" + emplacements
                + "]";
    }
}