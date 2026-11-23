package royaume.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;


import royaume.model.militaire.TypeTroupe;
import royaume.model.peuple.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;

import royaume.model.militaire.ContingentTroupe;
import royaume.model.militaire.SessionEntrainement;
import royaume.model.militaire.Attaque;

@Entity
@Table(name = "royaume")
public class Royaume {

	// =========================================================================
	// Attributs
	// =========================================================================

	@Id
	@Positive
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@NotBlank
	@Size(min = 2, max = 50)
	@Column(name = "nom_du_royaume", nullable = false)
	private String nom;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "peuple", nullable = false)
	@NotNull
	@Valid
	private Population peuple;
	@Min(1)
	@Max(10)
	@Column(nullable = false)
	private int niveau = 1;
	@PositiveOrZero
	@Column(name = "population_disponible", nullable = false)
	private int populationDisponible = 100;
	@PositiveOrZero
	@Column(name = "population_ouvriere", nullable = false)
	private int populationOuvriere = 0;
	@PositiveOrZero
	@Column(name = "population_entrainement", nullable = false)
	private int populationEnEntrainement = 0;
	@PositiveOrZero
	@Column(name = "population_militaire", nullable = false)
	private int populationMilitaire = 0;

	@Column(name = "en_ruine", nullable = false)
	private boolean enRuine = false;

	@Column(name = "bonus_croissance", nullable = false)
	private double bonusCroissance = 0.0;
	@NotNull
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "ressources", columnDefinition = "json", nullable = false)
	private List<@NotNull @Valid QuantiteRessource> ressources = creerRessourcesParDefaut();

	@OneToMany(
	        mappedBy = "royaume",
	        cascade = CascadeType.ALL,
	        orphanRemoval = true,
	        fetch = FetchType.LAZY)
	private List<Batiment> batiments = new ArrayList<>();

	@Transient
	private EmplacementCarte emplacement;

	@Transient
	private Map<TypeTroupe, ContingentTroupe> garnison = new EnumMap<>(TypeTroupe.class);

	@Transient
	private List<SessionEntrainement> sessions = new ArrayList<>();

	// =========================================================================
	// Constructeurs
	// =========================================================================

	public Royaume() {}

	public Royaume(Integer id, String nom, Population peuple) {
		validerPeuple(peuple);
		this.id = id;
		this.nom = nom;
		this.peuple = peuple;
		BatimentFactory.creerChateauInitial(this);
	}

	public Royaume(Integer id, String nom, Population peuple, int niveau, int populationDisponible,
			int populationOuvriere, int populationEnEntrainement, int populationMilitaire, boolean enRuine,
			List<QuantiteRessource> ressources) {
		validerPeuple(peuple);
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
		validerPeuple(peuple);
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
		validerPeuple(peuple);
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
	    batiment.setRoyaume(this);
		batiments.add(batiment);
	}

	public void retirerBatiment(Batiment batiment) {
		 if (batiment == null || !batiments.contains(batiment)) {
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
        batiment.setRoyaume(null);
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
		this.id = id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
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
	/*
	 * public void setRessources(List<QuantiteRessource> ressources) {
	 * validerRessources(ressources); this.ressources = creerRessourcesParDefaut();
	 * for (QuantiteRessource qr : ressources) { this.ressources.stream() .filter(r
	 * -> r.getRessource() == qr.getRessource()) .findFirst() .ifPresent(r ->
	 * r.setQuantite(qr.getQuantite())); } }
	 */

	public void setRessources(List<QuantiteRessource> ressources) {

		this.ressources = creerRessourcesParDefaut();

		if (ressources == null) {
			return;
		}

		validerRessources(ressources);

		for (QuantiteRessource qr : ressources) {
			this.ressources.stream()
				.filter(r -> r.getRessource() == qr.getRessource())
				.findFirst()
				.ifPresent(r -> r.setQuantite(qr.getQuantite()));
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

	private void validerPeuple(Population peuple) {
		// Juste pour les test JPA : null sera détecté par Jakarta Bean Validation
		if (peuple == null) {
			return;
		}
		if (!(peuple instanceof Canards
				|| peuple instanceof Gobelins
				|| peuple instanceof Licornes
				|| peuple instanceof Princesses
				|| peuple instanceof Teuteus)) {
			throw new IllegalArgumentException("Le peuple du royaume doit être l'un des 5 peuples disponibles (Canards, Gobelins, Licornes, Princesses, Teuteus).");
		}
	}

	/*
	 * 
	 * private void validerRessources(List<QuantiteRessource> initiales) { if
	 * (initiales != null) {
	 * 
	 * long nbDistinctes = initiales.stream() .map(QuantiteRessource::getRessource)
	 * .distinct() .count(); if (nbDistinctes != initiales.size()) { throw new
	 * IllegalArgumentException("Il ne peut pas y avoir plusieurs quantiteRessource d'un même type de ressource."
	 * ); } } }
	 * 
	 */
	private void validerRessources(List<QuantiteRessource> initiales) {

		if (initiales == null) {
			return;
		}

		// 1. Vérifier qu'aucun élément n'est null
		if (initiales.stream().anyMatch(qr -> qr == null)) {
			throw new IllegalArgumentException("La liste des ressources ne peut pas contenir d'élément null.");
		}

		// 2. Vérifier que chaque ressource possède un type
		if (initiales.stream().anyMatch(qr -> qr.getRessource() == null)) {
			throw new IllegalArgumentException("Une QuantiteRessource doit posséder un type de ressource.");
		}

		// 3. Vérifier qu'il n'y a pas de doublons
		long nbDistinctes = initiales.stream()
				.map(QuantiteRessource::getRessource)
				.distinct()
				.count();

		if (nbDistinctes != initiales.size()) {
			throw new IllegalArgumentException("Il ne peut pas y avoir plusieurs QuantiteRessource d'un même type de ressource.");
		}
	}



	// =========================================================================
	// Militaire
	// =========================================================================

	public boolean peutRecruter(TypeTroupe troupe) {
		return peutEntrainer(troupe, 1);
	}

	public boolean recruterTroupe(TypeTroupe troupe) {
		return entrainerTroupe(troupe, 1, LocalDateTime.now());
	}

	private boolean peutEntrainer(TypeTroupe type, int quantite) {
		if (type == null) {
			throw new IllegalArgumentException("Le type de troupe ne peut pas être null.");
		}
		if (quantite <= 0) {
			throw new IllegalArgumentException("La quantité doit être strictement positive.");
		}
		if (populationDisponible < quantite) {
			return false;
		}
		for (Map.Entry<Ressource, Integer> e : type.getCout().entrySet()) {
			if (!aAssezDeRessources(e.getKey(), e.getValue() * quantite)) {
				return false;
			}
		}
		return true;
	}

	/** Débite le coût, bascule les citoyens en entraînement et crée la session. */
	public boolean entrainerTroupe(TypeTroupe type, int quantite, LocalDateTime maintenant) {
		if (!peutEntrainer(type, quantite)) {
			return false;
		}
		// Tout est vérifié : pas de risque de paiement partiel
		type.getCout().forEach((r, c) -> debiterRessources(r, c * quantite));

		populationDisponible -= quantite;
		populationEnEntrainement += quantite;

		Duration duree = Duration.ofMinutes(5L * quantite);   // à ajuster
		sessions.add(new SessionEntrainement(type, quantite, maintenant, maintenant.plus(duree)));
		return true;
	}

	/** Appelé par SessionEntrainement.finaliserFormation(). */
	public void terminerFormation(TypeTroupe type, int quantite) {
		populationEnEntrainement -= quantite;
		populationMilitaire += quantite;
		ajouterTroupes(type, quantite);
	}

	/** À appeler périodiquement (scheduler plus tard). */
	public void traiterFormations(LocalDateTime maintenant) {
		sessions.stream()
				.filter(s -> !s.isFinalisee() && s.estTerminee(maintenant))
				.forEach(s -> s.finaliserFormation(this));
	}

	public void ajouterTroupes(TypeTroupe type, int qte) {
		if (qte <= 0) return;
		garnison.computeIfAbsent(type, t -> new ContingentTroupe(t, 0)).ajouter(qte);
	}

	public List<ContingentTroupe> getGarnison() {
		return List.copyOf(garnison.values());
	}

	public int getPuissanceDefensive() {
		return garnison.values().stream()
				.mapToInt(c -> c.getQuantite() * c.getType().getDefense())
				.sum();
	}

	/** Les troupes quittent la garnison (elles restent comptées dans populationMilitaire). */
	// passer par un contingent (un groupe de troupe)
	// (contingent fait office de map)   //  c'est bon ?
	public Attaque lancerAttaque(Royaume cible, Map<TypeTroupe, Integer> troupes,
								double distance, LocalDateTime maintenant) {
		// Vérification avant retrait, pour ne pas vider la garnison à moitié
		for (var e : troupes.entrySet()) {
			ContingentTroupe c = garnison.get(e.getKey());
			if (e.getValue() <= 0 || c == null || c.getQuantite() < e.getValue()) {
				throw new IllegalArgumentException("Effectif insuffisant pour " + e.getKey().getNom());
			}
		}
		List<ContingentTroupe> convoi = new ArrayList<>();
		for (var e : troupes.entrySet()) {
			garnison.get(e.getKey()).retirer(e.getValue());
			convoi.add(new ContingentTroupe(e.getKey(), e.getValue()));
		}
		return new Attaque(this, cible, convoi, maintenant, distance);
	}

	/** Pertes subies par la garnison (ratio entre 0 et 1). */
	public void appliquerPertes(double ratio) {
		for (ContingentTroupe c : garnison.values()) {
			int perdus = (int) Math.round(c.getQuantite() * ratio);
			c.retirer(perdus);
			perdreMilitaires(perdus);
		}
	}

	public void perdreMilitaires(int nombre) {
		populationMilitaire = Math.max(0, populationMilitaire - nombre);
	}

	/** Prélève des ressources dans la limite de la capacité d'emport et des stocks réels. */
	public List<QuantiteRessource> subirPillage(int capaciteEmportMax) {
		List<QuantiteRessource> pris = new ArrayList<>();
		int reste = capaciteEmportMax;
		for (QuantiteRessource qr : ressources) {
			int prelevement = Math.min(qr.getQuantite(), reste);
			if (prelevement > 0) {
				qr.retirer(prelevement);
				pris.add(new QuantiteRessource(prelevement, qr.getRessource()));
				reste -= prelevement;
			}
		}
		return pris;
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
