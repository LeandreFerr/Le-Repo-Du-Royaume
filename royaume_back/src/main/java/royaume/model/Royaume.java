package royaume.model;

import java.util.ArrayList;
import java.util.List;

public class Royaume {

	private Integer id;
	private String nom;
	private int niveau = 1;
	private int populationDisponible = 100;
	private int populationOuvriere = 0;
	private int populationEnEntrainement = 0;
	private int populationMilitaire = 0;
	private boolean enRuine = false;
	
	private List<QuantiteRessource> ressources = new ArrayList<>(
		List.of(
			new QuantiteRessource(0, Ressource.PIERRE),
			new QuantiteRessource(0, Ressource.BOIS),
			new QuantiteRessource(0, Ressource.OR),
			new QuantiteRessource(0, Ressource.NOURRITURE)
		)
	);


	// ---------------- CONSTRUCTEURS --------------------------
	
	public Royaume() {}
	
	
	public Royaume(Integer id, String nom) {
		checkIfIdIsValid(id);
		checkIfNomIdValid(nom);
		this.id = id;
		this.nom = nom;
	}
	
	
	public Royaume(Integer id, String nom, int niveau, int populationDisponible, int populationOuvriere,
			int populationEnEntrainement, int populationMilitaire, boolean enRuine,
			List<QuantiteRessource> ressources) {
		checkIfIdIsValid(id);
		checkIfNomIdValid(nom);
		checkIfNiveauIsValid(niveau);
		checkIfPopulationsDispoAreValids(populationDisponible, populationOuvriere, populationEnEntrainement, populationMilitaire);
		this.id = id;
		this.nom = nom;
		this.niveau = niveau;
		this.populationDisponible = populationDisponible;
		this.populationOuvriere = populationOuvriere;
		this.populationEnEntrainement = populationEnEntrainement;
		this.populationMilitaire = populationMilitaire;
		this.enRuine = enRuine;
		initQuantiteesRessources(ressources);
		
	}

	// -------------------------Méthodes helpers -------------------------------

	private void checkIfIdIsValid(Integer id) {
		if (id != null && id <= 0) {
			throw new IllegalArgumentException("L'id ne peut pas être inférieur ou égal à 0.");
		}
	}

	private void checkIfNomIdValid(String nom) {
		if (nom == null) {
			throw new IllegalArgumentException("Le nom du royaume ne peut pas être null.");
		} else if (nom.isEmpty()) {
			throw new IllegalArgumentException("Le nom du royaume ne peut pas être vide.");
		} else if (nom.isBlank()) {
			throw new IllegalArgumentException("Le nom du royaume ne peut pas être rempli d'espaces.");
		}
	}

	private void checkIfNiveauIsValid(int niveau) {
		if (niveau < 1) {
			throw new IllegalArgumentException("Le niveau du royaume ne peut pas être inférieur à 1.");
		}
	}

	private void checkIfPopulationsDispoAreValids(int populationDisponible, int populationOuvriere,
			int populationEnEntrainement, int populationMilitaire) {
		String message = "La quantité d'une population ne peut pas être négavite.";
		if (populationDisponible < 0) {
			throw new IllegalArgumentException(message);
		} else if (populationOuvriere < 0) {
			throw new IllegalArgumentException(message);
		} else if (populationEnEntrainement < 0) {
			throw new IllegalArgumentException(message);
		} else if (populationMilitaire < 0) {
			throw new IllegalArgumentException(message);
		}
	}

	private void initQuantiteesRessources(List<QuantiteRessource> initiales) {
		if (initiales != null) {
			// 1. Garde contre les éléments null (safe pour List.of)
			for (QuantiteRessource qr : initiales) {
				if (qr == null) {
					throw new IllegalArgumentException("La liste de ressources ne peut pas contenir d'élément null.");
				}
			}

			// 2. Garde contre les doublons
			long nbRessourcesDistinctes = initiales.stream()
					.map(QuantiteRessource::getRessource)
					.distinct()
					.count();

			if (nbRessourcesDistinctes != initiales.size()) {
				throw new IllegalArgumentException("Il ne peut pas y avoir plusieurs quantiteRessource d'un même type de ressource.");
			}

			// 3. Tout est valide : mise à jour des ressources
			for (QuantiteRessource qr : initiales) {
				this.ressources.stream()
					.filter(r -> r.getRessource() == qr.getRessource())
					.findFirst()
					.ifPresent(r -> r.setQuantite(qr.getQuantite()));
			}
		}
	}

	// ------------------------- Méthodes métier -------------------------------


	public void crediterRessources(Ressource ressource, int quantite) {
		ressources.stream()
			.filter(r -> r.getRessource() == ressource)
			.findFirst()
			.ifPresent(r -> r.ajouter(quantite));
	}


	public boolean aAssezDeRessources(Ressource ressource, int quantite) {
		return ressources.stream()
			.filter(r -> r.getRessource() == ressource)
			.anyMatch(r -> r.getQuantite()>= quantite);
	}

	public boolean debiterRessources(Ressource ressource, int quantite) {
		if (!aAssezDeRessources(ressource, quantite)) {
			return false;
		}

		ressources.stream()
			.filter(r -> r.getRessource() == ressource)
			.findFirst()
			.ifPresent(r -> r.retirer(quantite));
		return true;
	}

	public void appliquerCroissance(double taux) {
		// A voir si on l'utilise étant donné qu'on a le bâtiment "maison"
		populationDisponible = (int) Math.round(populationDisponible * (1 + taux));
	}



	// -------------------------Getters / Setters ------------------------------




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


	public int getNiveau() {
		return niveau;
	}


	public void setNiveau(int niveau) {
		this.niveau = niveau;
	}


	public int getPopulationDisponible() {
		return populationDisponible;
	}


	public void setPopulationDisponible(int populationDisponible) {
		this.populationDisponible = populationDisponible;
	}


	public int getPopulationOuvriere() {
		return populationOuvriere;
	}


	public void setPopulationOuvriere(int populationOuvriere) {
		this.populationOuvriere = populationOuvriere;
	}


	public int getPopulationEnEntrainement() {
		return populationEnEntrainement;
	}


	public void setPopulationEnEntrainement(int populationEnEntrainement) {
		this.populationEnEntrainement = populationEnEntrainement;
	}


	public int getPopulationMilitaire() {
		return populationMilitaire;
	}


	public void setPopulationMilitaire(int populationMilitaire) {
		this.populationMilitaire = populationMilitaire;
	}


	public boolean isEnRuine() {
		return enRuine;
	}


	public void setEnRuine(boolean enRuine) {
		this.enRuine = enRuine;
	}


	public List<QuantiteRessource> getRessources() {
		return ressources.stream()
				.map(r -> new QuantiteRessource(r.getQuantite(), r.getRessource()))
				.toList();
	}


	public void setRessources(List<QuantiteRessource> ressources) {
		this.ressources = ressources;
	}


	@Override
	public String toString() {
		return "Royaume [id=" + id + ", nom=" + nom + ", niveau=" + niveau + ", populationDisponible="
				+ populationDisponible + ", populationOuvriere=" + populationOuvriere + ", populationEnEntrainement="
				+ populationEnEntrainement + ", populationMilitaire=" + populationMilitaire + ", enRuine=" + enRuine
				+ ", ressources=" + ressources + "]";
	}
	
	
	
}
