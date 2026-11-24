package royaume.model.militaire;


import java.util.Set;
import jakarta.validation.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.time.LocalDateTime;


// mvn test -Dtest=ContingentTroupeTest

class ContingentTroupeTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void constructeurRejetteQuantiteNegative() {
        // Given
        ContingentTroupe contingent = new ContingentTroupe(TypeTroupe.ARCHER, -5);
        // When
        Set<ConstraintViolation<ContingentTroupe>> violations = validator.validate(contingent);
        // Then
        assertFalse(violations.isEmpty(), "La validation doit détecter une quantité négative");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("quantite")));
    }

    @Test
    void typeNullEstRejete() {
        ContingentTroupe contingent = new ContingentTroupe(null, 5);

        Set<ConstraintViolation<ContingentTroupe>> violations = validator.validate(contingent);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("type")));
    }

    @Test
    void quantiteZeroEstValide() {
        ContingentTroupe contingent = new ContingentTroupe(TypeTroupe.ARCHER, 0);

        assertTrue(validator.validate(contingent).isEmpty());
    }

    @Test
    void ajouter_incrementeLaQuantite() {
        ContingentTroupe c = new ContingentTroupe(TypeTroupe.LANCIER, 5);
        c.ajouter(3);
        assertEquals(8, c.getQuantite());
    }

    @Test
    void retirer_decrementeLaQuantite() {
        ContingentTroupe c = new ContingentTroupe(TypeTroupe.LANCIER, 5);
        c.retirer(2);
        assertEquals(3, c.getQuantite());
    }

    @Test
    void retirer_toutLeStock_donneZero() {
        ContingentTroupe c = new ContingentTroupe(TypeTroupe.ARCHER, 5);
        c.retirer(5);
        assertEquals(0, c.getQuantite());
    }

    @Test
    void retirer_plusQueLeStock_estRefuse_etNeChangeRien() {
        ContingentTroupe c = new ContingentTroupe(TypeTroupe.ARCHER, 5);
        assertThrows(IllegalArgumentException.class, () -> c.retirer(6));
        assertEquals(5, c.getQuantite());
    }

    @Test
    void quantiteNegative_estRefusee() {
        ContingentTroupe c = new ContingentTroupe(TypeTroupe.ARCHER, 5);
        assertThrows(IllegalArgumentException.class, () -> c.ajouter(-1));
        assertThrows(IllegalArgumentException.class, () -> c.retirer(-1));
    }

    @Test
    void quantiteZeroEstRejetee() {
        SessionEntrainement s = new SessionEntrainement(TypeTroupe.ARCHER, 0,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));

        Set<ConstraintViolation<SessionEntrainement>> violations = validator.validate(s);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("quantite")));
    }


}