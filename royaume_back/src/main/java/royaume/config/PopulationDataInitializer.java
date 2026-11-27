package royaume.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import royaume.dao.IDAOPopulation;
import royaume.model.peuple.Canards;
import royaume.model.peuple.Gobelins;
import royaume.model.peuple.Licornes;
import royaume.model.peuple.Population;
import royaume.model.peuple.Princesses;
import royaume.model.peuple.Teuteus;

@Configuration
@ConditionalOnProperty(
    name = "royaume.init.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class PopulationDataInitializer {

    @Bean
    @Order(1)
    CommandLineRunner initialiserPopulations(
            IDAOPopulation daoPopulation) {

        return args -> {

            if (daoPopulation.count() > 0) {
                return;
            }

            Population canards = new Canards();
            canards.setNom("Canards");
            canards.setTitreSouverain("Grand Canard");
            canards.setAttraction(1.0);

            Population gobelins = new Gobelins();
            gobelins.setNom("Gobelins");
            gobelins.setTitreSouverain("Chef gobelin");
            gobelins.setAttraction(1.0);

            Population licornes = new Licornes();
            licornes.setNom("Licornes");
            licornes.setTitreSouverain("Souverain des licornes");
            licornes.setAttraction(1.0);

            Population princesses = new Princesses();
            princesses.setNom("Princesses");
            princesses.setTitreSouverain("Princesse");
            princesses.setAttraction(1.0);

            Population teuteus = new Teuteus();
            teuteus.setNom("Teuteus");
            teuteus.setTitreSouverain("Chef des Teuteus");
            teuteus.setAttraction(1.0);

            daoPopulation.save(canards);
            daoPopulation.save(gobelins);
            daoPopulation.save(licornes);
            daoPopulation.save(princesses);
            daoPopulation.save(teuteus);
        };
    }
}
