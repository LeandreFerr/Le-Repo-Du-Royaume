package royaume.model;

import java.util.List;

public class PallierNiveau {        
	
	private Integer id;
	private int niveau;
	private int populationRequise;
	private List<QuantiteRessource> prerequis;
	
	
	public PallierNiveau(Integer id, int niveau, int populationRequise, List<QuantiteRessource> prerequis) {
		this.id = id;
		this.niveau = niveau;
		this.populationRequise = populationRequise;
		this.prerequis = prerequis;
	}

	public PallierNiveau() {
		
	}
	
		
	public Integer getId() {
		return id;
	}

	
	public int getNiveau() {
		return niveau;
	}

	
	public int getPopulationRequise() {
		return populationRequise;
	}

	

	public List<QuantiteRessource> getPrerequis() {
		return prerequis;
	}

	

	///_________________________________________________________________________________________________________
	
	/* >> permet de renvoyer la quantité exigée pour une ressource donnée*/
	
	public static int getCoutRessource(Ressource ressourceRecherchee, Royaume royaume, List<PallierNiveau> paliers) {
		
		int niveauActuel = royaume.getNiveau();
		
		PallierNiveau palierSuivant = paliers.stream().filter(p -> p.getNiveau() == niveauActuel + 1).findFirst().orElse(null);
	    if (palierSuivant == null) {
	        return 0;
	    }
	  
	      Ressource type = ressourceRecherchee;
	      int quantiteNecessaire = palierSuivant.getPrerequis().stream()
		    		.filter(r->r.getRessource()== ressourceRecherchee)
		    		.mapToInt(QuantiteRessource::getQuantite)
		    		.findFirst()
		    		.orElse(0);
	      
	      System.out.println("Ressource : " + type + " | Quantité nécessaire : " + quantiteNecessaire);
	      
	      
	      return quantiteNecessaire;
	  
	         }
	   
	
	public static int getRessourceManquante(Ressource ressourceRecherchee, Royaume royaume, List<PallierNiveau> paliers) {
		
		int niveauActuel = royaume.getNiveau();
		
		PallierNiveau palierSuivant = paliers.stream().filter(p -> p.getNiveau() == niveauActuel + 1).findFirst().orElse(null);
	    if (palierSuivant == null) {
	        return 0;
	    }
	  
	      Ressource type = ressourceRecherchee;
	      
	      int quantiteNecessaire = palierSuivant.getPrerequis().stream()
		    		.filter(r->r.getRessource()== ressourceRecherchee)
		    		.mapToInt(QuantiteRessource::getQuantite)
		    		.findFirst()
		    		.orElse(0);
	      
	      int quantitePossedee = royaume.getRessources().stream()
		    		.filter(r->r.getRessource()== ressourceRecherchee)
		    		.mapToInt(QuantiteRessource::getQuantite)
		    		.sum();
	      
	      int quantiteManquante = Math.max(0,  quantiteNecessaire - quantitePossedee);
	      
	      System.out.println("Ressource : " + type + " | Quantité manquante : " + quantiteManquante);
	      
	      
	      return quantiteManquante;
	  
		
	}
	    
	    
	
	
	
	/*// >> vérifie que la population totale et le stock de ressources satisfont le palier du niveau supérieur  

	public static boolean peutMonterDeNiveau(Royaume royaume, List<PallierNiveau> paliers) {
	
		
		 int niveauActuel = royaume.getNiveau();
		 
		 
		 PallierNiveau palierSuivant = paliers.stream().filter(p -> p.getNiveau() == niveauActuel + 1).findFirst().orElse(null);
		    if (palierSuivant == null) {
		        return false;
		    }

		    int popTotale = royaume.getPopulationDisponible() + royaume.getPopulationOuvriere()
		    					+ royaume.getPopulationMilitaire() + royaume.getPopulationEnEntrainement();

		    if (popTotale < palierSuivant.getPopulationRequise()) {
		        return false;
		    }

		    for (QuantiteRessource requis : palierSuivant.getPrerequis()) {

		        int quantiteDisponible = royaume.getRessources().stream()
		                .filter(r -> r.getRessource() == requis.getRessource())
		                .mapToInt(QuantiteRessource::getQuantite).sum();

		        if (quantiteDisponible < requis.getQuantite()) {
		            return false;
		        }
		    }

		    return true;
		 
	}
	*/
	
	/*//>>débite les ressources du palier et incrémente le niveau du royaume 
	
	public static void ameliorerNiveau(Royaume royaume, List<PallierNiveau> paliers) {

	    if (peutMonterDeNiveau(royaume, paliers)) {
	    	
	    	int niveauSuivant = royaume.getNiveau() + 1;

	        PallierNiveau palierSuivant = paliers.stream()
	            .filter(p -> p.getNiveau() == niveauSuivant)
	            .findFirst()
	            .orElse(null);
	    	
	    	//System.out.println( "Le royaume peut passer au niveau " + niveauSuivant);
	        
	        // débiter les ressources 
	       
	        for (QuantiteRessource requis : palierSuivant.getPrerequis()) {

	            for (QuantiteRessource ressource : royaume.getRessources()) {

	                if (ressource.getRessource() == requis.getRessource()) {

	                    ressource.setQuantite(
	                        ressource.getQuantite() - requis.getQuantite()
	                    );

	                    break;
	                }
	            }
	        }
	        
	       royaume.setNiveau(niveauSuivant);

	       System.out.println("Le royaume atteint le niveau " + royaume.getNiveau());
	        
	        
	    } else {
	        System.out.println(
	            "Ressources ou population insuffisantes pour monter de niveau."
	        );
	    }
	}*/
}

