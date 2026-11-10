package royaume.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import royaume.config.ConfigurationPaliers;

@DisplayName("Tests unitaires complets de PalierNiveau et montée de niveau (Module 3)")
public class PalierNiveauTest {

	private static final Population PEUPLE_TEST = new Canards(1, "Canards", "Archiduc", 0.05);

	private Royaume royaume;
	private List<PalierNiveau> paliers;

	@BeforeEach
	void setUp() {
		paliers = ConfigurationPaliers.getPaliers();

		List<QuantiteRessource> ressources = new ArrayList<>(List.of(
			new QuantiteRessource(1000, Ressource.OR),
			new QuantiteRessource(1000, Ressource.PIERRE),
			new QuantiteRessource(1000, Ressource.BOIS),
			new QuantiteRessource(7000, Ressource.NOURRITURE)
		));

		royaume = new Royaume(
			1, "Gondor", PEUPLE_TEST, 1,
			500, 500, 0, 100,
			false, ressources
		);
	}

	// =========================================================================
	// 1. Configuration des paliers
	// =========================================================================

	@Nested
	@DisplayName("1. Configuration des paliers")
	class ConfigurationPaliersTests {

		@Test
		@DisplayName("La configuration contient exactement 9 paliers pour les niveaux 2 à 10")
		void testConfigurationPaliers() {
			assertNotNull(paliers);
			assertEquals(9, paliers.size());

			for (int i = 0; i < paliers.size(); i++) {
				assertEquals(i + 2, paliers.get(i).getNiveau(),
						"Le palier d'indice " + i + " doit cibler le niveau " + (i + 2));
			}
		}

		@Test
		@DisplayName("La liste des paliers est non modifiable")
		void testListePaliersImmuable() {
			assertThrows(UnsupportedOperationException.class, () -> paliers.add(null));
		}
	}

	// =========================================================================
	// 2. Coût et calculs des ressources requises
	// =========================================================================

	@Nested
	@DisplayName("2. Coûts des ressources et ressources manquantes")
	class CoutsEtRessourcesManquantesTests {

		@Test
		@DisplayName("getCoutRessource retourne les coûts exigés du premier palier (niveau 2)")
		void testCoutRessourcesPremierPalier() {
			assertAll(
				() -> assertEquals(100, PalierNiveau.getCoutRessource(Ressource.OR, royaume, paliers)),
				() -> assertEquals(80, PalierNiveau.getCoutRessource(Ressource.PIERRE, royaume, paliers)),
				() -> assertEquals(60, PalierNiveau.getCoutRessource(Ressource.BOIS, royaume, paliers)),
				() -> assertEquals(90, PalierNiveau.getCoutRessource(Ressource.NOURRITURE, royaume, paliers))
			);
		}

		@Test
		@DisplayName("getCoutRessource retourne les coûts du deuxième palier (niveau 3)")
		void testCoutRessourcesDeuxiemePalier() {
			royaume.setNiveau(2);

			assertAll(
				() -> assertEquals(120, PalierNiveau.getCoutRessource(Ressource.OR, royaume, paliers)),
				() -> assertEquals(100, PalierNiveau.getCoutRessource(Ressource.PIERRE, royaume, paliers)),
				() -> assertEquals(100, PalierNiveau.getCoutRessource(Ressource.BOIS, royaume, paliers)),
				() -> assertEquals(1300, PalierNiveau.getCoutRessource(Ressource.NOURRITURE, royaume, paliers))
			);
		}

		@Test
		@DisplayName("getCoutRessource retourne 0 au niveau maximum (niveau 10)")
		void testCoutRessourceNiveauMaximum() {
			royaume.setNiveau(10);
			assertEquals(0, PalierNiveau.getCoutRessource(Ressource.OR, royaume, paliers));
		}

		@Test
		@DisplayName("getCoutRessource retourne 0 si la ressource est absente du palier")
		void testCoutRessourceAbsente() {
			List<PalierNiveau> palierPersonnalise = List.of(
				new PalierNiveau(1, 2, 100, List.of(new QuantiteRessource(100, Ressource.OR)))
			);
			assertEquals(0, PalierNiveau.getCoutRessource(Ressource.BOIS, royaume, palierPersonnalise));
		}

		@Test
		@DisplayName("getRessourceManquante retourne 0 si le stock du royaume est suffisant")
		void testAucuneRessourceManquante() {
			assertEquals(0, PalierNiveau.getRessourceManquante(Ressource.OR, royaume, paliers));
			assertEquals(0, PalierNiveau.getRessourceManquante(Ressource.PIERRE, royaume, paliers));
		}

		@Test
		@DisplayName("getRessourceManquante calcule exactement la quantité manquante")
		void testRessourcesManquantes() {
			// Le palier 2 exige 100 OR. On réduit le stock d'or à 50.
			royaume.debiterRessources(Ressource.OR, 950);

			int manqueOr = PalierNiveau.getRessourceManquante(Ressource.OR, royaume, paliers);
			assertEquals(50, manqueOr);
		}

		@Test
		@DisplayName("getRessourceManquante retourne le coût total si le stock de cette ressource est nul")
		void testRessourceManquanteStockNul() {
			royaume.debiterRessources(Ressource.OR, 1000);

			assertEquals(100, PalierNiveau.getRessourceManquante(Ressource.OR, royaume, paliers));
		}

		@Test
		@DisplayName("Méthodes utilitaires statiques sont null-safe")
		void testMethodesStatiquesNullSafe() {
			assertAll(
				() -> assertEquals(0, PalierNiveau.getCoutRessource(null, royaume, paliers)),
				() -> assertEquals(0, PalierNiveau.getCoutRessource(Ressource.OR, null, paliers)),
				() -> assertEquals(0, PalierNiveau.getCoutRessource(Ressource.OR, royaume, null)),
				() -> assertEquals(0, PalierNiveau.getRessourceManquante(null, royaume, paliers)),
				() -> assertEquals(0, PalierNiveau.getRessourceManquante(Ressource.OR, null, paliers)),
				() -> assertEquals(0, PalierNiveau.getRessourceManquante(Ressource.OR, royaume, null))
			);
		}
	}

	// =========================================================================
	// 3. Conditions de montée de niveau (peutMonterDeNiveau)
	// =========================================================================

	@Nested
	@DisplayName("3. Conditions de montée de niveau")
	class PeutMonterDeNiveauTests {

		@Test
		@DisplayName("peutMonterDeNiveau retourne true quand population et ressources suffisent")
		void testPeutMonterDeNiveauNominal() {
			assertTrue(royaume.peutMonterDeNiveau(paliers));
		}

		@Test
		@DisplayName("peutMonterDeNiveau retourne false si la population totale est insuffisante")
		void testPopulationInsuffisante() {
			royaume.setPopulationDisponible(10);
			royaume.setPopulationOuvriere(10);
			royaume.setPopulationMilitaire(0);
			royaume.setPopulationEnEntrainement(0);

			// Total = 20 < 100 exigé au palier 2
			assertFalse(royaume.peutMonterDeNiveau(paliers));
		}

		@Test
		@DisplayName("peutMonterDeNiveau retourne false si une ressource est insuffisante")
		void testRessourceInsuffisante() {
			// Palier 2 exige 100 OR. On réduit l'or à 10.
			royaume.debiterRessources(Ressource.OR, 990);

			assertFalse(royaume.peutMonterDeNiveau(paliers));
		}

		@Test
		@DisplayName("peutMonterDeNiveau retourne false au niveau maximum (10)")
		void testPeutMonterNiveauMaximum() {
			royaume.setNiveau(10);
			assertFalse(royaume.peutMonterDeNiveau(paliers));
		}

		@Test
		@DisplayName("Règle métier : un royaume en ruine ne peut jamais monter de niveau")
		void testRoyaumeEnRuineNePeutPasMonter() {
			royaume.setEnRuine(true);
			assertFalse(royaume.peutMonterDeNiveau(paliers));
		}

		@Test
		@DisplayName("peutMonterDeNiveau retourne false si la liste des paliers est null ou vide")
		void testPaliersNullOuVide() {
			assertAll(
				() -> assertFalse(royaume.peutMonterDeNiveau(null)),
				() -> assertFalse(royaume.peutMonterDeNiveau(new ArrayList<>()))
			);
		}

		@Test
		@DisplayName("peutMonterDeNiveau valide le seuil frontière exact de population (99 refuse, 100 accepte)")
		void testFrontierePopulationRequise() {
			// Given : Palier 2 exige exactement 100 habitants
			// Cas N - 1 (99 habitants)
			royaume.setPopulationDisponible(99);
			royaume.setPopulationOuvriere(0);
			royaume.setPopulationMilitaire(0);
			royaume.setPopulationEnEntrainement(0);

			// Then
			assertFalse(royaume.peutMonterDeNiveau(paliers), "99 habitants doit être refusé pour un seuil à 100");

			// When : Cas N (100 habitants exacts)
			royaume.setPopulationDisponible(100);

			// Then
			assertTrue(royaume.peutMonterDeNiveau(paliers), "100 habitants pile doit être accepté");
		}

		@Test
		@DisplayName("peutMonterDeNiveau prend en compte toutes les catégories de population (ouvriers et militaires)")
		void testPopulationRepartieOuvriersEtMilitaires() {
			// Given : 0 population disponible, mais 50 ouvriers et 50 militaires (= 100 habitants au total)
			royaume.setPopulationDisponible(0);
			royaume.setPopulationOuvriere(50);
			royaume.setPopulationMilitaire(50);
			royaume.setPopulationEnEntrainement(0);

			// Then
			assertTrue(royaume.peutMonterDeNiveau(paliers),
					"La population active au travail et à l'armée doit être comptabilisée dans la population totale");
		}
	}

	// =========================================================================
	// 4. Exécution de l'amélioration de niveau (ameliorerNiveau)
	// =========================================================================

	@Nested
	@DisplayName("4. Amélioration de niveau et débits atomiques")
	class AmeliorerNiveauTests {

		@Test
		@DisplayName("ameliorerNiveau retourne true et incrémente le niveau de 1 à 2")
		void testAmeliorerNiveauNominal() {
			boolean succes = royaume.ameliorerNiveau(paliers);

			assertTrue(succes);
			assertEquals(2, royaume.getNiveau());
		}

		@Test
		@DisplayName("ameliorerNiveau débite exactement les ressources prévues")
		void testAmeliorerNiveauDebiteRessources() {
			int orInitial = 1000;
			int pierreInitiale = 1000;
			int boisInitial = 1000;
			int nourritureInitiale = 7000;

			// Palier 2 : 100 Or, 80 Pierre, 60 Bois, 90 Nourriture
			boolean succes = royaume.ameliorerNiveau(paliers);

			assertTrue(succes);
			assertAll(
				() -> assertEquals(orInitial - 100, getStock(Ressource.OR)),
				() -> assertEquals(pierreInitiale - 80, getStock(Ressource.PIERRE)),
				() -> assertEquals(boisInitial - 60, getStock(Ressource.BOIS)),
				() -> assertEquals(nourritureInitiale - 90, getStock(Ressource.NOURRITURE))
			);
		}

		@Test
		@DisplayName("ameliorerNiveau fonctionne lorsque les stocks correspondent exactement aux coûts requis (solde final à 0)")
		void testAmeliorerNiveauStocksExactsTombeAZero() {
			// Given : Coûts stricts du palier 2 : 100 Or, 80 Pierre, 60 Bois, 90 Nourriture
			royaume.setRessources(List.of(
				new QuantiteRessource(100, Ressource.OR),
				new QuantiteRessource(80, Ressource.PIERRE),
				new QuantiteRessource(60, Ressource.BOIS),
				new QuantiteRessource(90, Ressource.NOURRITURE)
			));

			// When
			boolean succes = royaume.ameliorerNiveau(paliers);

			// Then
			assertTrue(succes, "Le passage de niveau doit réussir si le stock égale pile le coût requis");
			assertEquals(2, royaume.getNiveau());
			assertAll(
				() -> assertEquals(0, getStock(Ressource.OR), "Le stock d'or doit tomber pile à 0"),
				() -> assertEquals(0, getStock(Ressource.PIERRE), "Le stock de pierre doit tomber pile à 0"),
				() -> assertEquals(0, getStock(Ressource.BOIS), "Le stock de bois doit tomber pile à 0"),
				() -> assertEquals(0, getStock(Ressource.NOURRITURE), "Le stock de nourriture doit tomber pile à 0")
			);
		}

		@Test
		@DisplayName("ameliorerNiveau retourne false et ne modifie rien si les conditions ne sont pas réunies")
		void testAmeliorerNiveauRefuse() {
			royaume.debiterRessources(Ressource.OR, 990); // Reste 10 Or < 100 exigé

			boolean succes = royaume.ameliorerNiveau(paliers);

			assertFalse(succes);
			assertEquals(1, royaume.getNiveau());
			assertEquals(10, getStock(Ressource.OR));
		}

		@Test
		@DisplayName("ameliorerNiveau retourne false si le royaume est en ruine")
		void testAmeliorerNiveauRefuseSiEnRuine() {
			royaume.setEnRuine(true);

			boolean succes = royaume.ameliorerNiveau(paliers);

			assertFalse(succes);
			assertEquals(1, royaume.getNiveau());
		}

		@Test
		@DisplayName("Un royaume peut évoluer successivement jusqu'à épuisement des conditions")
		void testEvolutionSuccessive() {
			while (royaume.peutMonterDeNiveau(paliers)) {
				boolean succes = royaume.ameliorerNiveau(paliers);
				assertTrue(succes);
			}

			assertTrue(royaume.getNiveau() <= 10);
			assertFalse(royaume.peutMonterDeNiveau(paliers));
		}

		private int getStock(Ressource ressource) {
			return royaume.getRessources().stream()
					.filter(r -> r.getRessource() == ressource)
					.mapToInt(QuantiteRessource::getQuantite)
					.findFirst()
					.orElse(0);
		}
	}

	// =========================================================================
	// 5. Invariants, Encapsulation & POJO de PalierNiveau
	// =========================================================================

	@Nested
	@DisplayName("5. Invariants et encapsulation de PalierNiveau")
	class InvariantsPalierNiveauTests {

		@Test
		@DisplayName("Constructeur par défaut (JPA) instancie avec valeurs saines")
		void testConstructeurParDefautJPA() {
			PalierNiveau p = new PalierNiveau();
			assertAll(
				() -> assertNull(p.getId()),
				() -> assertEquals(0, p.getNiveau()),
				() -> assertEquals(0, p.getPopulationRequise()),
				() -> assertNotNull(p.getPrerequis()),
				() -> assertTrue(p.getPrerequis().isEmpty())
			);
		}

		@ParameterizedTest
		@ValueSource(ints = { 0, -1, -50 })
		@DisplayName("checkId rejette les id non positifs")
		void testIdInvalide(int idInvalide) {
			assertThrows(IllegalArgumentException.class,
				() -> new PalierNiveau(idInvalide, 2, 100, List.of()));
		}

		@ParameterizedTest
		@ValueSource(ints = { 0, 1, 11, 25, -1 })
		@DisplayName("checkNiveau rejette les niveaux hors [2, 10]")
		void testNiveauInvalide(int niveauInvalide) {
			assertThrows(IllegalArgumentException.class,
				() -> new PalierNiveau(1, niveauInvalide, 100, List.of()));
		}

		@ParameterizedTest
		@ValueSource(ints = { -1, -10, -500 })
		@DisplayName("checkPopulationRequise rejette les populations négatives")
		void testPopulationRequiseNegative(int popInvalide) {
			assertThrows(IllegalArgumentException.class,
				() -> new PalierNiveau(1, 2, popInvalide, List.of()));
		}

		@Test
		@DisplayName("setPrerequis rejette une liste avec élément null ou doublon de ressource")
		void testPrerequisValidation() {
			List<QuantiteRessource> avecNull = new ArrayList<>();
			avecNull.add(new QuantiteRessource(10, Ressource.OR));
			avecNull.add(null);

			List<QuantiteRessource> avecDoublon = List.of(
				new QuantiteRessource(10, Ressource.OR),
				new QuantiteRessource(20, Ressource.OR)
			);

			PalierNiveau p = new PalierNiveau(1, 2, 100, List.of());
			assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> p.setPrerequis(avecNull)),
				() -> assertThrows(IllegalArgumentException.class, () -> p.setPrerequis(avecDoublon))
			);
		}

		@Test
		@DisplayName("getPrerequis retourne une vue protégée contre les mutations directes")
		void testEncapsulationDefensivePrerequis() {
			PalierNiveau p = new PalierNiveau(1, 2, 100, List.of(new QuantiteRessource(50, Ressource.OR)));
			assertThrows(UnsupportedOperationException.class,
				() -> p.getPrerequis().add(new QuantiteRessource(10, Ressource.BOIS)));
		}

		@Test
		@DisplayName("Equals et HashCode respectent le contrat POJO")
		void testEqualsEtHashCode() {
			PalierNiveau p1 = new PalierNiveau(1, 2, 100, List.of());
			PalierNiveau p2 = new PalierNiveau(1, 2, 250, List.of());
			PalierNiveau p3 = new PalierNiveau(2, 3, 100, List.of());

			assertEquals(p1, p2);
			assertEquals(p1.hashCode(), p2.hashCode());
			assertFalse(p1.equals(p3));
		}
	}
}
