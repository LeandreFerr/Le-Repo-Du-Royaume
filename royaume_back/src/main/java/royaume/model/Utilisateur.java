package royaume.model;

import java.util.Objects;

public class Utilisateur {

	// =========================================================================
	// Attributs
	// =========================================================================

	private Integer id;
	private String pseudo;
	private String email;
	private String password;
	private boolean online = false;
	private boolean admin = false;
	private Royaume royaume;

	// =========================================================================
	// Constructeurs
	// =========================================================================

	public Utilisateur() {}

	public Utilisateur(Integer id, String pseudo, String email, String password, boolean online, boolean admin,
			Royaume royaume) {
		checkId(id);
		checkPseudo(pseudo);
		checkEmail(email);
		checkPassword(password);
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
		checkId(id);
		this.id = id;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		checkPseudo(pseudo);
		this.pseudo = pseudo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		checkEmail(email);
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		checkPassword(password);
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
	// Méthodes helpers & Validations
	// =========================================================================

	private void checkId(Integer id) {
		if (id != null && id <= 0) {
			throw new IllegalArgumentException("L'id de l'utilisateur doit être strictement positif.");
		}
	}

	private void checkPseudo(String pseudo) {
		if (pseudo == null || pseudo.isBlank()) {
			throw new IllegalArgumentException("Le pseudo de l'utilisateur ne peut pas être vide ou blanc.");
		}
	}

	private void checkEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("L'email de l'utilisateur ne peut pas être vide ou blanc.");
		}
	}

	private void checkPassword(String password) {
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Le mot de passe ne peut pas être vide ou blanc.");
		}
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
