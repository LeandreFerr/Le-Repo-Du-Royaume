package royaume.model.Militaire;

public class ContingentTroupe {
    private final TypeTroupe type;
    private int quantite;

    public ContingentTroupe(TypeTroupe type, int quantite) {
        if (quantite < 0) throw new IllegalArgumentException("Quantité négative");
        this.type = type;
        this.quantite = quantite;
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