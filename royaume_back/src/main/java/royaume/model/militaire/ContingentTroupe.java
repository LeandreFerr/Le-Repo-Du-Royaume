package royaume.model.militaire;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

//@Embeddable
public class ContingentTroupe {

    @NotNull
    private TypeTroupe type;

    @Min(0)
    private int quantite;

    /** Requis par JPA et Jackson (désérialisation du JSON). */
    public ContingentTroupe() {}

    public ContingentTroupe(TypeTroupe type, int quantite) {
        this.type = type;
        this.quantite = quantite;   // la validation (>= 0) est faite par Bean Validation
    }

    public void ajouter(int qte) {
        if (qte < 0) throw new IllegalArgumentException("Quantité négative");
        quantite += qte;
    }

    public void retirer(int qte) {
        if (qte < 0) throw new IllegalArgumentException("Quantité négative");
        if (qte > quantite) throw new IllegalArgumentException("Stock insuffisant : " + quantite);
        quantite -= qte;
    }

    public TypeTroupe getType() { return type; }
    public int getQuantite() { return quantite; }
}