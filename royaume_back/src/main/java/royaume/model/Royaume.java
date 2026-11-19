package royaume.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;

public class Royaume {

	// =========================================================================
	// Attributs
	// =========================================================================

	private Integer id;
	private String nom;
	private Population peuple;
	private int niveau = 1;
	private int populationDisponible = 100;
	private int populationOuvriere = 0;
	private int populationEnEntrainement = 0;
	private int populationMilitaire = 0;
	private boolean enRuine = false;
	private double bonusCroissance = 0.0;
	private List<QuantiteRessource> ressources = creerRessourcesParDefaut();
	private List<Batiment> batiments = new ArrayList<>();
	private EmplacementCarte emplacement;

	// =========================================================================
	// Constructeurs
	// =========================================================================

	public Royaume() {}

	public Royaume(Integer id, String nom, Population peuple) {
		checkId(id);
		checkNom(nom);
		validerPeuple(peuple);
		this.id = id;
		this.nom = nom;
		this.peuple = peuple;
		BatimentFactory.creerChateauInitial(this);
	}

	public Royaume(Integer id, String nom, Population peuple, int niveau, int populationDisponible,
			int populationOuvriere, int populationEnEntrainement, int populationMilitaire, boolean enRuine,
			List<QuantiteRessource> ressources) {
		checkId(id);
		checkNom(nom);
		validerPeuple(peuple);
		checkNiveau(niveau);
		checkPopulations(populationDisponible, populationOuvriere, populationEnEntrainement, populationMilitaire);
		this.id = id;
		this.nom = nom;
		this.peuple = peuple;
		this.niveau = niveau;
		this.populationDisponible = populationDisponible;
		this.populationOuvriere = populationOuvriere;
		this.populationEnEntrainement = populationEnEntrainement;
		this.populationMilitaire = populationMilitaire;
		this.enRuine = enRuine;
		setRessources(ressources);
		BatimentFactory.creerChateauInitial(this);
	}

	public Royaume(Integer id, String nom, Population peuple, int niveau, int populationDisponible,
			int populationOuvriere, int populationEnEntrainement, int populationMilitaire, boolean enRuine,
			List<QuantiteRessource> ressources, List<Batiment> batiments) {
		checkId(id);
		checkNom(nom);
		validerPeuple(peuple);
		checkNiveau(niveau);
		checkPopulations(populationDisponible, populationOuvriere, populationEnEntrainement, populationMilitaire);
		this.id = id;
		this.nom = nom;
		this.peuple = peuple;
		this.niveau = niveau;
		this.populationDisponible = populationDisponible;
		this.populationOuvriere = populationOuvriere;
		this.populationEnEntrainement = populationEnEntrainement;
		this.populationMilitaire = populationMilitaire;
		this.enRuine = enRuine;
		setRessources(ressources);
		this.batiments = batiments != null ? new ArrayList<>(batiments) : new ArrayList<>();
		if (getChateau() == null) {
			BatimentFactory.creerChateauInitial(this);
		}
	}

	// Constructeur pour pouvoir attacher un emplacement au royaume

	public Royaume(Integer id, String nom, Population peuple, int niveau, int populationDisponible,
			int populationOuvriere, int populationEnEntrainement, int populationMilitaire, boolean enRuine,
			List<QuantiteRessource> ressources, EmplacementCarte emplacement) {
		checkId(id);
		checkNom(nom);
		validerPeuple(peuple);
		checkNiveau(niveau);
		checkPopulations(populationDisponible, populationOuvriere, populationEnEntrainement, populationMilitaire);
		this.id = id;
		this.nom = nom;
		this.peuple = peuple;
		this.niveau = niveau;
		this.populationDisponible = populationDisponible;
		this.populationOuvriere = populationOuvriere;
		this.populationEnEntrainement = populationEnEntrainement;
		this.populationMilitaire = populationMilitaire;
		this.enRuine = enRuine;
		setRessources(ressources);
		this.emplacement = emplacement;
		BatimentFactory.creerChateauInitial(this);
	}

	// =========================================================================
	// Méthodes métier
	// =========================================================================

	public void crediterRessources(Ressource ressource, int quantite) {
		if (ressource == null) {
			throw new IllegalArgumentException("La ressource ne peut pas être null.");
		}
		if (quantite <= 0) {
			throw new IllegalArgumentException("La quantité à créditer doit être strictement positive.");
		}
		ressources.stream()
			.filter(r -> r.getRessource() == ressource)
			.findFirst()
			.ifPresentOrElse(
				r -> r.ajouter(quantite),
				() -> ressources.add(new QuantiteRessource(quantite, ressource))
			);
	}

	public boolean aAssezDeRessources(Ressource ressource, int quantite) {
		if (ressource == null) {
			throw new IllegalArgumentException("La ressource ne peut pas être null.");
		}
		if (quantite < 0) {
			throw new IllegalArgumentException("La quantité ne peut pas être négative.");
		}
		if (quantite == 0) {
			return true;
		}
		return ressources.stream()
			.filter(r -> r.getRessource() == ressource)
			.anyMatch(r -> r.getQuantite() >= quantite);
	}

	public boolean debiterRessources(Ressource ressource, int quantite) {
		if (ressource == null) {
			throw new IllegalArgumentException("La ressource ne peut pas être null.");
		}
		if (quantite <= 0) {
			throw new IllegalArgumentException("La quantité à débiter doit être strictement positive.");
		}
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
		if (!Double.isFinite(taux) || taux < -1.0) {
			throw new IllegalArgumentException("Le taux de croissance doit être un nombre fini supérieur ou égal à -1.0.");
		}
		double tauxTotal = taux + bonusCroissance;
		long resultat = Math.round(populationDisponible * (1.0 + tauxTotal));
		populationDisponible = Math.toIntExact(resultat);
	}

	public boolean peutMonterDeNiveau(List<PalierNiveau> paliers) {
		if (this.enRuine) {
			return false;
		}
		if (paliers == null || paliers.isEmpty()) {
			return false;
		}
		if (this.niveau >= 10) {
			return false;
		}

		int niveauSuivant = this.niveau + 1;
		PalierNiveau palierSuivant = paliers.stream()
				.filter(p -> p != null && p.getNiveau() == niveauSuivant)
				.findFirst()
				.orElse(null);

		if (palierSuivant == null) {
			return false;
		}

		int popTotale = populationDisponible + populationOuvriere + populationMilitaire + populationEnEntrainement;
		if (popTotale < palierSuivant.getPopulationRequise()) {
			return false;
		}

		if (palierSuivant.getPrerequis() != null) {
			for (QuantiteRessource requis : palierSuivant.getPrerequis()) {
				if (requis != null && !aAssezDeRessources(requis.getRessource(), requis.getQuantite())) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean ameliorerNiveau(List<PalierNiveau> paliers) {
		if (!peutMonterDeNiveau(paliers)) {
			System.out.println("Ressources ou population insuffisantes pour monter de niveau.");
			return false;
		}

		int niveauSuivant = this.niveau + 1;
		PalierNiveau palierSuivant = paliers.stream()
				.filter(p -> p != null && p.getNiveau() == niveauSuivant)
				.findFirst()
				.orElse(null);

		if (palierSuivant == null) {
			return false;
		}

		if (palierSuivant.getPrerequis() != null) {
			for (QuantiteRessource requis : palierSuivant.getPrerequis()) {
				if (requis != null) {
					boolean debitReussi = debiterRessources(requis.getRessource(), requis.getQuantite());
					if (!debitReussi) {
						throw new IllegalStateException("Impossible de débiter les ressources requises : " + requis.getRessource());
					}
				}
			}
		}

		setNiveau(niveauSuivant);
		Chateau chateau = getChateau();
		if (chateau != null) {
			chateau.ameliorer();
			chateau.alerteNiveau10();
		}
		System.out.println("Le royaume atteint le niveau " + this.niveau);
		return true;
	}

	// ------------------------- Gestion des bâtiments & ouvriers (Module 4) -------------------------

	public Chateau getChateau() {
		return batiments.stream()
				.filter(Chateau.class::isInstance)
				.map(Chateau.class::cast)
				.findFirst()
				.orElse(null);
	}

	public double getBonusCroissance() {
		return bonusCroissance;
	}

	public void ajouterBonusCroissance(double bonus) {
		bonusCroissance += bonus;
	}

	public void retirerBonusCroissance(double bonus) {
		bonusCroissance = Math.max(0.0, bonusCroissance - bonus);
	}

	public void ajouterBatiment(Batiment batiment) {
		if (batiment == null) {
			throw new IllegalArgumentException("Le bâtiment ne peut pas être null");
		}

		if (batiment instanceof Chateau) {
			boolean chateauExiste = batiments.stream().anyMatch(Chateau.class::isInstance);
			if (chateauExiste) {
				throw new IllegalArgumentException("Un château existe déjà dans ce royaume");
			}
		}

		if (batiment instanceof BatimentProduction production) {
			boolean ressourceExiste = batiments.stream()
					.filter(BatimentProduction.class::isInstance)
					.map(BatimentProduction.class::cast)
					.anyMatch(b -> b.getTypeRessource() == production.getTypeRessource());

			if (ressourceExiste) {
				throw new IllegalArgumentException("Un bâtiment produit déjà cette ressource dans ce royaume");
			}
		}

		batiments.add(batiment);
	}

	public void retirerBatiment(Batiment batiment) {
		if (batiment == null) {
			return;
		}

		if (batiment instanceof Chateau) {
			this.enRuine = true;
			return;
		}

		if (batiment instanceof Maison maison) {
			retirerBonusCroissance(maison.getBonusPropre());
		}

		batiments.remove(batiment);
	}

	public boolean estEnRuine() {
		Chateau chateau = getChateau();
		if (chateau != null && chateau.getPointsDeVie() == 0) {
			this.enRuine = true;
		}
		return this.enRuine;
	}

	public Batiment getBatimentParId(Integer id) {
		return batiments.stream()
				.filter(b -> b.getId() != null && b.getId().equals(id))
				.findFirst()
				.orElse(null);
	}

	public void recevoirDegats(Batiment batiment, int degats) {
		if (batiment == null || !batiments.contains(batiment)) {
			throw new IllegalArgumentException("Ce bâtiment n'appartient pas à ce royaume.");
		}

		batiment.subirDegats(degats);

		if (batiment.getPointsDeVie() == 0) {
			retirerBatiment(batiment);
		}
	}

	public void affecterOuvriers(int nombre) {
		if (nombre <= 0) {
			throw new IllegalArgumentException("Le nombre d'ouvriers doit être positif.");
		}

		if (populationDisponible < nombre) {
			throw new IllegalStateException("Population disponible insuffisante.");
		}

		populationDisponible -= nombre;
		populationOuvriere += nombre;
	}

	public void libererOuvriers(int nombre) {
		if (nombre <= 0 || populationOuvriere < nombre) {
			throw new IllegalArgumentException("Impossible de libérer ces ouvriers.");
		}

		populationOuvriere -= nombre;
		populationDisponible += nombre;
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

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		checkNom(nom);
		this.nom = nom;
	}

	public Population getPeuple() {
		return peuple;
	}

	public void setPeuple(Population peuple) {
		if (this.peuple != null) {
			throw new UnsupportedOperationException("Le peuple d'un royaume est définitif et ne peut pas être modifié.");
		}
		validerPeuple(peuple);
		this.peuple = peuple;
	}

	public Population getPopulation() {
		return getPeuple();
	}

	public void setPopulation(Population population) {
		setPeuple(population);
	}

	public int getNiveau() {
		return niveau;
	}

	public void setNiveau(int niveau) {
		checkNiveau(niveau);
		this.niveau = niveau;
	}

	public int getPopulationDisponible() {
		return populationDisponible;
	}

	public void setPopulationDisponible(int populationDisponible) {
		checkPopulation(populationDisponible, "disponible");
		this.populationDisponible = populationDisponible;
	}

	public int getPopulationOuvriere() {
		return populationOuvriere;
	}

	public void setPopulationOuvriere(int populationOuvriere) {
		checkPopulation(populationOuvriere, "ouvrière");
		this.populationOuvriere = populationOuvriere;
	}

	public int getPopulationEnEntrainement() {
		return populationEnEntrainement;
	}

	public void setPopulationEnEntrainement(int populationEnEntrainement) {
		checkPopulation(populationEnEntrainement, "en entraînement");
		this.populationEnEntrainement = populationEnEntrainement;
	}

	public int getPopulationMilitaire() {
		return populationMilitaire;
	}

	public void setPopulationMilitaire(int populationMilitaire) {
		checkPopulation(populationMilitaire, "militaire");
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
		validerRessources(ressources);
		this.ressources = creerRessourcesParDefaut();
		if (ressources != null) {
			for (QuantiteRessource qr : ressources) {
				this.ressources.stream()
					.filter(r -> r.getRessource() == qr.getRessource())
					.findFirst()
					.ifPresent(r -> r.setQuantite(qr.getQuantite()));
			}
		}
	}

	public List<Batiment> getBatiments() {
		return Collections.unmodifiableList(batiments);
	}

	public EmplacementCarte getEmplacement() {
		return emplacement;
	}

	public void setEmplacement(EmplacementCarte emplacement) {
		this.emplacement = emplacement;
	}

	public int getX() {
		if (emplacement == null) {
			throw new IllegalStateException("Le royaume n'est rattaché à aucun emplacement.");
		}

		return emplacement.getCoordX();
	}

	public int getY() {
		if (emplacement == null) {
			throw new IllegalStateException("Le royaume n'est rattaché à aucun emplacement.");
		}

		return emplacement.getCoordY();
	}

	// =========================================================================
	// Méthodes helpers & Validations
	// =========================================================================

	private static List<QuantiteRessource> creerRessourcesParDefaut() {
		return new ArrayList<>(List.of(
			new QuantiteRessource(0, Ressource.PIERRE),
			new QuantiteRessource(0, Ressource.BOIS),
			new QuantiteRessource(0, Ressource.OR),
			new QuantiteRessource(0, Ressource.NOURRITURE)
		));
	}

	private void checkId(Integer id) {
		if (id != null && id <= 0) {
			throw new IllegalArgumentException("L'id ne peut pas être inférieur ou égal à 0.");
		}
	}

	private void checkNom(String nom) {
		if (nom == null || nom.isBlank()) {
			throw new IllegalArgumentException("Le nom du royaume ne peut pas être vide ou blanc.");
		}
	}

	private void validerPeuple(Population peuple) {
		if (peuple == null) {
			throw new IllegalArgumentException("Le peuple du royaume ne peut pas être null.");
		}
		if (!(peuple instanceof Canards
				|| peuple instanceof Gobelins
				|| peuple instanceof Licornes
				|| peuple instanceof Princesses
				|| peuple instanceof Teuteus)) {
			throw new IllegalArgumentException("Le peuple du royaume doit être l'un des 5 peuples disponibles (Canards, Gobelins, Licornes, Princesses, Teuteus).");
		}
	}

	private void checkNiveau(int niveau) {
		if (niveau < 1 || niveau > 10) {
			throw new IllegalArgumentException("Le niveau du royaume doit être compris entre 1 et 10.");
		}
	}

	private void checkPopulations(int populationDisponible, int populationOuvriere,
			int populationEnEntrainement, int populationMilitaire) {
		checkPopulation(populationDisponible, "disponible");
		checkPopulation(populationOuvriere, "ouvrière");
		checkPopulation(populationEnEntrainement, "en entraînement");
		checkPopulation(populationMilitaire, "militaire");
	}

	private void checkPopulation(int valeur, String nomChamp) {
		if (valeur < 0) {
			throw new IllegalArgumentException("La population " + nomChamp + " ne peut pas être négative.");
		}
	}

	private void validerRessources(List<QuantiteRessource> initiales) {
		if (initiales != null) {
			for (QuantiteRessource qr : initiales) {
				if (qr == null) {
					throw new IllegalArgumentException("La liste de ressources ne peut pas contenir d'élément null.");
				}
			}
			long nbDistinctes = initiales.stream()
					.map(QuantiteRessource::getRessource)
					.distinct()
					.count();
			if (nbDistinctes != initiales.size()) {
				throw new IllegalArgumentException("Il ne peut pas y avoir plusieurs quantiteRessource d'un même type de ressource.");
			}
		}
	}

	// =========================================================================
	// Représentation
	// =========================================================================

	@Override
	public String toString() {
		return "Royaume [id=" + id + ", nom=" + nom + ", peuple=" + (peuple != null ? peuple.getNom() : null)
				+ ", niveau=" + niveau + ", populationDisponible=" + populationDisponible + ", populationOuvriere="
				+ populationOuvriere + ", populationEnEntrainement=" + populationEnEntrainement
				+ ", populationMilitaire=" + populationMilitaire + ", enRuine=" + enRuine
				+ ", ressources=" + ressources + "]";
	}
}
