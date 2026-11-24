package royaume.model.militaire;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import royaume.model.Royaume;


//  mvn test -Dtest=AttaqueTest



class AttaqueTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static boolean violationSur(Set<? extends ConstraintViolation<?>> violations, String champ) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(champ));
    }

    private static List<ContingentTroupe> convoi() {
        return List.of(new ContingentTroupe(TypeTroupe.ARCHER, 3));
    }

    // =========================================================================
    // Bean Validation
    // =========================================================================

    @Nested
    @DisplayName("Bean Validation")
    class ValidationTests {

        @Test
        @DisplayName("Un convoi vide est rejeté")
        void troupesVidesRejetees() {
            // Constructeur vide : le constructeur métier refuserait la liste vide
            Attaque a = new Attaque();

            Set<ConstraintViolation<Attaque>> violations = validator.validate(a);

            assertTrue(violationSur(violations, "troupesEnvoyees"));
        }

        @Test
        @DisplayName("Les champs obligatoires null sont détectés")
        void champsObligatoiresRejetes() {
            Attaque a = new Attaque();

            Set<ConstraintViolation<Attaque>> violations = validator.validate(a);

            assertAll(
                () -> assertTrue(violationSur(violations, "attaquant")),
                () -> assertTrue(violationSur(violations, "defenseur")),
                () -> assertTrue(violationSur(violations, "dateDepart")),
                () -> assertTrue(violationSur(violations, "dateImpact")),
                () -> assertTrue(violationSur(violations, "tempsTrajet"))
            );
        }

        @Test
        @DisplayName("dateRetourPrevue est nullable")
        void dateRetourPrevueNullable() {
            Attaque a = new Attaque(new Royaume(), new Royaume(), convoi(), LocalDateTime.now(), 10);

            Set<ConstraintViolation<Attaque>> violations = validator.validate(a);

            assertFalse(violationSur(violations, "dateRetourPrevue"));
        }

        @Test
        @DisplayName("Une attaque construite normalement est valide")
        void attaqueValide() {
            Attaque a = new Attaque(new Royaume(), new Royaume(), convoi(), LocalDateTime.now(), 10);

            assertTrue(validator.validate(a).isEmpty());
        }

        @Test
        @DisplayName("Un contingent invalide dans le convoi est détecté")
        void contingentInvalideDansLeConvoi() {
            // Constructeur vide : on évite calculerTempsAller, qui refuse un convoi sans quantité positive
            Attaque a = new Attaque();
            a.getTroupesEnvoyees().add(new ContingentTroupe(TypeTroupe.ARCHER, -1));

            Set<ConstraintViolation<Attaque>> violations = validator.validate(a);

            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().startsWith("troupesEnvoyees")));
        }
    }

    // =========================================================================
    // Contrôles du constructeur métier
    // =========================================================================

    @Nested
    @DisplayName("Constructeur métier")
    class ConstructeurTests {

        @Test
        @DisplayName("Auto-attaque refusée")
        void autoAttaqueRefusee() {
            Royaume r = new Royaume();

            assertThrows(IllegalArgumentException.class,
                    () -> new Attaque(r, r, convoi(), LocalDateTime.now(), 10));
        }

        @Test
        @DisplayName("Liste de troupes vide refusée")
        void troupesVidesRefusees() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Attaque(new Royaume(), new Royaume(), List.of(), LocalDateTime.now(), 10));
        }

        @Test
        @DisplayName("Distance nulle, négative ou non finie refusée")
        void distanceInvalideRefusee() {
            Royaume a = new Royaume();
            Royaume d = new Royaume();
            LocalDateTime now = LocalDateTime.now();

            assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Attaque(a, d, convoi(), now, 0)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Attaque(a, d, convoi(), now, -5)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Attaque(a, d, convoi(), now, Double.NaN))
            );
        }

        @Test
        @DisplayName("Le temps de trajet est calculé à partir de la distance")
        void tempsTrajetCalcule() {
            LocalDateTime depart = LocalDateTime.of(2026, 1, 1, 12, 0);
            // ARCHER : vitesse 10 -> 20 cases = 2 h
            Attaque a = new Attaque(new Royaume(), new Royaume(), convoi(), depart, 20);

            assertAll(
                () -> assertEquals(java.time.Duration.ofHours(2), a.getTempsTrajet()),
                () -> assertEquals(depart.plusHours(2), a.getDateImpact())
            );
        }
    }
}