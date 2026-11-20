package royaume.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

	// =========================================================================
	// Attributs
	// =========================================================================

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@NotBlank
	@Size(min = 3, max = 30)
	@Column(nullable = false, unique = true, length = 30)
	private String pseudo;
	@NotBlank
    @Email
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
	private String email;
	@NotBlank
    @Size(min = 6)
    @Column(nullable = false)
	private String password;
    @Column(nullable = false)
	private boolean online = false;
    @Column(nullable = false)
	private boolean admin = false;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "royaume_id", nullable = true)
	private Royaume royaume;

	// =========================================================================
	// Constructeurs
	// =========================================================================

	public Utilisateur() {}

	public Utilisateur(Integer id, String pseudo, String email, String password, boolean online, boolean admin,
			Royaume royaume) {
		
		this.id = id;
		this.pseudo = pseudo;
		this.email = email;
		this.password = password;
		this.online = online;
		this.admin = admin;
		this.royaume = royaume;
	}

	// =========================================================================
	// Méthodes métier
	// =========================================================================

	public boolean seConnecter(String password) {
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Le mot de passe fourni ne peut pas être null ou blanc.");
		}
		if (password.equals(this.password)) {
			this.online = true;
			return true;
		}
		return false;
	}

	public void seDeconnecter() {
		this.online = false;
	}

	public void abandonnerPartie() {
		this.royaume = null;
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

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public Royaume getRoyaume() {
		return royaume;
	}

	public void setRoyaume(Royaume royaume) {
		this.royaume = royaume;
	}

	

	// =========================================================================
	// Égalité & HashCode
	// =========================================================================

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Utilisateur that = (Utilisateur) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return id != null ? Objects.hash(id) : super.hashCode();
	}

	// =========================================================================
	// Représentation
	// =========================================================================

	@Override
	public String toString() {
		return "Utilisateur [id=" + id + ", pseudo=" + pseudo + ", email=" + email + ", password=****"
				+ ", online=" + online + ", admin=" + admin + ", royaume=" + royaume + "]";
	}
}
