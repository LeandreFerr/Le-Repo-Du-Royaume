package royaume.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EmplacementCarteTest {

    private Carte carte;
    private EmplacementCarte emplacement;
    private Royaume royaume;

    @BeforeEach
    void setUp() {
        carte = new Carte();

        emplacement = new EmplacementCarte(
                1,
                25,
                50,
                carte);

        royaume = new Royaume();
    }

    // =====================================================================
    // Constructeurs
    // =====================================================================

    @Test
    void constructeurParDefautDoitInitialiserLesValeursNeutres() {
        EmplacementCarte vide = new EmplacementCarte();

        assertAll(
                () -> assertNull(vide.getId()),
                () -> assertEquals(0, vide.getNumero()),
                () -> assertEquals(0, vide.getCoordX()),
                () -> assertEquals(0, vide.getCoordY()),
                () -> assertFalse(vide.isOccupe()),
                () -> assertNull(vide.getCarte()),
                () -> assertNull(vide.getRoyaume()));
    }

    @Test
    void constructeurSimplifieDoitCreerUnEmplacementLibre() {
        assertNull(emplacement.getId());
        assertEquals(1, emplacement.getNumero());
        assertEquals(25, emplacement.getCoordX());
        assertEquals(50, emplacement.getCoordY());
        assertFalse(emplacement.isOccupe());
        assertSame(carte, emplacement.getCarte());
        assertNull(emplacement.getRoyaume());
    }

    @Test
    void constructeurSimplifieAvecCarteNullDoitEtreAccepte() {
        EmplacementCarte sansCarte = new EmplacementCarte(2, 10, 20, null);

        assertNull(sansCarte.getCarte());
        assertEquals(2, sansCarte.getNumero());
        assertFalse(sansCarte.isOccupe());
    }

    @Test
    void constructeurCompletDoitInitialiserTousLesAttributs() {
        EmplacementCarte complet = new EmplacementCarte(7, 3, 30, 40, false, carte);

        assertAll(
                () -> assertEquals(7, complet.getId()),
                () -> assertEquals(3, complet.getNumero()),
                () -> assertEquals(30, complet.getCoordX()),
                () -> assertEquals(40, complet.getCoordY()),
                () -> assertFalse(complet.isOccupe()),
                () -> assertSame(carte, complet.getCarte()),
                () -> assertNull(complet.getRoyaume()));
    }

    @Test
    void constructeurCompletDoitAccepterUnIdNull() {
        EmplacementCarte complet = new EmplacementCarte(null, 3, 30, 40, false, carte);

        assertNull(complet.getId());
    }

    @Test
    void constructeurCompletDoitConserverLEtatOccupeFourni() {
        // Cas d'un emplacement rechargé depuis la persistance : l'état "occupé"
        // est repris tel quel, le royaume étant rattaché ensuite
        EmplacementCarte occupe = new EmplacementCarte(7, 3, 30, 40, true, carte);

        assertTrue(occupe.isOccupe());
        assertNull(occupe.getRoyaume());
    }

    @Test
    void unEmplacementCreeAvecLeConstructeurCompletOccupeNePermetPasDAssignerUnRoyaume() {
        EmplacementCarte occupe = new EmplacementCarte(7, 3, 30, 40, true, carte);

        assertThrows(
                IllegalStateException.class,
                () -> occupe.assignerRoyaume(royaume));

        assertNull(royaume.getEmplacement());
    }

    // =====================================================================
    // Getters & Setters
    // =====================================================================

    @Test
    void setIdDoitModifierLId() {
        emplacement.setId(42);
        assertEquals(42, emplacement.getId());

        emplacement.setId(null);
        assertNull(emplacement.getId());
    }

    @Test
    void setNumeroDoitModifierLeNumero() {
        emplacement.setNumero(9);

        assertEquals(9, emplacement.getNumero());
    }

    @Test
    void setCoordonneesDoitModifierLesCoordonnees() {
        emplacement.setCoordX(11);
        emplacement.setCoordY(22);

        assertEquals(11, emplacement.getCoordX());
        assertEquals(22, emplacement.getCoordY());
    }

    @Test
    void setCarteDoitModifierLaCarte() {
        Carte autreCarte = new Carte();

        emplacement.setCarte(autreCarte);
        assertSame(autreCarte, emplacement.getCarte());

        emplacement.setCarte(null);
        assertNull(emplacement.getCarte());
    }

    @Test
    void lesSettersNeDoiventPasModifierLEtatOccupe() {
        emplacement.assignerRoyaume(royaume);

        emplacement.setNumero(5);
        emplacement.setCoordX(1);
        emplacement.setCoordY(2);
        emplacement.setCarte(new Carte());

        assertTrue(emplacement.isOccupe());
        assertSame(royaume, emplacement.getRoyaume());
    }

    // =====================================================================
    // Assignation d'un royaume
    // =====================================================================

    @Test
    void assignerRoyaumeDoitOccuperLEmplacement() {
        emplacement.assignerRoyaume(royaume);

        assertTrue(emplacement.isOccupe());
        assertSame(royaume, emplacement.getRoyaume());
    }

    @Test
    void assignerRoyaumeDoitAussiRenseignerLEmplacementDuRoyaume() {
        emplacement.assignerRoyaume(royaume);

        assertSame(emplacement, royaume.getEmplacement());
    }

    @Test
    void assignerRoyaumeNullDoitLeverUneException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> emplacement.assignerRoyaume(null));

        assertEquals(
                "Aucun royaume à assigner.",
                exception.getMessage());

        assertFalse(emplacement.isOccupe());
        assertNull(emplacement.getRoyaume());
    }

    @Test
    void assignerRoyaumeNullSurUnEmplacementOccupeDoitLeverIllegalArgumentException() {
        emplacement.assignerRoyaume(royaume);

        // La validation de l'argument doit passer avant le contrôle d'occupation
        assertThrows(
                IllegalArgumentException.class,
                () -> emplacement.assignerRoyaume(null));

        assertSame(royaume, emplacement.getRoyaume());
        assertTrue(emplacement.isOccupe());
    }

    @Test
    void assignerUnDeuxiemeRoyaumeDoitLeverUneException() {
        Royaume deuxiemeRoyaume = new Royaume();

        emplacement.assignerRoyaume(royaume);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> emplacement.assignerRoyaume(deuxiemeRoyaume));

        assertEquals(
                "L'emplacement 1 est déjà occupé.",
                exception.getMessage());

        assertSame(royaume, emplacement.getRoyaume());
        assertTrue(emplacement.isOccupe());
        assertNull(deuxiemeRoyaume.getEmplacement());
    }

    @Test
    void assignerLeMemeRoyaumeDeuxFoisDoitLeverUneException() {
        emplacement.assignerRoyaume(royaume);

        assertThrows(
                IllegalStateException.class,
                () -> emplacement.assignerRoyaume(royaume));

        assertSame(royaume, emplacement.getRoyaume());
        assertSame(emplacement, royaume.getEmplacement());
    }

    @Test
    void leMessageDExceptionDoitMentionnerLeNumeroDeLEmplacement() {
        EmplacementCarte emplacementSept = new EmplacementCarte(7, 10, 10, carte);
        emplacementSept.assignerRoyaume(royaume);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> emplacementSept.assignerRoyaume(new Royaume()));

        assertEquals("L'emplacement 7 est déjà occupé.", exception.getMessage());
    }

    @Test
    void deuxEmplacementsDistinctsDoiventAccueillirDeuxRoyaumesIndependants() {
        EmplacementCarte autreEmplacement = new EmplacementCarte(2, 75, 50, carte);
        Royaume autreRoyaume = new Royaume();

        emplacement.assignerRoyaume(royaume);
        autreEmplacement.assignerRoyaume(autreRoyaume);

        assertSame(royaume, emplacement.getRoyaume());
        assertSame(autreRoyaume, autreEmplacement.getRoyaume());
        assertSame(emplacement, royaume.getEmplacement());
        assertSame(autreEmplacement, autreRoyaume.getEmplacement());
    }

    // =====================================================================
    // Libération
    // =====================================================================

    @Test
    void libererDoitRendreLEmplacementDisponible() {
        emplacement.assignerRoyaume(royaume);

        emplacement.liberer();

        assertFalse(emplacement.isOccupe());
        assertNull(emplacement.getRoyaume());
    }

    @Test
    void libererDoitRetirerLEmplacementDuRoyaume() {
        emplacement.assignerRoyaume(royaume);

        emplacement.liberer();

        assertNull(royaume.getEmplacement());
    }

    @Test
    void libererUnEmplacementDejaLibreNeDoitPasLeverException() {
        assertDoesNotThrow(() -> emplacement.liberer());

        assertFalse(emplacement.isOccupe());
        assertNull(emplacement.getRoyaume());
    }

    @Test
    void libererDeuxFoisDeSuiteNeDoitPasLeverException() {
        emplacement.assignerRoyaume(royaume);

        emplacement.liberer();

        assertDoesNotThrow(() -> emplacement.liberer());
        assertFalse(emplacement.isOccupe());
        assertNull(royaume.getEmplacement());
    }

    @Test
    void libererNeDoitPasAffecterLesAutresEmplacements() {
        EmplacementCarte autreEmplacement = new EmplacementCarte(2, 75, 50, carte);
        Royaume autreRoyaume = new Royaume();

        emplacement.assignerRoyaume(royaume);
        autreEmplacement.assignerRoyaume(autreRoyaume);

        emplacement.liberer();

        assertTrue(autreEmplacement.isOccupe());
        assertSame(autreRoyaume, autreEmplacement.getRoyaume());
        assertSame(autreEmplacement, autreRoyaume.getEmplacement());
    }

    @Test
    void libererDoitConserverLesCoordonneesEtLaCarte() {
        emplacement.assignerRoyaume(royaume);

        emplacement.liberer();

        assertEquals(1, emplacement.getNumero());
        assertEquals(25, emplacement.getCoordX());
        assertEquals(50, emplacement.getCoordY());
        assertSame(carte, emplacement.getCarte());
    }

    @Test
    void apresLiberationLEmplacementDoitPouvoirAccueillirUnNouveauRoyaume() {
        Royaume nouveauRoyaume = new Royaume();
        emplacement.assignerRoyaume(royaume);
        emplacement.liberer();

        emplacement.assignerRoyaume(nouveauRoyaume);

        assertTrue(emplacement.isOccupe());
        assertSame(nouveauRoyaume, emplacement.getRoyaume());
        assertSame(emplacement, nouveauRoyaume.getEmplacement());
        assertNull(royaume.getEmplacement());
    }

    @Test
    void apresLiberationLeMemeRoyaumeDoitPouvoirEtreReassigne() {
        emplacement.assignerRoyaume(royaume);
        emplacement.liberer();

        assertDoesNotThrow(() -> emplacement.assignerRoyaume(royaume));

        assertTrue(emplacement.isOccupe());
        assertSame(emplacement, royaume.getEmplacement());
    }

    // =====================================================================
    // Cohérence avec Royaume (coordonnées)
    // =====================================================================

    @Test
    void leRoyaumeAssigneDoitExposerLesCoordonneesDeLEmplacement() {
        emplacement.assignerRoyaume(royaume);

        assertEquals(25, royaume.getX());
        assertEquals(50, royaume.getY());
    }

    @Test
    void modifierLesCoordonneesDeLEmplacementDoitSeRepercuterSurLeRoyaume() {
        emplacement.assignerRoyaume(royaume);

        emplacement.setCoordX(80);
        emplacement.setCoordY(10);

        assertEquals(80, royaume.getX());
        assertEquals(10, royaume.getY());
    }

    @Test
    void apresLiberationLeRoyaumeNeDoitPlusPouvoirLireSesCoordonnees() {
        emplacement.assignerRoyaume(royaume);

        emplacement.liberer();

        assertThrows(IllegalStateException.class, () -> royaume.getX());
        assertThrows(IllegalStateException.class, () -> royaume.getY());
    }

    // =====================================================================
    // Intégration avec Carte
    // =====================================================================

    @Test
    void unEmplacementGenereParLaCarteDoitAppartenirALaListeDeLaCarte() {
        carte.genererEmplacements(3);

        for (EmplacementCarte genere : carte.getEmplacements()) {
            assertTrue(genere.getCarte().getEmplacements().contains(genere));
        }
    }

    @Test
    void unEmplacementGenereParLaCarteDoitPouvoirEtreLibereEtReutilise() {
        carte.genererEmplacements(1);
        EmplacementCarte unique = carte.getEmplacements().get(0);
        Royaume premier = new Royaume();
        Royaume second = new Royaume();

        carte.placerRoyaume(premier);
        unique.liberer();
        EmplacementCarte reutilise = carte.placerRoyaume(second);

        assertSame(unique, reutilise);
        assertSame(second, unique.getRoyaume());
    }

    // =====================================================================
    // Représentation
    // =====================================================================

    @Test
    void toStringDoitContenirLesInformationsDeLEmplacementLibre() {
        String texte = emplacement.toString();

        assertTrue(texte.contains("numero=1"));
        assertTrue(texte.contains("coordX=25"));
        assertTrue(texte.contains("coordY=50"));
        assertTrue(texte.contains("occupe=false"));
        assertTrue(texte.contains("royaume=null"));
    }

    @Test
    void toStringDoitMentionnerLeRoyaumeQuandLEmplacementEstOccupe() {
        emplacement.assignerRoyaume(royaume);

        String texte = emplacement.toString();

        assertTrue(texte.contains("occupe=true"));
        assertTrue(texte.contains("royaume=Royaume ["));
    }

    @Test
    void toStringNeDoitPasBoucleSurLaRelationBidirectionnelle() {
        emplacement.assignerRoyaume(royaume);

        assertDoesNotThrow(() -> emplacement.toString());
        assertDoesNotThrow(() -> royaume.toString());
    }

    // =====================================================================
    // Comportements à trancher (désactivés tant que le code n'est pas corrigé)
    // =====================================================================

    @Test
    void libererNeDoitPasDetacherUnRoyaumeDejaReaffecteAilleurs() {
        EmplacementCarte autreEmplacement = new EmplacementCarte(2, 75, 50, carte);

        emplacement.assignerRoyaume(royaume);
        autreEmplacement.assignerRoyaume(royaume);

        emplacement.liberer();

        assertSame(autreEmplacement, royaume.getEmplacement());
    }
}