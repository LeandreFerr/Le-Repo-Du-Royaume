package royaume.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import royaume.dao.IDAOPopulation;
import royaume.dao.PalierNiveauIDAO;
import royaume.service.ChantierScheduler;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ActiveProfiles("guest")
class MultiModeGuestConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IDAOPopulation daoPopulation;

    @Autowired
    private PalierNiveauIDAO palierNiveauDAO;

    @Test
    void guestProfile_devraitDesactiverSchedulersEtInitialiseurs() {
        assertTrue(applicationContext.getBeansOfType(SchedulingConfig.class).isEmpty());
        assertTrue(applicationContext.getBeansOfType(ChantierScheduler.class).isEmpty());
        assertTrue(applicationContext.getBeansOfType(InitialisationPaliers.class).isEmpty());
        assertTrue(applicationContext.getBeansOfType(PopulationDataInitializer.class).isEmpty());
        assertEquals(0, daoPopulation.count());
        assertEquals(0, palierNiveauDAO.count());
    }
}
