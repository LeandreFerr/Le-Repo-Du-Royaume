package royaume.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Embeddable
public class QuantiteRessource {

	@PositiveOrZero
	@Column(nullable = false)
	private int quantite;
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "ressource", nullable = false)
	private Ressource ressource;
	
	
	public QuantiteRessource() {}
	
	
	public QuantiteRessource(int quantite, Ressource ressource) {
		
		this.quantite = quantite;
		this.ressource = ressource;
	}
	
	//operations métiers doivent garder leur propre controle de validation (different des getter et setter)
	public void ajouter(int montant) {
		if (montant <= 0) {
        	throw new IllegalArgumentException("Le montant à ajouter doit être strictement positif.");
		} else if (Integer.MAX_VALUE - quantite < montant) {
			throw new ArithmeticException("La quantité de ressources ne peut pas dépasser Integer.MAX_VALUE.");
		}
		quantite += montant;
		
	}
	
	public void retirer(int montant) {
		if (montant <= 0) {
        	throw new IllegalArgumentException("Le montant à retirer doit être strictement positif.");
		}
		if (montant > quantite) {
			throw new IllegalArgumentException("Fonds insuffisants : impossible d'avoir un solde négatif.");
		}
		quantite -= montant;
	}
	
	
	public int getQuantite() {
		return quantite;
	}
	
	public void setQuantite(int quantite) {
			this.quantite = quantite;
	}
	
	public Ressource getRessource() {
		return ressource;
	}
	
	public void setRessource(Ressource ressource) {
			this.ressource = ressource;
	}
	
	@Override
	public String toString() {
		return "QuantiteRessource [quantite=" + quantite + ", ressource=" + ressource + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		
		if (o == null || getClass() != o.getClass()) return false;
		
		QuantiteRessource that = (QuantiteRessource) o;
		return quantite == that.quantite && ressource == that.ressource;
	}

	@Override
	public int hashCode() {
		return Objects.hash(quantite, ressource);
	}
	
}
