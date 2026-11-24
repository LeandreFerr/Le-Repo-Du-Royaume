package royaume;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/*@SpringBootApplication
public class RoyaumeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoyaumeApplication.class, args);
    }
}*/
import org.springframework.context.annotation.Bean;

import royaume.dao.IDAOMarche;
import royaume.dao.IDAOOffreMarche;
import royaume.dao.IDAOPopulation;
import royaume.dao.IDAORoyaume;
import royaume.dao.IDAOUtilisateur;
import royaume.model.OffreMarche;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;
import royaume.model.Utilisateur;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.Maison;
import royaume.model.peuple.Canards;
import royaume.model.peuple.Gobelins;
import royaume.model.peuple.Licornes;
import royaume.model.peuple.Population;
import royaume.model.peuple.Princesses;
import royaume.model.peuple.Teuteus;
import royaume.service.BatimentService;
import royaume.service.MarcheService;

@SpringBootApplication

public class RoyaumeApplication {

	
	public static void main(String[] args) {
		
		SpringApplication.run(RoyaumeApplication.class, args);
	}


		    @Bean
		    CommandLineRunner testJPA(
		            IDAOPopulation populationDAO,
		            IDAORoyaume royaumeDAO,
		            IDAOOffreMarche offreMarcheDAO,
		            IDAOUtilisateur utilisateurDAO,
		            IDAOMarche marcheDAO,
		            MarcheService marcheService,
		            BatimentService batimentService) {

		        return args -> {

		            System.out.println("========== TEST JPA ==========");
		            
		            List<Population> populations = List.of(
		            	    new Canards(null, "Canards", "Roi Coin-Coin", 1.5),
		            	    new Teuteus(null, "Teuteus", "Roi Teuteu", 0.4),
		            	    new Gobelins(null, "Gobelins", "Roi Gobelin", 1.9),
		            	    new Princesses(null, "Princesses", "Reine des Princesses", 15.0),
		            	    new Licornes(null, "Licornes", "Reine des Licornes", 1.0)
		            	);

		            	for (Population population : populations) {

		            	    population = populationDAO.save(population);
		            	    
		            	          String nomRoyaume = "Le Royaume des " + population.getNom();
		            	        Royaume royaume;

		            	        if (!royaumeDAO.existsByNom(nomRoyaume)) {
		            	            royaume = new Royaume(
		            	                null,
		            	                nomRoyaume,
		            	                population
		            	            );
		            	            royaume = royaumeDAO.save(royaume);
		            	            System.out.println("Royaume créé : " + royaume);
		            	        } else {
		            	        	royaume = royaumeDAO.findByNom(nomRoyaume).stream().findFirst().orElse(null);
		            	            System.out.println("Royaume existe déjà : " + nomRoyaume);
		            	        }

		            	        System.out.println("Population : " + population);
		            	        System.out.println("Royaume : " + royaume);
		            	    }
		            	
		            	
		            	 Maison maison = batimentService.creerMaison(
		            	            5,
		            	            null,
		            	            "Maison des licornes",
		            	            2
		            	        );

		            	        System.out.println("Maison créée : " + maison.getNom());
		            	  
		            	 BatimentProduction mine = batimentService.creerBatimentProduction(2,null, "Mine des teuteus",500,Ressource.OR,1,1);
				            	 
				         BatimentProduction taverne = batimentService.creerBatimentProduction(1,null, "Taverne des canards",500,Ressource.NOURRITURE,1,1);
				            	
		        
				         ///////////// MARCHE ////////////////////
				       
				        
				         QuantiteRessource orVendeur = new QuantiteRessource(500, Ressource.OR);
				         QuantiteRessource pierreVendeur = new QuantiteRessource(10, Ressource.PIERRE);
				         
				         OffreMarche offre = marcheService.publierOffre(2,orVendeur, pierreVendeur);
				         
				         
				         
				         

		            	  // Création d'un utilisateur
		                System.out.println("========== TEST UTILISATEUR ==========");

		                Utilisateur utilisateur = new Utilisateur(
		                    null,
		                    "Kat",
		                    "kitkat@test.fr",
		                    "motdepasse123",
		                    false,
		                    true,
		                    null
		                );

						if (utilisateurDAO.findByEmail("kitkat@test.fr").isEmpty()) {
							utilisateurDAO.save(utilisateur);
						}
		                System.out.println("Utilisateur créé : " + utilisateur);

		                Utilisateur kit = new Utilisateur(
		                	    null,
		                	    "Kit",
		                	    "kit@test.fr",
		                	    "motdepasse123",
		                	    false,
		                	    false,
		                	    null
		                	);

		                	if (utilisateurDAO.findByEmail("kit@test.fr").isEmpty()) {
		                	    utilisateurDAO.save(kit);
		                	    System.out.println("Utilisateur Kit créé !");
		                	} else {
		                	    System.out.println("Utilisateur Kit existe déjà.");
		                	}

		                
		    
		};
		    }
}
