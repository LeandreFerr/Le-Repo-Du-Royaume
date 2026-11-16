package royaume.model.batiment;

import java.util.ArrayList;
import java.util.List;

import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

public abstract class Batiment {
	
	protected Integer id;
	protected String nom;
	protected int niveau = 1;
	protected int pointsDeVie;
	protected int pointsDeVieMax;
	protected boolean enConstruction = false; // A passer à true dans Chantier mais là init à false.
	private ChantierConstruction chantier; // ça c'est optionnel ET nullable :  quand ya pas besion ;  on peut le mettre null
	
	protected int montantReparationEnCours = 0;
	
	
	public Batiment(Integer id, String nom, int niveau, int pointsDeVie, int pointsDeVieMax, boolean enConstruction) {
		this.id = id;
		this.nom = nom;
		this.niveau = niveau;
		this.pointsDeVie = pointsDeVie;
		this.pointsDeVieMax = pointsDeVieMax;
		this.enConstruction = enConstruction;
	}

	public Batiment() {
	}

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

	public int getPointsDeVie() {
		return pointsDeVie;
	}

	public void setPointsDeVie(int pointsDeVie) {
		this.pointsDeVie = pointsDeVie;
	}

	public int getPointsDeVieMax() {
		return pointsDeVieMax;
	}

	public void setPointsDeVieMax(int pointsDeVieMax) {
		this.pointsDeVieMax = pointsDeVieMax;
	}

	public boolean isEnConstruction() {
		return enConstruction;
	}

	public void setEnConstruction(boolean enConstruction) {
		this.enConstruction = enConstruction;
	}
	
	public ChantierConstruction getChantier() {
	    return chantier;
	}

	public void setChantier(ChantierConstruction chantier) {
	    this.chantier = chantier;
	}
	
	public int getMontantReparationEnCours() {
		return montantReparationEnCours;
	}

	public void setMontantReparationEnCours(int montantReparationEnCours) {
		this.montantReparationEnCours = montantReparationEnCours;
	}
	
	
	//___________________________________________________________________
	
	


	public void subirDegats(int degats) {
	
	    if (degats < 0) {
	        throw new IllegalArgumentException(
	            "Dégâts négatifs interdits"
	        );
	    }
	
	    pointsDeVie = Math.max(0, pointsDeVie - degats);
	}

	
	/*
	 * [REVIEW TICKET 2 - Par rapport à vos commentaires sur Trello]
	 * Votre raisonnement est 100% le bon : comme c'est le Royaume qui connaît et gère
	 * sa liste de bâtiments et pas l'inverse, avoir déplacé la destruction dans
	 * Royaume.recevoirDegats() / retirerBatiment() c'est nickel ! Vous pourrez
	 * supprimer ce code commenté du coup.
	 */
	/*   MIGRATION vers le Royaume 
	 * 
	 * Parceque le Royaume connais ses batiments VS le batiment ne connais pas son Royaume
	 * 
	 * 
	 * public void detruire(Royaume royaume) {

	    if (royaume == null) {
	        throw new IllegalArgumentException("Le royaume ne peut pas être null");
	    }

	    royaume.retirerBatiment(this);
	}*/
	
	
	//___________________________________

	 // Attention la fonction reparer va avec la fonction verifierEtFinaliserReparation
	// Ne pas mélanger avec fonction construire un bâtiment ! 
	public void reparer(int montant, int nbouvriersAssignes, Royaume royaume) {
		
		// [REVIEW TICKET 2] : Pensez à vérifier si le bâtiment est déjà en chantier 
		// On ne peut pas réparer un batiment en construction.
		// Sinon on dit que ça gele la construction le temps de la réparation.

		 if (montant < 0) {
	            throw new IllegalArgumentException("Réparation négative interdite");
	        }
		
		if (nbouvriersAssignes < 1) {
	        throw new IllegalArgumentException("Il faut au moins 1 ouvrier pour réparer.");
	    }
		
       this.montantReparationEnCours = montant;
        
        // On passe bien nbouvriersAssignes au lieu de 1,1 en dur
        this.chantier = new ChantierConstruction(null, this, royaume,1, nbouvriersAssignes, this.getTempsConstructionBase(), false);
        this.enConstruction = true; // S'assurer que le bâtiment passe en construction pour la réparation
    }

	
	/*public void verifierEtFinaliserReparation() {
	   
	    if (this.enConstruction && this.chantier != null && this.chantier.estPretAAchever()) {
	        
	        this.pointsDeVie = Math.min(this.pointsDeVieMax, this.pointsDeVie + this.montantReparationEnCours);
	        this.montantReparationEnCours = 0; // On remet à zéro

	        this.chantier.finaliser(); // ou ta logique de fin
	    }
	}*/
	
	
	public boolean estOperationnel() {
		
		// renvoie true si pointsDeVie > 0 AND !enConstruction : 
		
        return pointsDeVie > 0 && !enConstruction;
	}
	
	//______________________
	
	public void ameliorer(int nbouvriersAssignes, Royaume royaume) {
		
		// [REVIEW TICKET 2] : Même chose ici, vérifier si le bâtiment est déjà en chantier
		
		if (nbouvriersAssignes < 1) {
	        // [REVIEW TICKET 2] : Petit problème de copier-coller ici, je me suis permis de modifier :
			// réparer -> améliorer. Dans le texte de l'exception :)
	        throw new IllegalArgumentException("Il faut au moins 1 ouvrier pour améliorer.");
	    }
		
		// Si ce n'est pas le château, on vérifie qu'on ne dépasse pas le palier autorisé
		if (!(this instanceof Chateau)
		        && niveau >= royaume.getChateau().getNiveauPalierDebloque()) {

		    throw new IllegalStateException(
		        "Impossible d'améliorer : le niveau du bâtiment ne peut pas dépasser le niveau du château."
		    );
		}
	    
	    //  Vérifier si le royaume a assez de ressources pour le coût d'amélioration
	    List<QuantiteRessource> couts = getCoutAmelioration();
	    for (QuantiteRessource cout : couts) {
	        if (!royaume.aAssezDeRessources(cout.getRessource(), cout.getQuantite())) {
	            throw new IllegalStateException("Ressources insuffisantes pour améliorer le bâtiment ");
	        }
	    }
	    
	    // [REVIEW TICKET 2 - Réponse question Trello]
	    // Débiter les ressources directement ici au lancement du chantier, c'est nickel !
	    // Débiter les ressources du royaume
	    for (QuantiteRessource cout : couts) {
	       royaume.debiterRessources(cout.getRessource(), cout.getQuantite());
	    }
	    
	    this.chantier = new ChantierConstruction(null, this, royaume,1, nbouvriersAssignes, this.getTempsConstructionBase(), true);
        this.enConstruction = true; //
        
	}
	//////////////////////////////////////////////
	/// 
    /// // Version qui sera appelée quand le chantier se finalise
	/*
	 * [REVIEW TICKET 2 - Par rapport à vos commentaires Trello]
	 * Pour que ce soit plus clair, je pense que ça serait bien de renommer celle-ci en
	 * `void acheverAmelioration(Royaume royaume)` avec une visibilité au niveau du package
	 * (sans `public`, comme vous l'avez fait pour les constructeurs avec BatimentFactory).
	 * Comme ça, seul ChantierConstruction.finaliser() pourra l'appeler et personne ne
	 * pourra l'appeler par erreur depuis l'extérieur !
	 */
	public void ameliorer(Royaume royaume) {

	    if (royaume == null) {
	        throw new IllegalArgumentException(
	            "Le royaume ne peut pas être null."
	        );
	    }

	    if (!(this instanceof Chateau)
	            && niveau >= royaume.getChateau().getNiveauPalierDebloque()) {

	        throw new IllegalStateException(
	            "Impossible d'améliorer : le niveau du bâtiment ne peut pas dépasser le niveau du château."
	        );
	    }

	    niveau++;

	    pointsDeVieMax = calculerPointsDeVieMax();
	    pointsDeVie = pointsDeVieMax;
	}
	
	/*
	 * [REVIEW TICKET 2 - Réponse à un commentaire Trello]
	 * Comme `royaume.getChateau()` renvoie directement un objet de type `Chateau`,
	 * vous pouvez même supprimer cette méthode vide de `Batiment` (et retirer le
	 * `@Override` sur `ameliorer()` dans `Chateau`) : comme ça `ameliorer()` sans
	 * paramètre n'existera QUE dans `Chateau` et plus personne ne pourra l'appeler
	 * par erreur sur une Maison ou un autre bâtiment !
	 */
	// Celui la est spécifique au chateau pour Override
	public void ameliorer() {
	    }
	
	
	
	protected int calculerPointsDeVieMax() {
        return 50 * niveau;                        // PVmax = 50 * niveau du batiment
    }
	//______________________


	public abstract List<QuantiteRessource> getCoutAmelioration();

	public List<QuantiteRessource> getRessourceManquanteAmelioration(Royaume royaume) {
	    List<QuantiteRessource> ressourcesManquantes = new ArrayList<>();
	    List<QuantiteRessource> couts = getCoutAmelioration(); // Appellera la bonne version selon la fille (Mine, Carriere...)

	    for (QuantiteRessource cout : couts) {
	        Ressource ressource = cout.getRessource();
	        int quantiteRequise = cout.getQuantite();

	        int quantitePossedee = royaume.getRessources().stream()
	                .filter(r -> r.getRessource() == ressource)
	                .mapToInt(QuantiteRessource::getQuantite)
	                .findFirst()
	                .orElse(0);

	        if (quantitePossedee < quantiteRequise) {
	            int manque = quantiteRequise - quantitePossedee;
	            ressourcesManquantes.add(new QuantiteRessource(manque, ressource));
	        }
	    }

	    return ressourcesManquantes;
	}
	
	public abstract int getTempsConstructionBase() ;
	//  durée de base en secondes ou ticks
	
}
