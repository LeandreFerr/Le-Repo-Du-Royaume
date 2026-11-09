package royaume.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CarteTest {
    private Carte carte;

    @BeforeEach
    void setUp() {
        carte = new Carte();
    }

    /**
     * Crée un royaume rattaché à un emplacement placé aux coordonnées données.
     */
    private Royaume royaumeEn(int x, int y) {
        Royaume royaume = new Royaume();
        new EmplacementCarte(1, x, y, carte).assignerRoyaume(royaume);
        return royaume;
    }

    // =====================================================================
    // Constructeurs & valeurs par défaut
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

    @Test
    void constructeurParDefautDoitLaisserLaPartieNulle() {
        assertNull(carte.getPartie());
    }

    @Test
    void constructeurCompletDoitInitialiserTousLesAttributs() {
        EmplacementCarte e1 = new EmplacementCarte(1, 10, 10, carte);
        EmplacementCarte e2 = new EmplacementCarte(2, 20, 10, carte);
        List<EmplacementCarte> liste = new ArrayList<>(List.of(e1, e2));

        Carte c = new Carte(200, 120, 100, 60, null, liste);

        assertAll(
                () -> assertEquals(200, c.getLargeur()),
                () -> assertEquals(120, c.getHauteur()),
                () -> assertEquals(100, c.getCentreX()),
                () -> assertEquals(60, c.getCentreY()),
                () -> assertNull(c.getPartie()),
                () -> assertEquals(2, c.getEmplacements().size()),
                () -> assertSame(e1, c.getEmplacements().get(0)),
                () -> assertSame(e2, c.getEmplacements().get(1)));
    }

    @Test
    void constructeurCompletAvecEmplacementsNullDoitCreerUneListeVide() {
        Carte c = new Carte(200, 120, 100, 60, null, null);

        assertNotNull(c.getEmplacements());
        assertTrue(c.getEmplacements().isEmpty());
    }

    // =====================================================================
    // Getters & Setters
    // =====================================================================

    @Test
    void lesSettersDoiventModifierLesDimensionsEtLeCentre() {
        carte.setLargeur(200);
        carte.setHauteur(80);
        carte.setCentreX(10);
        carte.setCentreY(20);

        assertAll(
                () -> assertEquals(200, carte.getLargeur()),
                () -> assertEquals(80, carte.getHauteur()),
                () -> assertEquals(10, carte.getCentreX()),
                () -> assertEquals(20, carte.getCentreY()));
    }

    @Test
    void setPartieDoitMemoriserLaPartie() {
        // À adapter si Partie n'a pas de constructeur sans argument
        Partie partie = new Partie();

        carte.setPartie(partie);
        assertSame(partie, carte.getPartie());

        carte.setPartie(null);
        assertNull(carte.getPartie());
    }

    @Test
    void setEmplacementsDoitRemplacerLesEmplacementsExistants() {
        carte.genererEmplacements(3);
        EmplacementCarte nouveau = new EmplacementCarte(1, 5, 5, carte);

        carte.setEmplacements(new ArrayList<>(List.of(nouveau)));

        assertEquals(1, carte.getEmplacements().size());
        assertSame(nouveau, carte.getEmplacements().get(0));
    }

    @Test
    void setEmplacementsNullDoitRemplacerParUneListeVide() {
        carte.genererEmplacements(3);

        carte.setEmplacements(null);

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

    @ParameterizedTest
    @ValueSource(ints = { Integer.MIN_VALUE, -1, 0, 11, 100, Integer.MAX_VALUE })
    void genererEmplacementsHorsBornesDoitLeverUneExceptionMentionnantLeNombre(int nombre) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carte.genererEmplacements(nombre));

        assertTrue(exception.getMessage().contains(String.valueOf(nombre)));
    }

    @Test
    void genererEmplacementsHorsBornesNeDoitPasModifierLesEmplacementsExistants() {
        carte.genererEmplacements(3);
        List<EmplacementCarte> avant = new ArrayList<>(carte.getEmplacements());

        assertThrows(
                IllegalArgumentException.class,
                () -> carte.genererEmplacements(11));

        assertEquals(3, carte.getEmplacements().size());
        assertEquals(avant, carte.getEmplacements());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void genererEmplacementsValidesDoitProduireDesEmplacementsCoherents(int nombre) {
        carte.genererEmplacements(nombre);

        List<EmplacementCarte> liste = carte.getEmplacements();
        assertEquals(nombre, liste.size());

        int xPrecedent = 0;
        for (int i = 0; i < nombre; i++) {
            EmplacementCarte e = liste.get(i);

            assertEquals(i + 1, e.getNumero());
            assertEquals(carte.getHauteur() / 2, e.getCoordY());
            assertTrue(e.getCoordX() > xPrecedent, "Les abscisses doivent être strictement croissantes");
            assertTrue(e.getCoordX() < carte.getLargeur(), "L'abscisse doit rester dans la carte");
            assertFalse(e.isOccupe());
            assertNull(e.getRoyaume());
            assertSame(carte, e.getCarte());

            xPrecedent = e.getCoordX();
        }
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
    void genererUnSeulEmplacementDoitLePlacerAuCentre() {
        carte.genererEmplacements(1);

        EmplacementCarte unique = carte.getEmplacements().get(0);

        assertEquals(50, unique.getCoordX());
        assertEquals(50, unique.getCoordY());
    }

    @Test
    void genererDeuxEmplacementsDoitAppliquerLaDivisionEntiere() {
        carte.genererEmplacements(2);

        // 100 / (2 + 1) = 33 (division entière)
        assertEquals(33, carte.getEmplacements().get(0).getCoordX());
        assertEquals(66, carte.getEmplacements().get(1).getCoordX());
    }

    @Test
    void genererDixEmplacementsDoitCalculerLesCoordonneesExtremes() {
        carte.genererEmplacements(10);

        // 100 / (10 + 1) = 9 (division entière)
        assertEquals(9, carte.getEmplacements().get(0).getCoordX());
        assertEquals(90, carte.getEmplacements().get(9).getCoordX());
    }

    @Test
    void genererEmplacementsDoitTenirCompteDesDimensionsDeLaCarte() {
        carte.setLargeur(200);
        carte.setHauteur(80);

        carte.genererEmplacements(3);

        assertEquals(50, carte.getEmplacements().get(0).getCoordX());
        assertEquals(100, carte.getEmplacements().get(1).getCoordX());
        assertEquals(150, carte.getEmplacements().get(2).getCoordX());

        for (EmplacementCarte e : carte.getEmplacements()) {
            assertEquals(40, e.getCoordY());
        }
    }

    @Test
    void genererEmplacementsDoitTronquerLesCoordonneesNonEntieres() {
        carte.setLargeur(10);
        carte.setHauteur(11);

        carte.genererEmplacements(3);

        // 10 / 4 = 2 ; 11 / 2 = 5
        assertEquals(2, carte.getEmplacements().get(0).getCoordX());
        assertEquals(4, carte.getEmplacements().get(1).getCoordX());
        assertEquals(6, carte.getEmplacements().get(2).getCoordX());
        assertEquals(5, carte.getEmplacements().get(0).getCoordY());
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

    @Test
    void regenererMoinsDEmplacementsDoitReduireLaListe() {
        carte.genererEmplacements(5);

        carte.genererEmplacements(2);

        assertEquals(2, carte.getEmplacements().size());
        assertEquals(1, carte.getEmplacements().get(0).getNumero());
        assertEquals(2, carte.getEmplacements().get(1).getNumero());
    }

    @Test
    void regenererLesEmplacementsDoitCreerDeNouvellesInstances() {
        carte.genererEmplacements(3);
        List<EmplacementCarte> anciens = new ArrayList<>(carte.getEmplacements());

        carte.genererEmplacements(3);

        for (EmplacementCarte nouveau : carte.getEmplacements()) {
            assertFalse(anciens.contains(nouveau));
        }
    }

    @Test
    void regenererLesEmplacementsDoitRendreTousLesEmplacementsLibres() {
        carte.genererEmplacements(3);
        carte.placerRoyaume(new Royaume());
        carte.placerRoyaume(new Royaume());

        carte.genererEmplacements(3);

        assertTrue(
                carte.getEmplacements()
                        .stream()
                        .noneMatch(EmplacementCarte::isOccupe));
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

    @Test
    void getPremierEmplacementLibreDoitRetournerLePremierLibreMemeSiLesSuivantsSontOccupes() {
        carte.genererEmplacements(3);

        carte.getEmplacements().get(1).assignerRoyaume(new Royaume());
        carte.getEmplacements().get(2).assignerRoyaume(new Royaume());

        Optional<EmplacementCarte> resultat = carte.getPremierEmplacementLibre();

        assertTrue(resultat.isPresent());
        assertEquals(1, resultat.get().getNumero());
    }

    @Test
    void getPremierEmplacementLibreDoitTrouverUnEmplacementApresUneLiberation() {
        carte.genererEmplacements(2);
        carte.getEmplacements().get(0).assignerRoyaume(new Royaume());
        carte.getEmplacements().get(1).assignerRoyaume(new Royaume());
        assertTrue(carte.getPremierEmplacementLibre().isEmpty());

        carte.getEmplacements().get(1).liberer();

        Optional<EmplacementCarte> resultat = carte.getPremierEmplacementLibre();
        assertTrue(resultat.isPresent());
        assertEquals(2, resultat.get().getNumero());
    }

    @Test
    void getPremierEmplacementLibreDoitRespecterLOrdreDeLaListe() {
        EmplacementCarte e1 = new EmplacementCarte(1, 10, 10, carte);
        EmplacementCarte e2 = new EmplacementCarte(2, 20, 10, carte);
        carte.setEmplacements(new ArrayList<>(List.of(e2, e1)));

        Optional<EmplacementCarte> resultat = carte.getPremierEmplacementLibre();

        assertTrue(resultat.isPresent());
        assertSame(e2, resultat.get());
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
    void placerRoyaumeDoitRetournerUnEmplacementDeLaCarte() {
        carte.genererEmplacements(3);

        EmplacementCarte resultat = carte.placerRoyaume(new Royaume());

        assertSame(carte.getEmplacements().get(0), resultat);
        assertSame(carte, resultat.getCarte());
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
    void placerRoyaumeDoitPermettreDeLireLesCoordonneesDuRoyaume() {
        carte.genererEmplacements(3);

        Royaume royaume = new Royaume();
        carte.placerRoyaume(royaume);

        assertEquals(25, royaume.getX());
        assertEquals(50, royaume.getY());
    }

    @Test
    void placerAutantDeRoyaumesQueDEmplacementsDoitRemplirLaCarte() {
        carte.genererEmplacements(5);

        for (int i = 1; i <= 5; i++) {
            EmplacementCarte emplacement = carte.placerRoyaume(new Royaume());
            assertEquals(i, emplacement.getNumero());
        }

        assertTrue(carte.getEmplacements().stream().allMatch(EmplacementCarte::isOccupe));
        assertTrue(carte.getPremierEmplacementLibre().isEmpty());
        assertThrows(
                IllegalStateException.class,
                () -> carte.placerRoyaume(new Royaume()));
    }

    @Test
    void placerRoyaumeDoitReutiliserUnEmplacementLibere() {
        carte.genererEmplacements(3);
        Royaume a = new Royaume();
        Royaume b = new Royaume();
        Royaume c = new Royaume();

        carte.placerRoyaume(a);
        carte.placerRoyaume(b);
        carte.getEmplacements().get(0).liberer();

        EmplacementCarte resultat = carte.placerRoyaume(c);

        assertEquals(1, resultat.getNumero());
        assertSame(c, resultat.getRoyaume());
        assertNull(a.getEmplacement());
        assertEquals(2, b.getEmplacement().getNumero());
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
    void placerRoyaumeSurCartePleineNeDoitPasRattacherLeRoyaume() {
        carte.genererEmplacements(1);
        carte.placerRoyaume(new Royaume());
        Royaume refuse = new Royaume();

        assertThrows(
                IllegalStateException.class,
                () -> carte.placerRoyaume(refuse));

        assertNull(refuse.getEmplacement());
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

    @Test
    void placerUnRoyaumeNullDoitAvoirUnMessageExplicite() {
        carte.genererEmplacements(3);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carte.placerRoyaume(null));

        assertEquals(
                "Le royaume à placer ne peut pas être null.",
                exception.getMessage());
    }

    @Test
    void placerUnRoyaumeNullNeDoitOccuperAucunEmplacement() {
        carte.genererEmplacements(3);

        assertThrows(
                IllegalArgumentException.class,
                () -> carte.placerRoyaume(null));

        assertTrue(carte.getEmplacements().stream().noneMatch(EmplacementCarte::isOccupe));
    }

    @Test
    void placerUnRoyaumeNullSansEmplacementDoitLeverIllegalArgumentException() {
        // La validation de l'argument doit passer avant la recherche d'un emplacement
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
    void calculerDistanceEntreDoitEchouerSiLePremierRoyaumeNaPasDEmplacement() {
        Royaume sansEmplacement = new Royaume();
        Royaume place = royaumeEn(10, 20);

        assertThrows(
                IllegalStateException.class,
                () -> carte.calculerDistanceEntre(sansEmplacement, place));
    }

    @Test
    void calculerDistanceEntreDoitEchouerSiAucunRoyaumeNaDEmplacement() {
        Royaume royaume1 = new Royaume();
        Royaume royaume2 = new Royaume();

        assertThrows(
                IllegalStateException.class,
                () -> carte.calculerDistanceEntre(royaume1, royaume2));
    }

    @Test
    void calculerDistanceEntreAvecUnRoyaumeNullDoitLeverUneException() {
        Royaume royaume = new Royaume();

        assertThrows(
                IllegalArgumentException.class,
                () -> carte.calculerDistanceEntre(royaume, null));
    }

    @Test
    void calculerDistanceEntreAvecLePremierRoyaumeNullDoitLeverUneException() {
        Royaume royaume = royaumeEn(0, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> carte.calculerDistanceEntre(null, royaume));
    }

    @Test
    void calculerDistanceEntreAvecDeuxRoyaumesNullDoitLeverUneException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carte.calculerDistanceEntre(null, null));

        assertEquals(
                "Les deux royaumes doivent être renseignés.",
                exception.getMessage());
    }

    @Test
    void calculerDistanceEntreDeuxRoyaumesAuMemePointDoitEtreNulle() {
        Royaume royaume1 = royaumeEn(5, 5);
        Royaume royaume2 = royaumeEn(5, 5);

        assertEquals(0.0, carte.calculerDistanceEntre(royaume1, royaume2), 0.0001);
    }

    @Test
    void calculerDistanceEntreUnRoyaumeEtLuiMemeDoitEtreNulle() {
        Royaume royaume = royaumeEn(12, 34);

        assertEquals(0.0, carte.calculerDistanceEntre(royaume, royaume), 0.0001);
    }

    @Test
    void calculerDistanceEntreDeuxRoyaumesAlignesHorizontalementDoitEtreLaDifferenceDesAbscisses() {
        Royaume royaume1 = royaumeEn(0, 0);
        Royaume royaume2 = royaumeEn(10, 0);

        assertEquals(10.0, carte.calculerDistanceEntre(royaume1, royaume2), 0.0001);
    }

    @Test
    void calculerDistanceEntreDeuxRoyaumesAlignesVerticalementDoitEtreLaDifferenceDesOrdonnees() {
        Royaume royaume1 = royaumeEn(0, 0);
        Royaume royaume2 = royaumeEn(0, 7);

        assertEquals(7.0, carte.calculerDistanceEntre(royaume1, royaume2), 0.0001);
    }

    @Test
    void calculerDistanceEntreDoitGererLesCoordonneesNegatives() {
        Royaume royaume1 = royaumeEn(-3, -4);
        Royaume royaume2 = royaumeEn(0, 0);

        assertEquals(5.0, carte.calculerDistanceEntre(royaume1, royaume2), 0.0001);
    }

    @Test
    void calculerDistanceEntreDoitRetournerUneDistanceNonEntiere() {
        Royaume royaume1 = royaumeEn(0, 0);
        Royaume royaume2 = royaumeEn(1, 1);

        assertEquals(Math.sqrt(2), carte.calculerDistanceEntre(royaume1, royaume2), 0.0001);
    }

    @Test
    void calculerDistanceEntreDoitEtreSymetrique() {
        Royaume royaume1 = royaumeEn(2, 9);
        Royaume royaume2 = royaumeEn(14, -3);

        assertEquals(
                carte.calculerDistanceEntre(royaume1, royaume2),
                carte.calculerDistanceEntre(royaume2, royaume1),
                0.0);
    }

    @Test
    void calculerDistanceEntreDoitSuivreLeDeplacementDUnEmplacement() {
        Royaume royaume1 = royaumeEn(0, 0);
        Royaume royaume2 = royaumeEn(3, 4);
        assertEquals(5.0, carte.calculerDistanceEntre(royaume1, royaume2), 0.0001);

        royaume2.getEmplacement().setCoordX(6);
        royaume2.getEmplacement().setCoordY(8);

        assertEquals(10.0, carte.calculerDistanceEntre(royaume1, royaume2), 0.0001);
    }

    @Test
    void calculerDistanceEntreDeuxRoyaumesPlacesSurLaCarteGeneree() {
        carte.genererEmplacements(3);
        Royaume royaume1 = new Royaume();
        Royaume royaume2 = new Royaume();
        carte.placerRoyaume(royaume1); // (25, 50)
        carte.placerRoyaume(royaume2); // (50, 50)

        assertEquals(25.0, carte.calculerDistanceEntre(royaume1, royaume2), 0.0001);
    }

    // =====================================================================
    // Représentation
    // =====================================================================

    @Test
    void toStringDoitContenirLesInformationsDeLaCarte() {
        String texte = carte.toString();

        assertTrue(texte.contains("largeur=100"));
        assertTrue(texte.contains("hauteur=100"));
        assertTrue(texte.contains("centreX=50"));
        assertTrue(texte.contains("centreY=50"));
        assertTrue(texte.contains("partie=null"));
        assertTrue(texte.contains("emplacements=[]"));
    }

    @Test
    void toStringNeDoitPasBoucleSurUneCarteRemplie() {
        carte.genererEmplacements(3);
        carte.placerRoyaume(new Royaume());

        assertDoesNotThrow(() -> carte.toString());
    }

    @Test
    void genererEmplacementsDoitFonctionnerAvecUneListeImmuableFournieAuConstructeur() {
        Carte c = new Carte(100, 100, 50, 50, null, List.of());

        assertDoesNotThrow(() -> c.genererEmplacements(3));
        assertEquals(3, c.getEmplacements().size());
    }

    @Test
    void regenererLesEmplacementsDoitLibererLesRoyaumesDejaPlaces() {
        carte.genererEmplacements(3);
        Royaume royaume = new Royaume();
        carte.placerRoyaume(royaume);

        carte.genererEmplacements(3);

        assertNull(royaume.getEmplacement());
    }

    @Test
    void genererEmplacementsSurUneCarteTropEtroiteDoitLeverUneException() {
        carte.genererEmplacements(3);
        List<EmplacementCarte> avant = new ArrayList<>(carte.getEmplacements());
        carte.setLargeur(5);

        assertThrows(IllegalStateException.class, () -> carte.genererEmplacements(10));

        assertEquals(avant, carte.getEmplacements()); // rien n'a été modifié
    }

    @Test
    void setLargeurOuHauteurNonPositiveDoitLeverUneException() {
        assertThrows(IllegalArgumentException.class, () -> carte.setLargeur(0));
        assertThrows(IllegalArgumentException.class, () -> carte.setLargeur(-5));
        assertThrows(IllegalArgumentException.class, () -> carte.setHauteur(0));
        assertThrows(IllegalArgumentException.class, () -> carte.setHauteur(-5));
    }
}