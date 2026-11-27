package royaume.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.test.context.ActiveProfiles;

import royaume.dao.IDAOPopulation;
import royaume.dao.PalierNiveauIDAO;
import royaume.service.ChantierScheduler;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ActiveProfiles("host")
class MultiModeHostConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IDAOPopulation daoPopulation;

    @Autowired
    private PalierNiveauIDAO palierNiveauDAO;

    @Test
    void hostProfile_devraitActiverSchedulersEtInitialiseurs() {
        assertFalse(applicationContext.getBeansOfType(SchedulingConfig.class).isEmpty());
        assertFalse(applicationContext.getBeansOfType(ChantierScheduler.class).isEmpty());
        assertFalse(applicationContext.getBeansOfType(InitialisationPaliers.class).isEmpty());
        assertFalse(applicationContext.getBeansOfType(PopulationDataInitializer.class).isEmpty());
        assertEquals(5, daoPopulation.count());
        assertEquals(9, palierNiveauDAO.count());
    }

    @Test
    void hostEtGuestProperties_devraientContenirLesReglagesAttendus() throws IOException {
        Properties hostProps = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-host.properties"));
        assertEquals("true", hostProps.getProperty("royaume.scheduling.enabled"));
        assertEquals("true", hostProps.getProperty("royaume.init.enabled"));
        assertEquals("${HOST_DDL_AUTO:update}", hostProps.getProperty("spring.jpa.hibernate.ddl-auto"));

        Properties guestProps = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-guest.properties"));
        assertEquals("false", guestProps.getProperty("royaume.scheduling.enabled"));
        assertEquals("false", guestProps.getProperty("royaume.init.enabled"));
        assertEquals("validate", guestProps.getProperty("spring.jpa.hibernate.ddl-auto"));
    }
}
