package royaume.model;

import java.util.Objects;

public class QuantiteRessource {

	private int quantite;
	private Ressource ressource;
	
	
	public QuantiteRessource() {}
	
	
	public QuantiteRessource(int quantite, Ressource ressource) {
		if (quantite < 0) {
			throw new IllegalArgumentException("La quantité initiale ne peut pas être négative.");
		} else if (ressource == null) {
			throw new IllegalArgumentException("La ressource ne peut pas être null.");
		}
		this.quantite = quantite;
		this.ressource = ressource;
	}
	
	
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
		if (quantite >= 0) {
			this.quantite = quantite;
		} else {
			throw new IllegalArgumentException("La quantité ne peut pas être négative.");
		}
		
	}
	public Ressource getRessource() {
		return ressource;
	}
	public void setRessource(Ressource ressource) {
		if (ressource != null) {
			this.ressource = ressource;
		} else {
			throw new IllegalArgumentException("La ressource ne peut pas être null.");
		}
		
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
