package royaume.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CarteTest {
    private Carte carte;

    @BeforeEach
    void setUp() {
        carte = new Carte();
    }

    // =====================================================================
    // Valeurs par défaut
    // =====================================================================

    @Test
    void constructeurParDefautDoitInitialiserLaCarte() {
        assertEquals(100, carte.getLargeur());
        assertEquals(100, carte.getHauteur());
        assertEquals(50, carte.getCentreX());
        assertEquals(50, carte.getCentreY());
        assertNotNull(carte.getEmplacements());
        assertTrue(carte.getEmplacements().isEmpty());
    }

    // =====================================================================
    // Génération des emplacements
    // =====================================================================

    @Test
    void genererEmplacementsDoitCreerLeNombreDemande() {
        carte.genererEmplacements(3);

        assertEquals(3, carte.getEmplacements().size());
    }

    @Test
    void genererUnEmplacementDoitFonctionner() {
        carte.genererEmplacements(1);

        assertEquals(1, carte.getEmplacements().size());
    }

    @Test
    void genererDixEmplacementsDoitFonctionner() {
        carte.genererEmplacements(10);

        assertEquals(10, carte.getEmplacements().size());
    }

    @Test
    void genererZeroEmplacementDoitLeverUneException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> carte.genererEmplacements(0));
    }

    @Test
    void genererUnNombreNegatifDoitLeverUneException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> carte.genererEmplacements(-1));
    }

    @Test
    void genererOnzeEmplacementsDoitLeverUneException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> carte.genererEmplacements(11));
    }

    @Test
    void genererEmplacementsDoitNumeroterLesEmplacementsAPartirDeUn() {
        carte.genererEmplacements(3);

        assertEquals(1, carte.getEmplacements().get(0).getNumero());
        assertEquals(2, carte.getEmplacements().get(1).getNumero());
        assertEquals(3, carte.getEmplacements().get(2).getNumero());
    }

    @Test
    void genererTroisEmplacementsDoitCalculerLesCoordonneesAttendues() {
        carte.genererEmplacements(3);

        EmplacementCarte premier = carte.getEmplacements().get(0);
        EmplacementCarte deuxieme = carte.getEmplacements().get(1);
        EmplacementCarte troisieme = carte.getEmplacements().get(2);

        assertEquals(25, premier.getCoordX());
        assertEquals(50, premier.getCoordY());

        assertEquals(50, deuxieme.getCoordX());
        assertEquals(50, deuxieme.getCoordY());

        assertEquals(75, troisieme.getCoordX());
        assertEquals(50, troisieme.getCoordY());
    }

    @Test
    void lesEmplacementsGeneresDoiventEtreLibres() {
        carte.genererEmplacements(5);

        assertTrue(
                carte.getEmplacements()
                        .stream()
                        .noneMatch(EmplacementCarte::isOccupe));
    }

    @Test
    void lesEmplacementsGeneresDoiventEtreRattachesALaCarte() {
        carte.genererEmplacements(3);

        for (EmplacementCarte emplacement : carte.getEmplacements()) {
            assertSame(carte, emplacement.getCarte());
        }
    }

    @Test
    void regenererLesEmplacementsDoitRemplacerLesAnciens() {
        carte.genererEmplacements(3);

        carte.genererEmplacements(5);

        assertEquals(5, carte.getEmplacements().size());
    }

    // =====================================================================
    // Recherche d’un emplacement libre
    // =====================================================================

    @Test
    void getPremierEmplacementLibreDoitRetournerLePremier() {
        carte.genererEmplacements(3);

        Optional<EmplacementCarte> resultat = carte.getPremierEmplacementLibre();

        assertTrue(resultat.isPresent());
        assertEquals(1, resultat.get().getNumero());
    }

    @Test
    void getPremierEmplacementLibreDoitIgnorerLesEmplacementsOccupes() {
        carte.genererEmplacements(3);

        Royaume royaume = new Royaume();

        carte.getEmplacements()
                .get(0)
                .assignerRoyaume(royaume);

        Optional<EmplacementCarte> resultat = carte.getPremierEmplacementLibre();

        assertTrue(resultat.isPresent());
        assertEquals(2, resultat.get().getNumero());
    }

    @Test
    void getPremierEmplacementLibreDoitRetournerOptionalEmptySiCartePleine() {
        carte.genererEmplacements(2);

        Royaume royaume1 = new Royaume();
        Royaume royaume2 = new Royaume();

        carte.getEmplacements().get(0).assignerRoyaume(royaume1);
        carte.getEmplacements().get(1).assignerRoyaume(royaume2);

        Optional<EmplacementCarte> resultat = carte.getPremierEmplacementLibre();

        assertTrue(resultat.isEmpty());
    }

    @Test
    void getPremierEmplacementLibreDoitRetournerOptionalEmptySansEmplacement() {
        Optional<EmplacementCarte> resultat = carte.getPremierEmplacementLibre();

        assertTrue(resultat.isEmpty());
    }

    // =====================================================================
    // Placement d’un royaume
    // =====================================================================

    @Test
    void placerRoyaumeDoitUtiliserLePremierEmplacementLibre() {
        carte.genererEmplacements(3);

        Royaume royaume = new Royaume();

        EmplacementCarte resultat = carte.placerRoyaume(royaume);

        assertEquals(1, resultat.getNumero());
        assertTrue(resultat.isOccupe());
        assertSame(royaume, resultat.getRoyaume());
    }

    @Test
    void placerDeuxRoyaumesDoitUtiliserDeuxEmplacementsDifferents() {
        carte.genererEmplacements(3);

        Royaume premierRoyaume = new Royaume();
        Royaume deuxiemeRoyaume = new Royaume();

        EmplacementCarte premierEmplacement = carte.placerRoyaume(premierRoyaume);

        EmplacementCarte deuxiemeEmplacement = carte.placerRoyaume(deuxiemeRoyaume);

        assertEquals(1, premierEmplacement.getNumero());
        assertEquals(2, deuxiemeEmplacement.getNumero());

        assertNotSame(
                premierEmplacement,
                deuxiemeEmplacement);
    }

    @Test
    void placerRoyaumeDoitRenseignerLEmplacementDuRoyaume() {
        carte.genererEmplacements(3);

        Royaume royaume = new Royaume();

        EmplacementCarte emplacement = carte.placerRoyaume(royaume);

        assertSame(emplacement, royaume.getEmplacement());
    }

    @Test
    void placerRoyaumeSurCartePleineDoitLeverUneException() {
        carte.genererEmplacements(1);

        carte.placerRoyaume(new Royaume());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> carte.placerRoyaume(new Royaume()));

        assertEquals(
                "Aucun emplacement libre sur la carte.",
                exception.getMessage());
    }

    @Test
    void placerRoyaumeSansEmplacementGenereDoitLeverUneException() {
        assertThrows(
                IllegalStateException.class,
                () -> carte.placerRoyaume(new Royaume()));
    }

    @Test
    void placerUnRoyaumeNullDoitLeverUneException() {
        carte.genererEmplacements(3);

        assertThrows(
                IllegalArgumentException.class,
                () -> carte.placerRoyaume(null));
    }

    // =====================================================================
    // Distance entre royaumes
    // =====================================================================

    @Test
    void calculerDistanceEntreDoitUtiliserLesEmplacementsDesRoyaumes() {
        EmplacementCarte emplacement1 = new EmplacementCarte(1, 0, 0, carte);

        EmplacementCarte emplacement2 = new EmplacementCarte(2, 3, 4, carte);

        Royaume royaume1 = new Royaume();
        Royaume royaume2 = new Royaume();

        emplacement1.assignerRoyaume(royaume1);
        emplacement2.assignerRoyaume(royaume2);

        double distance = carte.calculerDistanceEntre(royaume1, royaume2);

        assertEquals(5.0, distance, 0.0001);
    }

    @Test
    void calculerDistanceEntreDoitEchouerSiUnRoyaumeNaPasDEmplacement() {
        Royaume royaume1 = new Royaume();
        Royaume royaume2 = new Royaume();

        EmplacementCarte emplacement = new EmplacementCarte(1, 10, 20, carte);

        emplacement.assignerRoyaume(royaume1);

        assertThrows(
                IllegalStateException.class,
                () -> carte.calculerDistanceEntre(
                        royaume1,
                        royaume2));
    }

    @Test
    void calculerDistanceEntreAvecUnRoyaumeNullDoitLeverUneException() {
        Royaume royaume = new Royaume();

        assertThrows(
                IllegalArgumentException.class,
                () -> carte.calculerDistanceEntre(royaume, null));
    }
}
