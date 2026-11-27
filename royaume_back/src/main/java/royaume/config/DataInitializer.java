package royaume.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

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
@Profile({"dev", "demo"})
public class DataInitializer {

	@Bean
	@Order(3)
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

			if (royaumeDAO.count() > 0) {
				System.out.println("========== DONNEES DEMO DEJA PRESENTES (SKIP) ==========");
				return;
			}

			System.out.println("========== INITIALISATION DONNEES DEMO ==========");

			Map<String, Population> popsParNom = new HashMap<>();
			for (Population p : populationDAO.findAll()) {
				popsParNom.put(p.getNom(), p);
			}

			List<Population> populationsParDefaut = List.of(
					new Canards(null, "Canards", "Roi Coin-Coin", 1.5),
					new Teuteus(null, "Teuteus", "Roi Teuteu", 0.4),
					new Gobelins(null, "Gobelins", "Roi Gobelin", 1.9),
					new Princesses(null, "Princesses", "Reine des Princesses", 15.0),
					new Licornes(null, "Licornes", "Reine des Licornes", 1.0)
			);

			for (Population candidate : populationsParDefaut) {
				popsParNom.computeIfAbsent(candidate.getNom(), nom -> populationDAO.save(candidate));
			}

			Map<String, Royaume> royaumesParPeuple = new HashMap<>();
			for (String nomPeuple : List.of("Canards", "Teuteus", "Gobelins", "Princesses", "Licornes")) {
				Population population = popsParNom.get(nomPeuple);
				String nomRoyaume = "Le Royaume des " + population.getNom();
				Royaume royaume = royaumeDAO.save(new Royaume(null, nomRoyaume, population));
				royaumesParPeuple.put(nomPeuple, royaume);
				System.out.println("Royaume créé : " + royaume.getNom() + " (id=" + royaume.getId() + ")");
			}

			Royaume rCanards = royaumesParPeuple.get("Canards");
			Royaume rTeuteus = royaumesParPeuple.get("Teuteus");
			Royaume rGobelins = royaumesParPeuple.get("Gobelins");
			Royaume rPrincesses = royaumesParPeuple.get("Princesses");
			Royaume rLicornes = royaumesParPeuple.get("Licornes");

			Integer idCanards = rCanards.getId();
			Integer idTeuteus = rTeuteus.getId();
			Integer idGobelins = rGobelins.getId();
			Integer idPrincesses = rPrincesses.getId();
			Integer idLicornes = rLicornes.getId();

			Maison maison = batimentService.creerMaison(
					idLicornes,
					null,
					"Maison des licornes",
					2
			);
			System.out.println("Maison créée : " + maison.getNom());

			batimentService.creerBatimentProduction(idTeuteus, null, "Mine des teuteus", 500, Ressource.OR, 1, 1);
			batimentService.creerBatimentProduction(idCanards, null, "Taverne des canards", 500, Ressource.NOURRITURE, 1, 1);

			///////////// MARCHE ////////////////////

			QuantiteRessource orVendeur = new QuantiteRessource(4, Ressource.OR);
			QuantiteRessource pierreVendeur = new QuantiteRessource(10, Ressource.PIERRE);
			QuantiteRessource boisExpediteur = new QuantiteRessource(4, Ressource.BOIS);
			QuantiteRessource nourritureDestinataire = new QuantiteRessource(5, Ressource.NOURRITURE);

			marcheService.publierOffre(idTeuteus, orVendeur, pierreVendeur);
			propositionEchangeService.creerProposition(idCanards, boisExpediteur, nourritureDestinataire);

			////////////////// PARTIES & CARTES ////////////////////

			List<Royaume> tousRoyaumes = List.of(rCanards, rTeuteus, rGobelins, rPrincesses, rLicornes);

			Partie partieEnCours = new Partie("Partie Démo - En cours", 1.0, 10);
			for (Royaume r : tousRoyaumes) {
				partieEnCours.inscrireRoyaume(r);
			}
			partieEnCours.demarrer();
			partieEnCours = partieDAO.save(partieEnCours);

			Carte carteEnCours = new Carte();
			carteEnCours.setPartie(partieEnCours);
			carteEnCours.setArchetype("VALLEE_ROYALE");
			carteEnCours.setSeed("demo-en-cours");
			carteEnCours.genererEmplacements(10);
			carteEnCours = carteDAO.saveAndFlush(carteEnCours);

			List<EmplacementCarte> emplacements =
					emplacementCarteDAO.findByCarteIdAndOccupeFalse(carteEnCours.getId());

			int indexLibre = 0;
			for (Royaume r : tousRoyaumes) {
				if (indexLibre < emplacements.size()) {
					EmplacementCarte emp = emplacements.get(indexLibre++);
					emp.assignerRoyaume(r);
					emplacementCarteDAO.saveAndFlush(emp);
				}
			}

			Partie partieOuverte = new Partie("Salon Démo - Ouvert", 1.0, 10);
			partieOuverte = partieDAO.save(partieOuverte);

			Carte carteOuverte = new Carte();
			carteOuverte.setPartie(partieOuverte);
			carteOuverte.setArchetype("LAC_CENTRAL");
			carteOuverte.setSeed("demo-salon-ouvert");
			carteOuverte.genererEmplacements(10);
			carteDAO.saveAndFlush(carteOuverte);

			////////////////// ENTRAINEMENT ////////////////////

			EntrainerTroupeRequest premierGroupe = new EntrainerTroupeRequest(idCanards, TypeTroupe.ARCHER, 4);
			EntrainerTroupeRequest secondGroupe = new EntrainerTroupeRequest(idTeuteus, TypeTroupe.CAVALIER, 3);
			EntrainerTroupeRequest troisiemeGroupe = new EntrainerTroupeRequest(idCanards, TypeTroupe.CAVALIER, 2);
			EntrainerTroupeRequest quatriemeGroupe = new EntrainerTroupeRequest(idCanards, TypeTroupe.LANCIER, 4);
			EntrainerTroupeRequest cinquiemeGroupe = new EntrainerTroupeRequest(idPrincesses, TypeTroupe.LANCIER, 3);

			SessionEntrainementResponse session = sessionEntrainementService.entrainer(premierGroupe);
			sessionEntrainementService.entrainer(secondGroupe);
			SessionEntrainementResponse sessionter = sessionEntrainementService.entrainer(troisiemeGroupe);
			SessionEntrainementResponse sessionquatre = sessionEntrainementService.entrainer(quatriemeGroupe);
			SessionEntrainementResponse sessioncinq = sessionEntrainementService.entrainer(cinquiemeGroupe);

			for (SessionEntrainementResponse s : List.of(session, sessionter, sessionquatre, sessioncinq)) {
				SessionEntrainement sessionEntity = sessionEntrainementDAO.findById(s.id()).orElseThrow();
				Royaume royaumeEntrainement = royaumeDAO.findById(s.royaumeId()).orElseThrow();
				sessionEntity.finaliserFormation(royaumeEntrainement);
				royaumeDAO.saveAndFlush(royaumeEntrainement);
				sessionEntrainementDAO.save(sessionEntity);
			}

			////////////////// BAGARRE ////////////////////

			Map<TypeTroupe, Integer> troupesCavaliers = Map.of(TypeTroupe.CAVALIER, 2);
			Map<TypeTroupe, Integer> troupesLanciers = Map.of(TypeTroupe.LANCIER, 3);

			AttaqueResponse attaque1 = attaqueService.lancer(new LancerAttaqueRequest(idCanards, idGobelins, troupesCavaliers));
			AttaqueResponse attaque2 = attaqueService.lancer(new LancerAttaqueRequest(idPrincesses, idTeuteus, troupesLanciers));
			attaqueService.lancer(new LancerAttaqueRequest(idCanards, idTeuteus, troupesLanciers));

			AttaqueResponse apresCombat1 = attaqueService.resoudreCombat(attaque1.id());
			attaqueService.resoudreCombat(attaque2.id());

			if (apresCombat1.statut() == StatutAttaque.RETOUR) {
				AttaqueResponse apresRetour = attaqueService.receptionnerRetour(attaque1.id());
				System.out.println("Après retour -> statut=" + apresRetour.statut());
			} else {
				System.out.println("Attaque terminée sans retour (statut=" + apresCombat1.statut() + ")");
			}

			////////////////// UTILISATEURS DEMO ////////////////////

			Utilisateur kat = new Utilisateur(
					null,
					"Kat",
					"kitkat@test.fr",
					"motdepasse123",
					false,
					true,
					rCanards
			);
			if (utilisateurDAO.findByEmail("kitkat@test.fr").isEmpty()) {
				utilisateurDAO.save(kat);
			}

			Utilisateur kit = new Utilisateur(
					null,
					"Kit",
					"kit@test.fr",
					"motdepasse123",
					false,
					false,
					rTeuteus
			);
			if (utilisateurDAO.findByEmail("kit@test.fr").isEmpty()) {
				utilisateurDAO.save(kit);
			}

			Utilisateur demo = new Utilisateur(
					null,
					"Demo",
					"demo@test.fr",
					"motdepasse123",
					false,
					false,
					null
			);
			if (utilisateurDAO.findByEmail("demo@test.fr").isEmpty()) {
				utilisateurDAO.save(demo);
			}
		};
	}
}
