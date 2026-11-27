package royaume.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Active la planification Spring uniquement lorsque le scheduling du moteur de jeu
 * est autorise (actif par defaut ou avec le profil {@code host}, desactive en profil {@code guest}).
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
    name = "royaume.scheduling.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class SchedulingConfig {
}
