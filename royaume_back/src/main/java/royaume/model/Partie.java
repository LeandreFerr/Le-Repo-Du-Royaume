package royaume.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "partie")
public class Partie {

	// =========================================================================
	// Attributs
	// =========================================================================
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
	private String nom;
	@NotNull
    @Column(name = "date_creation", nullable = false)
	private LocalDateTime dateCreation;
	@DecimalMin("0.1")
    @Column(name = "vitesse_jeu", nullable = false)
	private double vitesseJeu = 1.0;
	@Min(2)
    @Column(name = "max_joueurs", nullable = false)
	private int maxJoueurs = 10;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vainqueur_id", nullable = true)
	private Utilisateur vainqueur;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "type_victoire")
	private TypeVictoire typeVictoire;

	@OneToMany(mappedBy = "partie",
	            cascade = CascadeType.ALL,
	             orphanRemoval = true)
	private List<Message> messages = new ArrayList<>();
	
	@ManyToMany
    @JoinTable(
        name = "partie_royaumes",
        joinColumns = @JoinColumn(name = "partie_id"),
        inverseJoinColumns = @JoinColumn(name = "royaume_id"))
	private List<Royaume> royaumes = new ArrayList<>();

	@NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
	private StatutPartie statut = StatutPartie.EN_ATTENTE;

	// =========================================================================
	// Constructeurs
	// =========================================================================

	public Partie() {}

	public Partie(String nom, double vitesseJeu, int maxJoueurs) {
		this.nom = nom;
		this.dateCreation = LocalDateTime.now();
		this.vitesseJeu = vitesseJeu;
		this.maxJoueurs = maxJoueurs;
		this.messages =  new ArrayList<>(messages);
		this.royaumes = new ArrayList<>(royaumes);
		this.statut = StatutPartie.EN_ATTENTE;
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
		this.messages = (messages != null) ? new ArrayList<>(messages) : new ArrayList<>();
		this.royaumes = (royaumes != null) ? new ArrayList<>(royaumes) : new ArrayList<>();
		this.statut = (statut != null) ? statut : StatutPartie.EN_ATTENTE;
	}

	// =========================================================================
	// Méthodes métier
	// =========================================================================

	public void demarrer() {
		if (statut != StatutPartie.EN_ATTENTE) {
			throw new IllegalStateException("La partie ne peut pas démarrer car son statut est : " + statut);
		}
		if (royaumes.size() < 2) {
			throw new IllegalStateException("Impossible de démarrer la partie avec moins de 2 royaumes inscrits.");
		}
		this.statut = StatutPartie.EN_COURS;
	}

	public void terminer(Utilisateur vainqueur, TypeVictoire typeVictoire) {
		if (statut != StatutPartie.EN_COURS) {
			throw new IllegalStateException("Impossible de terminer une partie qui n'est pas en cours (statut actuel : " + statut + ").");
		}
		if (vainqueur == null) {
			throw new IllegalArgumentException("Le vainqueur ne peut pas être null.");
		}
		if (typeVictoire == null) {
			throw new IllegalArgumentException("Le type de victoire ne peut pas être null.");
		}
		if (vainqueur.getRoyaume() == null) {
			throw new IllegalArgumentException("Le vainqueur doit posséder un royaume valide.");
		}
		if (!royaumes.contains(vainqueur.getRoyaume())) {
			throw new IllegalArgumentException("Le royaume du vainqueur n'est pas inscrit dans cette partie.");
		}
		if (vainqueur.getRoyaume().isEnRuine()) {
			throw new IllegalStateException("Un royaume en ruine ne peut pas être déclaré vainqueur.");
		}
		if (getNJoueurs() == 0) {
			throw new IllegalStateException("Impossible de terminer la partie : aucun royaume n'est encore actif.");
		}

		this.vainqueur = vainqueur;
		this.typeVictoire = typeVictoire;
		this.statut = StatutPartie.TERMINEE;
	}

	public boolean inscrireRoyaume(Royaume royaume) {
		if (royaume == null) {
			throw new IllegalArgumentException("Le royaume à inscrire ne peut pas être null.");
		}
		if (royaume.isEnRuine()) {
			throw new IllegalArgumentException("Un royaume en ruine ne peut pas s'inscrire dans une nouvelle partie.");
		}
		if (statut != StatutPartie.EN_ATTENTE) {
			throw new IllegalStateException("Impossible d'inscrire un royaume : la partie n'est pas en attente.");
		}
		if (royaumes.contains(royaume)) {
			throw new IllegalArgumentException("Ce royaume est déjà inscrit dans la partie.");
		}
		if (royaumes.size() >= maxJoueurs) {
			throw new IllegalStateException("Capacité maximale de joueurs atteinte (" + maxJoueurs + ").");
		}

		royaumes.add(royaume);
		return true;
	}

	public void desinscrireRoyaume(Royaume royaume) {
		if (statut != StatutPartie.EN_ATTENTE) {
			throw new IllegalStateException("Impossible de se désinscrire : la partie est déjà lancée ou terminée.");
		}
		if (royaume == null || !royaumes.contains(royaume)) {
			throw new IllegalArgumentException("Le royaume est null ou n'est pas inscrit dans cette partie.");
		}
		royaumes.remove(royaume);
	}

	public void declarerForfait(Royaume royaume) {
		if (statut != StatutPartie.EN_COURS) {
			throw new IllegalStateException("Impossible de déclarer forfait : la partie n'est pas en cours.");
		}
		if (royaume == null || !royaumes.contains(royaume)) {
			throw new IllegalArgumentException("Le royaume est null ou n'appartient pas à cette partie.");
		}
		if (royaume.isEnRuine()) {
			throw new IllegalStateException("Ce royaume est déjà en ruine / a déjà déclaré forfait.");
		}
		royaume.setEnRuine(true);
	}

	public long getNJoueurs() {
		return royaumes.stream()
				.filter(r -> !r.isEnRuine())
				.count();
	}

	public boolean ajouterMessage(Message message) {
		if (message == null) {
			throw new IllegalArgumentException("Le message ne peut pas être null.");
		}
		if (messages.contains(message)) {
			throw new IllegalArgumentException("Ce message a déjà été ajouté dans la partie.");
		}
		message.setPartie(this);
		messages.add(message);
		return true;
	}
	
	public void supprimerMessage(Message message) {
	    messages.remove(message);
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
		this.statut = (statut != null) ? statut : StatutPartie.EN_ATTENTE;
	}

	public List<Royaume> getRoyaumes() {
		return Collections.unmodifiableList(royaumes);
	}

	public void setRoyaumes(List<Royaume> royaumes) {
		this.royaumes = (royaumes != null) ? new ArrayList<>(royaumes) : new ArrayList<>();
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
		return Collections.unmodifiableList(messages);
	}

	public void setMessages(List<Message> messages) {
		this.messages = (messages != null) ? new ArrayList<>(messages) : new ArrayList<>();
	}

	
	// =========================================================================
	// Représentation
	// =========================================================================

	@Override
	public String toString() {
		return "Partie [id=" + id + ", nom=" + nom + ", dateCreation=" + dateCreation + ", vitesseJeu=" + vitesseJeu
				+ ", maxJoueurs=" + maxJoueurs + ", vainqueur=" + (vainqueur != null ? vainqueur.getPseudo() : null)
				+ ", typeVictoire=" + typeVictoire + ", royaumes=" + royaumes + ", statut=" + statut + "]";
	}
}
