package royaume.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class QuantiteRessourceTest {
    private QuantiteRessource qr;

    @BeforeEach
    void setUp() {
        qr = new QuantiteRessource(50, Ressource.BOIS);
    }

    @Test
    @DisplayName("ajouter incrémente correctement la quantité")
    void testAjouterSucces() {
        qr.ajouter(30);
        assertEquals(80, qr.getQuantite());
    }

    @Test
    @DisplayName("ajouter lève une exception si montant <= 0")
    void testAjouterMontantInvalide() {
        assertThrows(IllegalArgumentException.class, () -> qr.ajouter(0));
        assertThrows(IllegalArgumentException.class, () -> qr.ajouter(-10));
    }

    @Test
    @DisplayName("retirer décrémente correctement la quantité")
    void testRetirerSucces() {
        qr.retirer(30);
        assertEquals(20, qr.getQuantite());
    }

    @Test
    @DisplayName("retirer lève une exception si montant <= 0")
    void testRetirerMontantInvalide() {
        assertThrows(IllegalArgumentException.class, () -> qr.retirer(0));
        assertThrows(IllegalArgumentException.class, () -> qr.retirer(-10));
    }

    @Test
    @DisplayName("retirer lève une exception en cas de solde négatif")
    void testRetirerSoldeInsuffisant() {
        assertThrows(IllegalArgumentException.class, () -> qr.retirer(51));
        assertEquals(50, qr.getQuantite(), "Le stock ne doit pas avoir changé suite à l'exception");
    }
}
