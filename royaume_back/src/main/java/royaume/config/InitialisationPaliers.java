package royaume.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import royaume.dao.PalierNiveauIDAO; import royaume.model.PalierNiveau;

@Component
@Order(2)
@ConditionalOnProperty(
    name = "royaume.init.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class InitialisationPaliers implements CommandLineRunner {

private final PalierNiveauIDAO repository;

public InitialisationPaliers(PalierNiveauIDAO repository) {
    this.repository = repository;
}

@Override
@Transactional
public void run(String... args) {

    if (repository.count() == 0) {

        List<PalierNiveau> paliers = ConfigurationPaliers.getPaliers()
            .stream()
            .map(p -> new PalierNiveau(
                null,
                p.getNiveau(),
                p.getPopulationRequise(),
                p.getPrerequis()
            ))
            .toList();

        repository.saveAll(paliers);

        System.out.println("Les 9 paliers ont été initialisés en base de données.");
    }
}


}