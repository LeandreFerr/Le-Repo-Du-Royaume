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
	
	
	
	/* >> vérifie que la population totale et le stock de ressources satisfont le palier du niveau supérieur  */

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
	
	
	/* >>débite les ressources du palier et incrémente le niveau du royaume */
	
	public static void ameliorerNiveau(Royaume royaume, List<PallierNiveau> paliers) {

	    if (peutMonterDeNiveau(royaume, paliers)) {
	        System.out.println( "Le royaume peut passer au niveau " + (royaume.getNiveau() + 1));

	        royaume.setNiveau(royaume.getNiveau()+1);
	        System.out.println("Le royaume atteint le  niveau  " +  royaume.getNiveau());
	        // débiter les ressources 
	        
	      //  royaume.setRessources(royaume.getRessources().retirer())
	        
	    } else {
	        System.out.println(
	            "Ressources ou population insuffisantes pour monter de niveau."
	        );
	    }
	}
}

