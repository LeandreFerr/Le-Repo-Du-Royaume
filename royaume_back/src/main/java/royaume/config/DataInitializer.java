package royaume.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
import royaume.model.militaire.StatutAttaque;
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

@Configuration
@Profile("dev")
public class DataInitializer {


		    @Bean
		    CommandLineRunner donneesDeTest(
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
				         
						PropositionEchange echange =
							propositionEchangeService.creerProposition(
								1,
								boisExpediteur,
								nourritureDestinataire
							);				         
				         
				         
				         
				         ////////////////// CARTE & EMPLACEMENTS (RAPIDE POUR BAGARRE) ////////////////////
				         
				         List<Royaume> tousRoyaumes = royaumeDAO.findAll();
				         boolean auMoinsUnRoyaumePlace = tousRoyaumes.stream()
				                 .anyMatch(r -> emplacementCarteDAO.findByRoyaumeId(r.getId()).isPresent());

				         if (!auMoinsUnRoyaumePlace) {
				             Partie partie = new Partie();
				             partie.setNom("Partie test");
				             partie.setDateCreation(LocalDateTime.now());
				             partie = partieDAO.save(partie);

				             Carte carte = new Carte();
				             carte.setPartie(partie);
				             carte.genererEmplacements(10);
				             carte = carteDAO.saveAndFlush(carte);

				             List<EmplacementCarte> emplacements =
				                     emplacementCarteDAO.findByCarteIdAndOccupeFalse(carte.getId());

				             int indexLibre = 0;
				             for (Royaume r : tousRoyaumes) {
				                 if (indexLibre < emplacements.size()) {
				                     EmplacementCarte emp = emplacements.get(indexLibre++);
				                     emp.assignerRoyaume(r);
				                     emplacementCarteDAO.saveAndFlush(emp);
				                 }
				             }
				         }

				         ////////////////// ENTRAINEMENT ////////////////////

				         EntrainerTroupeRequest premierGroupe = new EntrainerTroupeRequest(1, TypeTroupe.ARCHER,4);
				         EntrainerTroupeRequest secondGroupe = new EntrainerTroupeRequest(2, TypeTroupe.CAVALIER,3);
				         EntrainerTroupeRequest troisiemeGroupe = new EntrainerTroupeRequest(1, TypeTroupe.CAVALIER,2);
				         EntrainerTroupeRequest quatriemeGroupe = new EntrainerTroupeRequest(1, TypeTroupe.LANCIER,4);
				         EntrainerTroupeRequest cinquiemeGroupe = new EntrainerTroupeRequest(4, TypeTroupe.LANCIER,3);

				         SessionEntrainementResponse session = sessionEntrainementService.entrainer(premierGroupe);
				         SessionEntrainementResponse sessionbis = sessionEntrainementService.entrainer(secondGroupe);
				         SessionEntrainementResponse sessionter = sessionEntrainementService.entrainer(troisiemeGroupe);
				         SessionEntrainementResponse sessionquatre = sessionEntrainementService.entrainer(quatriemeGroupe);
				         SessionEntrainementResponse sessioncinq = sessionEntrainementService.entrainer(cinquiemeGroupe);

				         // FINALISER ENTRAINEMENT 

				         SessionEntrainement sessionEntity = sessionEntrainementDAO.findById(session.id()).orElseThrow();
				         Royaume royaumeEntrainement = royaumeDAO.findById(session.royaumeId()).orElseThrow();

				         sessionEntity.finaliserFormation(royaumeEntrainement);
				         royaumeDAO.saveAndFlush(royaumeEntrainement);
				         sessionEntrainementDAO.save(sessionEntity);
				         
				         SessionEntrainement sessionEntityTer = sessionEntrainementDAO.findById(sessionter.id()).orElseThrow();
				         Royaume royaumeEntrainementTer = royaumeDAO.findById(sessionter.royaumeId()).orElseThrow();
				         sessionEntityTer.finaliserFormation(royaumeEntrainementTer);
				         royaumeDAO.saveAndFlush(royaumeEntrainementTer);
				         sessionEntrainementDAO.save(sessionEntityTer);

				         SessionEntrainement sessionEntityQuatre = sessionEntrainementDAO.findById(sessionquatre.id()).orElseThrow();
				         Royaume royaumeEntrainementQuatre = royaumeDAO.findById(sessionquatre.royaumeId()).orElseThrow();
				         sessionEntityQuatre.finaliserFormation(royaumeEntrainementQuatre);
				         royaumeDAO.saveAndFlush(royaumeEntrainementQuatre);
				         sessionEntrainementDAO.save(sessionEntityQuatre);

				         SessionEntrainement sessionEntityCinq = sessionEntrainementDAO.findById(sessioncinq.id()).orElseThrow();
				         Royaume royaumeEntrainementCinq = royaumeDAO.findById(sessioncinq.royaumeId()).orElseThrow();
				         sessionEntityCinq.finaliserFormation(royaumeEntrainementCinq);
				         royaumeDAO.saveAndFlush(royaumeEntrainementCinq);
				         sessionEntrainementDAO.save(sessionEntityCinq);


				         ////////////////// BAGARRE  ALLER ////////////////////


				         Map<TypeTroupe, Integer> troupesCavaliers = Map.of(TypeTroupe.CAVALIER, 2);
				         Map<TypeTroupe, Integer> troupesLanciers = Map.of(TypeTroupe.LANCIER, 3);

				         emplacementCarteDAO.findByRoyaumeId(1).ifPresent(emp ->
				         System.out.println("Royaume 1 -> emplacement id=" + emp.getId()
				                 + ", numero=" + emp.getNumero()
				                 + ", occupe=" + emp.isOccupe()));

				         emplacementCarteDAO.findByRoyaumeId(3).ifPresent(emp ->
				         System.out.println("Royaume 3 -> emplacement id=" + emp.getId()
				                 + ", numero=" + emp.getNumero()
				                 + ", occupe=" + emp.isOccupe()));
				         
				         List<Royaume> tous = royaumeDAO.findAll();
				         System.out.println("IDs royaumes en base : " + tous.stream().map(Royaume::getId).toList());
				         System.out.println("Emp trouvé pour royaume 1 ? " + emplacementCarteDAO.findByRoyaumeId(1).isPresent());
				         System.out.println("Emp trouvé pour royaume 3 ? " + emplacementCarteDAO.findByRoyaumeId(3).isPresent());
				         
				         AttaqueResponse attaque1 = attaqueService.lancer(new LancerAttaqueRequest(1, 3, troupesCavaliers));
				         AttaqueResponse attaque2 = attaqueService.lancer(new LancerAttaqueRequest(4, 2, troupesLanciers));
				         AttaqueResponse attaque3 = attaqueService.lancer(new LancerAttaqueRequest(1, 2, troupesLanciers));
				         
				        
				         ////////////////// BAGARRE  IMPACT  ////////////////////
				        
				         AttaqueResponse apresCombat1 = attaqueService.resoudreCombat(attaque1.id());
				         AttaqueResponse apresCombat2 = attaqueService.resoudreCombat(attaque2.id());
				         
				         ////////////////// BAGARRE  RETOUR ////////////////////
				         
				         if (apresCombat1.statut() == StatutAttaque.RETOUR) {
				        	    AttaqueResponse apresRetour = attaqueService.receptionnerRetour(attaque1.id());
				        	    System.out.println("Après retour -> statut=" + apresRetour.statut());
				        	} else {
				        	    System.out.println("Attaque terminée sans retour (statut=" + apresCombat1.statut() + ")");
				        	}
				         


		            	  
//////////////////// TEST UTILISATEURS ////////////////////

System.out.println("========== TEST UTILISATEUR ==========");

// Récupération des royaumes
Royaume royaume1 = royaumeDAO.findById(1)
    .orElseThrow(() -> new IllegalStateException("Royaume 1 introuvable"));

Royaume royaume2 = royaumeDAO.findById(2)
    .orElseThrow(() -> new IllegalStateException("Royaume 2 introuvable"));

// Création de Kat -> Royaume 1
if (utilisateurDAO.findByEmail("kitkat@test.fr").isEmpty()) {

    Utilisateur kat = new Utilisateur(
        null,
        "Kat",
        "kitkat@test.fr",
        "motdepasse123",
        false,
        true,
        royaume1
    );

    utilisateurDAO.save(kat);
    System.out.println("Kat affectée au royaume 1 !");
}

// Création de Kit -> Royaume 2
if (utilisateurDAO.findByEmail("kit@test.fr").isEmpty()) {

    Utilisateur kit = new Utilisateur(
        null,
        "Kit",
        "kit@test.fr",
        "motdepasse123",
        false,
        false,
        royaume2
    );

    utilisateurDAO.save(kit);
    System.out.println("Kit affecté au royaume 2 !");
}


				};}}

