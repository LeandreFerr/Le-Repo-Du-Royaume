package royaume.model;

import java.time.LocalDateTime;

public class Message {

	// =========================================================================
	// Attributs
	// =========================================================================

	private Integer id;
	private String contenu;
	private LocalDateTime dateEnvoi;
	private boolean lu = false;

	private Partie partie;
	private Utilisateur expediteur;
	private Utilisateur destinataire;

	// =========================================================================
	// Constructeurs
	// =========================================================================

	public Message() {}

	public Message(Integer id, String contenu, LocalDateTime dateEnvoi, boolean lu, Partie partie,
			Utilisateur expediteur, Utilisateur destinataire) {
		checkId(id);
		checkContenu(contenu);
		checkExpediteur(expediteur);
		checkDestinataire(expediteur, destinataire);
		this.id = id;
		this.contenu = contenu;
		this.dateEnvoi = dateEnvoi;
		this.lu = lu;
		this.partie = partie;
		this.expediteur = expediteur;
		this.destinataire = destinataire;
	}

	// =========================================================================
	// Méthodes métier
	// =========================================================================

	public boolean estGlobal() {
		return destinataire == null;
	}

	// =========================================================================
	// Getters & Setters
	// =========================================================================

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		checkId(id);
		this.id = id;
	}

	public String getContenu() {
		return contenu;
	}

	public void setContenu(String contenu) {
		checkContenu(contenu);
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
		checkExpediteur(expediteur);
		this.expediteur = expediteur;
	}

	public Utilisateur getDestinataire() {
		return destinataire;
	}

	public void setDestinataire(Utilisateur destinataire) {
		checkDestinataire(this.expediteur, destinataire);
		this.destinataire = destinataire;
	}

	// =========================================================================
	// Méthodes helpers & Validations
	// =========================================================================

	private void checkId(Integer id) {
		if (id != null && id <= 0) {
			throw new IllegalArgumentException("L'id du message doit être strictement positif.");
		}
	}

	private void checkContenu(String contenu) {
		if (contenu == null || contenu.isBlank()) {
			throw new IllegalArgumentException("Le contenu du message ne peut pas être vide ou blanc.");
		}
	}

	private void checkExpediteur(Utilisateur expediteur) {
		if (expediteur == null) {
			throw new IllegalArgumentException("L'expéditeur du message ne peut pas être null.");
		}
	}

	private void checkDestinataire(Utilisateur expediteur, Utilisateur destinataire) {
		if (expediteur != null && destinataire != null && expediteur.equals(destinataire)) {
			throw new IllegalArgumentException("Un utilisateur ne peut pas s'envoyer un message privé à lui-même.");
		}
	}

	// =========================================================================
	// Représentation
	// =========================================================================

	@Override
	public String toString() {
		return "Message [id=" + id + ", contenu=" + contenu + ", dateEnvoi=" + dateEnvoi + ", lu=" + lu
				+ ", partie=" + (partie != null ? partie.getId() : null)
				+ ", expediteur=" + (expediteur != null ? expediteur.getPseudo() : null)
				+ ", destinataire=" + (destinataire != null ? destinataire.getPseudo() : null) + "]";
	}
}
