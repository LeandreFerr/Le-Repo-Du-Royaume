package royaume.model;

import java.time.LocalDateTime;

public class Message {

    private Integer id;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private boolean lu;

    private Partie partie;
    private Utilisateur expediteur;
    private Utilisateur destinataire;

    // Constructeurs
    public Message() {
    }

    public Message(Integer id, String contenu, LocalDateTime dateEnvoi, boolean lu, Partie partie,
            Utilisateur expediteur,
            Utilisateur destinataire) {
        this.id = id;
        this.contenu = contenu;
        this.dateEnvoi = dateEnvoi;
        this.lu = lu;
        this.partie = partie;
        this.expediteur = expediteur;
        this.destinataire = destinataire;
    }

    // Méthodes métier

    public boolean estGlobal() {
        return destinataire == null;
    }

    // getters setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public boolean isLu() {
        return lu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }

    public Partie getPartie() {
        return partie;
    }

    public void setPartie(Partie partie) {
        this.partie = partie;
    }

    public Utilisateur getExpediteur() {
        return expediteur;
    }

    public void setExpediteur(Utilisateur expediteur) {
        this.expediteur = expediteur;
    }

    public Utilisateur getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(Utilisateur destinataire) {
        this.destinataire = destinataire;
    }

    // toString

    @Override
    public String toString() {
        return "Message [id=" + id + ", contenu=" + contenu + ", dateEnvoi=" + dateEnvoi + ", lu=" + lu + ", partie="
                + partie + ", expediteur=" + expediteur + ", destinataire=" + destinataire + "]";
    }

}
