package royaume.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Partie {

    private Integer id;
    private String nom;
    private LocalDateTime dateCreation;
    private double vitesseJeu;
    private int maxJoueurs = 10;

    private Utilisateur vainqueur;
    private TypeVictoire typeVictoire;

    private List<Message> messages = new ArrayList<>();
    private List<Royaume> royaumes = new ArrayList<>();

    private StatutPartie statut = StatutPartie.EN_ATTENTE;

    // Constructeurs

    public Partie() {
    }

    public Partie(Integer id, String nom, LocalDateTime dateCreation, double vitesseJeu, int maxJoueurs,
            Utilisateur vainqueur, TypeVictoire typeVictoire, List<Message> messages,
            List<Royaume> royaumes, StatutPartie statut) {
        this.id = id;
        this.nom = nom;
        this.dateCreation = dateCreation;
        this.vitesseJeu = vitesseJeu;
        this.maxJoueurs = maxJoueurs;
        this.vainqueur = vainqueur;
        this.typeVictoire = typeVictoire;
        this.messages = messages;
        this.royaumes = royaumes;
        this.statut = statut;
    }

    // Méthodes métier

    public void demarrer() {

        if (statut == StatutPartie.EN_ATTENTE && royaumes.size() >= 2) {

            statut = StatutPartie.EN_COURS;
        }
    }

    public void terminer(Utilisateur vainqueur, TypeVictoire typeVictoire) {

        if (statut == StatutPartie.EN_COURS && vainqueur != null && getNJoueurs() > 1) {

            this.vainqueur = vainqueur;
            this.typeVictoire = typeVictoire;
            this.statut = StatutPartie.TERMINEE;

        }
    }

    public boolean inscrireRoyaume(Royaume royaume) {

        if (statut != StatutPartie.EN_ATTENTE) {
            return false;
        }
        if (royaumes.contains(royaume)) {
            return false;
        }
        if (royaumes.size() >= maxJoueurs) {
            return false;
        }

        royaumes.add(royaume);
        return true;
    }

    public void desinscrireRoyaume(Royaume royaume) {

        if (statut == StatutPartie.EN_ATTENTE) {
            royaumes.remove(royaume);
            // Libérer emplacement carte
        }

    }

    public void declarerForfait(Royaume royaume) {
        if (statut == StatutPartie.EN_COURS) {
            royaume.setEnRuine(true);
            // royaume.setPropriétaire(null);
        }
    }

    public long getNJoueurs() {
        return royaumes.stream()
                .filter(r -> r.isEnRuine())
                .count();
    }

    public boolean ajouterMessage(Message message) {
        message.setPartie(this);
        messages.add(message);
        return true;
    }

    // getters setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public double getVitesseJeu() {
        return vitesseJeu;
    }

    public void setVitesseJeu(double vitesseJeu) {
        this.vitesseJeu = vitesseJeu;
    }

    public int getMaxJoueurs() {
        return maxJoueurs;
    }

    public void setMaxJoueurs(int maxJoueurs) {
        this.maxJoueurs = maxJoueurs;
    }

    public StatutPartie getStatut() {
        return statut;
    }

    public void setStatut(StatutPartie statut) {
        this.statut = statut;
    }

    public List<Royaume> getRoyaumes() {
        return royaumes;
    }

    public void setRoyaumes(List<Royaume> royaumes) {
        this.royaumes = royaumes;
    }

    public Utilisateur getVainqueur() {
        return vainqueur;
    }

    public void setVainqueur(Utilisateur vainqueur) {
        this.vainqueur = vainqueur;
    }

    public TypeVictoire getTypeVictoire() {
        return typeVictoire;
    }

    public void setTypeVictoire(TypeVictoire typeVictoire) {
        this.typeVictoire = typeVictoire;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    // toString

    @Override
    public String toString() {
        return "Partie [id=" + id + ", nom=" + nom + ", dateCreation=" + dateCreation + ", vitesseJeu=" + vitesseJeu
                + ", maxJoueurs=" + maxJoueurs + ", vainqueur=" + vainqueur + ", typeVictoire=" + typeVictoire
                + ", royaumes=" + royaumes + ", statut=" + statut
                + "]";
    }

}
