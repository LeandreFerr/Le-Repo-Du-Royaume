package royaume.test;

import java.util.List;

import royaume.config.ConfigurationPalliers;
import royaume.model.PallierNiveau;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

//////////////////////////////////////////////////////////////////////////////////////////////////////
/// 						 	                                    		    				   /// 
/// 					 Test  PallierNiveau		                		  		        	   /// 
/// 																							   /// 
/// //////////////////////////////////////////////////////////////////////////////////////////////////



public class TestNiveau {
	  public static void main(String[] args) {
		  
		  
		  List<PallierNiveau> paliers = ConfigurationPalliers.getPaliers();

	///////////////////////////////////////////////////////////////////////////////////////////////

		    int populationDisponible = 500;
			int populationOuvriere = 500;
			int populationMilitaire = 100;
			int populationEnEntrainement = 201;  // 301
			List<QuantiteRessource> ressources = List.of(
		    /*new QuantiteRessource(600, Ressource.OR),
		    new QuantiteRessource(800, Ressource.PIERRE),
		    new QuantiteRessource(600, Ressource.BOIS),
		    new QuantiteRessource(4000, Ressource.NOURRITURE)*/
		    new QuantiteRessource(1000, Ressource.OR),
		    new QuantiteRessource(1000, Ressource.PIERRE),
		    new QuantiteRessource(1000, Ressource.BOIS),
		    new QuantiteRessource(6000, Ressource.NOURRITURE) // 7000 pour niveau 10 
		);
	
	
			Royaume royaume = new Royaume(1, "Fadastras", 1,  populationDisponible,  populationOuvriere,
					 populationEnEntrainement,  populationMilitaire, false, ressources);
		
		
		
		
  ////////////
  /// TEST ///
  ////////////
 
		
		
		
		while (royaume.peutMonterDeNiveau(paliers)) {

			boolean evolutionPossible = royaume.peutMonterDeNiveau(paliers);
	  		
			int coutBois =  PallierNiveau.getCoutRessource(Ressource.BOIS,royaume,paliers);
			int coutOr =  PallierNiveau.getCoutRessource(Ressource.OR,royaume,paliers);
			int coutPierre =  PallierNiveau.getCoutRessource(Ressource.PIERRE,royaume,paliers);
			int coutNourriture =  PallierNiveau.getCoutRessource(Ressource.NOURRITURE,royaume,paliers);
			
			
			System.out.println(evolutionPossible ? "Le royaume peut passer au niveau " + (royaume.getNiveau() + 1) : "Le royaume n'est pas prêt pour une évolution de niveau."
	  		);
	  		
	  		royaume.ameliorerNiveau(paliers); 
	  		
	  		}
	
			System.out.println( "Le royaume n'est plus prêt pour une évolution. Niveau final : " + royaume.getNiveau());
	
			int coutBois =  PallierNiveau.getCoutRessource(Ressource.BOIS,royaume,paliers);
			int coutOr =  PallierNiveau.getCoutRessource(Ressource.OR,royaume,paliers);
			int coutPierre =  PallierNiveau.getCoutRessource(Ressource.PIERRE,royaume,paliers);
			int coutNourriture =  PallierNiveau.getCoutRessource(Ressource.NOURRITURE,royaume,paliers);
			
			
			int manqueBois = PallierNiveau.getRessourceManquante(Ressource.BOIS,royaume,paliers);
			int manqueOr = PallierNiveau.getRessourceManquante(Ressource.OR,royaume,paliers);
			int manquePierre = PallierNiveau.getRessourceManquante(Ressource.PIERRE,royaume,paliers);
			int manqueNourriture = PallierNiveau.getRessourceManquante(Ressource.NOURRITURE,royaume,paliers);
			
	  
			System.out.println(royaume.getNiveau());
	  		}
	  
	  		
	  
	  
	  
	  
	 }
