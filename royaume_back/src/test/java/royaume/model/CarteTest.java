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

    /**
     * Rayon du cercle sur lequel sont répartis les emplacements :
     * moitié de la plus petite dimension, moins une marge de 10.
     */
    private int rayonAttendu() {
        return Math.min(carte.getLargeur(), carte.getHauteur()) / 2 - 10;
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

    @Test
    void constructeurCompletDoitCopierLaListeFournie() {
        EmplacementCarte e1 = new EmplacementCarte(1, 10, 10, carte);
        List<EmplacementCarte> liste = new ArrayList<>(List.of(e1));

        Carte c = new Carte(200, 120, 100, 60, null, liste);
        liste.clear();

        assertEquals(1, c.getEmplacements().size());
    }

    @Test
    void emplacementMarcheDoitEtreAuCentreDeLaCarte() {
        EmplacementCarte marche = carte.getEmplacementMarche();

        assertAll(
                () -> assertNotNull(marche),
                () -> assertEquals(0, marche.getNumero()),
                () -> assertEquals(50, marche.getCoordX()),
                () -> assertEquals(50, marche.getCoordY()),
                () -> assertSame(carte, marche.getCarte()));
    }

    @Test
    void emplacementMarcheNeDoitPasFairePartieDesEmplacementsDesRoyaumes() {
        carte.genererEmplacements(10);

        assertFalse(
                carte.getEmplacements().contains(carte.getEmplacementMarche()));
    }

    @Test
    void emplacementMarcheDoitToujoursResterEnCinquanteCinquante() {
        carte.genererEmplacements(10, true);

        assertAll(
                () -> assertEquals(50, carte.getEmplacementMarche().getCoordX()),
                () -> assertEquals(50, carte.getEmplacementMarche().getCoordY()));
    }

    @Test
    void emplacementMarcheDoitEtreInitialementLibreEtSansRoyaume() {
        EmplacementCarte marche = carte.getEmplacementMarche();

        assertAll(
                () -> assertFalse(marche.isOccupe()),
                () -> assertNull(marche.getRoyaume()),
                () -> assertNull(marche.getId()));
    }

    @Test
    void emplacementMarcheDoitResterLaMemeInstanceApresLesGenerations() {
        EmplacementCarte marcheInitial = carte.getEmplacementMarche();

        carte.genererEmplacements(5);
        carte.genererEmplacements(10, true);
        carte.genererEmplacements(2, false);

        assertSame(marcheInitial, carte.getEmplacementMarche());
    }

    @Test
    void placerRoyaumeNeDoitJamaisUtiliserLEmplacementDuMarche() {
        carte.genererEmplacements(5, true);

        for (int i = 0; i < 5; i++) {
            EmplacementCarte resultat = carte.placerRoyaume(new Royaume());

            assertNotSame(carte.getEmplacementMarche(), resultat);
            assertTrue(carte.getEmplacements().contains(resultat));
        }

        assertFalse(carte.getEmplacementMarche().isOccupe());
        assertNull(carte.getEmplacementMarche().getRoyaume());
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
    void setLargeurOuHauteurNonPositiveDoitLeverUneException() {
        assertThrows(IllegalArgumentException.class, () -> carte.setLargeur(0));
        assertThrows(IllegalArgumentException.class, () -> carte.setLargeur(-5));
        assertThrows(IllegalArgumentException.class, () -> carte.setHauteur(0));
        assertThrows(IllegalArgumentException.class, () -> carte.setHauteur(-5));
    }

    @Test
    void setLargeurOuHauteurNonPositiveDoitAvoirUnMessageExplicite() {
        IllegalArgumentException largeur = assertThrows(
                IllegalArgumentException.class,
                () -> carte.setLargeur(0));
        IllegalArgumentException hauteur = assertThrows(
                IllegalArgumentException.class,
                () -> carte.setHauteur(0));

        assertEquals("La largeur de la carte doit être strictement positive.", largeur.getMessage());
        assertEquals("La hauteur de la carte doit être strictement positive.", hauteur.getMessage());
    }

    @Test
    void setLargeurOuHauteurInvalideNeDoitPasModifierLesDimensions() {
        assertThrows(IllegalArgumentException.class, () -> carte.setLargeur(-5));
        assertThrows(IllegalArgumentException.class, () -> carte.setHauteur(0));

        assertEquals(100, carte.getLargeur());
        assertEquals(100, carte.getHauteur());
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

    @Test
    void setEmplacementsDoitCopierLaListeFournie() {
        EmplacementCarte e1 = new EmplacementCarte(1, 10, 10, carte);
        List<EmplacementCarte> liste = new ArrayList<>(List.of(e1));

        carte.setEmplacements(liste);
        liste.clear();

        assertEquals(1, carte.getEmplacements().size());
    }

    // =====================================================================
    // Génération des emplacements : nombre et validation
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

    @Test
    void genererEmplacementsDoitNumeroterLesEmplacementsAPartirDeUn() {
        carte.genererEmplacements(3);

        assertEquals(1, carte.getEmplacements().get(0).getNumero());
        assertEquals(2, carte.getEmplacements().get(1).getNumero());
        assertEquals(3, carte.getEmplacements().get(2).getNumero());
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

    // =====================================================================
    // Génération des emplacements : disposition en cercle
    // =====================================================================

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void genererEmplacementsValidesDoitProduireDesEmplacementsCoherents(int nombre) {
        carte.genererEmplacements(nombre);

        List<EmplacementCarte> liste = carte.getEmplacements();
        assertEquals(nombre, liste.size());

        int rayon = rayonAttendu();
        for (int i = 0; i < nombre; i++) {
            EmplacementCarte e = liste.get(i);
            double distanceAuCentre = Math.hypot(
                    e.getCoordX() - carte.getCentreX(),
                    e.getCoordY() - carte.getCentreY());

            assertEquals(i + 1, e.getNumero());
            // Tolérance de 1 : les coordonnées sont arrondies à l'entier
            assertEquals(rayon, distanceAuCentre, 1.0, "L'emplacement doit être sur le cercle");
            assertTrue(e.getCoordX() >= 0 && e.getCoordX() <= carte.getLargeur(),
                    "L'abscisse doit rester dans la carte");
            assertTrue(e.getCoordY() >= 0 && e.getCoordY() <= carte.getHauteur(),
                    "L'ordonnée doit rester dans la carte");
            assertFalse(e.isOccupe());
            assertNull(e.getRoyaume());
            assertSame(carte, e.getCarte());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void lePremierEmplacementGenereDoitEtreEnHautDuCercle(int nombre) {
        carte.genererEmplacements(nombre);

        EmplacementCarte premier = carte.getEmplacements().get(0);

        assertEquals(carte.getCentreX(), premier.getCoordX());
        assertEquals(carte.getCentreY() - rayonAttendu(), premier.getCoordY());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void lesEmplacementsGeneresDoiventAvoirDesCoordonneesDistinctes(int nombre) {
        carte.genererEmplacements(nombre);

        long distincts = carte.getEmplacements().stream()
                .map(e -> e.getCoordX() + ";" + e.getCoordY())
                .distinct()
                .count();

        assertEquals(nombre, distincts);
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 4, 6, 8, 10 })
    void lesEmplacementsDOppositionDoiventEtreSymetriquesParRapportAuCentre(int nombre) {
        carte.genererEmplacements(nombre);

        List<EmplacementCarte> liste = carte.getEmplacements();
        for (int i = 0; i < nombre / 2; i++) {
            EmplacementCarte a = liste.get(i);
            EmplacementCarte b = liste.get(i + nombre / 2);

            // Tolérance de 1 : arrondi à l'entier
            assertEquals(2 * carte.getCentreX(), a.getCoordX() + b.getCoordX(), 1);
            assertEquals(2 * carte.getCentreY(), a.getCoordY() + b.getCoordY(), 1);
        }
    }

    @Test
    void genererUnSeulEmplacementDoitLePlacerEnHautDuCercle() {
        carte.genererEmplacements(1);

        EmplacementCarte unique = carte.getEmplacements().get(0);

        assertEquals(50, unique.getCoordX());
        assertEquals(10, unique.getCoordY());
    }

    @Test
    void genererDeuxEmplacementsDoitLesPlacerAuxExtremitesDUnDiametre() {
        carte.genererEmplacements(2);

        assertEquals(50, carte.getEmplacements().get(0).getCoordX());
        assertEquals(10, carte.getEmplacements().get(0).getCoordY());
        assertEquals(50, carte.getEmplacements().get(1).getCoordX());
        assertEquals(90, carte.getEmplacements().get(1).getCoordY());
    }

    @Test
    void genererTroisEmplacementsDoitCalculerLesCoordonneesAttendues() {
        carte.genererEmplacements(3);

        EmplacementCarte premier = carte.getEmplacements().get(0);
        EmplacementCarte deuxieme = carte.getEmplacements().get(1);
        EmplacementCarte troisieme = carte.getEmplacements().get(2);

        assertEquals(50, premier.getCoordX());
        assertEquals(10, premier.getCoordY());

        assertEquals(85, deuxieme.getCoordX());
        assertEquals(70, deuxieme.getCoordY());

        assertEquals(15, troisieme.getCoordX());
        assertEquals(70, troisieme.getCoordY());
    }

    @Test
    void genererQuatreEmplacementsDoitLesPlacerAuxPointsCardinauxDansLeSensHoraire() {
        carte.genererEmplacements(4);

        List<EmplacementCarte> l = carte.getEmplacements();

        assertAll(
                // haut
                () -> assertEquals(50, l.get(0).getCoordX()),
                () -> assertEquals(10, l.get(0).getCoordY()),
                // droite
                () -> assertEquals(90, l.get(1).getCoordX()),
                () -> assertEquals(50, l.get(1).getCoordY()),
                // bas
                () -> assertEquals(50, l.get(2).getCoordX()),
                () -> assertEquals(90, l.get(2).getCoordY()),
                // gauche
                () -> assertEquals(10, l.get(3).getCoordX()),
                () -> assertEquals(50, l.get(3).getCoordY()));
    }

    @Test
    void genererDixEmplacementsDoitCalculerLesCoordonneesAttendues() {
        carte.genererEmplacements(10);

        int[][] attendu = {
                { 50, 10 }, { 74, 18 }, { 88, 38 }, { 88, 62 }, { 74, 82 },
                { 50, 90 }, { 26, 82 }, { 12, 62 }, { 12, 38 }, { 26, 18 } };

        for (int i = 0; i < attendu.length; i++) {
            EmplacementCarte e = carte.getEmplacements().get(i);

            assertEquals(attendu[i][0], e.getCoordX(), "x de l'emplacement " + (i + 1));
            assertEquals(attendu[i][1], e.getCoordY(), "y de l'emplacement " + (i + 1));
        }
    }

    @Test
    void genererEmplacementsDoitArrondirLesCoordonneesNonEntieres() {
        carte.genererEmplacements(3);

        // 40 * cos(30°) = 34,64 -> arrondi à 35 (une troncature donnerait 84 et 16)
        assertEquals(85, carte.getEmplacements().get(1).getCoordX());
        assertEquals(15, carte.getEmplacements().get(2).getCoordX());
    }

    @Test
    void genererEmplacementsDoitTenirCompteDesDimensionsEtDuCentreDeLaCarte() {
        carte.setLargeur(200);
        carte.setHauteur(80);
        carte.setCentreX(100);
        carte.setCentreY(40);
        // rayon = min(200, 80) / 2 - 10 = 30

        carte.genererEmplacements(3);

        assertEquals(100, carte.getEmplacements().get(0).getCoordX());
        assertEquals(10, carte.getEmplacements().get(0).getCoordY());
        assertEquals(126, carte.getEmplacements().get(1).getCoordX());
        assertEquals(55, carte.getEmplacements().get(1).getCoordY());
        assertEquals(74, carte.getEmplacements().get(2).getCoordX());
        assertEquals(55, carte.getEmplacements().get(2).getCoordY());
    }

    @Test
    void genererEmplacementsDoitUtiliserLaPlusPetiteDimensionPourLeRayon() {
        carte.setLargeur(200);
        carte.setHauteur(80); // rayon = 30 (et non 90)

        carte.genererEmplacements(4);

        for (EmplacementCarte e : carte.getEmplacements()) {
            double distance = Math.hypot(
                    e.getCoordX() - carte.getCentreX(),
                    e.getCoordY() - carte.getCentreY());
            assertEquals(30, distance, 1.0);
        }
    }

    @Test
    void genererEmplacementsDoitSuivreUnCentreDecale() {
        carte.setCentreX(30);
        carte.setCentreY(60);

        carte.genererEmplacements(4);

        List<EmplacementCarte> l = carte.getEmplacements();
        assertEquals(30, l.get(0).getCoordX());
        assertEquals(20, l.get(0).getCoordY());
        assertEquals(70, l.get(1).getCoordX());
        assertEquals(60, l.get(1).getCoordY());
    }

    // =====================================================================
    // Génération des emplacements : carte trop petite
    // =====================================================================

    @Test
    void genererEmplacementsSurUneCarteTropEtroiteDoitLeverUneException() {
        carte.genererEmplacements(3);
        List<EmplacementCarte> avant = new ArrayList<>(carte.getEmplacements());
        carte.setLargeur(5);

        assertThrows(IllegalStateException.class, () -> carte.genererEmplacements(10));

        assertEquals(avant, carte.getEmplacements()); // rien n'a été modifié
    }

    @Test
    void genererEmplacementsDoitLeverUneExceptionQuandLeRayonEstNul() {
        carte.setLargeur(20); // rayon = 20 / 2 - 10 = 0

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> carte.genererEmplacements(1));

        assertEquals(
                "La carte est trop petite pour générer les emplacements.",
                exception.getMessage());
    }

    @Test
    void genererEmplacementsDoitLeverUneExceptionQuandLaHauteurEstTropFaible() {
        carte.setHauteur(20); // rayon = 20 / 2 - 10 = 0

        assertThrows(IllegalStateException.class, () -> carte.genererEmplacements(1));
    }

    @Test
    void genererEmplacementsDoitFonctionnerAvecLeRayonMinimalDeUn() {
        carte.setLargeur(22); // rayon = 22 / 2 - 10 = 1

        carte.genererEmplacements(1);

        assertEquals(50, carte.getEmplacements().get(0).getCoordX());
        assertEquals(49, carte.getEmplacements().get(0).getCoordY());
    }

    @Test
    void genererEmplacementsSurUneCarteTropEtroiteNeDoitPasLibererLesRoyaumesPlaces() {
        carte.genererEmplacements(3);
        Royaume royaume = new Royaume();
        carte.placerRoyaume(royaume);
        carte.setLargeur(5);

        assertThrows(IllegalStateException.class, () -> carte.genererEmplacements(3));

        assertNotNull(royaume.getEmplacement());
        assertTrue(carte.getEmplacements().get(0).isOccupe());
    }

    @Test
    void leNombreInvalideDoitEtreRefuseAvantLeControleDeLaTailleDeLaCarte() {
        carte.setLargeur(5);

        assertThrows(IllegalArgumentException.class, () -> carte.genererEmplacements(0));
    }

    @Test
    void nombreSuperieurADixDoitEtreRefuseAvantLeControleDeLaTaille() {
        carte.setLargeur(5);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carte.genererEmplacements(11));

        assertTrue(exception.getMessage().contains("11"));
    }

    // =====================================================================
    // Régénération des emplacements
    // =====================================================================

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

    @Test
    void regenererLesEmplacementsDoitLibererLesRoyaumesDejaPlaces() {
        carte.genererEmplacements(3);
        Royaume royaume = new Royaume();
        carte.placerRoyaume(royaume);

        carte.genererEmplacements(3);

        assertNull(royaume.getEmplacement());
    }

    @Test
    void regenererAvecDecalageDoitLibererLesRoyaumesDejaPlaces() {
        carte.genererEmplacements(3, true);

        Royaume royaume = new Royaume();
        EmplacementCarte ancien = carte.placerRoyaume(royaume);

        carte.genererEmplacements(3, true);

        assertAll(
                () -> assertNull(royaume.getEmplacement()),
                () -> assertNull(ancien.getRoyaume()),
                () -> assertFalse(ancien.isOccupe()),
                () -> assertEquals(3, carte.getEmplacements().size()));
    }

    @Test
    void genererEmplacementsDoitFonctionnerAvecUneListeImmuableFournieAuConstructeur() {
        Carte c = new Carte(100, 100, 50, 50, null, List.of());

        assertDoesNotThrow(() -> c.genererEmplacements(3));
        assertEquals(3, c.getEmplacements().size());
    }

    @Test
    void replacerUnRoyaumeDejaPlaceNeDoitPasModifierLesAutresEmplacements() {
        carte.genererEmplacements(3);

        Royaume royaume = new Royaume();
        EmplacementCarte premier = carte.placerRoyaume(royaume);

        assertThrows(
                IllegalStateException.class,
                () -> carte.placerRoyaume(royaume));

        assertAll(
                () -> assertSame(premier, royaume.getEmplacement()),
                () -> assertSame(royaume, premier.getRoyaume()),
                () -> assertTrue(premier.isOccupe()),
                () -> assertFalse(carte.getEmplacements().get(1).isOccupe()),
                () -> assertFalse(carte.getEmplacements().get(2).isOccupe()));
    }

    // =====================================================================
    // Recherche d'un emplacement libre
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
    // Placement d'un royaume
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

        assertEquals(50, royaume.getX());
        assertEquals(10, royaume.getY());
    }

    @Test
    void placerPlusieursRoyaumesDoitLesPositionnerSurLeCercleDansLOrdre() {
        carte.genererEmplacements(4);

        Royaume haut = new Royaume();
        Royaume droite = new Royaume();
        carte.placerRoyaume(haut);
        carte.placerRoyaume(droite);

        assertEquals(50, haut.getX());
        assertEquals(10, haut.getY());
        assertEquals(90, droite.getX());
        assertEquals(50, droite.getY());
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
    void placerUnRoyaumeDejaPlaceDoitLeverUneExceptionSansOccuperUnSecondEmplacement() {
        carte.genererEmplacements(3);
        Royaume royaume = new Royaume();
        carte.placerRoyaume(royaume);

        assertThrows(
                IllegalStateException.class,
                () -> carte.placerRoyaume(royaume));

        long occupes = carte.getEmplacements().stream().filter(EmplacementCarte::isOccupe).count();
        assertEquals(1, occupes);
        assertSame(carte.getEmplacements().get(0), royaume.getEmplacement());
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
    void calculerDistanceEntreDeuxRoyaumesOpposesSurLaCarteGenereeDoitEtreLeDiametre() {
        carte.genererEmplacements(2);
        Royaume royaume1 = new Royaume();
        Royaume royaume2 = new Royaume();
        carte.placerRoyaume(royaume1); // (50, 10)
        carte.placerRoyaume(royaume2); // (50, 90)

        assertEquals(2 * rayonAttendu(), carte.calculerDistanceEntre(royaume1, royaume2), 0.0001);
        assertEquals(80.0, carte.calculerDistanceEntre(royaume1, royaume2), 0.0001);
    }

    @Test
    void calculerDistanceEntreDeuxRoyaumesVoisinsSurQuatreEmplacementsDoitEtreRayonFoisRacineDeDeux() {
        carte.genererEmplacements(4);
        Royaume haut = new Royaume();
        Royaume droite = new Royaume();
        carte.placerRoyaume(haut); // (50, 10)
        carte.placerRoyaume(droite); // (90, 50)

        assertEquals(40 * Math.sqrt(2), carte.calculerDistanceEntre(haut, droite), 0.0001);
    }

    @Test
    void calculerDistanceEntreDoitGererLesCoordonneesExtremes() {
        Royaume royaume1 = royaumeEn(Integer.MIN_VALUE, 0);
        Royaume royaume2 = royaumeEn(Integer.MAX_VALUE, 0);

        double distance = carte.calculerDistanceEntre(royaume1, royaume2);

        assertEquals(4294967295.0, distance, 0.0001);
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
    // =====================================================================
    // Génération des emplacements : décalage aléatoire
    // =====================================================================

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void surchargeSansDecalageDoitEtreIdentiqueALaMethodeHistorique(
            int nombre) {

        carte.genererEmplacements(nombre);

        List<String> coordonneesAttendues = carte.getEmplacements().stream()
                .map(e -> e.getNumero()
                        + ":" + e.getCoordX()
                        + ":" + e.getCoordY())
                .toList();

        carte.genererEmplacements(nombre, false);

        List<String> coordonneesObtenues = carte.getEmplacements().stream()
                .map(e -> e.getNumero()
                        + ":" + e.getCoordX()
                        + ":" + e.getCoordY())
                .toList();

        assertEquals(coordonneesAttendues, coordonneesObtenues);
    }

    @ParameterizedTest
    @ValueSource(ints = {
            Integer.MIN_VALUE,
            -1,
            0,
            11,
            100,
            Integer.MAX_VALUE
    })
    void surchargeAvecNombreInvalideDoitTransmettreLaValidation(
            int nombre) {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carte.genererEmplacements(nombre, true));

        assertTrue(exception.getMessage().contains(String.valueOf(nombre)));
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void genererEmplacementsAvecDecalageDoitFonctionnerPourDeuxADixRoyaumes(
            int nombre) {

        carte.genererEmplacements(nombre, true);

        assertEquals(nombre, carte.getEmplacements().size());

        for (int i = 0; i < nombre; i++) {
            EmplacementCarte emplacement = carte.getEmplacements().get(i);
            int numeroAttendu = i + 1;

            assertAll(
                    () -> assertEquals(
                            numeroAttendu,
                            emplacement.getNumero()),
                    () -> assertSame(
                            carte,
                            emplacement.getCarte()),
                    () -> assertFalse(
                            emplacement.isOccupe()),
                    () -> assertNull(
                            emplacement.getRoyaume()));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void genererEmplacementsAvecDecalageDoitRespecterLaPlagePourDeuxADixRoyaumes(
            int nombre) {

        carte.genererEmplacements(nombre);

        List<Integer> xSansDecalage = carte.getEmplacements().stream()
                .map(EmplacementCarte::getCoordX)
                .toList();

        List<Integer> ySansDecalage = carte.getEmplacements().stream()
                .map(EmplacementCarte::getCoordY)
                .toList();

        carte.genererEmplacements(nombre, true);

        for (int i = 0; i < nombre; i++) {
            EmplacementCarte emplacement = carte.getEmplacements().get(i);

            int decalageX = Math.abs(
                    emplacement.getCoordX()
                            - xSansDecalage.get(i));

            int decalageY = Math.abs(
                    emplacement.getCoordY()
                            - ySansDecalage.get(i));

            assertAll(
                    () -> assertTrue(
                            decalageX >= 2 && decalageX <= 4,
                            "Le décalage X de l'emplacement "
                                    + emplacement.getNumero()
                                    + " doit être compris entre 2 et 4"),
                    () -> assertTrue(
                            decalageY >= 2 && decalageY <= 4,
                            "Le décalage Y de l'emplacement "
                                    + emplacement.getNumero()
                                    + " doit être compris entre 2 et 4"));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void genererEmplacementsAvecDecalageDoitConserverDesCoordonneesDistinctes(
            int nombre) {

        carte.genererEmplacements(nombre, true);

        long nombreDeCoordonneesDistinctes = carte.getEmplacements().stream()
                .map(e -> e.getCoordX()
                        + ";" + e.getCoordY())
                .distinct()
                .count();

        assertEquals(nombre, nombreDeCoordonneesDistinctes);
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void genererEmplacementsAvecDecalageDoitResterDansLaCartePourDeuxADixRoyaumes(
            int nombre) {

        carte.genererEmplacements(nombre, true);

        for (EmplacementCarte emplacement : carte.getEmplacements()) {

            assertAll(
                    () -> assertTrue(
                            emplacement.getCoordX() >= 0,
                            "La coordonnée X doit être positive ou nulle"),
                    () -> assertTrue(
                            emplacement.getCoordX() <= 100,
                            "La coordonnée X ne doit pas dépasser 100"),
                    () -> assertTrue(
                            emplacement.getCoordY() >= 0,
                            "La coordonnée Y doit être positive ou nulle"),
                    () -> assertTrue(
                            emplacement.getCoordY() <= 100,
                            "La coordonnée Y ne doit pas dépasser 100"));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    void genererEmplacementsAvecDecalageNeDoitJamaisAtteindreLeMarche(
            int nombre) {

        carte.genererEmplacements(nombre, true);

        EmplacementCarte marche = carte.getEmplacementMarche();

        boolean superposition = carte.getEmplacements().stream()
                .anyMatch(e -> e.getCoordX() == marche.getCoordX()
                        && e.getCoordY() == marche.getCoordY());

        assertFalse(superposition);
    }

    @Test
    void surchargeAvecNombreInvalideNeDoitPasModifierLesEmplacementsExistants() {
        carte.genererEmplacements(5, true);

        List<EmplacementCarte> avant = new ArrayList<>(carte.getEmplacements());

        assertThrows(
                IllegalArgumentException.class,
                () -> carte.genererEmplacements(11, true));

        assertEquals(avant, carte.getEmplacements());
    }

}