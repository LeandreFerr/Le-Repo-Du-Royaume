import { describe, expect, it, vi } from 'vitest';
import {
  ARCHETYPES_CARTE,
  ArchetypeCarte,
  BiomeType,
  CarteDto,
  FeatureKind,
  GeneratedMap,
  InspectionInfo,
  MARKET_FREE_ZONE_RADIUS,
  MapFeature25D,
  VoronoiCell,
} from '../../models/carte.model';
import {
  DEFAULT_TILT_DEG,
  MAX_TILT_DEG,
  MIN_BILLBOARD_TILT_DEG,
  MIN_DENOM,
  MIN_TILT_DEG,
  MapRenderer25D,
  PERSPECTIVE_FOCAL_LENGTH,
  ProjectionParams,
  RenderMapState,
  clipPolygonNearPlane,
  clipPolylineNearPlane,
  clipSegmentNearPlane,
  createDefaultProjectionParams,
  getNearPlaneVMax,
  getProjectedAnchors,
  hitTestAnchor,
  projectPoint,
  renderMap25D,
  screenToWorld,
  screenToWorldClamped,
  toClipVertex,
} from './map-renderer-25d';

function makeParams(overrides: Partial<ProjectionParams> = {}): ProjectionParams {
  return {
    centreX: 500,
    centreY: 500,
    zoom: 1,
    panX: 0,
    panY: 0,
    tiltDeg: DEFAULT_TILT_DEG,
    screenCenterX: 600,
    screenCenterY: 400,
    focalLength: PERSPECTIVE_FOCAL_LENGTH,
    ...overrides,
  };
}

interface MockCanvasTrace {
  ctx: CanvasRenderingContext2D;
  fillStyles: string[];
  strokeStyles: string[];
  setLineDashCalls: number[][];
  lineDashOffsets: number[];
  fillTexts: Array<{ text: string; x: number; y: number }>;
  pathCoords: Array<{ op: string; x: number; y: number }>;
  strokeStates: Array<{ dash: number[]; dashOffset: number }>;
  fillCalls: Array<{ fillStyle: string; points: Array<{ x: number; y: number }> }>;
  saveCount: number;
  restoreCount: number;
  arcToCount: number;
  roundRectCount: number;
}

function createMockCanvasContext(options: { includeRoundRect?: boolean } = {}): MockCanvasTrace {
  const fillStyles: string[] = [];
  const strokeStyles: string[] = [];
  const setLineDashCalls: number[][] = [];
  const lineDashOffsets: number[] = [];
  const fillTexts: Array<{ text: string; x: number; y: number }> = [];
  const pathCoords: Array<{ op: string; x: number; y: number }> = [];
  const strokeStates: Array<{ dash: number[]; dashOffset: number }> = [];
  const fillCalls: Array<{ fillStyle: string; points: Array<{ x: number; y: number }> }> = [];

  let currentFillStyle: string | CanvasGradient | CanvasPattern = '#000000';
  let currentStrokeStyle: string | CanvasGradient | CanvasPattern = '#000000';
  let currentLineDash: number[] = [];
  let currentLineDashOffset = 0;
  let currentPath: Array<{ x: number; y: number }> = [];
  const stateStack: Array<{ dash: number[]; dashOffset: number }> = [];
  let saveCount = 0;
  let restoreCount = 0;
  let arcToCount = 0;
  let roundRectCount = 0;

  const recordCoord = (op: string, x: number, y: number): void => {
    pathCoords.push({ op, x, y });
  };

  const mock: Record<string, unknown> = {
    lineWidth: 1,
    lineCap: 'butt',
    lineJoin: 'miter',
    font: '12px sans-serif',
    textAlign: 'left',
    textBaseline: 'alphabetic',
    save: vi.fn(() => {
      saveCount += 1;
      stateStack.push({ dash: [...currentLineDash], dashOffset: currentLineDashOffset });
    }),
    restore: vi.fn(() => {
      restoreCount += 1;
      const prev = stateStack.pop();
      if (prev) {
        currentLineDash = [...prev.dash];
        currentLineDashOffset = prev.dashOffset;
      }
    }),
    beginPath: vi.fn(() => {
      currentPath = [];
    }),
    closePath: vi.fn(),
    fill: vi.fn(() => {
      fillCalls.push({
        fillStyle: typeof currentFillStyle === 'string' ? currentFillStyle : '<gradient>',
        points: currentPath.map((p) => ({ ...p })),
      });
    }),
    stroke: vi.fn(() => {
      strokeStates.push({ dash: [...currentLineDash], dashOffset: currentLineDashOffset });
    }),
    fillRect: vi.fn((x: number, y: number, w: number, h: number) => {
      recordCoord('fillRect', x, y);
      recordCoord('fillRectSize', w, h);
    }),
    moveTo: vi.fn((x: number, y: number) => {
      currentPath.push({ x, y });
      recordCoord('moveTo', x, y);
    }),
    lineTo: vi.fn((x: number, y: number) => {
      currentPath.push({ x, y });
      recordCoord('lineTo', x, y);
    }),
    quadraticCurveTo: vi.fn((cx: number, cy: number, x: number, y: number) => {
      recordCoord('qCtrl', cx, cy);
      recordCoord('qEnd', x, y);
    }),
    arc: vi.fn((x: number, y: number, r: number) => {
      recordCoord('arc', x, y);
      recordCoord('arcR', r, r);
    }),
    ellipse: vi.fn((x: number, y: number, rx: number, ry: number) => {
      recordCoord('ellipse', x, y);
      recordCoord('ellipseR', rx, ry);
    }),
    arcTo: vi.fn((x1: number, y1: number, x2: number, y2: number) => {
      arcToCount += 1;
      recordCoord('arcTo1', x1, y1);
      recordCoord('arcTo2', x2, y2);
    }),
    setLineDash: vi.fn((segments: number[]) => {
      currentLineDash = [...segments];
      setLineDashCalls.push([...segments]);
    }),
    getLineDash: vi.fn(() => [...currentLineDash]),
    measureText: vi.fn((text: string) => ({ width: text.length * 6.5 })),
    fillText: vi.fn((text: string, x: number, y: number) => {
      fillTexts.push({ text, x, y });
      recordCoord('fillText', x, y);
    }),
    createRadialGradient: vi.fn(() => ({
      addColorStop: vi.fn(),
    })),
  };

  if (options.includeRoundRect !== false) {
    mock['roundRect'] = vi.fn((x: number, y: number, w: number, h: number) => {
      roundRectCount += 1;
      recordCoord('roundRect', x, y);
      recordCoord('roundRectSize', w, h);
    });
  }

  Object.defineProperty(mock, 'fillStyle', {
    get: () => currentFillStyle,
    set: (val: string | CanvasGradient | CanvasPattern) => {
      currentFillStyle = val;
      if (typeof val === 'string') {
        fillStyles.push(val);
      }
    },
  });

  Object.defineProperty(mock, 'strokeStyle', {
    get: () => currentStrokeStyle,
    set: (val: string | CanvasGradient | CanvasPattern) => {
      currentStrokeStyle = val;
      if (typeof val === 'string') {
        strokeStyles.push(val);
      }
    },
  });

  Object.defineProperty(mock, 'lineDashOffset', {
    get: () => currentLineDashOffset,
    set: (val: number) => {
      currentLineDashOffset = val;
      lineDashOffsets.push(val);
    },
  });

  return {
    ctx: mock as unknown as CanvasRenderingContext2D,
    fillStyles,
    strokeStyles,
    setLineDashCalls,
    lineDashOffsets,
    fillTexts,
    pathCoords,
    strokeStates,
    fillCalls,
    get saveCount() {
      return saveCount;
    },
    get restoreCount() {
      return restoreCount;
    },
    get arcToCount() {
      return arcToCount;
    },
    get roundRectCount() {
      return roundRectCount;
    },
  };
}

function makeRichTestMap(
  archetype: ArchetypeCarte,
  occupe: boolean,
): {
  carte: CarteDto;
  map: GeneratedMap;
} {
  const carte: CarteDto = {
    largeur: 1000,
    hauteur: 1000,
    centreX: 500,
    centreY: 500,
    emplacements: [
      {
        numero: 1,
        coordX: 240,
        coordY: 320,
        occupe,
        royaume: occupe
          ? {
              nom: 'Valdore',
              couleurPrimaire: '#b91c1c',
              couleurSecondaire: '#fef3c7',
              niveau: 3,
            }
          : undefined,
      },
      {
        numero: 2,
        coordX: 760,
        coordY: 680,
        occupe,
        // Intentionally omit royaume even when occupe=true to verify fallback resilience
      },
    ],
  };

  const biomes: BiomeType[] = [
    'EAU_PROFONDE',
    'LAC',
    'RIVIERE',
    'PLAGE',
    'PRAIRIE',
    'FORET',
    'COLLINE',
    'MONTAGNE',
    'PAVE_MARCHE',
  ];

  const cells: VoronoiCell[] = biomes.map((biome, idx) => {
    const cx = 150 + (idx % 3) * 300;
    const cy = 150 + Math.floor(idx / 3) * 300;
    const isWater = biome === 'EAU_PROFONDE' || biome === 'LAC' || biome === 'RIVIERE';
    return {
      id: idx,
      center: { x: cx, y: cy },
      polygon: [
        { x: cx - 80, y: cy - 80 },
        { x: cx + 80, y: cy - 80 },
        { x: cx + 80, y: cy + 80 },
        { x: cx - 80, y: cy + 80 },
      ],
      neighbors: idx > 0 ? [idx - 1] : [],
      elevation: biome === 'MONTAGNE' ? 0.92 : 0.45,
      moisture: 0.65,
      isWater,
      waterKind:
        biome === 'LAC' || biome === 'EAU_PROFONDE'
          ? 'LAKE'
          : biome === 'RIVIERE'
            ? 'RIVER'
            : undefined,
      biome,
      isBoundary: idx === 0,
      terraformed: biome === 'PAVE_MARCHE',
      isMarketZone: biome === 'PAVE_MARCHE',
      ownerNumero: !isWater && biome !== 'PAVE_MARCHE' ? (idx % 2) + 1 : null,
      geodesicDistance: 120,
    };
  });

  const featureKinds: FeatureKind[] = [
    'TREE_OAK',
    'TREE_PINE',
    'HILL_MOUND',
    'RIDGE_CREST',
    'MARKET_HALL',
    'MARKET_STALL',
    'CASTLE_KEEP',
    'RAMPART_TOWER',
    'HAMLET_HOUSE',
    'WINDMILL',
    'CROP_FIELD',
    'SITE_PALISADE',
    'SITE_FOUNDATION',
  ];

  const features: MapFeature25D[] = featureKinds.map((kind, idx) => ({
    id: `feat-${idx}`,
    kind,
    x: 200 + (idx % 4) * 180,
    y: 180 + Math.floor(idx / 4) * 180,
    scale: 1,
    rotation: idx * 0.3,
    ownerNumero: (idx % 2) + 1,
    seedOffset: idx * 0.13,
  }));

  const map: GeneratedMap = {
    seed: 'test-seed',
    archetype,
    width: 1000,
    height: 1000,
    centreX: 500,
    centreY: 500,
    marketRadius: 75,
    marketFreeZoneRadius: MARKET_FREE_ZONE_RADIUS,
    cells,
    fiefs: [
      {
        numero: 1,
        occupe,
        royaume: carte.emplacements[0].royaume,
        center: { x: 240, y: 320 },
        cellIds: [4],
        borderSegments: [
          { a: { x: 180, y: 260 }, b: { x: 320, y: 260 }, dashed: !occupe, color: '#b91c1c' },
        ],
      },
      {
        numero: 2,
        occupe,
        center: { x: 760, y: 680 },
        cellIds: [5],
        borderSegments: [
          { a: { x: 680, y: 620 }, b: { x: 820, y: 620 }, dashed: !occupe, color: '#1d4ed8' },
        ],
      },
    ],
    routes: [
      {
        emplacementNumero: 1,
        from: { x: 240, y: 320 },
        to: { x: 500, y: 500 },
        path: [
          { x: 240, y: 320 },
          { x: 370, y: 410 },
          { x: 500, y: 500 },
        ],
        cellIds: [4, 8],
        bridges: [
          {
            start: { x: 350, y: 396 },
            end: { x: 390, y: 424 },
            midpoint: { x: 370, y: 410 },
            angle: Math.atan2(28, 40),
            length: 48.8,
          },
        ],
        totalLength: 316,
      },
    ],
    features,
    riverPaths: [
      [
        { x: 120, y: 200 },
        { x: 360, y: 400 },
        { x: 620, y: 740 },
      ],
    ],
  };

  return { carte, map };
}

describe('map-renderer-25d R4 projection & inverse math', () => {
  it('exposes exact R4 constants and creates valid default projection params', () => {
    expect(PERSPECTIVE_FOCAL_LENGTH).toBe(1400);
    expect(MIN_BILLBOARD_TILT_DEG).toBe(20);
    expect(MIN_DENOM).toBe(40);
    expect(MIN_TILT_DEG).toBe(0);
    expect(MAX_TILT_DEG).toBe(60);
    expect(DEFAULT_TILT_DEG).toBe(40);

    const defaults = createDefaultProjectionParams(1200, 800);
    expect(defaults.centreX).toBe(500);
    expect(defaults.centreY).toBe(500);
    expect(defaults.tiltDeg).toBe(40);
    expect(defaults.screenCenterX).toBe(600);
    expect(defaults.screenCenterY).toBe(400);
    expect(defaults.zoom).toBeGreaterThan(0);
    expect(defaults.focalLength).toBe(1400);
  });

  it('maintains median-line invariant at y = 500 (zDepth === +0, k === 1, visible === true) across tilts and zooms', () => {
    for (const tiltDeg of [0, 15, 20, 40, 60]) {
      for (const zoom of [0.4, 1, 2.5]) {
        const params = makeParams({ tiltDeg, zoom, panY: 0 });
        const pt = projectPoint(320, 500, 0, params);

        expect(Object.is(pt.zDepth, 0)).toBe(true);
        expect(Object.is(pt.v, 0)).toBe(true);
        expect(Object.is(pt.k, 1)).toBe(true);
        expect(pt.visible).toBe(true);
        expect(pt.screenX).toBeCloseTo(params.screenCenterX + (320 - 500) * zoom, 10);
        expect(pt.screenY).toBe(params.screenCenterY);
      }
    }
  });

  it('enforces MIN_BILLBOARD_TILT_DEG = 20 for vertical relief h and clamps tiltDeg to [0, 60]', () => {
    const p0 = projectPoint(500, 500, 50, makeParams({ tiltDeg: 0 }));
    const p15 = projectPoint(500, 500, 50, makeParams({ tiltDeg: 15 }));
    const p20 = projectPoint(500, 500, 50, makeParams({ tiltDeg: 20 }));
    const p40 = projectPoint(500, 500, 50, makeParams({ tiltDeg: 40 }));
    const p60 = projectPoint(500, 500, 50, makeParams({ tiltDeg: 60 }));

    expect(p0.screenY).toBeCloseTo(p15.screenY, 10);
    expect(p15.screenY).toBeCloseTo(p20.screenY, 10);
    expect(p40.screenY).toBeLessThan(p20.screenY);
    expect(p60.screenY).toBeLessThan(p40.screenY);

    const pNeg = projectPoint(300, 620, 25, makeParams({ tiltDeg: -15 }));
    expect(pNeg).toEqual(projectPoint(300, 620, 25, makeParams({ tiltDeg: 0 })));

    const pOver = projectPoint(300, 620, 25, makeParams({ tiltDeg: 85 }));
    expect(pOver).toEqual(projectPoint(300, 620, 25, makeParams({ tiltDeg: 60 })));
  });

  it('guards behind-camera singularities (denom <= MIN_DENOM) and invalid camera params without NaN/Infinity', () => {
    const params = makeParams({ tiltDeg: 60 });
    const sin60 = Math.sin((60 * Math.PI) / 180);
    const vAtZeroDenom = PERSPECTIVE_FOCAL_LENGTH / sin60;

    const atZero = projectPoint(500, 500 + vAtZeroDenom, 0, params);
    expect(atZero.visible).toBe(false);
    expect(atZero.k).toBeCloseTo(PERSPECTIVE_FOCAL_LENGTH / MIN_DENOM, 10);
    expect(Number.isFinite(atZero.screenX)).toBe(true);
    expect(Number.isFinite(atZero.screenY)).toBe(true);

    const behind = projectPoint(650, 2500, 10, params);
    expect(behind.visible).toBe(false);
    expect(behind.k).toBe(PERSPECTIVE_FOCAL_LENGTH / MIN_DENOM);
    expect(Number.isFinite(behind.screenX)).toBe(true);
    expect(Number.isFinite(behind.screenY)).toBe(true);

    for (const invalid of [
      makeParams({ zoom: 0 }),
      makeParams({ zoom: -1 }),
      makeParams({ focalLength: 0 }),
      makeParams({ focalLength: -50 }),
      makeParams({ panX: Number.NaN }),
    ]) {
      const res = projectPoint(500, 500, 0, invalid);
      expect(res.visible).toBe(false);
      expect(res.k).toBe(0);
      expect(Number.isFinite(res.screenX)).toBe(true);
      expect(Number.isFinite(res.screenY)).toBe(true);
    }
  });

  it('inverts visible ground points via screenToWorld with < 1e-6 error across tilts, zooms, and pans', () => {
    const worldSamples = [
      { x: 0, y: 0 },
      { x: 500, y: 500 },
      { x: 1000, y: 1000 },
      { x: 237, y: 781 },
      { x: 842, y: 164 },
    ];

    for (const tiltDeg of [0, 15, 20, 40, 60]) {
      for (const zoom of [0.5, 1, 2.5]) {
        for (const [panX, panY] of [
          [-150, 90],
          [0, 0],
          [120, -110],
        ]) {
          const params = makeParams({ tiltDeg, zoom, panX, panY });
          for (const pt of worldSamples) {
            const proj = projectPoint(pt.x, pt.y, 0, params);
            if (!proj.visible) {
              continue;
            }
            const inv = screenToWorld(proj.screenX, proj.screenY, params);
            expect(inv).not.toBeNull();
            expect(Math.abs(inv!.x - pt.x)).toBeLessThan(1e-6);
            expect(Math.abs(inv!.y - pt.y)).toBeLessThan(1e-6);
          }
        }
      }
    }
  });

  it('returns null from screenToWorld at or above the horizon and non-null just below', () => {
    for (const tiltDeg of [40, 60]) {
      const params = makeParams({ tiltDeg });
      const rad = (tiltDeg * Math.PI) / 180;
      const horizonY =
        params.screenCenterY - (PERSPECTIVE_FOCAL_LENGTH * Math.cos(rad)) / Math.sin(rad);

      expect(screenToWorld(params.screenCenterX, horizonY, params)).toBeNull();
      expect(screenToWorld(params.screenCenterX, -5000, params)).toBeNull();

      const below = screenToWorld(params.screenCenterX, horizonY + 1, params);
      expect(below).not.toBeNull();
      expect(Number.isFinite(below!.x)).toBe(true);
      expect(Number.isFinite(below!.y)).toBe(true);
    }
  });

  it('supports 3-arg, 5-arg, and 7-arg screenToWorldClamped overloads and stays bounded past the horizon', () => {
    const params60 = makeParams({ tiltDeg: 60 });
    const projCenter = projectPoint(500, 500, 0, params60);

    const c3 = screenToWorldClamped(projCenter.screenX, projCenter.screenY, params60);
    expect(c3.x).toBeCloseTo(500, 6);
    expect(c3.y).toBeCloseTo(500, 6);

    // 5-arg (width, height) must clamp to [0, 800] x [0, 600], NOT set minX=800
    const c5 = screenToWorldClamped(projCenter.screenX, projCenter.screenY, params60, 800, 600);
    expect(c5.x).toBeCloseTo(500, 6);
    expect(c5.y).toBeCloseTo(500, 6);

    // 7-arg (minX, maxX, minY, maxY)
    const c7 = screenToWorldClamped(2000, -5000, params60, 100, 900, 50, 950);
    expect(c7.x).toBe(900);
    expect(c7.y).toBe(50);

    // Horizon stress sweep across extreme screenY and subnormal tilt
    for (const tiltDeg of [60, 1e-9]) {
      const p = makeParams({ tiltDeg });
      for (const sx of [-1000, 0, p.screenCenterX, 2000]) {
        for (const sy of [-5000, -1500, 0, 500, 5000]) {
          const res = screenToWorldClamped(sx, sy, p, 100, 900, 50, 950);
          expect(Number.isFinite(res.x)).toBe(true);
          expect(Number.isFinite(res.y)).toBe(true);
          expect(res.x).toBeGreaterThanOrEqual(100);
          expect(res.x).toBeLessThanOrEqual(900);
          expect(res.y).toBeGreaterThanOrEqual(50);
          expect(res.y).toBeLessThanOrEqual(950);
        }
      }
    }
  });

  it('maintains horizon continuity in screenToWorldClamped across horizon ± 0.1 px', () => {
    const cases = [
      { zoom: 5, tiltDeg: 60, dx: 0.5 },
      { zoom: 5, tiltDeg: 60, dx: -0.5 },
      { zoom: 2.5, tiltDeg: 20, dx: 0.05 },
      { zoom: 2.5, tiltDeg: 20, dx: -0.05 },
      { zoom: 1, tiltDeg: 40, dx: 0 },
    ];

    for (const { zoom, tiltDeg, dx } of cases) {
      const params = makeParams({
        zoom,
        tiltDeg,
        panX: 0,
        panY: 0,
        screenCenterX: 640,
        screenCenterY: 360,
      });
      const rad = (tiltDeg * Math.PI) / 180;
      const horizonY =
        params.screenCenterY - (PERSPECTIVE_FOCAL_LENGTH * Math.cos(rad)) / Math.sin(rad);

      const above = screenToWorldClamped(params.screenCenterX + dx, horizonY - 0.1, params);
      const below = screenToWorldClamped(params.screenCenterX + dx, horizonY + 0.1, params);

      expect(above.y).toBe(0);
      expect(below.y).toBe(0);
      expect(Math.abs(above.x - below.x)).toBeLessThanOrEqual(1);
    }

    // Tight horizon limit continuity across all zoom in [0.4..5], tiltDeg in [20..60], |dx| >= 0.05
    for (const zoom of [0.4, 1, 2.5, 5]) {
      for (const tiltDeg of [20, 40, 60]) {
        const params = makeParams({ zoom, tiltDeg, screenCenterX: 640, screenCenterY: 360 });
        const rad = (tiltDeg * Math.PI) / 180;
        const horizonY =
          params.screenCenterY - (PERSPECTIVE_FOCAL_LENGTH * Math.cos(rad)) / Math.sin(rad);
        for (const dx of [-3, -0.5, -0.05, 0, 0.05, 0.5, 3]) {
          const above = screenToWorldClamped(params.screenCenterX + dx, horizonY - 1e-4, params);
          const below = screenToWorldClamped(params.screenCenterX + dx, horizonY + 1e-4, params);
          expect(Math.abs(above.x - below.x)).toBeLessThanOrEqual(1);
          expect(Math.abs(above.y - below.y)).toBeLessThanOrEqual(1);
        }
      }
    }
  });
});

describe('map-renderer-25d near-plane clipping & anchors', () => {
  it('clips polygons in (u, v, h) space and keeps bit-identical shared Voronoi edges', () => {
    const params = makeParams({ tiltDeg: 60 });
    const vMax = getNearPlaneVMax(params);
    expect(Number.isFinite(vMax)).toBe(true);

    const a = { u: -100, v: vMax - 120, h: 10 };
    const b = { u: 140, v: vMax + 180, h: 30 };
    const c = { u: -160, v: vMax + 180, h: 20 };
    const d = { u: 220, v: vMax - 80, h: 14 };

    const clipped1 = clipPolygonNearPlane([a, b, c], vMax);
    expect(clipped1.length).toBeGreaterThanOrEqual(3);
    for (const vtx of clipped1) {
      expect(vtx.v).toBeLessThanOrEqual(vMax + 1e-9);
    }

    // Adjacent polygon shares edge (a, b) in opposite direction (b, a)
    const clipped2 = clipPolygonNearPlane([d, b, a], vMax);
    expect(clipped2.length).toBeGreaterThanOrEqual(3);

    const edge1 = clipped1.find((p) => Math.abs(p.v - vMax) < 1e-9 && p.u > -50 && p.u < 50);
    const edge2 = clipped2.find((p) => Math.abs(p.v - vMax) < 1e-9 && p.u > -50 && p.u < 50);
    expect(edge1).toBeDefined();
    expect(edge2).toBeDefined();
    expect(Object.is(edge1!.u, edge2!.u)).toBe(true);
    expect(Object.is(edge1!.v, edge2!.v)).toBe(true);
    expect(Object.is(edge1!.h, edge2!.h)).toBe(true);

    // Direct segment check in opposite directions also yields bit-identical intersection
    const segAB = clipSegmentNearPlane(a, b, vMax);
    const segBA = clipSegmentNearPlane(b, a, vMax);
    expect(segAB).not.toBeNull();
    expect(segBA).not.toBeNull();
    expect(Object.is(segAB![1].u, segBA![0].u)).toBe(true);
    expect(Object.is(segAB![1].v, segBA![0].v)).toBe(true);
    expect(Object.is(segAB![1].h, segBA![0].h)).toBe(true);
  });

  it('splits polylines dipping past vMax into separate visible sub-polylines', () => {
    const vMax = 1000;
    const pts = [
      { u: -100, v: 800, h: 0 },
      { u: -50, v: 950, h: 0 },
      { u: 0, v: 1200, h: 0 },
      { u: 50, v: 920, h: 0 },
      { u: 100, v: 750, h: 0 },
    ];
    const pieces = clipPolylineNearPlane(pts, vMax);
    expect(pieces.length).toBe(2);
    expect(pieces[0].length).toBeGreaterThanOrEqual(2);
    expect(pieces[1].length).toBeGreaterThanOrEqual(2);
    for (const piece of pieces) {
      for (const vtx of piece) {
        expect(vtx.v).toBeLessThanOrEqual(vMax + 1e-9);
      }
    }

    expect(
      clipSegmentNearPlane({ u: 0, v: 1100, h: 0 }, { u: 10, v: 1300, h: 0 }, vMax),
    ).toBeNull();
    expect(toClipVertex(500, 500, 12, makeParams()).h).toBe(12);
  });

  it('rejects tangent-only and zero-area polygons in clipPolygonNearPlane while preserving shared edges', () => {
    const vMax = 1000;

    // Single-vertex tangent touch at v === vMax
    const singleVertexTangent = clipPolygonNearPlane(
      [
        { u: 10, v: vMax, h: 5 },
        { u: 20, v: vMax + 100, h: 5 },
        { u: 0, v: vMax + 100, h: 5 },
      ],
      vMax,
    );
    expect(singleVertexTangent).toEqual([]);

    // Single-edge tangent touch along v === vMax
    const edgeTangent = clipPolygonNearPlane(
      [
        { u: 10, v: vMax, h: 5 },
        { u: 30, v: vMax, h: 5 },
        { u: 20, v: vMax + 100, h: 5 },
      ],
      vMax,
    );
    expect(edgeTangent).toEqual([]);

    // Collinear vertices on v === vMax
    const collinear = clipPolygonNearPlane(
      [
        { u: 10, v: vMax, h: 5 },
        { u: 20, v: vMax, h: 5 },
        { u: 30, v: vMax, h: 5 },
      ],
      vMax,
    );
    expect(collinear).toEqual([]);

    // Genuine interior polygon with a vertex touching v === vMax has no duplicates and keeps bit-identical shared edges
    const a = { u: -40, v: vMax - 50, h: 10 };
    const b = { u: 20, v: vMax, h: 5 };
    const c = { u: 60, v: vMax + 80, h: 20 };
    const d = { u: -90, v: vMax - 60, h: 12 };

    const poly1 = clipPolygonNearPlane([a, b, c], vMax);
    const poly2 = clipPolygonNearPlane([d, a, c], vMax);
    expect(poly1.length).toBe(3);
    expect(poly2.length).toBe(4);
    for (let i = 0; i < poly1.length; i++) {
      const curr = poly1[i];
      const next = poly1[(i + 1) % poly1.length];
      expect(
        Math.abs(curr.u - next.u) > 1e-9 ||
          Math.abs(curr.v - next.v) > 1e-9 ||
          Math.abs(curr.h - next.h) > 1e-9,
      ).toBe(true);
    }
    expect(Object.is(poly1[0].u, poly2[3].u)).toBe(true);
    expect(Object.is(poly1[0].v, poly2[3].v)).toBe(true);
    expect(Object.is(poly1[0].h, poly2[3].h)).toBe(true);
  });

  it('produces zero consecutive duplicate vertices in clipPolylineNearPlane on v === vMax boundary crossings', () => {
    const vMax = 1000;

    // Visits v === vMax right before exiting and right when re-entering
    const crossing = clipPolylineNearPlane(
      [
        { u: 0, v: vMax - 50, h: 0 },
        { u: 10, v: vMax, h: 0 },
        { u: 20, v: vMax + 40, h: 0 },
        { u: 30, v: vMax, h: 0 },
        { u: 40, v: vMax - 50, h: 0 },
      ],
      vMax,
    );

    expect(crossing.length).toBe(2);
    for (const piece of crossing) {
      expect(piece.length).toBe(2);
      for (let i = 0; i < piece.length - 1; i++) {
        const p = piece[i];
        const q = piece[i + 1];
        expect(
          Math.abs(p.u - q.u) > 1e-9 || Math.abs(p.v - q.v) > 1e-9 || Math.abs(p.h - q.h) > 1e-9,
        ).toBe(true);
      }
    }

    // Pure tangent-only polyline visiting v === vMax from outside returns empty
    const tangentOnly = clipPolylineNearPlane(
      [
        { u: 0, v: vMax + 20, h: 0 },
        { u: 10, v: vMax, h: 0 },
        { u: 20, v: vMax + 20, h: 0 },
      ],
      vMax,
    );
    expect(tangentOnly).toEqual([]);
  });

  it('computes projected anchors and performs front-to-back hit testing', () => {
    const params = makeParams({ tiltDeg: 40 });
    const carte: CarteDto = {
      largeur: 1000,
      hauteur: 1000,
      centreX: 500,
      centreY: 500,
      emplacements: [
        { numero: 1, coordX: 500, coordY: 505, occupe: true },
        { numero: 2, coordX: 250, coordY: 300, occupe: false },
      ],
    };

    const anchors = getProjectedAnchors(carte, params);
    expect(Object.keys(anchors)).toEqual(['market', 'emp-1', 'emp-2']);
    const p1 = projectPoint(500, 505, 0, params);
    expect(anchors['emp-1']).toEqual({
      screenX: p1.screenX,
      screenY: p1.screenY,
      visible: p1.visible,
    });

    // Overlapping market and emp-1: emp-1 has larger screenY (further south / closer to camera)
    const hit = hitTestAnchor(p1.screenX, p1.screenY, anchors, 24);
    expect(hit).toEqual({ type: 'EMPLACEMENT', numero: 1 });

    // Far away returns null
    expect(hitTestAnchor(-9999, -9999, anchors, 24)).toBeNull();

    // Empty emplacements still returns market
    const emptyAnchors = getProjectedAnchors({ ...carte, emplacements: [] }, params);
    expect(Object.keys(emptyAnchors)).toEqual(['market']);
  });
});

describe('renderMap25D & MapRenderer25D procedural canvas passes', () => {
  it('sorts 2.5D billboard features ascending by Y (north before south)', () => {
    const { carte, map } = makeRichTestMap('VALLEE_ROYALE', true);
    map.fiefs = [];
    carte.emplacements = [];
    map.features = [
      {
        id: 'south-keep',
        kind: 'CASTLE_KEEP',
        x: 500,
        y: 800,
        scale: 1,
        primaryColor: '#222222',
        secondaryColor: '#fefefe',
        seedOffset: 0.1,
      },
      {
        id: 'north-keep',
        kind: 'CASTLE_KEEP',
        x: 500,
        y: 200,
        scale: 1,
        primaryColor: '#111111',
        secondaryColor: '#fefefe',
        seedOffset: 0.2,
      },
    ];

    const trace = createMockCanvasContext();
    const state: RenderMapState = {
      projection: makeParams({ tiltDeg: 40 }),
      showLabels: false,
    };

    renderMap25D(trace.ctx, 1200, 800, map, carte, state, 0);

    const idxNorth = trace.fillStyles.indexOf('#111111');
    const idxSouth = trace.fillStyles.indexOf('#222222');
    expect(idxNorth).toBeGreaterThan(-1);
    expect(idxSouth).toBeGreaterThan(-1);
    expect(idxNorth).toBeLessThan(idxSouth);
  });

  it('draws solid borders when occupe=true and dashed [8, 6] borders when occupe=false, then resets', () => {
    const { carte: cOcc, map: mOcc } = makeRichTestMap('VALLEE_ROYALE', true);
    const traceOcc = createMockCanvasContext();
    renderMap25D(
      traceOcc.ctx,
      1200,
      800,
      mOcc,
      cOcc,
      { projection: makeParams({ tiltDeg: 40 }) },
      0,
    );
    expect(traceOcc.setLineDashCalls.some((d) => d.length === 2 && d[0] === 8 && d[1] === 6)).toBe(
      false,
    );
    expect(traceOcc.setLineDashCalls[traceOcc.setLineDashCalls.length - 1]).toEqual([]);

    const { carte: cFree, map: mFree } = makeRichTestMap('VALLEE_ROYALE', false);
    const traceFree = createMockCanvasContext();
    renderMap25D(
      traceFree.ctx,
      1200,
      800,
      mFree,
      cFree,
      { projection: makeParams({ tiltDeg: 40 }) },
      0,
    );
    expect(traceFree.setLineDashCalls.some((d) => d.length === 2 && d[0] === 8 && d[1] === 6)).toBe(
      true,
    );
    expect(traceFree.setLineDashCalls[traceFree.setLineDashCalls.length - 1]).toEqual([]);
  });

  it('varies animated water, river glint, banners, and windmill sails deterministically with timeMs', () => {
    const { carte, map } = makeRichTestMap('CONFLUENCES', true);
    const state: RenderMapState = {
      projection: makeParams({ tiltDeg: 40 }),
    };

    const traceT0 = createMockCanvasContext();
    renderMap25D(traceT0.ctx, 1200, 800, map, carte, state, 0);

    const traceT1 = createMockCanvasContext();
    renderMap25D(traceT1.ctx, 1200, 800, map, carte, state, 1250);

    expect(traceT0.lineDashOffsets).not.toEqual(traceT1.lineDashOffsets);
    expect(traceT0.pathCoords).not.toEqual(traceT1.pathCoords);
  });

  it('falls back to arcTo when ctx.roundRect is absent', () => {
    const { carte, map } = makeRichTestMap('VALLEE_ROYALE', true);
    const traceNoRoundRect = createMockCanvasContext({ includeRoundRect: false });
    const renderer = new MapRenderer25D();

    renderer.render(
      traceNoRoundRect.ctx,
      1200,
      800,
      map,
      carte,
      { projection: makeParams({ tiltDeg: 40 }) },
      500,
    );

    expect(traceNoRoundRect.roundRectCount).toBe(0);
    expect(traceNoRoundRect.arcToCount).toBeGreaterThan(0);
  });

  it('handles all 5 archetypes, occupancy states, clipping zoom, inspection/dragPreview, and empty/sparse maps safely', () => {
    for (const arch of ARCHETYPES_CARTE) {
      for (const occupe of [true, false]) {
        const { carte, map } = makeRichTestMap(arch.id, occupe);
        for (const tiltDeg of [0, 40, 60]) {
          const trace = createMockCanvasContext();
          const inspection: InspectionInfo = {
            targetType: 'EMPLACEMENT',
            numero: 1,
            label: '#1 · Valdore',
            coordX: 240,
            coordY: 320,
            distanceMarche: 316,
            distancesRoyaumes: [
              { numero: 2, nom: 'Azurmont', occupe, couleurPrimaire: '#1d4ed8', distance: 632 },
            ],
          };

          renderMap25D(
            trace.ctx,
            1200,
            800,
            map,
            carte,
            {
              projection: makeParams({ tiltDeg, zoom: 2.5, panY: 400 }),
              hoveredTarget: { type: 'MARCHE' },
              selectedTarget: { type: 'EMPLACEMENT', numero: 1 },
              inspection,
              dragPreview: { numero: 2, coordX: 700, coordY: 620 },
              highlightMarketZone: true,
            },
            250,
          );

          for (const pt of trace.pathCoords) {
            expect(Number.isFinite(pt.x)).toBe(true);
            expect(Number.isFinite(pt.y)).toBe(true);
          }

          const texts = trace.fillTexts.map((t) => t.text);
          expect(texts).toContain('Marché Central');
          if (occupe) {
            expect(texts).toContain('#1 · Valdore');
            expect(texts).toContain('#2 · Azurmont');
          } else {
            expect(texts).toContain('#1 · Libre');
            expect(texts).toContain('#2 · Libre');
          }
        }
      }
    }

    // Empty/sparse map & invalid dimensions never throw
    const emptyMap: GeneratedMap = {
      seed: 'empty',
      archetype: 'LAC_CENTRAL',
      width: 1000,
      height: 1000,
      centreX: 500,
      centreY: 500,
      marketRadius: 75,
      marketFreeZoneRadius: 110,
      cells: [],
      fiefs: [],
      routes: [],
      features: [],
      riverPaths: [],
    };
    const emptyCarte: CarteDto = {
      largeur: 1000,
      hauteur: 1000,
      centreX: 500,
      centreY: 500,
      emplacements: [],
    };

    const traceEmpty = createMockCanvasContext();
    expect(() =>
      renderMap25D(
        traceEmpty.ctx,
        800,
        600,
        emptyMap,
        emptyCarte,
        { projection: createDefaultProjectionParams(800, 600, emptyCarte) },
        0,
      ),
    ).not.toThrow();
    expect(traceEmpty.fillTexts.map((t) => t.text)).toContain('Marché Central');

    // Invalid canvas dimensions return safely
    const traceInvalid = createMockCanvasContext();
    expect(() =>
      renderMap25D(
        traceInvalid.ctx,
        0,
        -100,
        emptyMap,
        emptyCarte,
        { projection: makeParams() },
        0,
      ),
    ).not.toThrow();
    expect(traceInvalid.pathCoords.length).toBe(0);
  });

  it('maintains save/restore balance, isolates pre-dirty setLineDash state, and sanitizes hostile NaN DTO fields', () => {
    const { carte, map } = makeRichTestMap('VALLEE_ROYALE', true);
    const trace = createMockCanvasContext();

    // Dirty caller context before renderMap25D
    trace.ctx.setLineDash([4, 4]);
    trace.ctx.lineDashOffset = 7;

    renderMap25D(
      trace.ctx,
      1200,
      800,
      map,
      carte,
      { projection: makeParams({ tiltDeg: 40 }) },
      100,
    );

    expect(trace.saveCount).toBeGreaterThan(0);
    expect(trace.saveCount).toBe(trace.restoreCount);
    // First stroke inside Pass 1 must see cleared dash state, never [4, 4] or offset 7
    expect(trace.strokeStates.length).toBeGreaterThan(0);
    expect(trace.strokeStates[0]).toEqual({ dash: [], dashOffset: 0 });
    // Caller context state restored after renderMap25D
    expect(trace.ctx.getLineDash()).toEqual([4, 4]);
    expect(trace.ctx.lineDashOffset).toBe(7);

    // Hostile NaN fields across secondary DTO properties must not throw or emit NaN canvas coordinates
    const hostileCarte: CarteDto = {
      ...carte,
      emplacements: [
        { numero: 1, coordX: Number.NaN, coordY: 320, occupe: true },
        ...carte.emplacements.slice(1),
      ],
    };
    const hostileMap: GeneratedMap = {
      ...map,
      cells: map.cells.map((c, idx) =>
        idx === 0
          ? {
              ...c,
              center: { x: Number.NaN, y: c.center.y },
              polygon: c.polygon.map((p, pi) => (pi === 0 ? { x: Number.NaN, y: p.y } : p)),
            }
          : c,
      ),
      features: map.features.map((f) => ({
        ...f,
        seedOffset: Number.NaN,
        rotation: Number.NaN,
      })),
      riverPaths: [
        [
          { x: Number.NaN, y: 200 },
          { x: 360, y: 400 },
          { x: 620, y: 740 },
        ],
      ],
      fiefs: map.fiefs.map((f) => ({
        ...f,
        borderSegments: [
          ...f.borderSegments,
          { a: { x: Number.NaN, y: 260 }, b: { x: 320, y: 260 }, dashed: false, color: '#b91c1c' },
        ],
      })),
      routes: map.routes.map((r) => ({
        ...r,
        path: [{ x: Number.NaN, y: 320 }, ...r.path],
        bridges: [
          ...r.bridges,
          {
            start: { x: 350, y: 396 },
            end: { x: 390, y: 424 },
            midpoint: { x: Number.NaN, y: 410 },
            angle: Number.NaN,
            length: 48.8,
          },
        ],
      })),
    };

    const hostileTrace = createMockCanvasContext();
    expect(() =>
      renderMap25D(
        hostileTrace.ctx,
        1200,
        800,
        hostileMap,
        hostileCarte,
        {
          projection: makeParams({ tiltDeg: 40 }),
          dragPreview: { numero: 1, coordX: Number.NaN, coordY: 400 },
          inspection: {
            targetType: 'EMPLACEMENT',
            numero: 1,
            label: '#1',
            coordX: Number.NaN,
            coordY: 320,
            distanceMarche: Number.NaN,
            distancesRoyaumes: [],
          },
        },
        100,
      ),
    ).not.toThrow();

    expect(hostileTrace.saveCount).toBe(hostileTrace.restoreCount);
    for (const pt of hostileTrace.pathCoords) {
      expect(Number.isFinite(pt.x)).toBe(true);
      expect(Number.isFinite(pt.y)).toBe(true);
    }
  });

  it('pins terraformed MONTAGNE cell (elevation 0.95) to h = 0 matching screenToWorld', () => {
    const params = makeParams({ tiltDeg: 45, zoom: 1.5, panX: 25, panY: -15 });
    const square = [
      { x: 420, y: 420 },
      { x: 580, y: 420 },
      { x: 580, y: 580 },
      { x: 420, y: 580 },
    ];
    const baseCarte: CarteDto = {
      largeur: 1000,
      hauteur: 1000,
      centreX: 500,
      centreY: 500,
      emplacements: [],
    };
    const makeSingleCellMap = (terraformed: boolean): GeneratedMap => ({
      seed: 'terraform-test',
      archetype: 'HAUTES_TERRES',
      width: 1000,
      height: 1000,
      centreX: 500,
      centreY: 500,
      marketRadius: 75,
      marketFreeZoneRadius: MARKET_FREE_ZONE_RADIUS,
      cells: [
        {
          id: 1,
          center: { x: 500, y: 500 },
          polygon: square,
          neighbors: [],
          elevation: 0.95,
          moisture: 0.3,
          isWater: false,
          biome: 'MONTAGNE',
          isBoundary: false,
          terraformed,
          isMarketZone: false,
          ownerNumero: null,
          geodesicDistance: 0,
        },
      ],
      fiefs: [],
      routes: [],
      features: [],
      riverPaths: [],
    });

    const traceFlat = createMockCanvasContext();
    renderMap25D(
      traceFlat.ctx,
      1200,
      800,
      makeSingleCellMap(true),
      baseCarte,
      { projection: params, showLabels: false },
      0,
    );

    // Pass 1 emits 5 fills (shadow, south, west, east, top board); index 5 is the Pass 2 cell polygon
    const cellFillFlat = traceFlat.fillCalls[5];
    expect(cellFillFlat).toBeDefined();
    expect(cellFillFlat.points.length).toBe(4);
    for (let i = 0; i < 4; i++) {
      const inv = screenToWorld(cellFillFlat.points[i].x, cellFillFlat.points[i].y, params);
      expect(inv).not.toBeNull();
      expect(Math.abs(inv!.x - square[i].x)).toBeLessThan(1e-6);
      expect(Math.abs(inv!.y - square[i].y)).toBeLessThan(1e-6);
    }

    // Contrast with non-terraformed MONTAGNE at elevation 0.95 (must lift upward, so h=0 inverse shifts y north)
    const traceElevated = createMockCanvasContext();
    renderMap25D(
      traceElevated.ctx,
      1200,
      800,
      makeSingleCellMap(false),
      baseCarte,
      { projection: params, showLabels: false },
      0,
    );
    const cellFillElevated = traceElevated.fillCalls[5];
    expect(cellFillElevated).toBeDefined();
    const invElevated = screenToWorld(
      cellFillElevated.points[0].x,
      cellFillElevated.points[0].y,
      params,
    );
    expect(invElevated).not.toBeNull();
    expect(Math.abs(invElevated!.y - square[0].y)).toBeGreaterThan(10);
  });
});
