package royaume.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Affiche au demarrage une banniere recapitulative du mode d'execution
 * (Host, Invite ou Standard) sans exposer les identifiants de connexion.
 */
@Component
@Order(100)
public class MultiModeStartupLogger implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiModeStartupLogger.class);

    private final Environment environment;

    public MultiModeStartupLogger(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String[] activeProfiles = environment.getActiveProfiles();
        String role = determinerRole(activeProfiles);
        String profilsTexte = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "default";
        boolean schedulingActif = environment.getProperty("royaume.scheduling.enabled", Boolean.class, true);
        boolean initActif = environment.getProperty("royaume.init.enabled", Boolean.class, true);
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto", "non defini");
        String datasourceUrl = environment.getProperty("spring.datasource.url", "non definie");

        LOGGER.info("============================================================");
        LOGGER.info("ROYAUME BACKEND - MODE : {}", role);
        LOGGER.info("Profils actifs         : {}", profilsTexte);
        LOGGER.info("Scheduling (moteur)    : {}", schedulingActif ? "ACTIF" : "DESACTIVE");
        LOGGER.info("Init referentiel       : {}", initActif ? "ACTIF" : "DESACTIVE");
        LOGGER.info("Hibernate ddl-auto     : {}", ddlAuto);
        LOGGER.info("Datasource URL         : {}", datasourceUrl);
        LOGGER.info("============================================================");
    }

    private String determinerRole(String[] activeProfiles) {
        if (Arrays.asList(activeProfiles).contains("guest")) {
            return "INVITE (GUEST)";
        }
        if (Arrays.asList(activeProfiles).contains("host")) {
            return "HOST";
        }
        return "STANDARD";
    }
}
