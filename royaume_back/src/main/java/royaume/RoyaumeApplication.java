package royaume;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling 
public class RoyaumeApplication {

    @PostConstruct
    public void initTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(RoyaumeApplication.class, args);
    }
}
/*
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

		    
		};
}
}*/