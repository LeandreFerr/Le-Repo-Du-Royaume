package royaume.dto.request;

public record PartieRequest(
        String nom,
        double vitesseJeu,
        int maxJoueurs,
        String archetype,
        String seed) {

    public PartieRequest(String nom, double vitesseJeu, int maxJoueurs) {
        this(nom, vitesseJeu, maxJoueurs, null, null);
    }
}
