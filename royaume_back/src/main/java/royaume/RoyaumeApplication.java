package royaume;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

import royaume.dao.IDAOAttaque;
import royaume.dao.IDAOCarte;
import royaume.dao.IDAOEmplacementCarte;
import royaume.dao.IDAOMarche;
import royaume.dao.IDAOOffreMarche;
import royaume.dao.IDAOPartie;
import royaume.dao.IDAOPopulation;
import royaume.dao.IDAOPropositionEchange;
import royaume.dao.IDAORoyaume;
import royaume.dao.IDAOSessionEntrainement;
import royaume.dao.IDAOUtilisateur;
import royaume.dto.request.EntrainerTroupeRequest;
import royaume.dto.request.LancerAttaqueRequest;
import royaume.dto.response.AttaqueResponse;
import royaume.dto.response.SessionEntrainementResponse;
import royaume.model.Carte;
import royaume.model.EmplacementCarte;
import royaume.model.Partie;
import royaume.model.PropositionEchange;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
import royaume.model.Royaume;
import royaume.model.Utilisateur;
import royaume.model.batiment.Maison;
import royaume.model.militaire.SessionEntrainement;
import royaume.model.militaire.TypeTroupe;
import royaume.model.peuple.Canards;
import royaume.model.peuple.Gobelins;
import royaume.model.peuple.Licornes;
import royaume.model.peuple.Population;
import royaume.model.peuple.Princesses;
import royaume.model.peuple.Teuteus;
import royaume.service.AttaqueService;
import royaume.service.BatimentService;
import royaume.service.MarcheService;
import royaume.service.PropositionEchangeService;
import royaume.service.SessionEntrainementService;

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
		            IDAOPropositionEchange propositionEchangeDAO,
		            IDAOAttaque attaqueDAO,
		            IDAOSessionEntrainement sessionEntrainementDAO,
		            IDAOPartie partieDAO,
		            IDAOCarte carteDAO,
		            IDAOEmplacementCarte emplacementCarteDAO,
		            MarcheService marcheService,
		            BatimentService batimentService,
		            PropositionEchangeService propositionEchangeService,
		            AttaqueService attaqueService,
		            SessionEntrainementService sessionEntrainementService) {

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
		            	  
		            	  batimentService.creerBatimentProduction(2,null, "Mine des teuteus",500,Ressource.OR,1,1);
				            	 
				          batimentService.creerBatimentProduction(1,null, "Taverne des canards",500,Ressource.NOURRITURE,1,1);
				            	
		        
				         ///////////// MARCHE ////////////////////
				       
				        
				         QuantiteRessource orVendeur = new QuantiteRessource(4, Ressource.OR);
				         QuantiteRessource pierreVendeur = new QuantiteRessource(10, Ressource.PIERRE);
				         QuantiteRessource boisExpediteur = new QuantiteRessource(4, Ressource.BOIS);
				         QuantiteRessource nourritureDestinataire= new QuantiteRessource(5, Ressource.NOURRITURE);
				         
				         marcheService.publierOffre(2,orVendeur, pierreVendeur);
				         
				         PropositionEchange echange = propositionEchangeService.creerProposition(1, 3, boisExpediteur, nourritureDestinataire);
				         
				         echange = propositionEchangeService.accepterProposition(echange.getId());
				         
				         
				         
				         ////////////////// CARTE & EMPLACEMENTS (RAPIDE POUR BAGARRE) ////////////////////
				         
				         Partie partie = new Partie();
				         partie.setNom("Partie test");
				         partie.setDateCreation(LocalDateTime.now());
				         partie = partieDAO.save(partie);

				         Carte carte = new Carte();
				         carte.setPartie(partie);
				         carte.genererEmplacements(10);
				         carte = carteDAO.saveAndFlush(carte);

				         // Récupération des emplacements et royaumes
				         List<EmplacementCarte> emplacements =
				                 emplacementCarteDAO.findByCarteId(carte.getId());

				         List<Royaume> royaumes = royaumeDAO.findAll();

				         if (royaumes.size() < 2 || emplacements.size() < 2) {
				             throw new RuntimeException("Pas assez de royaumes ou d'emplacements !");
				         }

				         // Placement de deux royaumes
				         Royaume attaquant = royaumeDAO.findById(1).orElseThrow();
				         Royaume defenseur = royaumeDAO.findById(3).orElseThrow();

				         emplacements.get(0).assignerRoyaume(attaquant);
				         emplacementCarteDAO.saveAndFlush(emplacements.get(0));

				         emplacements.get(1).assignerRoyaume(defenseur);
				         emplacementCarteDAO.saveAndFlush(emplacements.get(1));

				         // Vérification
				         System.out.println("Attaquant : " + attaquant.getId());
				         System.out.println("Défenseur : " + defenseur.getId());
				         
				         ////////////////// BAGARRE ////////////////////
				         
				         /*Royaume attaquant = royaumeDAO.findById(1).orElseThrow();
				         Royaume defenseur = royaumeDAO.findById(3).orElseThrow();
				         Royaume attaquantbis = royaumeDAO.findById(4).orElseThrow();
				         Royaume defenseurbis = royaumeDAO.findById(2).orElseThrow();
				         
				         List<ContingentTroupe> troupesEnvoyees = List.of(new ContingentTroupe(TypeTroupe.CAVALIER,10)
				        );
				         List<ContingentTroupe> troupesEnvoyeesbis = List.of(new ContingentTroupe(TypeTroupe.LANCIER,30)
						);
				         
				         Attaque attaque = new Attaque(attaquant,defenseur,troupesEnvoyees, LocalDateTime.now(),1);
				         Attaque attaquebis = new Attaque(attaquantbis,defenseurbis,troupesEnvoyeesbis, LocalDateTime.now(),0.08);
				         Attaque attaqueter = new Attaque(attaquant,defenseurbis,troupesEnvoyeesbis, LocalDateTime.now(),1);
				         
				         attaque = attaqueDAO.save(attaque);
				         attaquebis = attaqueDAO.save(attaquebis);
				         attaqueter = attaqueDAO.save(attaqueter);*/
				         
				         Map<TypeTroupe, Integer> troupesCavaliers = Map.of(TypeTroupe.CAVALIER, 10);
				         Map<TypeTroupe, Integer> troupesLanciers = Map.of(TypeTroupe.LANCIER, 30);

				         AttaqueResponse attaque1 = attaqueService.lancer(new LancerAttaqueRequest(1, 3, troupesCavaliers));
				         AttaqueResponse attaque2 = attaqueService.lancer(new LancerAttaqueRequest(4, 2, troupesLanciers));
				         AttaqueResponse attaque3 = attaqueService.lancer(new LancerAttaqueRequest(1, 2, troupesLanciers));
				         
				         ////////////////// ENTRAINEMENT ////////////////////
				         
				         EntrainerTroupeRequest premierGroupe = new EntrainerTroupeRequest(1, TypeTroupe.ARCHER,4);
				         EntrainerTroupeRequest secondGroupe = new EntrainerTroupeRequest(2, TypeTroupe.CAVALIER,3);
				         
				         SessionEntrainementResponse session = sessionEntrainementService.entrainer(premierGroupe);
				         SessionEntrainementResponse sessionbis = sessionEntrainementService.entrainer(secondGroupe);
				         
				         // FINALISER ENTRAINEMENT 
				         
				         SessionEntrainement sessionEntity = sessionEntrainementDAO.findById(session.id()).orElseThrow();
				         Royaume royaumeEntrainement = royaumeDAO.findById(session.royaumeId()).orElseThrow();

				         sessionEntity.finaliserFormation(royaumeEntrainement);
				         royaumeDAO.saveAndFlush(royaumeEntrainement);
				         sessionEntrainementDAO.save(sessionEntity);
				         
				         
		    

				         
				         


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
