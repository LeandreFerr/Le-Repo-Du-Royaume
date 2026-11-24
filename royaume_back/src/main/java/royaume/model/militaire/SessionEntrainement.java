package royaume.model.militaire;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import royaume.model.Royaume;

@Entity
@Table(name = "session_entrainement")
public class SessionEntrainement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_troupe", nullable = false)
    @NotNull
    private TypeTroupe typeTroupe;

    @Positive
    @Column(nullable = false)
    private int quantite;

    @NotNull
    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @NotNull
    @Column(name = "date_fin_prevue", nullable = false)
    private LocalDateTime dateFinPrevue;

    @Column(nullable = false)
    private boolean finalisee = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "royaume_id", nullable = false)
    @NotNull
    private Royaume royaume;

    /** Requis par JPA. */
    public SessionEntrainement() {}

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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public boolean isFinalisee() { return finalisee; }
    public TypeTroupe getTypeTroupe() { return typeTroupe; }
    public int getQuantite() { return quantite; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public LocalDateTime getDateFinPrevue() { return dateFinPrevue; }
    public Royaume getRoyaume() { return royaume; }
    public void setRoyaume(Royaume royaume) { this.royaume = royaume; }
}