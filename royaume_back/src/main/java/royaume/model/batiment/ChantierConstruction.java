package royaume.model.batiment;

import java.time.Duration;
import java.time.LocalDateTime;

import royaume.model.Royaume;

public class ChantierConstruction {

    private Integer id;

    private int ouvriersAssignes = 0;
    private int ouvriersMinimum = 1;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFinPrevue;

    private boolean amelioration = false;

    private long tempsBaseSecondes;

    private Batiment batiment;
    private Royaume royaume;

    private boolean termine = false;

    // ---------------- CONSTRUCTEUR ----------------

    public ChantierConstruction(
            Integer id,
            Batiment batiment,
            Royaume royaume,
            int ouvriersMinimum,
            int ouvriersInitiaux,
            long tempsBaseSecondes,
            boolean amelioration) {

        if (batiment == null || royaume == null) {
            throw new IllegalArgumentException(
                "Le bâtiment et le royaume sont obligatoires."
            );
        }

        if (ouvriersMinimum < 1 ||
            ouvriersInitiaux < ouvriersMinimum ||
            tempsBaseSecondes <= 0) {

            throw new IllegalArgumentException(
                "Paramètres du chantier invalides."
            );
        }

        this.id = id;
        this.batiment = batiment;
        this.royaume = royaume;

        this.ouvriersMinimum = ouvriersMinimum;
        this.tempsBaseSecondes = tempsBaseSecondes;
        this.amelioration = amelioration;

        // Affectation initiale des ouvriers
        royaume.affecterOuvriers(ouvriersInitiaux);

        this.ouvriersAssignes = ouvriersInitiaux;

        this.dateDebut = LocalDateTime.now();

        // Liaison entre le bâtiment et son chantier
        batiment.setEnConstruction(true);
        batiment.setChantier(this);

        recalculerDateFin();
    }

    // ---------------- MÉTHODES MÉTIER ----------------

    public void assignerOuvriers(int nombre) {

        if (termine) {
            throw new IllegalStateException(
                "Le chantier est terminé."
            );
        }

        if (nombre <= 0) {
            throw new IllegalArgumentException(
                "Le nombre d'ouvriers doit être positif."
            );
        }

        royaume.affecterOuvriers(nombre);

        ouvriersAssignes += nombre;

        recalculerDateFin();
    }

    public void retirerOuvriers(int nombre) {

        if (termine) {
            throw new IllegalStateException(
                "Le chantier est terminé."
            );
        }

        if (nombre <= 0) {
            throw new IllegalArgumentException(
                "Le nombre doit être positif."
            );
        }

        if (ouvriersAssignes - nombre < ouvriersMinimum) {
            throw new IllegalArgumentException(
                "Impossible de descendre sous le minimum d'ouvriers."
            );
        }

        ouvriersAssignes -= nombre;

        royaume.libererOuvriers(nombre);

        recalculerDateFin();
    }

    public long calculerTempsRestant() {
        return calculerTempsRestant(LocalDateTime.now());
    }

    // Surcharge pratique pour les tests unitaires
    public long calculerTempsRestant(LocalDateTime maintenant) {

        long secondes = Duration.between(
            maintenant,
            dateFinPrevue
        ).getSeconds();

        return Math.max(0, secondes);
    }

    public void recalculerDateFin() {

        if (ouvriersAssignes <= 0) {
            throw new IllegalStateException(
                "Un chantier doit avoir au moins un ouvrier."
            );
        }

        long nouvelleDuree =
            (long) Math.ceil(
                (double) tempsBaseSecondes / ouvriersAssignes
            );

        dateFinPrevue = LocalDateTime.now()
            .plusSeconds(nouvelleDuree);
    }

    public boolean estPretAAchever() {
        return estPretAAchever(LocalDateTime.now());
    }

    // Surcharge pour faciliter les tests
    public boolean estPretAAchever(LocalDateTime maintenant) {

        return !termine &&
               !maintenant.isBefore(dateFinPrevue);
    }

    // ---------------- FINALISATION ----------------
    public void finaliser() {
        finaliser(LocalDateTime.now());
    }
    
    
  

    public void finaliser(LocalDateTime maintenant) {

        // Vérifier si le chantier est déjà terminé
        if (termine) {
            return;
        }

        // Vérifier si la date de fin est atteinte
        if (!estPretAAchever(maintenant)) {
            throw new IllegalStateException(
                "Le chantier n'est pas encore terminé."
            );
        }

        // Si c'est une amélioration, augmenter le niveau
        if (amelioration) {
            batiment.ameliorer();
        }

        // Restituer les ouvriers au royaume
        royaume.libererOuvriers(ouvriersAssignes);

        ouvriersAssignes = 0;

        // Rendre le bâtiment opérationnel
        batiment.setEnConstruction(false);

        // Supprimer la liaison avec le chantier
        batiment.setChantier(null);

        // Marquer le chantier comme terminé
        termine = true;
    }
    
    // ---------------- GETTERS / SETTERS ----------------

    public Integer getId() {
        return id;
    }

    public int getOuvriersAssignes() {
        return ouvriersAssignes;
    }

    public int getOuvriersMinimum() {
        return ouvriersMinimum;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public LocalDateTime getDateFinPrevue() {
        return dateFinPrevue;
    }

    public boolean isAmelioration() {
        return amelioration;
    }

    public Batiment getBatiment() {
        return batiment;
    }

    public Royaume getRoyaume() {
        return royaume;
    }

    public boolean isTermine() {
        return termine;
    }
}
