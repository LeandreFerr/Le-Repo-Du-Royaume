package royaume.model;

public class QuantiteRessource {

	private int quantite;
	private Ressource ressource;
	
	
	public QuantiteRessource() {}
	
	
	public QuantiteRessource(int quantite, Ressource ressource) {
		this.quantite = quantite;
		this.ressource = ressource;
	}
	
	
	public void ajouter(int montant) {
		if (montant > 0) {
			quantite += montant;
		}
		
	}
	
	public void retirer(int montant) {
		if (montant > 0) {
			quantite -= montant;
		}
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
	
}
