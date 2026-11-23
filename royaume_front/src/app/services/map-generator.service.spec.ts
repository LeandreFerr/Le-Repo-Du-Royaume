import { TestBed } from '@angular/core/testing';
import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import {
  ArchetypeCarte,
  BaseTerrainMesh,
  CarteDto,
  EMPLACEMENT_TERRAFORM_RADIUS,
  EmplacementCarteDto,
  GeneratedMap,
  MARKET_FREE_ZONE_RADIUS,
  MARKET_TERRAFORM_RADIUS,
  Point2D,
  createDefaultCarte,
} from '../models/carte.model';
import {
  EMPLACEMENT_FEATURE_EXCLUSION_RADIUS,
  MapGeneratorService,
  ROUTE_FEATURE_EXCLUSION_RADIUS,
  UNOCCUPIED_BORDER_COLOR,
  VORONOI_POINT_COUNT,
  classifyCellBiome,
} from './map-generator.service';
import {
  createDefaultRoyaume,
  euclideanDistance,
  generateCircularPlacements,
} from './placement-royaumes.util';

function shoelaceArea(poly: readonly Point2D[]): number {
  let sum = 0;
  for (let i = 0; i < poly.length; i++) {
    const a = poly[i];
    const b = poly[(i + 1) % poly.length];
    sum += a.x * b.y - b.x * a.y;
  }
  return Math.abs(sum) * 0.5;
}

function pointToSegmentDist(
  px: number,
  py: number,
  ax: number,
  ay: number,
  bx: number,
  by: number,
): number {
  const dx = bx - ax;
  const dy = by - ay;
  const lenSq = dx * dx + dy * dy;
  if (lenSq <= 1e-12) {
    return Math.hypot(px - ax, py - ay);
  }
  const t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / lenSq));
  return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
}

function isInsidePolygon(px: number, py: number, poly: readonly Point2D[]): boolean {
  let hasPos = false;
  let hasNeg = false;
  for (let i = 0; i < poly.length; i++) {
    const a = poly[i];
    const b = poly[(i + 1) % poly.length];
    const cross = (b.x - a.x) * (py - a.y) - (b.y - a.y) * (px - a.x);
    if (cross > 1e-6) {
      hasPos = true;
    } else if (cross < -1e-6) {
      hasNeg = true;
    }
    if (hasPos && hasNeg) {
      return false;
    }
  }
  return poly.length >= 3;
}

function findSegmentIntersection(
  a: Point2D,
  b: Point2D,
  c: Point2D,
  d: Point2D,
): Point2D | null {
  const rx = b.x - a.x;
  const ry = b.y - a.y;
  const sx = d.x - c.x;
  const sy = d.y - c.y;
  const denom = rx * sy - ry * sx;
  if (Math.abs(denom) < 1e-9) {
    return null;
  }
  const qpx = c.x - a.x;
  const qpy = c.y - a.y;
  const t = (qpx * sy - qpy * sx) / denom;
  const u = (qpx * ry - qpy * rx) / denom;
  if (t >= -1e-6 && t <= 1 + 1e-6 && u >= -1e-6 && u <= 1 + 1e-6) {
    const tc = Math.max(0, Math.min(1, t));
    return { x: a.x + tc * rx, y: a.y + tc * ry };
  }
  return null;
}

function polylineLength(poly: readonly Point2D[]): number {
  let len = 0;
  for (let i = 0; i < poly.length - 1; i++) {
    len += Math.hypot(poly[i + 1].x - poly[i].x, poly[i + 1].y - poly[i].y);
  }
  return len;
}

function buildTestCarte(
  count: number,
  seed: string | number,
  occupiedPattern: (numero: number) => boolean = (n) => n % 2 === 1,
): CarteDto {
  const raw = count > 0 ? generateCircularPlacements(count, seed) : [];
  const emplacements: EmplacementCarteDto[] = raw.map((emp) => {
    const occupe = occupiedPattern(emp.numero);
    return {
      ...emp,
      occupe,
      ...(occupe ? { royaume: { ...createDefaultRoyaume(emp.numero), niveau: (emp.numero % 6) + 1 } } : {}),
    };
  });
  return createDefaultCarte(emplacements);
}

describe('MapGeneratorService', () => {
  let service: MapGeneratorService;

  beforeAll(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MapGeneratorService);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('DI, validation & zero Math.random() guarantee', () => {
    it('resolves via TestBed.inject(MapGeneratorService)', () => {
      expect(service).toBeInstanceOf(MapGeneratorService);
    });

    it('throws RangeError for invalid dimensions or unsupported archetype', () => {
      expect(() => service.generateBaseTerrain('seed', 'VALLEE_ROYALE', 0, 1000)).toThrow(RangeError);
      expect(() => service.generateBaseTerrain('seed', 'VALLEE_ROYALE', 1000, -5)).toThrow(RangeError);
      expect(() => service.generateBaseTerrain('seed', 'VALLEE_ROYALE', Number.NaN, 1000)).toThrow(
        RangeError,
      );
      expect(() =>
        service.generateBaseTerrain('seed', 'INVALID_ARCHETYPE' as ArchetypeCarte),
      ).toThrow(RangeError);
    });

    it('never calls Math.random() during generateBaseTerrain, populateMapDomains, or generateMap', () => {
      const randomSpy = vi.spyOn(Math, 'random');
      const carte = buildTestCarte(5, 'no-random-seed');

      const base = service.generateBaseTerrain('no-random-seed', 'VALLEE_ROYALE');
      service.populateMapDomains(base, carte);
      service.generateMap(carte, 'no-random-seed-2', 'CONFLUENCES');

      expect(randomSpy).not.toHaveBeenCalled();
    });
  });

  describe('Determinism & seed equivalence', () => {
    it('produces identical GeneratedMap for identical seed, archetype, and carte', () => {
      const carte = buildTestCarte(6, 'det-seed');
      const mapA = service.generateMap(carte, 'det-seed', 'FORET_NOIRE');
      const freshService = new MapGeneratorService();
      const mapB = freshService.generateMap(carte, 'det-seed', 'FORET_NOIRE');

      expect(mapA).toEqual(mapB);
    });

    it('normalizes numeric 42 === "42" and -0 === 0', () => {
      const baseNum = service.generateBaseTerrain(42, 'VALLEE_ROYALE');
      const baseStr = service.generateBaseTerrain('42', 'VALLEE_ROYALE');
      expect(baseNum).toEqual(baseStr);

      const baseNegZero = service.generateBaseTerrain(-0, 'HAUTES_TERRES');
      const basePosZero = service.generateBaseTerrain(0, 'HAUTES_TERRES');
      expect(baseNegZero).toEqual(basePosZero);
      expect(baseNegZero.seed).toBe('0');
    });

    it('produces distinct terrains and features for distinct seeds', () => {
      const carte = buildTestCarte(4, 'royaume-alpha');
      const mapA = service.generateMap(carte, 'royaume-alpha', 'VALLEE_ROYALE');
      const mapB = service.generateMap(carte, 'royaume-beta', 'VALLEE_ROYALE');

      const elevDiffs = mapA.cells.filter((c, idx) => c.elevation !== mapB.cells[idx].elevation).length;
      expect(elevDiffs).toBeGreaterThan(100);
      expect(mapA.features).not.toEqual(mapB.features);
    });
  });

  describe('Step 1: Continuous Voronoi / Lloyd mesh invariants', () => {
    let baseMesh: BaseTerrainMesh;

    beforeAll(() => {
      baseMesh = service.generateBaseTerrain('mesh-invariants', 'VALLEE_ROYALE');
    });

    it('generates 1400 cells with valid centers, unclosed positive-area polygons, and symmetric neighbors', () => {
      expect(baseMesh.cells.length).toBe(VORONOI_POINT_COUNT);

      for (let i = 0; i < baseMesh.cells.length; i++) {
        const cell = baseMesh.cells[i];
        expect(cell.id).toBe(i);
        expect(cell.center.x).toBeGreaterThan(0);
        expect(cell.center.x).toBeLessThan(baseMesh.width);
        expect(cell.center.y).toBeGreaterThan(0);
        expect(cell.center.y).toBeLessThan(baseMesh.height);

        // Unclosed polygon ring with positive shoelace area
        expect(cell.polygon.length).toBeGreaterThanOrEqual(3);
        const first = cell.polygon[0];
        const last = cell.polygon[cell.polygon.length - 1];
        expect(Math.hypot(first.x - last.x, first.y - last.y)).toBeGreaterThan(1e-6);
        expect(shoelaceArea(cell.polygon)).toBeGreaterThan(0);
        for (const v of cell.polygon) {
          expect(v.x).toBeGreaterThanOrEqual(-1e-6);
          expect(v.x).toBeLessThanOrEqual(baseMesh.width + 1e-6);
          expect(v.y).toBeGreaterThanOrEqual(-1e-6);
          expect(v.y).toBeLessThanOrEqual(baseMesh.height + 1e-6);
        }

        // Symmetric neighbor graph with no self-loops
        expect(cell.neighbors.length).toBeGreaterThanOrEqual(2);
        for (const nId of cell.neighbors) {
          expect(nId).not.toBe(cell.id);
          expect(nId).toBeGreaterThanOrEqual(0);
          expect(nId).toBeLessThan(baseMesh.cells.length);
          expect(baseMesh.cells[nId].neighbors).toContain(cell.id);
        }

        // Bounded fields and unpopulated domain defaults
        expect(Number.isFinite(cell.elevation)).toBe(true);
        expect(cell.elevation).toBeGreaterThanOrEqual(0);
        expect(cell.elevation).toBeLessThanOrEqual(1);
        expect(Number.isFinite(cell.moisture)).toBe(true);
        expect(cell.moisture).toBeGreaterThanOrEqual(0);
        expect(cell.moisture).toBeLessThanOrEqual(1);
        expect(cell.terraformed).toBe(false);
        expect(cell.isMarketZone).toBe(false);
        expect(cell.ownerNumero).toBeNull();
        expect(cell.geodesicDistance).toBe(Number.POSITIVE_INFINITY);
      }
    });

    it('exports classifyCellBiome covering all BiomeType outcomes consistently', () => {
      expect(
        classifyCellBiome(
          { elevation: 0.42, moisture: 0.5, isWater: false, terraformed: true },
          'VALLEE_ROYALE',
          { isMarketPlaza: true },
        ),
      ).toBe('PAVE_MARCHE');
      expect(
        classifyCellBiome(
          { elevation: 0.42, moisture: 0.8, isWater: false, terraformed: true },
          'FORET_NOIRE',
        ),
      ).toBe('PRAIRIE');
      expect(
        classifyCellBiome(
          { elevation: 0.1, moisture: 0.8, isWater: true, waterKind: 'LAKE', terraformed: false },
          'LAC_CENTRAL',
        ),
      ).toBe('EAU_PROFONDE');
      expect(
        classifyCellBiome(
          { elevation: 0.2, moisture: 0.8, isWater: true, waterKind: 'RIVER', terraformed: false },
          'CONFLUENCES',
        ),
      ).toBe('RIVIERE');
      expect(
        classifyCellBiome(
          { elevation: 0.32, moisture: 0.4, isWater: false, terraformed: false },
          'LAC_CENTRAL',
          { nearWater: true },
        ),
      ).toBe('PLAGE');
      expect(
        classifyCellBiome(
          { elevation: 0.82, moisture: 0.3, isWater: false, terraformed: false },
          'HAUTES_TERRES',
        ),
      ).toBe('MONTAGNE');
    });
  });

  describe('Step 2: 5 AoE archetypes differentiation & water geometry', () => {
    const sharedSeed = 'aoe-archetype-audit';
    const baseByArchetype = new Map<ArchetypeCarte, BaseTerrainMesh>();
    const mapByArchetype = new Map<ArchetypeCarte, GeneratedMap>();

    beforeAll(() => {
      const carte = buildTestCarte(6, sharedSeed, () => true);
      const archetypes: ArchetypeCarte[] = [
        'VALLEE_ROYALE',
        'FORET_NOIRE',
        'HAUTES_TERRES',
        'CONFLUENCES',
        'LAC_CENTRAL',
      ];
      for (const arch of archetypes) {
        const base = service.generateBaseTerrain(sharedSeed, arch);
        baseByArchetype.set(arch, base);
        mapByArchetype.set(arch, service.populateMapDomains(base, carte));
      }
    });

    it('FORET_NOIRE yields highest forest cell ratio and tree feature count', () => {
      const foretBase = baseByArchetype.get('FORET_NOIRE')!;
      const valleeBase = baseByArchetype.get('VALLEE_ROYALE')!;
      const hautesBase = baseByArchetype.get('HAUTES_TERRES')!;

      const forestRatio = (b: BaseTerrainMesh): number =>
        b.cells.filter((c) => c.biome === 'FORET').length / b.cells.length;

      expect(forestRatio(foretBase)).toBeGreaterThan(forestRatio(valleeBase));
      expect(forestRatio(foretBase)).toBeGreaterThan(forestRatio(hautesBase));

      const treeCount = (m: GeneratedMap): number =>
        m.features.filter((f) => f.kind === 'TREE_OAK' || f.kind === 'TREE_PINE').length;

      expect(treeCount(mapByArchetype.get('FORET_NOIRE')!)).toBeGreaterThan(
        treeCount(mapByArchetype.get('VALLEE_ROYALE')!),
      );
      expect(treeCount(mapByArchetype.get('FORET_NOIRE')!)).toBeGreaterThan(
        treeCount(mapByArchetype.get('HAUTES_TERRES')!),
      );
    });

    it('HAUTES_TERRES yields highest mean elevation, rocky highlands, and pine dominance', () => {
      const hautesBase = baseByArchetype.get('HAUTES_TERRES')!;
      const valleeBase = baseByArchetype.get('VALLEE_ROYALE')!;
      const foretBase = baseByArchetype.get('FORET_NOIRE')!;

      const meanElev = (b: BaseTerrainMesh): number =>
        b.cells.reduce((acc, c) => acc + c.elevation, 0) / b.cells.length;
      const highlandRatio = (b: BaseTerrainMesh): number =>
        b.cells.filter((c) => c.biome === 'COLLINE' || c.biome === 'MONTAGNE').length /
        b.cells.length;

      expect(meanElev(hautesBase)).toBeGreaterThan(meanElev(valleeBase));
      expect(meanElev(hautesBase)).toBeGreaterThan(meanElev(foretBase));
      expect(highlandRatio(hautesBase)).toBeGreaterThan(highlandRatio(valleeBase));
      expect(highlandRatio(hautesBase)).toBeGreaterThan(highlandRatio(foretBase));

      const hautesMap = mapByArchetype.get('HAUTES_TERRES')!;
      const hillRidges = (m: GeneratedMap): number =>
        m.features.filter((f) => f.kind === 'HILL_MOUND' || f.kind === 'RIDGE_CREST').length;
      expect(hillRidges(hautesMap)).toBeGreaterThan(
        hillRidges(mapByArchetype.get('VALLEE_ROYALE')!),
      );

      const pines = hautesMap.features.filter((f) => f.kind === 'TREE_PINE').length;
      const oaks = hautesMap.features.filter((f) => f.kind === 'TREE_OAK').length;
      expect(pines).toBeGreaterThanOrEqual(oaks);
    });

    it('CONFLUENCES carves converging river legs with connected RIVER chains and smoothed riverPaths', () => {
      const confBase = baseByArchetype.get('CONFLUENCES')!;
      const hautesBase = baseByArchetype.get('HAUTES_TERRES')!;

      expect(confBase.riverPaths.length).toBeGreaterThanOrEqual(3);
      const riverCells = confBase.cells.filter((c) => c.biome === 'RIVIERE');
      const hautesRiverCells = hautesBase.cells.filter((c) => c.biome === 'RIVIERE');
      expect(riverCells.length).toBeGreaterThan(hautesRiverCells.length);
      for (const rc of riverCells) {
        expect(rc.isWater).toBe(true);
        expect(rc.waterKind).toBe('RIVER');
      }
      // Market center remains dry
      const centerCells = confBase.cells.filter(
        (c) => euclideanDistance(c.center.x, c.center.y, 500, 500) <= MARKET_TERRAFORM_RADIUS,
      );
      expect(centerCells.some((c) => !c.isWater)).toBe(true);
    });

    it('LAC_CENTRAL forms an annular lake around a dry central Market island', () => {
      const lacBase = baseByArchetype.get('LAC_CENTRAL')!;
      const lacMap = mapByArchetype.get('LAC_CENTRAL')!;

      const islandCells = lacBase.cells.filter(
        (c) => euclideanDistance(c.center.x, c.center.y, 500, 500) <= MARKET_TERRAFORM_RADIUS,
      );
      expect(islandCells.length).toBeGreaterThan(0);
      expect(islandCells.every((c) => !c.isWater)).toBe(true);

      const annulusCells = lacBase.cells.filter((c) => {
        const d = euclideanDistance(c.center.x, c.center.y, 500, 500);
        return d >= 108 && d <= 208;
      });
      expect(annulusCells.length).toBeGreaterThan(0);
      const annulusLakeRatio =
        annulusCells.filter((c) => c.isWater && c.waterKind === 'LAKE').length /
        annulusCells.length;
      expect(annulusLakeRatio).toBeGreaterThan(0.9);

      const outerRingCells = lacMap.cells.filter((c) => {
        const d = euclideanDistance(c.center.x, c.center.y, 500, 500);
        return d >= 280 && d <= 420;
      });
      const outerDryRatio = outerRingCells.filter((c) => !c.isWater).length / outerRingCells.length;
      expect(outerDryRatio).toBeGreaterThan(0.9);
    });

    it('VALLEE_ROYALE balances meadows/forests and includes flowing rivers', () => {
      const valleeBase = baseByArchetype.get('VALLEE_ROYALE')!;
      const pastoralRatio =
        valleeBase.cells.filter((c) => c.biome === 'PRAIRIE' || c.biome === 'FORET').length /
        valleeBase.cells.length;
      expect(pastoralRatio).toBeGreaterThan(0.5);
      expect(valleeBase.riverPaths.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe('Step 3: Terraformation (Market r=75, Emplacements r=85, overlaps & river clipping)', () => {
    it('flattens and dries Market r<=75 and emplacement r<=85 even when placed directly inside LAC_CENTRAL', () => {
      const baseLac = service.generateBaseTerrain('terraform-lac', 'LAC_CENTRAL');
      const carte: CarteDto = createDefaultCarte([
        {
          numero: 1,
          coordX: 500 + 160,
          coordY: 500, // right inside the annular lake!
          occupe: true,
          royaume: createDefaultRoyaume(1),
        },
        {
          numero: 2,
          coordX: 500 + 205, // overlaps plateau 1 (45px apart < 170px)
          coordY: 500,
          occupe: false,
        },
      ]);

      const map = service.populateMapDomains(baseLac, carte);

      const marketCells = map.cells.filter(
        (c) => euclideanDistance(c.center.x, c.center.y, 500, 500) <= MARKET_TERRAFORM_RADIUS,
      );
      expect(marketCells.length).toBeGreaterThan(0);
      for (const mc of marketCells) {
        expect(mc.isWater).toBe(false);
        expect(mc.waterKind).toBeUndefined();
        expect(mc.terraformed).toBe(true);
        expect(mc.biome === 'PAVE_MARCHE' || mc.biome === 'PRAIRIE').toBe(true);
      }
      expect(marketCells.some((c) => c.biome === 'PAVE_MARCHE')).toBe(true);

      for (const emp of carte.emplacements) {
        const empCells = map.cells.filter(
          (c) =>
            euclideanDistance(c.center.x, c.center.y, emp.coordX, emp.coordY) <=
            EMPLACEMENT_TERRAFORM_RADIUS,
        );
        expect(empCells.length).toBeGreaterThan(0);
        for (const ec of empCells) {
          expect(ec.isWater).toBe(false);
          expect(ec.waterKind).toBeUndefined();
          expect(ec.terraformed).toBe(true);
          expect(ec.biome === 'PRAIRIE' || ec.biome === 'PAVE_MARCHE').toBe(true);
        }
      }
    });

    it('clips riverPaths out of terraformed Market and emplacement disks', () => {
      const baseConf = service.generateBaseTerrain('clip-river', 'CONFLUENCES');
      // Find a point right on a river path to place an emplacement
      const samplePath = baseConf.riverPaths[0];
      const midPt = samplePath[Math.floor(samplePath.length / 2)];
      const carte = createDefaultCarte([
        {
          numero: 1,
          coordX: Math.round(midPt.x),
          coordY: Math.round(midPt.y),
          occupe: true,
          royaume: createDefaultRoyaume(1),
        },
      ]);

      const map = service.populateMapDomains(baseConf, carte);
      for (const path of map.riverPaths) {
        for (const pt of path) {
          expect(euclideanDistance(pt.x, pt.y, 500, 500)).toBeGreaterThanOrEqual(
            MARKET_TERRAFORM_RADIUS - 0.05,
          );
          expect(
            euclideanDistance(
              pt.x,
              pt.y,
              carte.emplacements[0].coordX,
              carte.emplacements[0].coordY,
            ),
          ).toBeGreaterThanOrEqual(EMPLACEMENT_TERRAFORM_RADIUS - 0.05);
        }
      }
    });
  });

  describe('Step 4: Market free zone & geodesic Voronoi fiefs', () => {
    it('preserves Market free zone d<110 and partitions 100% of non-water non-market land across water barriers', () => {
      for (const arch of ['VALLEE_ROYALE', 'LAC_CENTRAL'] as const) {
        const carte = buildTestCarte(6, `fief-${arch}`, (n) => n % 2 === 1);
        const map = service.generateMap(carte, `fief-${arch}`, arch);

        const seenCellIds = new Set<number>();
        for (const cell of map.cells) {
          const dMarket = euclideanDistance(cell.center.x, cell.center.y, map.centreX, map.centreY);
          if (dMarket < MARKET_FREE_ZONE_RADIUS) {
            expect(cell.isMarketZone).toBe(true);
            expect(cell.ownerNumero).toBeNull();
          } else {
            expect(cell.isMarketZone).toBe(false);
          }

          if (cell.isWater) {
            expect(cell.ownerNumero).toBeNull();
          } else if (!cell.isMarketZone) {
            expect(cell.ownerNumero).not.toBeNull();
            expect(cell.ownerNumero!).toBeGreaterThanOrEqual(1);
            expect(cell.ownerNumero!).toBeLessThanOrEqual(6);
          }
        }

        expect(map.fiefs.length).toBe(carte.emplacements.length);
        for (const fief of map.fiefs) {
          const emp = carte.emplacements.find((e) => e.numero === fief.numero)!;
          expect(fief.occupe).toBe(emp.occupe);

          const expectedIds = map.cells
            .filter((c) => c.ownerNumero === fief.numero)
            .map((c) => c.id);
          expect(fief.cellIds).toEqual(expectedIds);
          for (const cid of fief.cellIds) {
            expect(seenCellIds.has(cid)).toBe(false);
            seenCellIds.add(cid);
          }

          expect(fief.borderSegments.length).toBeGreaterThan(0);
          for (const seg of fief.borderSegments) {
            expect(seg.dashed).toBe(!emp.occupe);
            if (emp.occupe) {
              expect(seg.color).toBe(emp.royaume!.couleurPrimaire);
            } else {
              expect(seg.color).toBe(UNOCCUPIED_BORDER_COLOR);
            }
          }
        }
      }
    });
  });

  describe('Step 5: A* trade routes & bridge detection', () => {
    it('connects every emplacement to Market with continuous adjacent A* cell chains and bridges over water', () => {
      for (const arch of ['LAC_CENTRAL', 'CONFLUENCES'] as const) {
        const carte = buildTestCarte(5, `route-${arch}`, () => true);
        const map = service.generateMap(carte, `route-${arch}`, arch);

        expect(map.routes.length).toBe(carte.emplacements.length);
        let totalBridges = 0;

        for (const route of map.routes) {
          const emp = carte.emplacements.find((e) => e.numero === route.emplacementNumero)!;
          expect(route.from).toEqual({ x: emp.coordX, y: emp.coordY });
          expect(route.to).toEqual({ x: map.centreX, y: map.centreY });
          expect(route.path.length).toBeGreaterThanOrEqual(2);
          expect(route.totalLength).toBeGreaterThanOrEqual(
            euclideanDistance(route.from.x, route.from.y, route.to.x, route.to.y) - 1e-6,
          );

          for (let i = 0; i < route.cellIds.length - 1; i++) {
            const a = map.cells[route.cellIds[i]];
            const b = map.cells[route.cellIds[i + 1]];
            expect(a.neighbors).toContain(b.id);
          }

          for (const br of route.bridges) {
            expect(br.length).toBeGreaterThan(0);
            expect(Number.isFinite(br.angle)).toBe(true);
            expect(br.midpoint.x).toBeCloseTo((br.start.x + br.end.x) * 0.5, 6);
            expect(br.midpoint.y).toBeCloseTo((br.start.y + br.end.y) * 0.5, 6);
            totalBridges++;
          }
        }

        expect(totalBridges).toBeGreaterThan(0);
      }
    });

    it('emits zero bridges for a short dry route inside the Market plateau', () => {
      const carte = createDefaultCarte([
        {
          numero: 1,
          coordX: 555,
          coordY: 500,
          occupe: true,
          royaume: createDefaultRoyaume(1),
        },
      ]);
      const map = service.generateMap(carte, 'dry-short', 'LAC_CENTRAL');
      expect(map.routes.length).toBe(1);
      expect(map.routes[0].bridges.length).toBe(0);
    });
  });

  describe('Step 6: 2.5D features, occupancy distinction & strict exclusion masks', () => {
    it('distinguishes occupied fortified domains from unoccupied clearings and omits optional royaume safely', () => {
      const carte = createDefaultCarte([
        {
          numero: 1,
          coordX: 250,
          coordY: 300,
          occupe: true, // royaume intentionally omitted!
        },
        {
          numero: 2,
          coordX: 760,
          coordY: 700,
          occupe: false,
          royaume: createDefaultRoyaume(2),
        },
      ]);

      const map = service.generateMap(carte, 'features-distinction', 'VALLEE_ROYALE');
      const ids = new Set<string>();
      let prevY = -Infinity;

      for (const f of map.features) {
        expect(ids.has(f.id)).toBe(false);
        ids.add(f.id);
        expect(Number.isFinite(f.x)).toBe(true);
        expect(Number.isFinite(f.y)).toBe(true);
        expect(f.scale).toBeGreaterThan(0);
        expect(f.seedOffset).toBeGreaterThanOrEqual(0);
        expect(f.seedOffset).toBeLessThan(1);
        expect(f.y).toBeGreaterThanOrEqual(prevY);
        prevY = f.y;
      }

      const marketHalls = map.features.filter((f) => f.kind === 'MARKET_HALL');
      const marketStalls = map.features.filter((f) => f.kind === 'MARKET_STALL');
      expect(marketHalls.length).toBe(1);
      expect(marketHalls[0].x).toBe(500);
      expect(marketHalls[0].y).toBe(500);
      expect(marketStalls.length).toBeGreaterThanOrEqual(6);
      for (const stall of marketStalls) {
        expect(euclideanDistance(stall.x, stall.y, 500, 500)).toBeLessThanOrEqual(
          MARKET_TERRAFORM_RADIUS,
        );
        expect(stall.primaryColor).toBeTruthy();
        expect(stall.secondaryColor).toBeTruthy();
      }

      // Occupied #1
      const occFeatures = map.features.filter((f) => f.ownerNumero === 1);
      expect(occFeatures.filter((f) => f.kind === 'CASTLE_KEEP').length).toBe(1);
      expect(occFeatures.filter((f) => f.kind === 'RAMPART_TOWER').length).toBeGreaterThanOrEqual(4);
      expect(occFeatures.filter((f) => f.kind === 'WINDMILL').length).toBeGreaterThanOrEqual(1);

      // Unoccupied #2
      const unoccFeatures = map.features.filter((f) => f.ownerNumero === 2);
      expect(unoccFeatures.filter((f) => f.kind === 'SITE_FOUNDATION').length).toBe(1);
      expect(unoccFeatures.filter((f) => f.kind === 'SITE_PALISADE').length).toBeGreaterThanOrEqual(4);
      expect(
        unoccFeatures.some((f) =>
          ['CASTLE_KEEP', 'RAMPART_TOWER', 'HAMLET_HOUSE', 'WINDMILL', 'CROP_FIELD'].includes(
            f.kind,
          ),
        ),
      ).toBe(false);
    });

    it('enforces strict exclusion masks on all natural features in FORET_NOIRE and HAUTES_TERRES', () => {
      for (const arch of ['FORET_NOIRE', 'HAUTES_TERRES'] as const) {
        const carte = buildTestCarte(6, `excl-${arch}`, () => true);
        const map = service.generateMap(carte, `excl-${arch}`, arch);

        const naturalFeatures = map.features.filter((f) =>
          ['TREE_OAK', 'TREE_PINE', 'HILL_MOUND', 'RIDGE_CREST'].includes(f.kind),
        );
        expect(naturalFeatures.length).toBeGreaterThan(0);

        for (const nf of naturalFeatures) {
          // Containing cell (id encoded in nat-<cellId>-<k>) must be dry land containing (nf.x, nf.y)
          const match = /^nat-(\d+)-\d+$/.exec(nf.id);
          expect(match).not.toBeNull();
          const cellId = Number(match![1]);
          const cell = map.cells[cellId];
          expect(cell.isWater).toBe(false);
          expect(isInsidePolygon(nf.x, nf.y, cell.polygon)).toBe(true);

          // Market plaza exclusion
          expect(euclideanDistance(nf.x, nf.y, map.centreX, map.centreY)).toBeGreaterThanOrEqual(
            MARKET_TERRAFORM_RADIUS,
          );

          // Emplacement clearing exclusion
          for (const emp of carte.emplacements) {
            expect(euclideanDistance(nf.x, nf.y, emp.coordX, emp.coordY)).toBeGreaterThanOrEqual(
              EMPLACEMENT_FEATURE_EXCLUSION_RADIUS,
            );
          }

          // Trade route corridor exclusion
          for (const route of map.routes) {
            for (let i = 0; i < route.path.length - 1; i++) {
              const dist = pointToSegmentDist(
                nf.x,
                nf.y,
                route.path[i].x,
                route.path[i].y,
                route.path[i + 1].x,
                route.path[i + 1].y,
              );
              expect(dist).toBeGreaterThanOrEqual(ROUTE_FEATURE_EXCLUSION_RADIUS - 1e-6);
            }
          }
        }
      }
    });
  });

  describe('Edge cases, BaseTerrainMesh immutability & LRU cache isolation', () => {
    it('supports N = 0..10 emplacements cleanly using a single BaseTerrainMesh', () => {
      const base = service.generateBaseTerrain('n-sweep', 'VALLEE_ROYALE');

      const map0 = service.populateMapDomains(base, createDefaultCarte([]));
      expect(map0.fiefs).toEqual([]);
      expect(map0.routes).toEqual([]);
      expect(map0.features.some((f) => f.kind === 'MARKET_HALL')).toBe(true);
      expect(map0.features.filter((f) => f.kind === 'MARKET_STALL').length).toBeGreaterThanOrEqual(
        6,
      );

      for (let n = 1; n <= 10; n++) {
        const carte = buildTestCarte(n, 'n-sweep', () => true);
        const map = service.populateMapDomains(base, carte);
        expect(map.fiefs.length).toBe(n);
        expect(map.routes.length).toBe(n);
      }
    });

    it('handles placement dragged into Market d<110, directly onto (500, 500), and duplicate coordinates', () => {
      const base = service.generateBaseTerrain('edge-drag', 'VALLEE_ROYALE');
      const carte = createDefaultCarte([
        { numero: 1, coordX: 540, coordY: 500, occupe: true, royaume: createDefaultRoyaume(1) },
        { numero: 2, coordX: 500, coordY: 500, occupe: true, royaume: createDefaultRoyaume(2) },
        { numero: 3, coordX: 250, coordY: 250, occupe: true, royaume: createDefaultRoyaume(3) },
        { numero: 4, coordX: 250, coordY: 250, occupe: true, royaume: createDefaultRoyaume(4) },
      ]);

      const map = service.populateMapDomains(base, carte);

      // Market free zone remains ownerNumero === null
      for (const c of map.cells) {
        if (euclideanDistance(c.center.x, c.center.y, 500, 500) < MARKET_FREE_ZONE_RADIUS) {
          expect(c.isMarketZone).toBe(true);
          expect(c.ownerNumero).toBeNull();
        }
      }

      // Inner Market square remains PAVE_MARCHE even with kingdom #2 at (500, 500)
      const centerCell = map.cells.reduce((best, cur) =>
        euclideanDistance(cur.center.x, cur.center.y, 500, 500) <
        euclideanDistance(best.center.x, best.center.y, 500, 500)
          ? cur
          : best,
      );
      expect(centerCell.biome).toBe('PAVE_MARCHE');

      // Duplicate coordinates give #4 empty cellIds without throwing, and still guarantee CASTLE_KEEP + WINDMILL
      const fief4 = map.fiefs.find((f) => f.numero === 4)!;
      expect(fief4.cellIds).toEqual([]);
      for (const num of [1, 2, 3, 4]) {
        const owns = map.features.filter((f) => f.ownerNumero === num);
        expect(owns.some((f) => f.kind === 'CASTLE_KEEP')).toBe(true);
        expect(owns.some((f) => f.kind === 'WINDMILL')).toBe(true);
      }

      // Routes handle startIdx === goalIdx without NaN
      for (const route of map.routes) {
        expect(Number.isFinite(route.totalLength)).toBe(true);
      }
    });

    it('keeps BaseTerrainMesh strictly immutable across populateMapDomains calls and isolates cached clones', () => {
      const base = service.generateBaseTerrain('imm-seed', 'VALLEE_ROYALE');
      const snapshot = structuredClone(base);

      const carteA = buildTestCarte(4, 'imm-A', () => true);
      const carteB = buildTestCarte(7, 'imm-B', (n) => n % 2 === 0);

      const mapA = service.populateMapDomains(base, carteA);
      service.populateMapDomains(base, carteB);

      expect(base).toEqual(snapshot);
      expect(mapA).toEqual(service.generateMap(carteA, 'imm-seed', 'VALLEE_ROYALE'));

      // Mutate returned BaseTerrainMesh and confirm cache is uncorrupted
      base.cells[0].biome = 'MONTAGNE';
      base.cells[0].isWater = true;
      base.riverPaths.push([{ x: 0, y: 0 }, { x: 10, y: 10 }]);
      const freshBase = service.generateBaseTerrain('imm-seed', 'VALLEE_ROYALE');
      expect(freshBase).toEqual(snapshot);
    });

    it('evicts oldest entries cleanly past LRU cache size 12 while remaining deterministic', () => {
      const localService = new MapGeneratorService();
      const first = localService.generateBaseTerrain('lru-0', 'VALLEE_ROYALE');
      for (let i = 1; i <= 14; i++) {
        localService.generateBaseTerrain(`lru-${i}`, 'VALLEE_ROYALE');
      }
      const recomputedFirst = localService.generateBaseTerrain('lru-0', 'VALLEE_ROYALE');
      expect(recomputedFirst).toEqual(first);
    });

    it('normalizes -0 coordinates on fief.center cleanly', () => {
      const base = service.generateBaseTerrain('neg-zero-fief', 'VALLEE_ROYALE');
      const carte = createDefaultCarte([
        {
          numero: 1,
          coordX: -0,
          coordY: -0,
          occupe: true,
          royaume: createDefaultRoyaume(1),
        },
      ]);
      const map = service.populateMapDomains(base, carte);
      expect(Object.is(map.fiefs[0].center.x, -0)).toBe(false);
      expect(Object.is(map.fiefs[0].center.y, -0)).toBe(false);
      expect(map.fiefs[0].center).toEqual({ x: 0, y: 0 });
    });
  });

  describe('Regression tests: BridgeSegment alignment, river crossings & orphan river stubs', () => {
    it('aligns Mechanism 1 polyline BridgeSegments with curved isWater route cells (LAC_CENTRAL seed=-999999 N=4 emp=3)', () => {
      const carte = buildTestCarte(4, -999999, () => true);
      const map = service.generateMap(carte, -999999, 'LAC_CENTRAL');

      const route3 = map.routes.find((r) => r.emplacementNumero === 3)!;
      expect(route3).toBeDefined();
      expect(route3.bridges.length).toBeGreaterThanOrEqual(1);

      for (const route of map.routes) {
        const waterCells = route.cellIds
          .map((id) => map.cells[id])
          .filter((c) => c.isWater);
        for (const wc of waterCells) {
          const minDist = route.bridges.reduce(
            (best, br) =>
              Math.min(
                best,
                pointToSegmentDist(
                  wc.center.x,
                  wc.center.y,
                  br.start.x,
                  br.start.y,
                  br.end.x,
                  br.end.y,
                ),
              ),
            Infinity,
          );
          expect(minDist).toBeLessThanOrEqual(2);
        }

        // Every BridgeSegment emitted for the route lies directly along route.path (endpoints <= 1px off route.path)
        for (const br of route.bridges) {
          const endpoints = [br.start, br.end];
          for (const ep of endpoints) {
            let minPathDist = Infinity;
            for (let i = 0; i < route.path.length - 1; i++) {
              minPathDist = Math.min(
                minPathDist,
                pointToSegmentDist(
                  ep.x,
                  ep.y,
                  route.path[i].x,
                  route.path[i].y,
                  route.path[i + 1].x,
                  route.path[i + 1].y,
                ),
              );
            }
            // Mechanism 2 bridges extend +-16px along segment direction beyond vertices, so check line alignment near hit via midpoint
            const midDist = (() => {
              let best = Infinity;
              for (let i = 0; i < route.path.length - 1; i++) {
                best = Math.min(
                  best,
                  pointToSegmentDist(
                    br.midpoint.x,
                    br.midpoint.y,
                    route.path[i].x,
                    route.path[i].y,
                    route.path[i + 1].x,
                    route.path[i + 1].y,
                  ),
                );
              }
              return best;
            })();
            expect(Math.min(minPathDist, midDist)).toBeLessThanOrEqual(2);
          }
        }
      }
    });

    it('covers every confluence river crossing without corner-cutting (CONFLUENCES seed="conf-13" N=10 emp=4)', () => {
      const carte = buildTestCarte(10, 'conf-13', () => true);
      const map = service.generateMap(carte, 'conf-13', 'CONFLUENCES');

      const route4 = map.routes.find((r) => r.emplacementNumero === 4)!;
      expect(route4).toBeDefined();

      for (const route of map.routes) {
        for (let i = 0; i < route.path.length - 1; i++) {
          for (const rPath of map.riverPaths) {
            for (let r = 0; r < rPath.length - 1; r++) {
              const hit = findSegmentIntersection(
                route.path[i],
                route.path[i + 1],
                rPath[r],
                rPath[r + 1],
              );
              if (!hit) {
                continue;
              }
              const minBridgeDist = route.bridges.reduce(
                (best, br) =>
                  Math.min(
                    best,
                    pointToSegmentDist(
                      hit.x,
                      hit.y,
                      br.start.x,
                      br.start.y,
                      br.end.x,
                      br.end.y,
                    ),
                  ),
                Infinity,
              );
              expect(minBridgeDist).toBeLessThanOrEqual(2);
              const intersectsBridge = route.bridges.some((br) => {
                const brHit = findSegmentIntersection(br.start, br.end, rPath[r], rPath[r + 1]);
                return brHit !== null && Math.hypot(brHit.x - hit.x, brHit.y - hit.y) <= 2;
              });
              expect(intersectsBridge).toBe(true);
            }
          }
        }
      }
    });

    it('does not suppress river crossings before a lake bridgehead (LAC_CENTRAL seed="stress-36" N=10 emp=5)', () => {
      const carte = buildTestCarte(10, 'stress-36', () => true);
      const map = service.generateMap(carte, 'stress-36', 'LAC_CENTRAL');

      const route5 = map.routes.find((r) => r.emplacementNumero === 5)!;
      expect(route5).toBeDefined();

      let checkedIntersections = 0;
      for (const route of map.routes) {
        for (let i = 0; i < route.path.length - 1; i++) {
          for (const rPath of map.riverPaths) {
            for (let r = 0; r < rPath.length - 1; r++) {
              const hit = findSegmentIntersection(
                route.path[i],
                route.path[i + 1],
                rPath[r],
                rPath[r + 1],
              );
              if (!hit) {
                continue;
              }
              checkedIntersections++;
              const minBridgeDist = route.bridges.reduce(
                (best, br) =>
                  Math.min(
                    best,
                    pointToSegmentDist(
                      hit.x,
                      hit.y,
                      br.start.x,
                      br.start.y,
                      br.end.x,
                      br.end.y,
                    ),
                  ),
                Infinity,
              );
              expect(minBridgeDist).toBeLessThanOrEqual(2);
              const intersectsBridge = route.bridges.some((br) => {
                const brHit = findSegmentIntersection(br.start, br.end, rPath[r], rPath[r + 1]);
                return brHit !== null && Math.hypot(brHit.x - hit.x, brHit.y - hit.y) <= 2;
              });
              expect(intersectsBridge).toBe(true);
            }
          }
        }
      }
      expect(checkedIntersections).toBeGreaterThan(0);
    });

    it('prunes short (<25px) orphan riverPaths stubs on dry terraformed cells (FORET_NOIRE seed="undefined" N=10)', () => {
      const carte = buildTestCarte(10, 'undefined', () => true);
      const map = service.generateMap(carte, 'undefined', 'FORET_NOIRE');

      for (const rPath of map.riverPaths) {
        const len = polylineLength(rPath);
        if (len < 25) {
          let acc = 0;
          let mid = rPath[0];
          const half = len * 0.5;
          for (let i = 0; i < rPath.length - 1; i++) {
            const seg = Math.hypot(rPath[i + 1].x - rPath[i].x, rPath[i + 1].y - rPath[i].y);
            if (seg > 1e-9 && acc + seg >= half) {
              const t = (half - acc) / seg;
              mid = {
                x: rPath[i].x + (rPath[i + 1].x - rPath[i].x) * t,
                y: rPath[i].y + (rPath[i + 1].y - rPath[i].y) * t,
              };
              break;
            }
            acc += seg;
          }
          let nearest = map.cells[0];
          let bestDist = Infinity;
          for (const c of map.cells) {
            const d = Math.hypot(c.center.x - mid.x, c.center.y - mid.y);
            if (d < bestDist) {
              bestDist = d;
              nearest = c;
            }
          }
          expect(nearest.isWater).toBe(true);
        }
      }
    });
  });
});
