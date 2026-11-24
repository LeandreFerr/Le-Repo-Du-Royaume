package royaume.model.ticker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Tests unitaires — HorlogeJeu (Ticker)")
class HorlogeJeuTest {

    private static final LocalDateTime DATE_DEPART = LocalDateTime.of(2026, 11, 23, 12, 0, 0);
    private HorlogeJeu horloge;

    @BeforeEach
    void setUp() {
        horloge = new HorlogeJeu(DATE_DEPART);
    }

    @Nested
    @DisplayName("Initialisation et constructeurs")
    class InitialisationTests {

        @Test
        @DisplayName("Le constructeur par défaut initialise un temps courant non null et une vitesse à 1.0")
        void testConstructeurParDefaut() {
            HorlogeJeu defaut = new HorlogeJeu();

            assertNotNull(defaut.getTempsCourant());
            assertEquals(1.0, defaut.getVitesseSimulation());
        }

        @Test
        @DisplayName("initialiser() positionne exactement le temps courant")
        void testInitialiserNominal() {
            LocalDateTime nouvelleDate = LocalDateTime.of(2027, 1, 1, 8, 30);

            horloge.initialiser(nouvelleDate);

            assertEquals(nouvelleDate, horloge.getTempsCourant());
        }

        @Test
        @DisplayName("initialiser(null) lève IllegalArgumentException")
        void testInitialiserNullRejete() {
            assertThrows(IllegalArgumentException.class, () -> horloge.initialiser(null));
            assertThrows(IllegalArgumentException.class, () -> new HorlogeJeu(null));
        }

        @Test
        @DisplayName("Les setters rejettent tempsCourant null et vitesseSimulation null ou <= 0")
        void testSettersRejettentValeursInvalides() {
            assertThrows(IllegalArgumentException.class, () -> horloge.setTempsCourant(null));
            assertThrows(IllegalArgumentException.class, () -> horloge.setVitesseSimulation(null));
            assertThrows(IllegalArgumentException.class, () -> horloge.setVitesseSimulation(0.0));
            assertThrows(IllegalArgumentException.class, () -> horloge.setVitesseSimulation(-1.5));
        }
    }

    @Nested
    @DisplayName("Avance du temps")
    class AvancerTempsTests {

        @Test
        @DisplayName("avancerTemps() incrémente fidèlement le tempsCourant (simple et cumulé)")
        void testAvancerTempsNominalEtCumule() {
            horloge.avancerTemps(Duration.ofMinutes(30));
            assertEquals(DATE_DEPART.plusMinutes(30), horloge.getTempsCourant());

            horloge.avancerTemps(Duration.ofHours(2).plusSeconds(15));
            assertEquals(DATE_DEPART.plusHours(2).plusMinutes(30).plusSeconds(15), horloge.getTempsCourant());
        }

        @Test
        @DisplayName("avancerTemps(Duration.ZERO) ne modifie pas le tempsCourant")
        void testAvancerTempsZero() {
            horloge.avancerTemps(Duration.ZERO);

            assertEquals(DATE_DEPART, horloge.getTempsCourant());
        }

        @Test
        @DisplayName("avancerMinutes() et avancerHeures() avancent exactement de la durée demandée")
        void testAvancerMinutesEtHeures() {
            horloge.avancerMinutes(45);
            assertEquals(DATE_DEPART.plusMinutes(45), horloge.getTempsCourant());

            horloge.avancerHeures(3);
            assertEquals(DATE_DEPART.plusHours(3).plusMinutes(45), horloge.getTempsCourant());
        }

        @Test
        @DisplayName("Les durées null ou négatives lèvent IllegalArgumentException sans altérer tempsCourant")
        void testDureesInvalidesRejetees() {
            assertThrows(IllegalArgumentException.class, () -> horloge.avancerTemps(null));
            assertThrows(IllegalArgumentException.class, () -> horloge.avancerTemps(Duration.ofMinutes(-1)));
            assertThrows(IllegalArgumentException.class, () -> horloge.avancerMinutes(-5));
            assertThrows(IllegalArgumentException.class, () -> horloge.avancerHeures(-1));

            assertEquals(DATE_DEPART, horloge.getTempsCourant());
        }
    }
}
