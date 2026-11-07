package royaume.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests unitaires robustes de la hiérarchie Population (Module 2)")
public class PopulationTest {

	// =========================================================================
	// 1. Hiérarchie et Polymorphisme
	// =========================================================================

	@Nested
	@DisplayName("1. Hiérarchie et Polymorphisme")
	class HierarchieEtPolymorphismeTests {

		@Test
		@DisplayName("Population est une classe abstraite et les 5 sous-classes héritent polymorphiquement")
		void testHierarchiePolymorpheDesCinqPeuples() {
			List<Population> peuples = List.of(
				new Canards(1, "Canards", "Archiduc Colvert", 0.05),
				new Gobelins(2, "Gobelins", "Chef Pillard", 0.0),
				new Licornes(3, "Licornes", "Impératrice Astrale", 0.15),
				new Princesses(4, "Princesses", "Reine Pyromancienne", 0.20),
				new Teuteus(5, "Teuteus", "Grand Glouton", 0.0)
			);

			assertTrue(Modifier.isAbstract(Population.class.getModifiers()),
					"Population doit être une classe abstraite");
			assertEquals(5, peuples.size());

			peuples.forEach(peuple -> {
				assertInstanceOf(Population.class, peuple);
				assertDoesNotThrow(() -> peuple.activerCapaciteSpecial());
				assertFalse(peuple.getDescriptionCapacite().isBlank());
			});
		}

		@Test
		@DisplayName("Chaque peuple possède sa capacité spéciale par défaut assignée dans super()")
		void testCapaciteSpecialeParDefaut() {
			assertEquals("danse des canards", new Canards(1, "Canards", "Archiduc", 0.05).getCapaciteSpeciale());
			assertEquals("voler", new Gobelins(2, "Gobelins", "Chef", 0.0).getCapaciteSpeciale());
			assertEquals("peter des arcs en ciels", new Licornes(3, "Licornes", "Impératrice", 0.15).getCapaciteSpeciale());
			assertEquals("roter du feu", new Princesses(4, "Princesses", "Reine", 0.20).getCapaciteSpeciale());
			assertEquals("manger crottes de nez", new Teuteus(5, "Teuteus", "Glouton", 0.0).getCapaciteSpeciale());
		}

		@Test
		@DisplayName("Constructeurs vides sans arguments des 5 peuples pour compatibilité JPA")
		void testConstructeursVidesDesCinqPeuplesJPA() {
			Canards canards = new Canards();
			Gobelins gobelins = new Gobelins();
			Licornes licornes = new Licornes();
			Princesses princesses = new Princesses();
			Teuteus teuteus = new Teuteus();

			assertAll(
				() -> assertEquals("danse des canards", canards.getCapaciteSpeciale()),
				() -> assertEquals("voler", gobelins.getCapaciteSpeciale()),
				() -> assertEquals("peter des arcs en ciels", licornes.getCapaciteSpeciale()),
				() -> assertEquals("roter du feu", princesses.getCapaciteSpeciale()),
				() -> assertEquals("manger crottes de nez", teuteus.getCapaciteSpeciale())
			);
		}
	}

	// =========================================================================
	// 2. Encapsulation stricte
	// =========================================================================

	@Nested
	@DisplayName("2. Encapsulation stricte")
	class EncapsulationTests {

		@Test
		@DisplayName("Aucun attribut des sous-classes ni de Population ne doit être exposé en visibilité public")
		void testEncapsulationStricteSansChampPublic() {
			List<Class<? extends Population>> classesPeuples = List.of(
				Canards.class, Gobelins.class, Licornes.class, Princesses.class, Teuteus.class
			);

			for (Class<? extends Population> clazz : classesPeuples) {
				assertEquals(0, clazz.getFields().length,
						clazz.getSimpleName() + " ne doit exposer aucun champ public mutable");
			}
			assertEquals(0, Population.class.getFields().length,
					"Population ne doit exposer aucun champ public mutable");
		}

		@Test
		@DisplayName("Chaque peuple conserve ses attributs encapsulés et accessibles via getters")
		void testAttributsEncapsulesPopulation() {
			Population licornes = new Licornes(3, "Licornes", "Souveraine Céleste", 0.15);

			assertAll(
				() -> assertEquals(3, licornes.getId()),
				() -> assertEquals("Licornes", licornes.getNom()),
				() -> assertEquals("Souveraine Céleste", licornes.getTitreSouverain()),
				() -> assertEquals(0.15, licornes.getAttraction()),
				() -> assertEquals("peter des arcs en ciels", licornes.getCapaciteSpeciale())
			);
		}

		@Test
		@DisplayName("Les attributs de la classe abstraite Population sont protected (# UML)")
		void testAttributsSontProtectedConformeUML() {
			Field[] fields = Population.class.getDeclaredFields();
			for (Field field : fields) {
				assertTrue(Modifier.isProtected(field.getModifiers()),
						"L'attribut " + field.getName() + " doit être protected pour respecter le diagramme UML (#)");
			}
		}
	}

	// =========================================================================
	// 3. Validation des invariants
	// =========================================================================

	@Nested
	@DisplayName("3. Validation des invariants")
	class ValidationInvariantsTests {

		@Test
		@DisplayName("Constructeur rejette un nom null ou blanc")
		void testConstructeurRejetteNomInvalide() {
			assertThrows(IllegalArgumentException.class, () -> new Canards(1, null, "Seigneur", 0.0));
			assertThrows(IllegalArgumentException.class, () -> new Canards(1, "", "Seigneur", 0.0));
			assertThrows(IllegalArgumentException.class, () -> new Canards(1, "   ", "Seigneur", 0.0));
		}

		@Test
		@DisplayName("Constructeur rejette un titre souverain null ou blanc")
		void testConstructeurRejetteTitreInvalide() {
			assertThrows(IllegalArgumentException.class, () -> new Gobelins(1, "Gobelins", null, 0.0));
			assertThrows(IllegalArgumentException.class, () -> new Gobelins(1, "Gobelins", "", 0.0));
			assertThrows(IllegalArgumentException.class, () -> new Gobelins(1, "Gobelins", "   ", 0.0));
		}

		@Test
		@DisplayName("Constructeur rejette une attraction null, négative ou infinie/NaN")
		void testConstructeurRejetteAttractionInvalide() {
			assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> new Teuteus(1, "Teuteus", "Roi", null)),
				() -> assertThrows(IllegalArgumentException.class, () -> new Teuteus(1, "Teuteus", "Roi", -0.01)),
				() -> assertThrows(IllegalArgumentException.class, () -> new Teuteus(1, "Teuteus", "Roi", Double.NaN)),
				() -> assertThrows(IllegalArgumentException.class, () -> new Teuteus(1, "Teuteus", "Roi", Double.POSITIVE_INFINITY)),
				() -> assertThrows(IllegalArgumentException.class, () -> new Teuteus(1, "Teuteus", "Roi", Double.NEGATIVE_INFINITY))
			);
		}

		@ParameterizedTest
		@ValueSource(ints = { 0, -1, -50, Integer.MIN_VALUE })
		@DisplayName("Constructeur rejette un id non positif")
		void testConstructeurRejetteIdNonPositif(int idInvalide) {
			assertThrows(IllegalArgumentException.class, () -> new Princesses(idInvalide, "Princesses", "Reine", 0.0));
		}

		@Test
		@DisplayName("Constructeur autorise un id null pour les entités non persistées")
		void testConstructeurAutoriseIdNull() {
			Canards canards = new Canards(null, "Canards", "Chef", 0.0);
			assertEquals(null, canards.getId());
		}

		@Test
		@DisplayName("Setters valident strictement leurs entrées")
		void testSettersValidation() {
			Population canards = new Canards(1, "Canards", "Chef", 0.0);

			assertThrows(IllegalArgumentException.class, () -> canards.setId(0));
			assertThrows(IllegalArgumentException.class, () -> canards.setId(-5));
			assertThrows(IllegalArgumentException.class, () -> canards.setNom(null));
			assertThrows(IllegalArgumentException.class, () -> canards.setNom("  "));
			assertThrows(IllegalArgumentException.class, () -> canards.setTitreSouverain(null));
			assertThrows(IllegalArgumentException.class, () -> canards.setTitreSouverain(""));
			assertThrows(IllegalArgumentException.class, () -> canards.setAttraction(null));
			assertThrows(IllegalArgumentException.class, () -> canards.setAttraction(-0.5));
			assertThrows(IllegalArgumentException.class, () -> canards.setCapaciteSpeciale("   "));

			// Modifications valides
			canards.setId(10);
			canards.setNom("Super Canards");
			canards.setTitreSouverain("Grand Duc");
			canards.setAttraction(0.10);
			canards.setCapaciteSpeciale("voler en v");

			assertEquals(10, canards.getId());
			assertEquals("Super Canards", canards.getNom());
			assertEquals("Grand Duc", canards.getTitreSouverain());
			assertEquals(0.10, canards.getAttraction());
			assertEquals("voler en v", canards.getCapaciteSpeciale());
		}
	}

	// =========================================================================
	// 4. Cycle Poule - Renard - Vipère
	// =========================================================================

	@Nested
	@DisplayName("4. Cycle Poule - Renard - Vipère (Canards > Teuteus > Licornes > Princesses > Gobelins > Canards)")
	class PouleRenardVipereTests {

		private Canards canards;
		private Teuteus teuteus;
		private Licornes licornes;
		private Princesses princesses;
		private Gobelins gobelins;

		@BeforeEach
		void setUp() {
			canards = new Canards(1, "Canards", "Archiduc", 0.05);
			teuteus = new Teuteus(2, "Teuteus", "Glouton", 0.0);
			licornes = new Licornes(3, "Licornes", "Impératrice", 0.15);
			princesses = new Princesses(4, "Princesses", "Reine", 0.20);
			gobelins = new Gobelins(5, "Gobelins", "Chef", 0.0);
		}

		@Test
		@DisplayName("1. Canards : avantage sur Teuteus (1.20), faiblesse face aux Gobelins (0.80), neutre sur les autres (1.00)")
		void testAvantagesCanards() {
			assertAll(
				() -> assertEquals(1.20, canards.getAvantageCombatContre(teuteus), 0.001),
				() -> assertEquals(0.80, canards.getAvantageCombatContre(gobelins), 0.001),
				() -> assertEquals(1.00, canards.getAvantageCombatContre(licornes), 0.001),
				() -> assertEquals(1.00, canards.getAvantageCombatContre(princesses), 0.001),
				() -> assertEquals(1.00, canards.getAvantageCombatContre(canards), 0.001),
				() -> assertEquals(1.00, canards.getAvantageCombatContre(null), 0.001)
			);
		}

		@Test
		@DisplayName("2. Teuteus : avantage sur Licornes (1.20), faiblesse face aux Canards (0.80), neutre sur les autres (1.00)")
		void testAvantagesTeuteus() {
			assertAll(
				() -> assertEquals(1.20, teuteus.getAvantageCombatContre(licornes), 0.001),
				() -> assertEquals(0.80, teuteus.getAvantageCombatContre(canards), 0.001),
				() -> assertEquals(1.00, teuteus.getAvantageCombatContre(princesses), 0.001),
				() -> assertEquals(1.00, teuteus.getAvantageCombatContre(gobelins), 0.001),
				() -> assertEquals(1.00, teuteus.getAvantageCombatContre(teuteus), 0.001)
			);
		}

		@Test
		@DisplayName("3. Licornes : avantage sur Princesses (1.20), faiblesse face aux Teuteus (0.80), neutre sur les autres (1.00)")
		void testAvantagesLicornes() {
			assertAll(
				() -> assertEquals(1.20, licornes.getAvantageCombatContre(princesses), 0.001),
				() -> assertEquals(0.80, licornes.getAvantageCombatContre(teuteus), 0.001),
				() -> assertEquals(1.00, licornes.getAvantageCombatContre(canards), 0.001),
				() -> assertEquals(1.00, licornes.getAvantageCombatContre(gobelins), 0.001),
				() -> assertEquals(1.00, licornes.getAvantageCombatContre(licornes), 0.001)
			);
		}

		@Test
		@DisplayName("4. Princesses : avantage sur Gobelins (1.20), faiblesse face aux Licornes (0.80), neutre sur les autres (1.00)")
		void testAvantagesPrincesses() {
			assertAll(
				() -> assertEquals(1.20, princesses.getAvantageCombatContre(gobelins), 0.001),
				() -> assertEquals(0.80, princesses.getAvantageCombatContre(licornes), 0.001),
				() -> assertEquals(1.00, princesses.getAvantageCombatContre(canards), 0.001),
				() -> assertEquals(1.00, princesses.getAvantageCombatContre(teuteus), 0.001),
				() -> assertEquals(1.00, princesses.getAvantageCombatContre(princesses), 0.001)
			);
		}

		@Test
		@DisplayName("5. Gobelins : avantage sur Canards (1.20), faiblesse face aux Princesses (0.80), neutre sur les autres (1.00)")
		void testAvantagesGobelins() {
			assertAll(
				() -> assertEquals(1.20, gobelins.getAvantageCombatContre(canards), 0.001),
				() -> assertEquals(0.80, gobelins.getAvantageCombatContre(princesses), 0.001),
				() -> assertEquals(1.00, gobelins.getAvantageCombatContre(teuteus), 0.001),
				() -> assertEquals(1.00, gobelins.getAvantageCombatContre(licornes), 0.001),
				() -> assertEquals(1.00, gobelins.getAvantageCombatContre(gobelins), 0.001)
			);
		}
	}

	// =========================================================================
	// 5. Traits passifs et multiplicateurs
	// =========================================================================

	@Nested
	@DisplayName("5. Traits passifs et multiplicateurs propres à chaque peuple")
	class TraitsPassifsEtMultiplicateursTests {

		@Test
		@DisplayName("Canards : bonus attaque à 1.25, production neutre")
		void testTraitsCanards() {
			Canards canards = new Canards(1, "Canards", "Archiduc", 0.05);
			assertEquals(1.25, canards.getBonusAttaque());
			assertEquals(0.00, canards.getTauxPillage());
			assertEquals(1.00, canards.getMalusCombatAdversaire());
			assertEquals(1.00, canards.getMalusProductionAdversaire());
			assertEquals(1.00, canards.getBonusProduction(Ressource.BOIS));
			assertEquals(1.00, canards.getBonusProduction(Ressource.NOURRITURE));
			assertEquals(0, canards.getBonusCroissancePopulation());
		}

		@Test
		@DisplayName("Gobelins : taux de pillage à 0.05 (5%), autres multiplicateurs neutres")
		void testTraitsGobelins() {
			Gobelins gobelins = new Gobelins(2, "Gobelins", "Chef", 0.0);
			assertEquals(0.05, gobelins.getTauxPillage());
			assertEquals(1.00, gobelins.getBonusAttaque());
			assertEquals(1.00, gobelins.getMalusCombatAdversaire());
			assertEquals(1.00, gobelins.getBonusProduction(Ressource.OR));
		}

		@Test
		@DisplayName("Licornes : malus combat adversaire à 0.90 (-10%), autres neutres")
		void testTraitsLicornes() {
			Licornes licornes = new Licornes(3, "Licornes", "Impératrice", 0.15);
			assertEquals(0.90, licornes.getMalusCombatAdversaire());
			assertEquals(1.00, licornes.getBonusAttaque());
			assertEquals(0.00, licornes.getTauxPillage());
			assertEquals(1.00, licornes.getBonusProduction(Ressource.PIERRE));
		}

		@Test
		@DisplayName("Princesses : malus production adversaire à 0.70 (-30%), autres neutres")
		void testTraitsPrincesses() {
			Princesses princesses = new Princesses(4, "Princesses", "Reine", 0.20);
			assertEquals(0.70, princesses.getMalusProductionAdversaire());
			assertEquals(1.00, princesses.getBonusAttaque());
			assertEquals(1.00, princesses.getBonusProduction(Ressource.BOIS));
		}

		@Test
		@DisplayName("Teuteus : bonus nourriture à 1.50 (+50%) sur getBonusProduction(NOURRITURE) et +2 citoyens")
		void testTraitsTeuteus() {
			Teuteus teuteus = new Teuteus(5, "Teuteus", "Glouton", 0.0);
			assertEquals(1.50, teuteus.getBonusProduction(Ressource.NOURRITURE));
			assertEquals(1.00, teuteus.getBonusProduction(Ressource.BOIS));
			assertEquals(1.00, teuteus.getBonusProduction(Ressource.OR));
			assertEquals(2, teuteus.getBonusCroissancePopulation());
			assertEquals(1.00, teuteus.getBonusAttaque());
			assertEquals(0.00, teuteus.getTauxPillage());
		}
	}

	// =========================================================================
	// 6. Equals, HashCode et ToString
	// =========================================================================

	@Nested
	@DisplayName("6. Equals, HashCode et ToString")
	class EqualsHashCodeToStringTests {

		@Test
		@DisplayName("Equals et HashCode se basent sur l'id et le nom")
		void testEqualsEtHashCode() {
			Population p1 = new Canards(1, "Canards", "Duc", 0.05);
			Population p2 = new Canards(1, "Canards", "Grand Duc", 0.10);
			Population p3 = new Gobelins(2, "Gobelins", "Chef", 0.0);

			assertEquals(p1, p2);
			assertEquals(p1.hashCode(), p2.hashCode());
			assertNotEquals(p1, p3);
		}

		@Test
		@DisplayName("ToString contient le nom de la classe, l'id, le nom et le titre")
		void testToString() {
			Population p = new Licornes(3, "Licornes", "Impératrice", 0.15);
			String repr = p.toString();

			assertTrue(repr.contains("Licornes"));
			assertTrue(repr.contains("Impératrice"));
			assertTrue(repr.contains("0.15"));
		}
	}
}
