package royaume.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
@Entity
@Table(name = "message")
public class Message {

	// =========================================================================
	// Attributs
	// =========================================================================

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
	private String contenu;
	@NotNull
	@Column(name = "date_envoi", nullable = false)
	private LocalDateTime dateEnvoi;
    @Column(nullable = false)
	private boolean lu = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partie_id", nullable = false)
	private Partie partie;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expediteur_id", nullable = false)
	private Utilisateur expediteur;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = true)
	private Utilisateur destinataire;

	// =========================================================================
	// Constructeurs
	// =========================================================================

	public Message() {}

	public Message(Integer id, String contenu, LocalDateTime dateEnvoi, boolean lu, Partie partie,
			Utilisateur expediteur, Utilisateur destinataire) {
		
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
