package royaume.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Contrat du Socle — Enum Ressource & Pureté POJO")
class RessourceTest {

    private static final Class<?>[] CLASSES_SOCLE = {
            Ressource.class,
            QuantiteRessource.class,
            Royaume.class
    };

    @Test
    @DisplayName("Ressource définit exactement les 4 ressources fondamentales du jeu")
    void testValeursEnumRessource() {
        Set<String> noms = Arrays.stream(Ressource.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertAll(
            () -> assertEquals(4, Ressource.values().length,
                    "L'énumération Ressource doit contenir exactement 4 types"),
            () -> assertEquals(Set.of("PIERRE", "BOIS", "OR", "NOURRITURE"), noms),
            () -> assertNotNull(Ressource.valueOf("PIERRE")),
            () -> assertNotNull(Ressource.valueOf("BOIS")),
            () -> assertNotNull(Ressource.valueOf("OR")),
            () -> assertNotNull(Ressource.valueOf("NOURRITURE"))
        );
    }

    @Test
    @DisplayName("Architecture POJO : aucune annotation Spring, JPA, Hibernate ou Jackson sur les classes du socle")
    void testAbsenceTotaleAnnotationsFramework() {
        List<String> violations = new ArrayList<>();

        for (Class<?> clazz : CLASSES_SOCLE) {
            collecterAnnotationsExternes(clazz.getAnnotations(), clazz.getSimpleName(), violations);

            for (Field field : clazz.getDeclaredFields()) {
                collecterAnnotationsExternes(
                        field.getAnnotations(),
                        clazz.getSimpleName() + "." + field.getName(),
                        violations);
            }

            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                collecterAnnotationsExternes(
                        constructor.getAnnotations(),
                        clazz.getSimpleName() + "<init>",
                        violations);
            }

            for (Method method : clazz.getDeclaredMethods()) {
                collecterAnnotationsExternes(
                        method.getAnnotations(),
                        clazz.getSimpleName() + "." + method.getName(),
                        violations);
            }
        }

        assertEquals(List.of(), violations,
                "Aucune annotation de framework externe ne doit figurer sur le modèle POJO");
    }

    @Test
    @DisplayName("Encapsulation : tous les attributs d'instance de QuantiteRessource et Royaume sont strictement private")
    void testTousLesChampsInstanceSontPrivate() {
        Class<?>[] classesEntites = { QuantiteRessource.class, Royaume.class };
        List<String> champsNonPrives = new ArrayList<>();

        for (Class<?> clazz : classesEntites) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isSynthetic()
                        && !Modifier.isStatic(field.getModifiers())
                        && !Modifier.isPrivate(field.getModifiers())) {
                    champsNonPrives.add(clazz.getSimpleName() + "." + field.getName());
                }
            }
        }

        assertEquals(List.of(), champsNonPrives,
                "Tous les attributs d'instance du socle doivent être déclarés private");
    }

    private static void collecterAnnotationsExternes(
            Annotation[] annotations,
            String cible,
            List<String> violations) {
        for (Annotation annotation : annotations) {
            String nomPackage = annotation.annotationType().getPackageName();
            if (!nomPackage.startsWith("java.lang")) {
                violations.add(cible + " -> @" + annotation.annotationType().getName());
            }
        }
    }
}
