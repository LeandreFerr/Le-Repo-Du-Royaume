import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  ARCHETYPES_CARTE,
  CarteDto,
  createDefaultCarte,
  DEFAULT_CARTE_CENTRE_X,
  DEFAULT_CARTE_CENTRE_Y,
  DEFAULT_CARTE_HEIGHT,
  DEFAULT_CARTE_WIDTH,
  EMPLACEMENT_TERRAFORM_RADIUS,
  EmplacementCarteDto,
  MARKET_FREE_ZONE_RADIUS,
  MARKET_TERRAFORM_RADIUS,
} from '../models/carte.model';
import {
  createDefaultRoyaume,
  DEFAULT_HERALDIC_ROYAUMES,
  euclideanDistance,
  generateCircularPlacements,
  MAX_EMPLACEMENTS,
  MAX_RADIUS_JITTER,
  MAX_THETA_JITTER_FACTOR,
  MIN_EMPLACEMENTS,
  R_BASE,
  togglePlacementOccupe,
  updatePlacementCoords,
} from './placement-royaumes.util';
import { createSeededRng } from './prng.util';

function wrapAngleSigned(rad: number): number {
  const a = ((((rad + Math.PI) % (2 * Math.PI)) + 2 * Math.PI) % (2 * Math.PI)) - Math.PI;
  return a === -Math.PI ? Math.PI : a;
}

function wrapAnglePositive(rad: number): number {
  return ((rad % (2 * Math.PI)) + 2 * Math.PI) % (2 * Math.PI);
}

describe('carte.model defaults', () => {
  it('exposes R2/R3 dimension and radius constants', () => {
    expect(DEFAULT_CARTE_WIDTH).toBe(1000);
    expect(DEFAULT_CARTE_HEIGHT).toBe(1000);
    expect(DEFAULT_CARTE_CENTRE_X).toBe(500);
    expect(DEFAULT_CARTE_CENTRE_Y).toBe(500);
    expect(MARKET_TERRAFORM_RADIUS).toBe(75);
    expect(EMPLACEMENT_TERRAFORM_RADIUS).toBe(85);
    expect(MARKET_FREE_ZONE_RADIUS).toBe(110);
    expect(ARCHETYPES_CARTE.map((a) => a.id)).toEqual([
      'VALLEE_ROYALE',
      'FORET_NOIRE',
      'HAUTES_TERRES',
      'CONFLUENCES',
      'LAC_CENTRAL',
    ]);
  });

  it('createDefaultCarte() defaults to 1000x1000 centered at (500, 500)', () => {
    const emptyCarte = createDefaultCarte();
    expect(emptyCarte).toEqual({
      largeur: 1000,
      hauteur: 1000,
      centreX: 500,
      centreY: 500,
      emplacements: [],
    });

    const inputEmps: EmplacementCarteDto[] = [
      { numero: 1, coordX: 840, coordY: 500, occupe: false },
    ];
    const clonedCarte = createDefaultCarte(inputEmps);
    expect(clonedCarte.emplacements).toEqual(inputEmps);
    expect(clonedCarte.emplacements).not.toBe(inputEmps);
  });
});

describe('placement-royaumes.util', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('exports exact R2 placement constants', () => {
    expect(R_BASE).toBe(340);
    expect(MIN_EMPLACEMENTS).toBe(1);
    expect(MAX_EMPLACEMENTS).toBe(10);
    expect(MAX_THETA_JITTER_FACTOR).toBe(0.22);
    expect(MAX_RADIUS_JITTER).toBe(35);
  });

  describe('generateCircularPlacements validation', () => {
    it('throws RangeError for invalid placement counts', () => {
      for (const badCount of [0, -1, 11, 2.5, Number.NaN, Number.POSITIVE_INFINITY]) {
        expect(() => generateCircularPlacements(badCount, 'seed-1')).toThrow(RangeError);
      }
    });

    it('throws RangeError for invalid dimensions or center options', () => {
      expect(() => generateCircularPlacements(4, 'seed-1', undefined, { largeur: 0 })).toThrow(
        RangeError,
      );
      expect(() => generateCircularPlacements(4, 'seed-1', undefined, { hauteur: -100 })).toThrow(
        RangeError,
      );
      expect(() =>
        generateCircularPlacements(4, 'seed-1', undefined, { centreX: Number.NaN }),
      ).toThrow(RangeError);
      expect(() =>
        generateCircularPlacements(4, 'seed-1', undefined, { centreY: Number.POSITIVE_INFINITY }),
      ).toThrow(RangeError);
    });
  });

  describe('determinism and zero Math.random()', () => {
    it('produces identical placements for identical (count, seed) and diverges for different seeds', () => {
      const randomSpy = vi.spyOn(Math, 'random');

      const a1 = generateCircularPlacements(6, 'seed-royaume');
      const a2 = generateCircularPlacements(6, 'seed-royaume');
      const b = generateCircularPlacements(6, 'seed-autre');

      expect(a1).toEqual(a2);
      expect(a1).not.toEqual(b);
      expect(randomSpy).not.toHaveBeenCalled();
    });
  });

  describe('circular noisy geometry bounds for N = 1..10', () => {
    const seeds = ['royaume-1', 42, '', 'Épée-🏰'];
    const epsAngMax = Math.asin(Math.SQRT1_2 / (R_BASE - MAX_RADIUS_JITTER));

    it('satisfies count, numero, integer clamping, radial bounds, and angular bounds for all N = 1..10', () => {
      for (const seed of seeds) {
        const expectedTheta0 = createSeededRng(seed, 'placement')() * 2 * Math.PI;

        for (let n = MIN_EMPLACEMENTS; n <= MAX_EMPLACEMENTS; n++) {
          const placements = generateCircularPlacements(n, seed);
          expect(placements).toHaveLength(n);

          const angles: number[] = [];

          for (let i = 0; i < n; i++) {
            const emp = placements[i];
            expect(emp.numero).toBe(i + 1);
            expect(Number.isInteger(emp.coordX)).toBe(true);
            expect(Number.isInteger(emp.coordY)).toBe(true);
            expect(Object.is(emp.coordX, -0)).toBe(false);
            expect(Object.is(emp.coordY, -0)).toBe(false);
            expect(emp.coordX).toBeGreaterThanOrEqual(0);
            expect(emp.coordX).toBeLessThanOrEqual(DEFAULT_CARTE_WIDTH);
            expect(emp.coordY).toBeGreaterThanOrEqual(0);
            expect(emp.coordY).toBeLessThanOrEqual(DEFAULT_CARTE_HEIGHT);

            const dx = emp.coordX - DEFAULT_CARTE_CENTRE_X;
            const dy = emp.coordY - DEFAULT_CARTE_CENTRE_Y;
            const d = Math.hypot(dx, dy);
            expect(d).toBeGreaterThanOrEqual(R_BASE - MAX_RADIUS_JITTER - Math.SQRT1_2);
            expect(d).toBeLessThanOrEqual(R_BASE + MAX_RADIUS_JITTER + Math.SQRT1_2);

            const phi = Math.atan2(dy, dx);
            angles.push(phi);

            // Exact PRNG theta0 verification
            const nominal = (2 * Math.PI * i) / n + expectedTheta0;
            const diffFromExpected = Math.abs(wrapAngleSigned(phi - nominal));
            expect(diffFromExpected).toBeLessThanOrEqual(
              (MAX_THETA_JITTER_FACTOR * Math.PI) / n + epsAngMax,
            );
          }

          // Black-box theta0 reconstruction independent of PRNG internals
          const c = angles.map((phi, i) => wrapAngleSigned(phi - (2 * Math.PI * i) / n));
          const unwrapped = c.map((ci) => c[0] + wrapAngleSigned(ci - c[0]));
          const theta0Est = (Math.min(...unwrapped) + Math.max(...unwrapped)) / 2;

          for (let i = 0; i < n; i++) {
            const dev = Math.abs(
              wrapAngleSigned(angles[i] - ((2 * Math.PI * i) / n + theta0Est)),
            );
            expect(dev).toBeLessThanOrEqual(
              (MAX_THETA_JITTER_FACTOR * Math.PI) / n + epsAngMax,
            );
          }

          // Monotonic cyclic spacing for N >= 2 (no overlap)
          if (n >= 2) {
            const jitterAllowance = 2 * ((MAX_THETA_JITTER_FACTOR * Math.PI) / n) + 2 * epsAngMax;
            const minExpectedStep = (2 * Math.PI) / n - jitterAllowance;
            const maxExpectedStep = (2 * Math.PI) / n + jitterAllowance;
            expect(minExpectedStep).toBeGreaterThan(0);
            expect(maxExpectedStep).toBeLessThan(2 * Math.PI);

            for (let i = 0; i < n; i++) {
              const nextIdx = (i + 1) % n;
              const step = wrapAnglePositive(angles[nextIdx] - angles[i]);
              expect(step).toBeGreaterThanOrEqual(minExpectedStep);
              expect(step).toBeLessThanOrEqual(maxExpectedStep);
            }
          }
        }
      }
    });

    it('clamps coordinates to custom viewport bounds', () => {
      const placements = generateCircularPlacements(8, 'tight-map', undefined, {
        largeur: 400,
        hauteur: 400,
        centreX: 200,
        centreY: 200,
      });

      expect(placements).toHaveLength(8);
      for (const emp of placements) {
        expect(Number.isInteger(emp.coordX)).toBe(true);
        expect(Number.isInteger(emp.coordY)).toBe(true);
        expect(emp.coordX).toBeGreaterThanOrEqual(0);
        expect(emp.coordX).toBeLessThanOrEqual(400);
        expect(emp.coordY).toBeGreaterThanOrEqual(0);
        expect(emp.coordY).toBeLessThanOrEqual(400);
      }
    });
  });

  describe('existing state preservation', () => {
    it('preserves occupe, royaume, and id by numero across count and seed changes without mutating input', () => {
      const initial: EmplacementCarteDto[] = [
        {
          id: 101,
          numero: 1,
          coordX: 100,
          coordY: 100,
          occupe: true,
          royaume: {
            id: 11,
            nom: 'Couronne Custom',
            couleurPrimaire: '#112233',
            couleurSecondaire: '#ddeeff',
            niveau: 4,
          },
        },
        {
          id: 102,
          numero: 2,
          coordX: 200,
          coordY: 200,
          occupe: false,
        },
        {
          id: 103,
          numero: 3,
          coordX: 300,
          coordY: 300,
          occupe: true,
        },
      ];
      const snapshot = structuredClone(initial);

      const grown = generateCircularPlacements(5, 'seed-a', initial);
      expect(initial).toEqual(snapshot);
      expect(grown).toHaveLength(5);
      expect(grown[0].id).toBe(101);
      expect(grown[0].occupe).toBe(true);
      expect(grown[0].royaume).toEqual(initial[0].royaume);
      expect(grown[0].royaume).not.toBe(initial[0].royaume);
      expect(grown[1].id).toBe(102);
      expect(grown[1].occupe).toBe(false);
      expect(grown[1].royaume).toBeUndefined();
      expect(grown[2].id).toBe(103);
      expect(grown[2].occupe).toBe(true);
      expect(grown[2].royaume).toBeDefined();
      expect(grown[3].occupe).toBe(false);
      expect(grown[4].occupe).toBe(false);

      // Re-keying / shrinking from sparse/out-of-order array matches by numero, not array index
      const shrunk = generateCircularPlacements(2, 'seed-b', [grown[2], grown[0]]);
      expect(shrunk).toHaveLength(2);
      expect(shrunk[0].id).toBe(101);
      expect(shrunk[0].royaume?.nom).toBe('Couronne Custom');
      expect(shrunk[1].numero).toBe(2);
      expect(shrunk[1].id).toBeUndefined();
      expect(shrunk[1].occupe).toBe(false);
    });
  });

  describe('default heraldic palettes', () => {
    it('defines 10 distinct readable kingdoms with unique names and #RRGGBB colors', () => {
      expect(DEFAULT_HERALDIC_ROYAUMES).toHaveLength(10);

      const names = new Set<string>();
      const primaryColors = new Set<string>();
      const hexRegex = /^#[0-9a-fA-F]{6}$/;

      for (const royaume of DEFAULT_HERALDIC_ROYAUMES) {
        expect(royaume.nom.trim().length).toBeGreaterThan(0);
        expect(royaume.couleurPrimaire).toMatch(hexRegex);
        expect(royaume.couleurSecondaire).toMatch(hexRegex);
        expect(royaume.couleurPrimaire.toLowerCase()).not.toBe(
          royaume.couleurSecondaire.toLowerCase(),
        );
        expect(royaume.niveau).toBe(1);
        names.add(royaume.nom);
        primaryColors.add(royaume.couleurPrimaire.toLowerCase());
      }

      expect(names.size).toBe(10);
      expect(primaryColors.size).toBe(10);

      const copyA = createDefaultRoyaume(1);
      const copyB = createDefaultRoyaume(1);
      copyA.nom = 'Mutated';
      expect(copyB.nom).toBe('Valdore');
      expect(createDefaultRoyaume(11).nom).toBe('Valdore');
    });
  });

  describe('updatePlacementCoords', () => {
    it('immutably rounds, eliminates -0, clamps to [0, largeur] x [0, hauteur], and guards NaN', () => {
      const carte: CarteDto = createDefaultCarte([
        { numero: 1, coordX: 300, coordY: 400, occupe: false },
        { numero: 2, coordX: 700, coordY: 600, occupe: true, royaume: createDefaultRoyaume(2) },
      ]);
      const original = structuredClone(carte);

      const clamped = updatePlacementCoords(carte, 1, -12.7, 1045.4);
      expect(carte).toEqual(original);
      expect(clamped).not.toBe(carte);
      expect(clamped.emplacements[0].coordX).toBe(0);
      expect(Object.is(clamped.emplacements[0].coordX, -0)).toBe(false);
      expect(clamped.emplacements[0].coordY).toBe(1000);

      const negZero = updatePlacementCoords(carte, 1, -0.2, 250.6);
      expect(Object.is(negZero.emplacements[0].coordX, -0)).toBe(false);
      expect(negZero.emplacements[0].coordX).toBe(0);
      expect(negZero.emplacements[0].coordY).toBe(251);

      const nanGuarded = updatePlacementCoords(carte, 2, Number.NaN, Number.POSITIVE_INFINITY);
      expect(nanGuarded.emplacements[1].coordX).toBe(700);
      expect(nanGuarded.emplacements[1].coordY).toBe(600);

      const unknown = updatePlacementCoords(carte, 99, 123, 456);
      expect(unknown).not.toBe(carte);
      expect(unknown.emplacements).toEqual(carte.emplacements);
    });
  });

  describe('togglePlacementOccupe', () => {
    it('immutably toggles occupe, assigns default royaume on false -> true, and preserves custom royaume', () => {
      const carte: CarteDto = createDefaultCarte([
        { numero: 1, coordX: 250, coordY: 300, occupe: false },
        {
          numero: 2,
          coordX: 750,
          coordY: 700,
          occupe: true,
          royaume: {
            nom: 'Royaume Personnel',
            couleurPrimaire: '#445566',
            couleurSecondaire: '#ffffff',
            niveau: 7,
          },
        },
      ]);
      const original = structuredClone(carte);

      const occupied = togglePlacementOccupe(carte, 1, true);
      expect(carte).toEqual(original);
      expect(occupied.emplacements[0].occupe).toBe(true);
      expect(occupied.emplacements[0].royaume).toEqual(createDefaultRoyaume(1));

      // Flip true -> false -> true on custom kingdom preserves custom royaume
      const vacated = togglePlacementOccupe(occupied, 2);
      expect(vacated.emplacements[1].occupe).toBe(false);
      expect(vacated.emplacements[1].royaume?.nom).toBe('Royaume Personnel');

      const reoccupied = togglePlacementOccupe(vacated, 2);
      expect(reoccupied.emplacements[1].occupe).toBe(true);
      expect(reoccupied.emplacements[1].royaume?.nom).toBe('Royaume Personnel');
      expect(reoccupied.emplacements[1].royaume?.niveau).toBe(7);
    });
  });

  describe('euclideanDistance', () => {
    it('computes exact distances, symmetry, zero distance, and non-finite guards', () => {
      expect(euclideanDistance(0, 0, 3, 4)).toBe(5);
      expect(euclideanDistance(100, 200, 100, 200)).toBe(0);
      expect(euclideanDistance(120, 340, 500, 500)).toBe(euclideanDistance(500, 500, 120, 340));
      expect(euclideanDistance(Number.NaN, 0, 10, 10)).toBe(0);
      expect(euclideanDistance(0, 0, Number.POSITIVE_INFINITY, 10)).toBe(0);
    });
  });
});
