package royaume.model.Militaire;

import royaume.model.Royaume;
import java.time.LocalDateTime;

public class SessionEntrainement {
    private final TypeTroupe typeTroupe;
    private final int quantite;
    private final LocalDateTime dateDebut;
    private final LocalDateTime dateFinPrevue;
    private boolean finalisee = false;

    public SessionEntrainement(TypeTroupe type, int quantite, LocalDateTime debut, LocalDateTime fin) {
        this.typeTroupe = type;
        this.quantite = quantite;
        this.dateDebut = debut;
        this.dateFinPrevue = fin;
    }

    public boolean estTerminee(LocalDateTime maintenant) {
        return !maintenant.isBefore(dateFinPrevue);
    }

    /** Transfère les recrues : en entraînement -> militaire + garnison. */
    public void finaliserFormation(Royaume royaume) {
        if (finalisee) return;
        royaume.terminerFormation(typeTroupe, quantite);
        finalisee = true;
    }

    public boolean isFinalisee() { return finalisee; }
    public TypeTroupe getTypeTroupe() { return typeTroupe; }
    public int getQuantite() { return quantite; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public LocalDateTime getDateFinPrevue() { return dateFinPrevue; }
}