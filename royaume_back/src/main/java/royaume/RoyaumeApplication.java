package royaume;

		import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import royaume.dao.IDAOPopulation;
import royaume.dao.IDAORoyaume;
import royaume.model.Canards;
import royaume.model.Royaume;

@SpringBootApplication
public class RoyaumeApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(RoyaumeApplication.class, args);
	}

		    @Bean
		    CommandLineRunner testJPA(
		            IDAOPopulation populationDAO,
		            IDAORoyaume royaumeDAO) {

		        return args -> {

		            System.out.println("========== TEST JPA ==========");

		            // 1. Création et enregistrement d'une population
		            Canards canards = new Canards(
		                    null,
		                    "Canards",
		                    "Roi Coin-Coin",
		                    1.5
		            );

		            canards = (Canards) populationDAO.save(canards);

		            System.out.println("Population enregistrée : " + canards);

		            // 2. Récupération des populations
		            System.out.println("\n--- Liste des populations ---");

		            populationDAO.findAll()
		                    .forEach(System.out::println);

		            // 3. Création et enregistrement d'un royaume
		            Royaume royaume = new Royaume(
		                    null,
		                    "Le Royaume des Canards",
		                    canards
		            );

		            royaume = royaumeDAO.save(royaume);

		            System.out.println("\nRoyaume enregistré : " + royaume);

		            // 4. Récupération des royaumes
		            System.out.println("\n--- Liste des royaumes ---");

		            royaumeDAO.findAll()
		                    .forEach(System.out::println);

		            System.out.println("\n========== FIN TEST JPA ==========");
		        };
		    
		}
}
