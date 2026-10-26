package royaume.test;

import java.util.List;

import royaume.model.PallierNiveau;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;

//////////////////////////////////////////////////////////////////////////////////////////////////////
/// 						 	                                    		    				   /// 
/// 					  On parle pas des batiments requis					      				   /// 
/// 																							   /// 
/// //////////////////////////////////////////////////////////////////////////////////////////////////



public class TestNiveau {
	  public static void main(String[] args) {
		  
		  
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	List<QuantiteRessource> rNiv2 =  List.of(new QuantiteRessource(100, Ressource.OR),
											 new QuantiteRessource(80, Ressource.PIERRE),
											 new QuantiteRessource(60, Ressource.BOIS),
											 new QuantiteRessource(90, Ressource.NOURRITURE));				 
											 
	PallierNiveau Niveau2 = new PallierNiveau(1, 2, 100, rNiv2);
			
	
	List<QuantiteRessource> rNiv3 =  List.of(new QuantiteRessource(120, Ressource.OR),
			 new QuantiteRessource(100, Ressource.PIERRE),
			 new QuantiteRessource(100, Ressource.BOIS),
			 new QuantiteRessource(1300, Ressource.NOURRITURE));				 
			 
	PallierNiveau Niveau3 = new PallierNiveau(2, 3, 200, rNiv3);
	
	
	List<QuantiteRessource> rNiv4 = List.of(
		    new QuantiteRessource(200, Ressource.OR),
		    new QuantiteRessource(150, Ressource.PIERRE),
		    new QuantiteRessource(150, Ressource.BOIS),
		    new QuantiteRessource(2000, Ressource.NOURRITURE)
		);
		PallierNiveau Niveau4 = new PallierNiveau(3, 4, 300, rNiv4);


		List<QuantiteRessource> rNiv5 = List.of(
		    new QuantiteRessource(300, Ressource.OR),
		    new QuantiteRessource(250, Ressource.PIERRE),
		    new QuantiteRessource(250, Ressource.BOIS),
		    new QuantiteRessource(2800, Ressource.NOURRITURE)
		);
		PallierNiveau Niveau5 = new PallierNiveau(4, 5, 400, rNiv5);


		List<QuantiteRessource> rNiv6 = List.of(
		    new QuantiteRessource(400, Ressource.OR),
		    new QuantiteRessource(350, Ressource.PIERRE),
		    new QuantiteRessource(350, Ressource.BOIS),
		    new QuantiteRessource(3600, Ressource.NOURRITURE)
		);
		PallierNiveau Niveau6 = new PallierNiveau(5, 6, 500, rNiv6);


		List<QuantiteRessource> rNiv7 = List.of(
		    new QuantiteRessource(550, Ressource.OR),
		    new QuantiteRessource(500, Ressource.PIERRE),
		    new QuantiteRessource(500, Ressource.BOIS),
		    new QuantiteRessource(4400, Ressource.NOURRITURE)
		);
		PallierNiveau Niveau7 = new PallierNiveau(6, 7, 600, rNiv7);


		List<QuantiteRessource> rNiv8 = List.of(
		    new QuantiteRessource(700, Ressource.OR),
		    new QuantiteRessource(650, Ressource.PIERRE),
		    new QuantiteRessource(650, Ressource.BOIS),
		    new QuantiteRessource(5200, Ressource.NOURRITURE)
		);
		PallierNiveau Niveau8 = new PallierNiveau(7, 8, 700, rNiv8);


		List<QuantiteRessource> rNiv9 = List.of(
		    new QuantiteRessource(850, Ressource.OR),
		    new QuantiteRessource(800, Ressource.PIERRE),
		    new QuantiteRessource(800, Ressource.BOIS),
		    new QuantiteRessource(6100, Ressource.NOURRITURE)
		);
		PallierNiveau Niveau9 = new PallierNiveau(8, 9, 800, rNiv9);


		List<QuantiteRessource> rNiv10 = List.of(
		    new QuantiteRessource(1000, Ressource.OR),
		    new QuantiteRessource(1000, Ressource.PIERRE),
		    new QuantiteRessource(1000, Ressource.BOIS),
		    new QuantiteRessource(7000, Ressource.NOURRITURE)
		);
		PallierNiveau Niveau10 = new PallierNiveau(9, 10, 1000, rNiv10);
		
	///////////////////////////////////////////////////////////////////////////////////////////////

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
		
		List<PallierNiveau> paliers = List.of(Niveau2, Niveau3, Niveau4, Niveau5, Niveau6, Niveau7, Niveau8, Niveau9, Niveau10);
		
  ////////////
  /// TEST ///
  ////////////
		while (PallierNiveau.peutMonterDeNiveau(royaume, paliers)) {

			boolean evolutionPossible = PallierNiveau.peutMonterDeNiveau(royaume, paliers);
	  		System.out.println(evolutionPossible ? "Le royaume peut passer au niveau " + (royaume.getNiveau() + 1) : "Le royaume n'est pas prêt pour une évolution de niveau."
	  		);
	  		
	  		PallierNiveau.ameliorerNiveau(royaume, paliers); }
	
			System.out.println( "Le royaume n'est plus prêt pour une évolution. Niveau final : " + royaume.getNiveau()
			);
  		
	  	}
	 }
