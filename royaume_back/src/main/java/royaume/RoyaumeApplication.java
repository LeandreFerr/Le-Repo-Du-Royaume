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

import royaume.dao.IDAOPopulation;
import royaume.dao.IDAORoyaume;
import royaume.dao.IDAOUtilisateur;
import royaume.model.Royaume;
import royaume.model.Utilisateur;
import royaume.model.peuple.Canards;
import royaume.model.peuple.Gobelins;
import royaume.model.peuple.Licornes;
import royaume.model.peuple.Population;
import royaume.model.peuple.Princesses;
import royaume.model.peuple.Teuteus;

@SpringBootApplication
public class RoyaumeApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(RoyaumeApplication.class, args);
	}

		    @Bean
		    CommandLineRunner testJPA(
		            IDAOPopulation populationDAO,
		            IDAORoyaume royaumeDAO,
		            IDAOUtilisateur utilisateurDAO) {

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

		            	    Royaume royaume = new Royaume(
		            	        null,
		            	        "Le Royaume des " + population.getNom(),
		            	        population
		            	    );

		            	    royaumeDAO.save(royaume);

		            	    System.out.println("Population : " + population);
		            	    System.out.println("Royaume : " + royaume);
		            	}

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

		                utilisateur = utilisateurDAO.save(utilisateur);

		                System.out.println("Utilisateur créé : " + utilisateur);

		    
		};
}
}