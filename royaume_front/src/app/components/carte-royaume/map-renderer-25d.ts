import {
  ArchetypeCarte,
  BiomeType,
  CarteDto,
  DEFAULT_CARTE_CENTRE_X,
  DEFAULT_CARTE_CENTRE_Y,
  DEFAULT_CARTE_HEIGHT,
  DEFAULT_CARTE_WIDTH,
  EmplacementCarteDto,
  FeatureKind,
  FiefDomain,
  GeneratedMap,
  InspectionInfo,
  MARKET_FREE_ZONE_RADIUS,
  MapFeature25D,
  Point2D,
  VoronoiCell,
} from '../../models/carte.model';
import { createDefaultRoyaume, euclideanDistance } from '../../services/placement-royaumes.util';

// ============================================================================
// R4 Projection & Clipping Constants
// ============================================================================

export const PERSPECTIVE_FOCAL_LENGTH = 1400;
export const MIN_BILLBOARD_TILT_DEG = 20;
export const MIN_DENOM = 40;
export const MIN_TILT_DEG = 0;
export const MAX_TILT_DEG = 60;
export const DEFAULT_TILT_DEG = 40;

const HORIZON_EPSILON_MIN = 1e-6;
const SLAB_THICKNESS = 22;
const SPATIAL_GRID_SIZE = 20;

// ============================================================================
// Public Types & Interfaces
// ============================================================================

export interface ProjectionParams {
  centreX: number;
  centreY: number;
  zoom: number;
  panX: number;
  panY: number;
  tiltDeg: number;
  screenCenterX: number;
  screenCenterY: number;
  focalLength?: number;
}

export interface ProjectedPoint {
  screenX: number;
  screenY: number;
  zDepth: number;
  k: number;
  u: number;
  v: number;
  visible: boolean;
}

export interface ProjectedAnchor {
  screenX: number;
  screenY: number;
  visible: boolean;
}

export type ProjectedAnchorMap = Record<string, ProjectedAnchor>;

export type MapInteractionTarget = { type: 'MARCHE' } | { type: 'EMPLACEMENT'; numero: number };

export interface DragPreviewState {
  numero: number;
  coordX: number;
  coordY: number;
}

export interface RenderMapState {
  projection: ProjectionParams;
  hoveredTarget?: MapInteractionTarget | null;
  selectedTarget?: MapInteractionTarget | null;
  inspection?: InspectionInfo | null;
  dragPreview?: DragPreviewState | null;
  highlightedNumeros?: readonly number[];
  highlightMarketZone?: boolean;
  showLabels?: boolean;
  showBorders?: boolean;
  showRoutes?: boolean;
}

export interface ClipVertex {
  u: number;
  v: number;
  h: number;
}

// ============================================================================
// Pure Math Helpers & Projection
// ============================================================================

function normZero(n: number): number {
  return n === 0 ? 0 : n;
}

function isFinitePoint(pt: Point2D | null | undefined): pt is Point2D {
  return !!pt && Number.isFinite(pt.x) && Number.isFinite(pt.y);
}

function clamp(value: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, value));
}

function clampTiltDeg(tiltDeg: number): number {
  return clamp(tiltDeg, MIN_TILT_DEG, MAX_TILT_DEG);
}

function resolveFocalLength(params: ProjectionParams): number {
  return params.focalLength ?? PERSPECTIVE_FOCAL_LENGTH;
}

function isValidProjectionParams(params: ProjectionParams): boolean {
  const f = resolveFocalLength(params);
  return (
    Number.isFinite(params.centreX) &&
    Number.isFinite(params.centreY) &&
    Number.isFinite(params.zoom) &&
    params.zoom > 0 &&
    Number.isFinite(params.panX) &&
    Number.isFinite(params.panY) &&
    Number.isFinite(params.tiltDeg) &&
    Number.isFinite(params.screenCenterX) &&
    Number.isFinite(params.screenCenterY) &&
    Number.isFinite(f) &&
    f > 0
  );
}

function invisibleFallbackPoint(params: ProjectionParams): ProjectedPoint {
  return {
    screenX: Number.isFinite(params.screenCenterX) ? normZero(params.screenCenterX) : 0,
    screenY: Number.isFinite(params.screenCenterY) ? normZero(params.screenCenterY) : 0,
    zDepth: 0,
    k: 0,
    u: 0,
    v: 0,
    visible: false,
  };
}

export function createDefaultProjectionParams(
  width: number,
  height: number,
  carte?: Pick<CarteDto, 'centreX' | 'centreY'>,
): ProjectionParams {
  const safeW = Number.isFinite(width) && width > 0 ? width : DEFAULT_CARTE_WIDTH;
  const safeH = Number.isFinite(height) && height > 0 ? height : DEFAULT_CARTE_HEIGHT;
  const centreX =
    carte && Number.isFinite(carte.centreX) ? normZero(carte.centreX) : DEFAULT_CARTE_CENTRE_X;
  const centreY =
    carte && Number.isFinite(carte.centreY) ? normZero(carte.centreY) : DEFAULT_CARTE_CENTRE_Y;
  const zoom = Math.max(0.25, Math.min(safeW, safeH) / 1120);

  return {
    centreX,
    centreY,
    zoom,
    panX: 0,
    panY: 0,
    tiltDeg: DEFAULT_TILT_DEG,
    screenCenterX: normZero(safeW * 0.5),
    screenCenterY: normZero(safeH * 0.5),
    focalLength: PERSPECTIVE_FOCAL_LENGTH,
  };
}

export function projectPoint(
  x: number,
  y: number,
  h: number,
  params: ProjectionParams,
): ProjectedPoint {
  if (
    !Number.isFinite(x) ||
    !Number.isFinite(y) ||
    !Number.isFinite(h) ||
    !isValidProjectionParams(params)
  ) {
    return invisibleFallbackPoint(params);
  }

  const focalLength = resolveFocalLength(params);
  const clampedTilt = clampTiltDeg(params.tiltDeg);
  const alpha = (clampedTilt * Math.PI) / 180;
  const sinA = Math.sin(alpha);
  const cosA = Math.cos(alpha);
  const sinEff = Math.sin((Math.max(clampedTilt, MIN_BILLBOARD_TILT_DEG) * Math.PI) / 180);

  const u = normZero((x - params.centreX) * params.zoom + params.panX);
  const v = normZero((y - params.centreY) * params.zoom + params.panY);
  const zDepth = normZero(-v * sinA);

  const denom = focalLength - v * sinA;
  const visible = denom > MIN_DENOM;
  const safeDenom = visible ? denom : MIN_DENOM;
  const k = focalLength / safeDenom;

  const screenX = normZero(params.screenCenterX + u * k);
  const screenY = normZero(params.screenCenterY + (v * cosA - h * sinEff) * k);

  if (
    !Number.isFinite(u) ||
    !Number.isFinite(v) ||
    !Number.isFinite(zDepth) ||
    !Number.isFinite(k) ||
    !Number.isFinite(screenX) ||
    !Number.isFinite(screenY)
  ) {
    return invisibleFallbackPoint(params);
  }

  return {
    screenX,
    screenY,
    zDepth,
    k,
    u,
    v,
    visible,
  };
}

export function screenToWorld(
  screenX: number,
  screenY: number,
  params: ProjectionParams,
): Point2D | null {
  if (!Number.isFinite(screenX) || !Number.isFinite(screenY) || !isValidProjectionParams(params)) {
    return null;
  }

  const focalLength = resolveFocalLength(params);
  const clampedTilt = clampTiltDeg(params.tiltDeg);
  const alpha = (clampedTilt * Math.PI) / 180;
  const sinA = Math.sin(alpha);
  const cosA = Math.cos(alpha);

  const dx = screenX - params.screenCenterX;
  const dy = screenY - params.screenCenterY;
  const denom = focalLength * cosA + dy * sinA;

  if (!Number.isFinite(denom) || denom <= HORIZON_EPSILON_MIN) {
    return null;
  }

  const v = (focalLength * dy) / denom;
  const u = (focalLength * cosA * dx) / denom;
  const x = normZero(params.centreX + (u - params.panX) / params.zoom);
  const y = normZero(params.centreY + (v - params.panY) / params.zoom);

  if (!Number.isFinite(x) || !Number.isFinite(y)) {
    return null;
  }

  return { x, y };
}

export function screenToWorldClamped(
  screenX: number,
  screenY: number,
  params: ProjectionParams,
): Point2D;
export function screenToWorldClamped(
  screenX: number,
  screenY: number,
  params: ProjectionParams,
  width: number,
  height: number,
): Point2D;
export function screenToWorldClamped(
  screenX: number,
  screenY: number,
  params: ProjectionParams,
  minX: number,
  maxX: number,
  minY: number,
  maxY: number,
): Point2D;
export function screenToWorldClamped(
  screenX: number,
  screenY: number,
  params: ProjectionParams,
  a?: number,
  b?: number,
  c?: number,
  d?: number,
): Point2D {
  let rawMinX = 0;
  let rawMaxX = DEFAULT_CARTE_WIDTH;
  let rawMinY = 0;
  let rawMaxY = DEFAULT_CARTE_HEIGHT;

  if (c !== undefined && d !== undefined) {
    rawMinX = a ?? 0;
    rawMaxX = b ?? DEFAULT_CARTE_WIDTH;
    rawMinY = c;
    rawMaxY = d;
  } else if (a !== undefined && b !== undefined) {
    rawMinX = 0;
    rawMaxX = a;
    rawMinY = 0;
    rawMaxY = b;
  }

  const boundsValid =
    Number.isFinite(rawMinX) &&
    Number.isFinite(rawMaxX) &&
    Number.isFinite(rawMinY) &&
    Number.isFinite(rawMaxY);
  const minX = boundsValid ? Math.min(rawMinX, rawMaxX) : 0;
  const maxX = boundsValid ? Math.max(rawMinX, rawMaxX) : DEFAULT_CARTE_WIDTH;
  const minY = boundsValid ? Math.min(rawMinY, rawMaxY) : 0;
  const maxY = boundsValid ? Math.max(rawMinY, rawMaxY) : DEFAULT_CARTE_HEIGHT;

  const clampResult = (pt: Point2D): Point2D => ({
    x: normZero(clamp(pt.x, minX, maxX)),
    y: normZero(clamp(pt.y, minY, maxY)),
  });

  if (!Number.isFinite(screenX) || !Number.isFinite(screenY) || !isValidProjectionParams(params)) {
    const safeCx = Number.isFinite(params.centreX) ? params.centreX : DEFAULT_CARTE_CENTRE_X;
    const safeCy = Number.isFinite(params.centreY) ? params.centreY : DEFAULT_CARTE_CENTRE_Y;
    return clampResult({ x: safeCx, y: safeCy });
  }

  const direct = screenToWorld(screenX, screenY, params);
  if (direct !== null) {
    return clampResult(direct);
  }

  // At or above the horizon (denom <= HORIZON_EPSILON_MIN), return the exact asymptotic limit
  const dx = screenX - params.screenCenterX;
  const fallbackX =
    dx === 0 ? clamp(params.centreX - params.panX / params.zoom, minX, maxX) : dx > 0 ? maxX : minX;

  return clampResult({ x: fallbackX, y: minY });
}

// ============================================================================
// 1D Sutherland-Hodgman Near-Plane Clipping in (u, v, h) Space
// ============================================================================

export function getNearPlaneVMax(params: ProjectionParams): number {
  const focalLength = resolveFocalLength(params);
  if (!Number.isFinite(focalLength) || focalLength <= 0 || !Number.isFinite(params.tiltDeg)) {
    return Infinity;
  }
  const clampedTilt = clampTiltDeg(params.tiltDeg);
  const alpha = (clampedTilt * Math.PI) / 180;
  const sinA = Math.sin(alpha);
  if (sinA <= 1e-9) {
    return Infinity;
  }
  return (focalLength - MIN_DENOM) / sinA;
}

export function toClipVertex(
  x: number,
  y: number,
  h: number,
  params: ProjectionParams,
): ClipVertex {
  const u = normZero((x - params.centreX) * params.zoom + params.panX);
  const v = normZero((y - params.centreY) * params.zoom + params.panY);
  return { u, v, h };
}

function intersectNearPlane(a: ClipVertex, b: ClipVertex, vMax: number): ClipVertex {
  const p0 = a.v <= b.v ? a : b;
  const p1 = a.v <= b.v ? b : a;
  const dv = p1.v - p0.v;
  if (dv <= 1e-12) {
    return { u: p0.u, v: vMax, h: p0.h };
  }
  const t = Math.max(0, Math.min(1, (vMax - p0.v) / dv));
  return {
    u: p0.u + (p1.u - p0.u) * t,
    v: vMax,
    h: p0.h + (p1.h - p0.h) * t,
  };
}

function isSameClipVertex(a: ClipVertex, b: ClipVertex): boolean {
  return Math.abs(a.u - b.u) <= 1e-9 && Math.abs(a.v - b.v) <= 1e-9 && Math.abs(a.h - b.h) <= 1e-9;
}

export function clipPolygonNearPlane(vertices: readonly ClipVertex[], vMax: number): ClipVertex[] {
  if (vertices.length < 3) {
    return [];
  }
  if (!Number.isFinite(vMax)) {
    return vertices.map((vtx) => ({ ...vtx }));
  }

  const out: ClipVertex[] = [];
  let prev = vertices[vertices.length - 1];
  let prevInside = prev.v <= vMax;

  const pushUnique = (vtx: ClipVertex): void => {
    if (out.length === 0 || !isSameClipVertex(out[out.length - 1], vtx)) {
      out.push(vtx);
    }
  };

  for (let i = 0; i < vertices.length; i++) {
    const curr = vertices[i];
    const currInside = curr.v <= vMax;

    if (currInside) {
      if (!prevInside) {
        pushUnique(intersectNearPlane(prev, curr, vMax));
      }
      pushUnique({ ...curr });
    } else if (prevInside) {
      pushUnique(intersectNearPlane(prev, curr, vMax));
    }

    prev = curr;
    prevInside = currInside;
  }

  if (out.length > 1 && isSameClipVertex(out[out.length - 1], out[0])) {
    out.pop();
  }

  if (out.length < 3) {
    return [];
  }

  // Reject degenerate zero-area / tangent-only polygons (check UV, UH, VH projections so vertical diorama walls survive)
  const u0 = out[0].u;
  const v0 = out[0].v;
  const h0 = out[0].h;
  let twiceAreaUV = 0;
  let twiceAreaUH = 0;
  let twiceAreaVH = 0;
  for (let i = 1; i < out.length - 1; i++) {
    const p = out[i];
    const q = out[i + 1];
    const pu = p.u - u0;
    const pv = p.v - v0;
    const ph = p.h - h0;
    const qu = q.u - u0;
    const qv = q.v - v0;
    const qh = q.h - h0;
    twiceAreaUV += pu * qv - qu * pv;
    twiceAreaUH += pu * qh - qu * ph;
    twiceAreaVH += pv * qh - qv * ph;
  }
  const maxArea =
    Math.max(Math.abs(twiceAreaUV), Math.abs(twiceAreaUH), Math.abs(twiceAreaVH)) * 0.5;
  if (!Number.isFinite(maxArea) || maxArea <= 1e-9) {
    return [];
  }

  return out;
}

export function clipSegmentNearPlane(
  a: ClipVertex,
  b: ClipVertex,
  vMax: number,
): [ClipVertex, ClipVertex] | null {
  if (!Number.isFinite(vMax)) {
    return [{ ...a }, { ...b }];
  }
  const aInside = a.v <= vMax;
  const bInside = b.v <= vMax;
  if (!aInside && !bInside) {
    return null;
  }
  if (aInside && bInside) {
    return [{ ...a }, { ...b }];
  }
  const inter = intersectNearPlane(a, b, vMax);
  return aInside ? [{ ...a }, inter] : [inter, { ...b }];
}

export function clipPolylineNearPlane(points: readonly ClipVertex[], vMax: number): ClipVertex[][] {
  if (points.length < 2) {
    return [];
  }
  if (!Number.isFinite(vMax)) {
    return [points.map((p) => ({ ...p }))];
  }

  const segments: ClipVertex[][] = [];
  let current: ClipVertex[] = [];

  for (let i = 0; i < points.length - 1; i++) {
    const clipped = clipSegmentNearPlane(points[i], points[i + 1], vMax);
    if (!clipped) {
      if (current.length >= 2) {
        segments.push(current);
      }
      current = [];
      continue;
    }

    const [p0, p1] = clipped;
    if (isSameClipVertex(p0, p1)) {
      continue;
    }
    if (current.length === 0) {
      current.push(p0, p1);
    } else {
      const last = current[current.length - 1];
      if (!isSameClipVertex(last, p0)) {
        if (current.length >= 2) {
          segments.push(current);
        }
        current = [p0, p1];
      } else if (!isSameClipVertex(last, p1)) {
        current.push(p1);
      }
    }
  }

  if (current.length >= 2) {
    segments.push(current);
  }

  return segments;
}

// ============================================================================
// Anchors & Hit Testing (DOM-Free)
// ============================================================================

export function getProjectedAnchors(carte: CarteDto, params: ProjectionParams): ProjectedAnchorMap {
  const anchors: ProjectedAnchorMap = {};
  const cx = Number.isFinite(carte?.centreX) ? carte.centreX : DEFAULT_CARTE_CENTRE_X;
  const cy = Number.isFinite(carte?.centreY) ? carte.centreY : DEFAULT_CARTE_CENTRE_Y;

  const marketProj = projectPoint(cx, cy, 0, params);
  anchors['market'] = {
    screenX: marketProj.screenX,
    screenY: marketProj.screenY,
    visible: marketProj.visible,
  };

  const emplacements = carte?.emplacements ?? [];
  for (let i = 0; i < emplacements.length; i++) {
    const emp = emplacements[i];
    const proj = projectPoint(emp.coordX, emp.coordY, 0, params);
    anchors[`emp-${emp.numero}`] = {
      screenX: proj.screenX,
      screenY: proj.screenY,
      visible: proj.visible,
    };
  }

  return anchors;
}

export function hitTestAnchor(
  screenX: number,
  screenY: number,
  anchors: ProjectedAnchorMap,
  hitRadiusPx = 24,
): MapInteractionTarget | null {
  if (
    !Number.isFinite(screenX) ||
    !Number.isFinite(screenY) ||
    !Number.isFinite(hitRadiusPx) ||
    hitRadiusPx <= 0
  ) {
    return null;
  }

  interface Candidate {
    target: MapInteractionTarget;
    anchor: ProjectedAnchor;
    dist: number;
  }

  const candidates: Candidate[] = [];
  const keys = Object.keys(anchors);

  for (let i = 0; i < keys.length; i++) {
    const key = keys[i];
    const anchor = anchors[key];
    if (!anchor || !anchor.visible) {
      continue;
    }
    const dist = Math.hypot(screenX - anchor.screenX, screenY - anchor.screenY);
    if (dist > hitRadiusPx) {
      continue;
    }

    if (key === 'market') {
      candidates.push({ target: { type: 'MARCHE' }, anchor, dist });
    } else if (key.startsWith('emp-')) {
      const numero = Number(key.slice(4));
      if (Number.isInteger(numero) && numero > 0) {
        candidates.push({ target: { type: 'EMPLACEMENT', numero }, anchor, dist });
      }
    }
  }

  if (candidates.length === 0) {
    return null;
  }

  candidates.sort((a, b) => {
    const yDiff = b.anchor.screenY - a.anchor.screenY;
    if (Math.abs(yDiff) > 1e-6) {
      return yDiff;
    }
    return a.dist - b.dist;
  });

  return candidates[0].target;
}

// ============================================================================
// Procedural Canvas 2D Engine — Palettes, Color Utilities & Caching
// ============================================================================

interface RgbColor {
  r: number;
  g: number;
  b: number;
}

const BIOME_BASE_RGB: Record<BiomeType, RgbColor> = {
  EAU_PROFONDE: { r: 26, g: 75, b: 110 },
  LAC: { r: 42, g: 111, b: 151 },
  RIVIERE: { r: 51, g: 133, b: 166 },
  PLAGE: { r: 216, g: 197, b: 147 },
  PRAIRIE: { r: 120, g: 158, b: 84 },
  FORET: { r: 74, g: 115, b: 59 },
  COLLINE: { r: 140, g: 139, b: 94 },
  MONTAGNE: { r: 138, g: 134, b: 126 },
  PAVE_MARCHE: { r: 198, g: 182, b: 148 },
};

const ARCHETYPE_BACKDROP: Record<
  ArchetypeCarte,
  { inner: string; outer: string; waterTint: string }
> = {
  VALLEE_ROYALE: { inner: '#273124', outer: '#141913', waterTint: 'rgba(42, 111, 151, 0.25)' },
  FORET_NOIRE: { inner: '#1d2b22', outer: '#0f1612', waterTint: 'rgba(34, 88, 112, 0.28)' },
  HAUTES_TERRES: { inner: '#252d36', outer: '#13171d', waterTint: 'rgba(54, 114, 156, 0.25)' },
  CONFLUENCES: { inner: '#202d33', outer: '#11181c', waterTint: 'rgba(51, 133, 166, 0.30)' },
  LAC_CENTRAL: { inner: '#1e2c38', outer: '#101820', waterTint: 'rgba(40, 125, 175, 0.32)' },
};

const FEATURE_LAYER_PRIORITY: Record<FeatureKind, number> = {
  CROP_FIELD: 0,
  SITE_FOUNDATION: 1,
  HILL_MOUND: 2,
  RIDGE_CREST: 3,
  SITE_PALISADE: 4,
  MARKET_STALL: 5,
  HAMLET_HOUSE: 6,
  TREE_OAK: 7,
  TREE_PINE: 8,
  RAMPART_TOWER: 9,
  WINDMILL: 10,
  MARKET_HALL: 11,
  CASTLE_KEEP: 12,
};

interface PreparedCellData {
  cell: VoronoiCell;
  worldHeight: number;
  vertexHeights: number[];
  shadeFactor: number;
  shoreEdges: Array<[Point2D, Point2D]>;
}

interface PreparedMapCache {
  sortedCells: PreparedCellData[];
  grid: VoronoiCell[][];
  gridWidth: number;
  gridHeight: number;
  cellById: Map<number, PreparedCellData>;
}

const preparedMapCache = new WeakMap<GeneratedMap, PreparedMapCache>();

function parseHexToRgb(hex: string | undefined): RgbColor {
  if (!hex) {
    return { r: 148, g: 163, b: 184 };
  }
  const clean = hex.trim().replace('#', '');
  if (clean.length === 3) {
    const r = parseInt(clean[0] + clean[0], 16);
    const g = parseInt(clean[1] + clean[1], 16);
    const b = parseInt(clean[2] + clean[2], 16);
    if (Number.isFinite(r) && Number.isFinite(g) && Number.isFinite(b)) {
      return { r, g, b };
    }
  } else if (clean.length === 6) {
    const r = parseInt(clean.slice(0, 2), 16);
    const g = parseInt(clean.slice(2, 4), 16);
    const b = parseInt(clean.slice(4, 6), 16);
    if (Number.isFinite(r) && Number.isFinite(g) && Number.isFinite(b)) {
      return { r, g, b };
    }
  }
  return { r: 148, g: 163, b: 184 };
}

function rgbString(r: number, g: number, b: number, a = 1): string {
  const cr = Math.round(clamp(r, 0, 255));
  const cg = Math.round(clamp(g, 0, 255));
  const cb = Math.round(clamp(b, 0, 255));
  if (a >= 0.999) {
    return `rgb(${cr}, ${cg}, ${cb})`;
  }
  return `rgba(${cr}, ${cg}, ${cb}, ${clamp(a, 0, 1).toFixed(3)})`;
}

function getCellWorldHeight(cell: VoronoiCell): number {
  if (cell.isWater || cell.terraformed) {
    return 0;
  }
  const elev = clamp(Number.isFinite(cell.elevation) ? cell.elevation : 0.3, 0, 1);
  switch (cell.biome) {
    case 'EAU_PROFONDE':
    case 'LAC':
    case 'RIVIERE':
    case 'PAVE_MARCHE':
      return 0;
    case 'PLAGE':
      return elev * 3;
    case 'PRAIRIE':
    case 'FORET':
      return 1.5 + elev * 5;
    case 'COLLINE':
      return 6 + elev * 12;
    case 'MONTAGNE':
      return 16 + elev * 18;
  }
}

function quantizeVertexKey(x: number, y: number): string {
  return `${Math.round(x * 4)},${Math.round(y * 4)}`;
}

function buildPreparedMapCache(
  map: GeneratedMap,
  mapWidth: number,
  mapHeight: number,
): PreparedMapCache {
  const cells = map.cells ?? [];
  const vertexStats = new Map<string, { sum: number; count: number; pinnedZero: boolean }>();
  const rawCellById = new Map<number, VoronoiCell>();
  const worldHeights = new Map<number, number>();

  for (let i = 0; i < cells.length; i++) {
    const cell = cells[i];
    rawCellById.set(cell.id, cell);
    const wh = getCellWorldHeight(cell);
    worldHeights.set(cell.id, wh);
    const pinned = cell.isWater || cell.terraformed;
    const poly = cell.polygon ?? [];

    for (let j = 0; j < poly.length; j++) {
      const pt = poly[j];
      if (!isFinitePoint(pt)) {
        continue;
      }
      const key = quantizeVertexKey(pt.x, pt.y);
      const entry = vertexStats.get(key);
      if (!entry) {
        vertexStats.set(key, { sum: wh, count: 1, pinnedZero: pinned });
      } else {
        entry.sum += wh;
        entry.count += 1;
        if (pinned) {
          entry.pinnedZero = true;
        }
      }
    }
  }

  const grid: VoronoiCell[][] = Array.from(
    { length: SPATIAL_GRID_SIZE * SPATIAL_GRID_SIZE },
    () => [],
  );
  const cellW = mapWidth / SPATIAL_GRID_SIZE;
  const cellH = mapHeight / SPATIAL_GRID_SIZE;

  const prepared: PreparedCellData[] = [];
  const cellById = new Map<number, PreparedCellData>();

  for (let i = 0; i < cells.length; i++) {
    const cell = cells[i];
    const wh = worldHeights.get(cell.id) ?? 0;
    const poly = cell.polygon ?? [];
    const vertexHeights: number[] = new Array(poly.length);

    for (let j = 0; j < poly.length; j++) {
      const pt = poly[j];
      if (!isFinitePoint(pt)) {
        vertexHeights[j] = 0;
        continue;
      }
      const entry = vertexStats.get(quantizeVertexKey(pt.x, pt.y));
      if (!entry || entry.pinnedZero || cell.isWater || cell.terraformed) {
        vertexHeights[j] = 0;
      } else {
        vertexHeights[j] = entry.sum / entry.count;
      }
    }

    // NW directional slope shading (-0.7071, -0.7071)
    let gx = 0;
    let gy = 0;
    let gCount = 0;
    const neighbors = cell.neighbors ?? [];
    let hasLandNeighbor = false;

    for (let n = 0; n < neighbors.length; n++) {
      const nb = rawCellById.get(neighbors[n]);
      if (!nb) {
        continue;
      }
      if (!nb.isWater) {
        hasLandNeighbor = true;
      }
      if (
        !isFinitePoint(cell.center) ||
        !isFinitePoint(nb.center) ||
        !Number.isFinite(cell.elevation) ||
        !Number.isFinite(nb.elevation)
      ) {
        continue;
      }
      const dx = nb.center.x - cell.center.x;
      const dy = nb.center.y - cell.center.y;
      const dist = Math.hypot(dx, dy);
      if (dist > 1e-3) {
        const de = (nb.elevation - cell.elevation) / dist;
        gx += de * (dx / dist);
        gy += de * (dy / dist);
        gCount += 1;
      }
    }

    let shadeFactor = 0;
    if (gCount > 0 && !cell.isWater) {
      gx /= gCount;
      gy /= gCount;
      const slopeDot = gx * -0.7071 + gy * -0.7071;
      shadeFactor = clamp(slopeDot * 14, -0.22, 0.22);
      if (cell.terraformed) {
        shadeFactor *= 0.25;
      }
    }

    // Detect shoreline edges for water cells touching land
    const shoreEdges: Array<[Point2D, Point2D]> = [];
    if (cell.isWater && hasLandNeighbor && poly.length >= 3) {
      for (let e = 0; e < poly.length; e++) {
        const a = poly[e];
        const b = poly[(e + 1) % poly.length];
        if (!isFinitePoint(a) || !isFinitePoint(b)) {
          continue;
        }
        const mx = (a.x + b.x) * 0.5;
        const my = (a.y + b.y) * 0.5;
        let touchesLand = false;
        for (let n = 0; n < neighbors.length; n++) {
          const nb = rawCellById.get(neighbors[n]);
          if (!nb || nb.isWater) {
            continue;
          }
          const nbPoly = nb.polygon ?? [];
          for (let k = 0; k < nbPoly.length; k++) {
            const na = nbPoly[k];
            const nbPt = nbPoly[(k + 1) % nbPoly.length];
            if (!isFinitePoint(na) || !isFinitePoint(nbPt)) {
              continue;
            }
            const nmx = (na.x + nbPt.x) * 0.5;
            const nmy = (na.y + nbPt.y) * 0.5;
            if (Math.hypot(nmx - mx, nmy - my) < 2.5) {
              touchesLand = true;
              break;
            }
          }
          if (touchesLand) {
            break;
          }
        }
        if (touchesLand) {
          shoreEdges.push([a, b]);
        }
      }
    }

    const data: PreparedCellData = {
      cell,
      worldHeight: wh,
      vertexHeights,
      shadeFactor,
      shoreEdges,
    };
    prepared.push(data);
    cellById.set(cell.id, data);

    if (isFinitePoint(cell.center)) {
      const gxIdx = clamp(Math.floor(cell.center.x / Math.max(1, cellW)), 0, SPATIAL_GRID_SIZE - 1);
      const gyIdx = clamp(Math.floor(cell.center.y / Math.max(1, cellH)), 0, SPATIAL_GRID_SIZE - 1);
      grid[gyIdx * SPATIAL_GRID_SIZE + gxIdx].push(cell);
    }
  }

  prepared.sort((a, b) => {
    const ay = isFinitePoint(a.cell.center) ? a.cell.center.y : 0;
    const by = isFinitePoint(b.cell.center) ? b.cell.center.y : 0;
    const dy = ay - by;
    if (Math.abs(dy) > 1e-6) {
      return dy;
    }
    return a.cell.id - b.cell.id;
  });

  return {
    sortedCells: prepared,
    grid,
    gridWidth: mapWidth,
    gridHeight: mapHeight,
    cellById,
  };
}

function getPreparedMapCache(
  map: GeneratedMap,
  mapWidth: number,
  mapHeight: number,
): PreparedMapCache {
  const existing = preparedMapCache.get(map);
  if (existing && existing.gridWidth === mapWidth && existing.gridHeight === mapHeight) {
    return existing;
  }
  const built = buildPreparedMapCache(map, mapWidth, mapHeight);
  preparedMapCache.set(map, built);
  return built;
}

function sampleGroundHeight(cache: PreparedMapCache, x: number, y: number): number {
  if (!Number.isFinite(x) || !Number.isFinite(y) || cache.sortedCells.length === 0) {
    return 0;
  }
  const cellW = Math.max(1, cache.gridWidth / SPATIAL_GRID_SIZE);
  const cellH = Math.max(1, cache.gridHeight / SPATIAL_GRID_SIZE);
  const gx = clamp(Math.floor(x / cellW), 0, SPATIAL_GRID_SIZE - 1);
  const gy = clamp(Math.floor(y / cellH), 0, SPATIAL_GRID_SIZE - 1);

  let bestCell: VoronoiCell | null = null;
  let bestDistSq = Infinity;

  for (let oy = -1; oy <= 1; oy++) {
    const cy = gy + oy;
    if (cy < 0 || cy >= SPATIAL_GRID_SIZE) {
      continue;
    }
    for (let ox = -1; ox <= 1; ox++) {
      const cx = gx + ox;
      if (cx < 0 || cx >= SPATIAL_GRID_SIZE) {
        continue;
      }
      const bucket = cache.grid[cy * SPATIAL_GRID_SIZE + cx];
      for (let i = 0; i < bucket.length; i++) {
        const c = bucket[i];
        if (!isFinitePoint(c.center)) {
          continue;
        }
        const dx = c.center.x - x;
        const dy = c.center.y - y;
        const dSq = dx * dx + dy * dy;
        if (dSq < bestDistSq) {
          bestDistSq = dSq;
          bestCell = c;
        }
      }
    }
  }

  if (!bestCell) {
    return 0;
  }
  const data = cache.cellById.get(bestCell.id);
  return data ? data.worldHeight : 0;
}

// ============================================================================
// Canvas Path Projection Helpers
// ============================================================================

interface ProjectionTrig {
  focalLength: number;
  sinA: number;
  cosA: number;
  sinEff: number;
  vMax: number;
}

function buildProjectionTrig(params: ProjectionParams): ProjectionTrig {
  const focalLength = resolveFocalLength(params);
  const clampedTilt = clampTiltDeg(params.tiltDeg);
  const alpha = (clampedTilt * Math.PI) / 180;
  const sinA = Math.sin(alpha);
  const cosA = Math.cos(alpha);
  const sinEff = Math.sin((Math.max(clampedTilt, MIN_BILLBOARD_TILT_DEG) * Math.PI) / 180);
  const vMax = getNearPlaneVMax(params);
  return { focalLength, sinA, cosA, sinEff, vMax };
}

function projectClipVertex(
  vtx: ClipVertex,
  params: ProjectionParams,
  trig: ProjectionTrig,
): Point2D {
  const denom = Math.max(trig.focalLength - vtx.v * trig.sinA, MIN_DENOM);
  const k = trig.focalLength / denom;
  return {
    x: normZero(params.screenCenterX + vtx.u * k),
    y: normZero(params.screenCenterY + (vtx.v * trig.cosA - vtx.h * trig.sinEff) * k),
  };
}

function traceClippedPolygon(
  ctx: CanvasRenderingContext2D,
  vertices: readonly ClipVertex[],
  params: ProjectionParams,
  trig: ProjectionTrig,
): boolean {
  const clipped = clipPolygonNearPlane(vertices, trig.vMax);
  if (clipped.length < 3) {
    return false;
  }
  ctx.beginPath();
  for (let i = 0; i < clipped.length; i++) {
    const pt = projectClipVertex(clipped[i], params, trig);
    if (i === 0) {
      ctx.moveTo(pt.x, pt.y);
    } else {
      ctx.lineTo(pt.x, pt.y);
    }
  }
  ctx.closePath();
  return true;
}

function traceClippedPolyline(
  ctx: CanvasRenderingContext2D,
  vertices: readonly ClipVertex[],
  params: ProjectionParams,
  trig: ProjectionTrig,
): boolean {
  const pieces = clipPolylineNearPlane(vertices, trig.vMax);
  if (pieces.length === 0) {
    return false;
  }
  ctx.beginPath();
  for (let p = 0; p < pieces.length; p++) {
    const seg = pieces[p];
    for (let i = 0; i < seg.length; i++) {
      const pt = projectClipVertex(seg[i], params, trig);
      if (i === 0) {
        ctx.moveTo(pt.x, pt.y);
      } else {
        ctx.lineTo(pt.x, pt.y);
      }
    }
  }
  return true;
}

function traceRoundedRect(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  w: number,
  h: number,
  r: number,
): void {
  const radius = Math.max(0, Math.min(r, w * 0.5, h * 0.5));
  ctx.beginPath();
  if (typeof ctx.roundRect === 'function') {
    ctx.roundRect(x, y, w, h, radius);
    return;
  }
  ctx.moveTo(x + radius, y);
  ctx.arcTo(x + w, y, x + w, y + h, radius);
  ctx.arcTo(x + w, y + h, x, y + h, radius);
  ctx.arcTo(x, y + h, x, y, radius);
  ctx.arcTo(x, y, x + w, y, radius);
  ctx.closePath();
}

function computeCellFillColor(data: PreparedCellData): string {
  const cell = data.cell;
  const base = BIOME_BASE_RGB[cell.biome] ?? BIOME_BASE_RGB.PRAIRIE;
  let r = base.r;
  let g = base.g;
  let b = base.b;

  const moisture = clamp(Number.isFinite(cell.moisture) ? cell.moisture : 0.5, 0, 1);
  const elev = clamp(Number.isFinite(cell.elevation) ? cell.elevation : 0.4, 0, 1);

  if (cell.biome === 'PRAIRIE') {
    const lush = (moisture - 0.5) * 28;
    r -= lush * 0.6;
    g += lush * 0.7;
    b -= lush * 0.2;
  } else if (cell.biome === 'FORET') {
    const deep = (moisture - 0.5) * 18;
    r -= deep * 0.4;
    g += deep * 0.4;
  } else if (cell.biome === 'MONTAGNE' && elev > 0.84) {
    const snowT = clamp((elev - 0.84) / 0.16, 0, 1);
    r = r + (232 - r) * snowT;
    g = g + (237 - g) * snowT;
    b = b + (242 - b) * snowT;
  }

  const elevLift = cell.isWater ? 0 : (elev - 0.45) * 20;
  const lightBoost = 1 + data.shadeFactor;
  r = (r + elevLift) * lightBoost;
  g = (g + elevLift) * lightBoost;
  b = (b + elevLift) * lightBoost;

  return rgbString(r, g, b);
}

// ============================================================================
// Pass 1: Backdrop & 2.5D Tabletop Diorama Slab
// ============================================================================

function renderPass1BackdropAndSlab(
  ctx: CanvasRenderingContext2D,
  width: number,
  height: number,
  mapWidth: number,
  mapHeight: number,
  archetype: ArchetypeCarte,
  params: ProjectionParams,
  trig: ProjectionTrig,
): void {
  const palette = ARCHETYPE_BACKDROP[archetype] ?? ARCHETYPE_BACKDROP.VALLEE_ROYALE;
  if (typeof ctx.createRadialGradient === 'function') {
    const bg = ctx.createRadialGradient(
      width * 0.5,
      height * 0.48,
      Math.min(width, height) * 0.08,
      width * 0.5,
      height * 0.5,
      Math.hypot(width, height) * 0.65,
    );
    bg.addColorStop(0, palette.inner);
    bg.addColorStop(1, palette.outer);
    ctx.fillStyle = bg;
  } else {
    ctx.fillStyle = palette.inner;
  }
  ctx.fillRect(0, 0, width, height);

  // Faint ambient cartographic grid in backdrop
  ctx.strokeStyle = 'rgba(226, 214, 184, 0.04)';
  ctx.lineWidth = 1;
  const step = 96;
  ctx.beginPath();
  for (let gx = step; gx < width; gx += step) {
    ctx.moveTo(gx, 0);
    ctx.lineTo(gx, height);
  }
  for (let gy = step; gy < height; gy += step) {
    ctx.moveTo(0, gy);
    ctx.lineTo(width, gy);
  }
  ctx.stroke();

  const slabH = -SLAB_THICKNESS * params.zoom;
  const shadowH = -(SLAB_THICKNESS + 10) * params.zoom;

  const topCorners: ClipVertex[] = [
    toClipVertex(0, 0, 0, params),
    toClipVertex(mapWidth, 0, 0, params),
    toClipVertex(mapWidth, mapHeight, 0, params),
    toClipVertex(0, mapHeight, 0, params),
  ];

  const shadowCorners: ClipVertex[] = [
    toClipVertex(12, 16, shadowH, params),
    toClipVertex(mapWidth + 12, 16, shadowH, params),
    toClipVertex(mapWidth + 12, mapHeight + 16, shadowH, params),
    toClipVertex(12, mapHeight + 16, shadowH, params),
  ];

  if (traceClippedPolygon(ctx, shadowCorners, params, trig)) {
    ctx.fillStyle = 'rgba(0, 0, 0, 0.42)';
    ctx.fill();
  }

  // Extruded diorama sides (South, West, East)
  const southWall: ClipVertex[] = [
    toClipVertex(0, mapHeight, 0, params),
    toClipVertex(mapWidth, mapHeight, 0, params),
    toClipVertex(mapWidth, mapHeight, slabH, params),
    toClipVertex(0, mapHeight, slabH, params),
  ];
  if (traceClippedPolygon(ctx, southWall, params, trig)) {
    ctx.fillStyle = '#4a3a28';
    ctx.fill();
    ctx.strokeStyle = '#2d2217';
    ctx.lineWidth = 1;
    ctx.stroke();
  }

  const westWall: ClipVertex[] = [
    toClipVertex(0, 0, 0, params),
    toClipVertex(0, mapHeight, 0, params),
    toClipVertex(0, mapHeight, slabH, params),
    toClipVertex(0, 0, slabH, params),
  ];
  if (traceClippedPolygon(ctx, westWall, params, trig)) {
    ctx.fillStyle = '#5c4933';
    ctx.fill();
  }

  const eastWall: ClipVertex[] = [
    toClipVertex(mapWidth, 0, 0, params),
    toClipVertex(mapWidth, mapHeight, 0, params),
    toClipVertex(mapWidth, mapHeight, slabH, params),
    toClipVertex(mapWidth, 0, slabH, params),
  ];
  if (traceClippedPolygon(ctx, eastWall, params, trig)) {
    ctx.fillStyle = '#3b2e20';
    ctx.fill();
  }

  // Top base board surface (acts as fallback board when cells array is empty)
  if (traceClippedPolygon(ctx, topCorners, params, trig)) {
    ctx.fillStyle = '#68824c';
    ctx.fill();
    ctx.strokeStyle = '#c8b48c';
    ctx.lineWidth = 1.5;
    ctx.stroke();
  }
}

// ============================================================================
// Pass 2: Clipped Voronoi Terrain Polygons
// ============================================================================

function renderPass2Terrain(
  ctx: CanvasRenderingContext2D,
  cache: PreparedMapCache,
  params: ProjectionParams,
  trig: ProjectionTrig,
): void {
  const cells = cache.sortedCells;
  for (let i = 0; i < cells.length; i++) {
    const data = cells[i];
    const poly = data.cell.polygon;
    if (!poly || poly.length < 3) {
      continue;
    }

    const clipVerts: ClipVertex[] = [];
    for (let j = 0; j < poly.length; j++) {
      const pt = poly[j];
      if (!isFinitePoint(pt)) {
        continue;
      }
      const h = (data.vertexHeights[j] ?? 0) * params.zoom;
      clipVerts.push(toClipVertex(pt.x, pt.y, Number.isFinite(h) ? h : 0, params));
    }

    if (clipVerts.length < 3 || !traceClippedPolygon(ctx, clipVerts, params, trig)) {
      continue;
    }

    const fillColor = computeCellFillColor(data);
    ctx.fillStyle = fillColor;
    ctx.fill();

    // Hairline suppression between adjacent Voronoi cells
    ctx.strokeStyle = fillColor;
    ctx.lineWidth = 0.75;
    ctx.stroke();
  }
}

// ============================================================================
// Pass 3: Animated Lakes, Shorelines & Rivers
// ============================================================================

function renderPass3Water(
  ctx: CanvasRenderingContext2D,
  cache: PreparedMapCache,
  riverPaths: readonly Point2D[][],
  archetype: ArchetypeCarte,
  params: ProjectionParams,
  trig: ProjectionTrig,
  timeMs: number,
): void {
  const cells = cache.sortedCells;
  const waterTint = (ARCHETYPE_BACKDROP[archetype] ?? ARCHETYPE_BACKDROP.VALLEE_ROYALE).waterTint;

  // Water shorelines and animated lake ripples
  for (let i = 0; i < cells.length; i++) {
    const data = cells[i];
    const cell = data.cell;
    if (!cell.isWater) {
      continue;
    }

    // Shoreline sand/foam rim
    if (data.shoreEdges.length > 0) {
      ctx.strokeStyle = 'rgba(235, 224, 192, 0.55)';
      ctx.lineWidth = Math.max(1, 2.2 * params.zoom);
      ctx.lineCap = 'round';
      for (let e = 0; e < data.shoreEdges.length; e++) {
        const [a, b] = data.shoreEdges[e];
        if (!isFinitePoint(a) || !isFinitePoint(b)) {
          continue;
        }
        const seg = clipSegmentNearPlane(
          toClipVertex(a.x, a.y, 0, params),
          toClipVertex(b.x, b.y, 0, params),
          trig.vMax,
        );
        if (!seg) {
          continue;
        }
        const sa = projectClipVertex(seg[0], params, trig);
        const sb = projectClipVertex(seg[1], params, trig);
        ctx.beginPath();
        ctx.moveTo(sa.x, sa.y);
        ctx.lineTo(sb.x, sb.y);
        ctx.stroke();
      }
    }

    // Animated ripples on lakes / deep water
    if (
      (cell.waterKind === 'LAKE' || cell.biome === 'LAC' || cell.biome === 'EAU_PROFONDE') &&
      isFinitePoint(cell.center)
    ) {
      const cellId = Number.isFinite(cell.id) ? cell.id : 0;
      const wavePhase = timeMs * 0.0022 + cellId * 0.73;
      const driftX = Math.sin(wavePhase) * 6;
      const driftY = Math.cos(wavePhase * 0.8) * 2;
      const halfSpan = 8 + (Math.abs(cellId) % 5) * 1.5;

      const seg = clipSegmentNearPlane(
        toClipVertex(cell.center.x - halfSpan + driftX, cell.center.y + driftY, 0, params),
        toClipVertex(cell.center.x + halfSpan + driftX, cell.center.y + driftY, 0, params),
        trig.vMax,
      );
      if (seg) {
        const p0 = projectClipVertex(seg[0], params, trig);
        const p1 = projectClipVertex(seg[1], params, trig);
        const alpha = 0.22 + 0.14 * Math.sin(wavePhase * 1.3);
        ctx.strokeStyle = `rgba(210, 238, 255, ${clamp(alpha, 0.08, 0.4).toFixed(3)})`;
        ctx.lineWidth = Math.max(1, 1.4 * params.zoom);
        ctx.beginPath();
        ctx.moveTo(p0.x, p0.y);
        ctx.lineTo(p1.x, p1.y);
        ctx.stroke();
      }
    }
  }

  // Smoothed continuous river paths with banks & animated glint
  ctx.lineJoin = 'round';
  ctx.lineCap = 'round';

  for (let r = 0; r < riverPaths.length; r++) {
    const path = riverPaths[r];
    if (!path || path.length < 2) {
      continue;
    }

    const clipVerts: ClipVertex[] = [];
    for (let j = 0; j < path.length; j++) {
      const pt = path[j];
      if (!isFinitePoint(pt)) {
        continue;
      }
      const gh = sampleGroundHeight(cache, pt.x, pt.y) * 0.2 * params.zoom;
      clipVerts.push(toClipVertex(pt.x, pt.y, gh, params));
    }

    // Outer mossy/sandy bank
    if (clipVerts.length >= 2 && traceClippedPolyline(ctx, clipVerts, params, trig)) {
      ctx.setLineDash([]);
      ctx.strokeStyle = 'rgba(130, 128, 88, 0.65)';
      ctx.lineWidth = Math.max(3, 10 * params.zoom);
      ctx.stroke();

      // River water body
      ctx.strokeStyle = '#3385a6';
      ctx.lineWidth = Math.max(2, 6.5 * params.zoom);
      ctx.stroke();

      // Subtle archetype water tint
      ctx.strokeStyle = waterTint;
      ctx.lineWidth = Math.max(2, 6.5 * params.zoom);
      ctx.stroke();

      // Animated downstream glint
      ctx.setLineDash([10, 16]);
      ctx.lineDashOffset = -((timeMs * 0.025) % 26);
      ctx.strokeStyle = 'rgba(205, 240, 255, 0.55)';
      ctx.lineWidth = Math.max(1, 1.8 * params.zoom);
      ctx.stroke();
      ctx.setLineDash([]);
      ctx.lineDashOffset = 0;
    }
  }
}

// ============================================================================
// Pass 4: Fief Territory Tints, Market Free Zone & Solid/Dashed Borders
// ============================================================================

function getSelectedOrHoveredNumero(state: RenderMapState): number | null {
  if (state.selectedTarget?.type === 'EMPLACEMENT') {
    return state.selectedTarget.numero;
  }
  if (state.hoveredTarget?.type === 'EMPLACEMENT') {
    return state.hoveredTarget.numero;
  }
  return null;
}

function renderPass4Fiefs(
  ctx: CanvasRenderingContext2D,
  cache: PreparedMapCache,
  map: GeneratedMap,
  carte: CarteDto,
  state: RenderMapState,
  params: ProjectionParams,
  trig: ProjectionTrig,
): void {
  const fiefs = map.fiefs ?? [];
  const emplacements = carte?.emplacements ?? [];

  const fiefByNumero = new Map<number, FiefDomain>();
  for (let i = 0; i < fiefs.length; i++) {
    fiefByNumero.set(fiefs[i].numero, fiefs[i]);
  }

  const empByNumero = new Map<number, EmplacementCarteDto>();
  for (let i = 0; i < emplacements.length; i++) {
    empByNumero.set(emplacements[i].numero, emplacements[i]);
  }

  const activeNumero = getSelectedOrHoveredNumero(state);
  const highlightedSet = new Set<number>(state.highlightedNumeros ?? []);

  // Territory tint on owned, non-water, non-market cells
  const cells = cache.sortedCells;
  for (let i = 0; i < cells.length; i++) {
    const data = cells[i];
    const cell = data.cell;
    if (cell.isWater || cell.isMarketZone || cell.ownerNumero === null) {
      continue;
    }
    const poly = cell.polygon;
    if (!poly || poly.length < 3) {
      continue;
    }

    const fief = fiefByNumero.get(cell.ownerNumero);
    const emp = empByNumero.get(cell.ownerNumero);
    const occupe = fief?.occupe ?? emp?.occupe ?? false;
    const colorHex =
      fief?.royaume?.couleurPrimaire ??
      emp?.royaume?.couleurPrimaire ??
      createDefaultRoyaume(cell.ownerNumero).couleurPrimaire;
    const rgb = parseHexToRgb(colorHex);

    const isFocused = activeNumero === cell.ownerNumero || highlightedSet.has(cell.ownerNumero);
    const baseAlpha = occupe ? 0.18 : 0.07;
    const distFalloff = Number.isFinite(cell.geodesicDistance)
      ? clamp(1 - cell.geodesicDistance / 900, 0.72, 1)
      : 0.85;
    const alpha = clamp(baseAlpha * distFalloff + (isFocused ? 0.1 : 0), 0.04, 0.42);

    const clipVerts: ClipVertex[] = [];
    for (let j = 0; j < poly.length; j++) {
      const pt = poly[j];
      if (!isFinitePoint(pt)) {
        continue;
      }
      const h = (data.vertexHeights[j] ?? 0) * params.zoom;
      clipVerts.push(toClipVertex(pt.x, pt.y, Number.isFinite(h) ? h : 0, params));
    }

    if (clipVerts.length >= 3 && traceClippedPolygon(ctx, clipVerts, params, trig)) {
      ctx.fillStyle = rgbString(rgb.r, rgb.g, rgb.b, alpha);
      ctx.fill();
    }
  }

  // Market free-zone dashed ring (r = marketFreeZoneRadius)
  const cx = Number.isFinite(map.centreX) ? map.centreX : (carte.centreX ?? DEFAULT_CARTE_CENTRE_X);
  const cy = Number.isFinite(map.centreY) ? map.centreY : (carte.centreY ?? DEFAULT_CARTE_CENTRE_Y);
  const freeRadius =
    Number.isFinite(map.marketFreeZoneRadius) && map.marketFreeZoneRadius > 0
      ? map.marketFreeZoneRadius
      : MARKET_FREE_ZONE_RADIUS;

  const ringSteps = 48;
  const ringVerts: ClipVertex[] = new Array(ringSteps + 1);
  for (let s = 0; s <= ringSteps; s++) {
    const theta = (s / ringSteps) * Math.PI * 2;
    ringVerts[s] = toClipVertex(
      cx + Math.cos(theta) * freeRadius,
      cy + Math.sin(theta) * freeRadius,
      0,
      params,
    );
  }

  if (traceClippedPolyline(ctx, ringVerts, params, trig)) {
    ctx.setLineDash([7, 5]);
    ctx.strokeStyle = state.highlightMarketZone
      ? 'rgba(251, 191, 36, 0.92)'
      : 'rgba(245, 222, 160, 0.65)';
    ctx.lineWidth = state.highlightMarketZone
      ? Math.max(2, 3 * params.zoom)
      : Math.max(1.2, 2 * params.zoom);
    ctx.stroke();
    ctx.setLineDash([]);
  }

  // Fief borders: continuous solid (occupe=true) vs dashed (occupe=false)
  if (state.showBorders !== false) {
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    for (let f = 0; f < fiefs.length; f++) {
      const fief = fiefs[f];
      const segs = fief.borderSegments ?? [];
      if (segs.length === 0) {
        continue;
      }

      const isFocused = activeNumero === fief.numero || highlightedSet.has(fief.numero);
      const fallbackColor =
        fief.royaume?.couleurPrimaire ?? createDefaultRoyaume(fief.numero).couleurPrimaire;

      for (let s = 0; s < segs.length; s++) {
        const seg = segs[s];
        if (!seg || !isFinitePoint(seg.a) || !isFinitePoint(seg.b)) {
          continue;
        }
        const ha = sampleGroundHeight(cache, seg.a.x, seg.a.y) * params.zoom;
        const hb = sampleGroundHeight(cache, seg.b.x, seg.b.y) * params.zoom;
        const clipped = clipSegmentNearPlane(
          toClipVertex(seg.a.x, seg.a.y, ha, params),
          toClipVertex(seg.b.x, seg.b.y, hb, params),
          trig.vMax,
        );
        if (!clipped) {
          continue;
        }

        const p0 = projectClipVertex(clipped[0], params, trig);
        const p1 = projectClipVertex(clipped[1], params, trig);
        const strokeColor = seg.color || fallbackColor;

        if (seg.dashed) {
          ctx.setLineDash([8, 6]);
          ctx.strokeStyle = 'rgba(15, 23, 42, 0.4)';
          ctx.lineWidth = Math.max(1.8, (isFocused ? 4.4 : 3.4) * params.zoom);
          ctx.beginPath();
          ctx.moveTo(p0.x, p0.y);
          ctx.lineTo(p1.x, p1.y);
          ctx.stroke();

          ctx.strokeStyle = strokeColor;
          ctx.lineWidth = Math.max(1.2, (isFocused ? 2.6 : 1.9) * params.zoom);
          ctx.beginPath();
          ctx.moveTo(p0.x, p0.y);
          ctx.lineTo(p1.x, p1.y);
          ctx.stroke();
        } else {
          ctx.setLineDash([]);
          ctx.strokeStyle = 'rgba(15, 23, 42, 0.6)';
          ctx.lineWidth = Math.max(2.2, (isFocused ? 5.2 : 4.0) * params.zoom);
          ctx.beginPath();
          ctx.moveTo(p0.x, p0.y);
          ctx.lineTo(p1.x, p1.y);
          ctx.stroke();

          ctx.strokeStyle = strokeColor;
          ctx.lineWidth = Math.max(1.4, (isFocused ? 3.2 : 2.3) * params.zoom);
          ctx.beginPath();
          ctx.moveTo(p0.x, p0.y);
          ctx.lineTo(p1.x, p1.y);
          ctx.stroke();
        }
      }
    }

    ctx.setLineDash([]);
  }
}

// ============================================================================
// Pass 5: Market Plaza, Trade Routes, 2.5D Arched Bridges & Crop Fields
// ============================================================================

function renderBridge25D(
  ctx: CanvasRenderingContext2D,
  bridge: { start: Point2D; end: Point2D; midpoint: Point2D; angle: number; length: number },
  params: ProjectionParams,
  trig: ProjectionTrig,
): void {
  if (
    !bridge ||
    !Number.isFinite(bridge.length) ||
    bridge.length <= 0 ||
    !Number.isFinite(bridge.angle) ||
    !isFinitePoint(bridge.midpoint)
  ) {
    return;
  }

  const dx = Math.cos(bridge.angle);
  const dy = Math.sin(bridge.angle);
  const nx = -dy;
  const ny = dx;
  const halfLen = bridge.length * 0.5;
  const halfW = 5.5;
  const archPeak = clamp(bridge.length * 0.22, 6, 18);
  const steps = 6;

  // Water drop shadow at h = 0
  const shadowVerts: ClipVertex[] = [
    toClipVertex(
      bridge.midpoint.x - dx * halfLen + nx * halfW,
      bridge.midpoint.y - dy * halfLen + ny * halfW,
      0,
      params,
    ),
    toClipVertex(
      bridge.midpoint.x + dx * halfLen + nx * halfW,
      bridge.midpoint.y + dy * halfLen + ny * halfW,
      0,
      params,
    ),
    toClipVertex(
      bridge.midpoint.x + dx * halfLen - nx * halfW,
      bridge.midpoint.y + dy * halfLen - ny * halfW,
      0,
      params,
    ),
    toClipVertex(
      bridge.midpoint.x - dx * halfLen - nx * halfW,
      bridge.midpoint.y - dy * halfLen - ny * halfW,
      0,
      params,
    ),
  ];
  if (traceClippedPolygon(ctx, shadowVerts, params, trig)) {
    ctx.fillStyle = 'rgba(12, 24, 38, 0.35)';
    ctx.fill();
  }

  // Piers & arched deck quads
  for (let i = 0; i < steps; i++) {
    const t0 = i / steps;
    const t1 = (i + 1) / steps;
    const s0 = (t0 - 0.5) * bridge.length;
    const s1 = (t1 - 0.5) * bridge.length;
    const h0 = archPeak * 4 * t0 * (1 - t0) * params.zoom;
    const h1 = archPeak * 4 * t1 * (1 - t1) * params.zoom;

    const x0 = bridge.midpoint.x + dx * s0;
    const y0 = bridge.midpoint.y + dy * s0;
    const x1 = bridge.midpoint.x + dx * s1;
    const y1 = bridge.midpoint.y + dy * s1;

    // Support piers at internal stations
    if (i > 0) {
      const pierClip = clipSegmentNearPlane(
        toClipVertex(x0 - nx * (halfW - 1), y0 - ny * (halfW - 1), 0, params),
        toClipVertex(x0 - nx * (halfW - 1), y0 - ny * (halfW - 1), h0, params),
        trig.vMax,
      );
      if (pierClip) {
        const pa = projectClipVertex(pierClip[0], params, trig);
        const pb = projectClipVertex(pierClip[1], params, trig);
        ctx.strokeStyle = '#5a422c';
        ctx.lineWidth = Math.max(1.5, 2.5 * params.zoom);
        ctx.beginPath();
        ctx.moveTo(pa.x, pa.y);
        ctx.lineTo(pb.x, pb.y);
        ctx.stroke();
      }
    }

    // Oak plank deck quad
    const deckQuad: ClipVertex[] = [
      toClipVertex(x0 + nx * halfW, y0 + ny * halfW, h0, params),
      toClipVertex(x1 + nx * halfW, y1 + ny * halfW, h1, params),
      toClipVertex(x1 - nx * halfW, y1 - ny * halfW, h1, params),
      toClipVertex(x0 - nx * halfW, y0 - ny * halfW, h0, params),
    ];
    if (traceClippedPolygon(ctx, deckQuad, params, trig)) {
      ctx.fillStyle = i % 2 === 0 ? '#a87c4f' : '#9a7046';
      ctx.fill();
      ctx.strokeStyle = '#5c4028';
      ctx.lineWidth = 0.8;
      ctx.stroke();
    }

    // Twin side railings
    const railLift = 3.5 * params.zoom;
    for (const side of [-1, 1]) {
      const railClip = clipSegmentNearPlane(
        toClipVertex(x0 + nx * halfW * side, y0 + ny * halfW * side, h0 + railLift, params),
        toClipVertex(x1 + nx * halfW * side, y1 + ny * halfW * side, h1 + railLift, params),
        trig.vMax,
      );
      if (railClip) {
        const ra = projectClipVertex(railClip[0], params, trig);
        const rb = projectClipVertex(railClip[1], params, trig);
        ctx.strokeStyle = '#6b4b2d';
        ctx.lineWidth = Math.max(1, 1.6 * params.zoom);
        ctx.beginPath();
        ctx.moveTo(ra.x, ra.y);
        ctx.lineTo(rb.x, rb.y);
        ctx.stroke();
      }
    }
  }
}

function renderCropFieldDecal(
  ctx: CanvasRenderingContext2D,
  cache: PreparedMapCache,
  feature: MapFeature25D,
  params: ProjectionParams,
  trig: ProjectionTrig,
): void {
  if (!Number.isFinite(feature.x) || !Number.isFinite(feature.y)) {
    return;
  }
  const s = Number.isFinite(feature.scale) && feature.scale > 0 ? feature.scale : 1;
  const halfW = 16 * s;
  const halfH = 11 * s;
  const seedOffset = Number.isFinite(feature.seedOffset) ? feature.seedOffset : 0;
  const rot =
    feature.rotation !== undefined && Number.isFinite(feature.rotation)
      ? feature.rotation
      : seedOffset * Math.PI;
  const cosR = Math.cos(rot);
  const sinR = Math.sin(rot);
  const gh = sampleGroundHeight(cache, feature.x, feature.y) * params.zoom;

  const toWorld = (lx: number, ly: number): ClipVertex =>
    toClipVertex(feature.x + lx * cosR - ly * sinR, feature.y + lx * sinR + ly * cosR, gh, params);

  const quad = [
    toWorld(-halfW, -halfH),
    toWorld(halfW, -halfH),
    toWorld(halfW, halfH),
    toWorld(-halfW, halfH),
  ];

  if (!traceClippedPolygon(ctx, quad, params, trig)) {
    return;
  }
  ctx.fillStyle = '#735738';
  ctx.fill();
  ctx.strokeStyle = '#4e3b25';
  ctx.lineWidth = 1;
  ctx.stroke();

  // 5 plowed crop rows
  ctx.strokeStyle = '#bfa14a';
  ctx.lineWidth = Math.max(1, 1.7 * params.zoom * s);
  for (let row = -2; row <= 2; row++) {
    const ry = (row / 2.5) * halfH;
    const clipped = clipSegmentNearPlane(
      toWorld(-halfW * 0.85, ry),
      toWorld(halfW * 0.85, ry),
      trig.vMax,
    );
    if (clipped) {
      const a = projectClipVertex(clipped[0], params, trig);
      const b = projectClipVertex(clipped[1], params, trig);
      ctx.beginPath();
      ctx.moveTo(a.x, a.y);
      ctx.lineTo(b.x, b.y);
      ctx.stroke();
    }
  }
}

function renderPass5DecalsRoadsAndBridges(
  ctx: CanvasRenderingContext2D,
  cache: PreparedMapCache,
  map: GeneratedMap,
  carte: CarteDto,
  state: RenderMapState,
  params: ProjectionParams,
  trig: ProjectionTrig,
): void {
  const cx = Number.isFinite(map.centreX) ? map.centreX : (carte.centreX ?? DEFAULT_CARTE_CENTRE_X);
  const cy = Number.isFinite(map.centreY) ? map.centreY : (carte.centreY ?? DEFAULT_CARTE_CENTRE_Y);
  const plazaRadius =
    Number.isFinite(map.marketRadius) && map.marketRadius > 0 ? map.marketRadius : 75;

  // Market cobblestone plaza at h = 0
  const plazaVerts: ClipVertex[] = [];
  const discVerts: ClipVertex[] = [];
  const steps = 32;
  for (let i = 0; i < steps; i++) {
    const theta = (i / steps) * Math.PI * 2;
    plazaVerts.push(
      toClipVertex(
        cx + Math.cos(theta) * plazaRadius,
        cy + Math.sin(theta) * plazaRadius,
        0,
        params,
      ),
    );
    discVerts.push(
      toClipVertex(
        cx + Math.cos(theta) * plazaRadius * 0.62,
        cy + Math.sin(theta) * plazaRadius * 0.62,
        0,
        params,
      ),
    );
  }

  if (traceClippedPolygon(ctx, plazaVerts, params, trig)) {
    ctx.fillStyle = '#c6b694';
    ctx.fill();
    ctx.strokeStyle = '#99896b';
    ctx.lineWidth = Math.max(1, 1.5 * params.zoom);
    ctx.stroke();
  }
  if (traceClippedPolygon(ctx, discVerts, params, trig)) {
    ctx.fillStyle = '#d7cbb0';
    ctx.fill();
    ctx.strokeStyle = '#a89879';
    ctx.lineWidth = 1;
    ctx.stroke();
  }

  // Radial stone joints on Market plaza
  ctx.strokeStyle = 'rgba(130, 114, 88, 0.35)';
  ctx.lineWidth = 1;
  for (let k = 0; k < 8; k++) {
    const theta = (k / 8) * Math.PI * 2;
    const spoke = clipSegmentNearPlane(
      toClipVertex(cx + Math.cos(theta) * 18, cy + Math.sin(theta) * 18, 0, params),
      toClipVertex(
        cx + Math.cos(theta) * plazaRadius * 0.94,
        cy + Math.sin(theta) * plazaRadius * 0.94,
        0,
        params,
      ),
      trig.vMax,
    );
    if (spoke) {
      const a = projectClipVertex(spoke[0], params, trig);
      const b = projectClipVertex(spoke[1], params, trig);
      ctx.beginPath();
      ctx.moveTo(a.x, a.y);
      ctx.lineTo(b.x, b.y);
      ctx.stroke();
    }
  }

  // Trade routes & bridges
  if (state.showRoutes !== false) {
    const routes = map.routes ?? [];
    const activeNumero = getSelectedOrHoveredNumero(state);
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    for (let i = 0; i < routes.length; i++) {
      const route = routes[i];
      const rawPath =
        route.path && route.path.length >= 2
          ? route.path
          : route.from && route.to
            ? [route.from, route.to]
            : null;
      if (!rawPath || rawPath.length < 2) {
        continue;
      }

      const clipVerts: ClipVertex[] = [];
      for (let j = 0; j < rawPath.length; j++) {
        const pt = rawPath[j];
        if (!isFinitePoint(pt)) {
          continue;
        }
        const gh = sampleGroundHeight(cache, pt.x, pt.y) * params.zoom;
        clipVerts.push(toClipVertex(pt.x, pt.y, gh, params));
      }

      const isFocused = activeNumero === route.emplacementNumero;
      if (clipVerts.length >= 2 && traceClippedPolyline(ctx, clipVerts, params, trig)) {
        ctx.setLineDash([]);
        ctx.strokeStyle = '#6e5a42';
        ctx.lineWidth = Math.max(2.6, (isFocused ? 7.5 : 5.5) * params.zoom);
        ctx.stroke();

        ctx.strokeStyle = isFocused ? '#e2cfa3' : '#c8b48c';
        ctx.lineWidth = Math.max(1.4, (isFocused ? 4.4 : 3.2) * params.zoom);
        ctx.stroke();
      }

      const bridges = route.bridges ?? [];
      for (let b = 0; b < bridges.length; b++) {
        renderBridge25D(ctx, bridges[b], params, trig);
      }
    }
  }

  // Crop fields (ground decals)
  const features = map.features ?? [];
  for (let i = 0; i < features.length; i++) {
    if (features[i].kind === 'CROP_FIELD') {
      renderCropFieldDecal(ctx, cache, features[i], params, trig);
    }
  }
}

// ============================================================================
// Pass 6: Y-Sorted 2.5D Billboards (12 Remaining FeatureKinds)
// ============================================================================

function drawWavingBanner(
  ctx: CanvasRenderingContext2D,
  bx: number,
  by: number,
  poleH: number,
  w: number,
  h: number,
  primary: string,
  secondary: string,
  timeMs: number,
  seedOffset: number,
): void {
  const safeSeed = Number.isFinite(seedOffset) ? seedOffset : 0;
  const phase = timeMs * 0.005 + safeSeed * Math.PI * 2;
  ctx.strokeStyle = '#473623';
  ctx.lineWidth = Math.max(1, w * 0.14);
  ctx.beginPath();
  ctx.moveTo(bx, by);
  ctx.lineTo(bx, by - poleH);
  ctx.stroke();

  const topY = by - poleH;
  const w1 = Math.sin(phase) * h * 0.22;
  const w2 = Math.sin(phase + 1.4) * h * 0.3;

  ctx.fillStyle = primary;
  ctx.beginPath();
  ctx.moveTo(bx, topY);
  ctx.lineTo(bx + w * 0.5, topY + w1);
  ctx.lineTo(bx + w, topY + w2);
  ctx.lineTo(bx + w * 0.85, topY + h * 0.5 + w2 * 0.7);
  ctx.lineTo(bx + w, topY + h + w2);
  ctx.lineTo(bx + w * 0.5, topY + h + w1);
  ctx.lineTo(bx, topY + h);
  ctx.closePath();
  ctx.fill();

  ctx.fillStyle = secondary;
  ctx.beginPath();
  ctx.arc(bx + w * 0.38, topY + h * 0.5 + w1 * 0.4, Math.max(1.2, h * 0.22), 0, Math.PI * 2);
  ctx.fill();
}

function renderBillboardFeature(
  ctx: CanvasRenderingContext2D,
  feature: MapFeature25D,
  sx: number,
  sy: number,
  s: number,
  sinEff: number,
  primary: string,
  secondary: string,
  timeMs: number,
): void {
  if (!Number.isFinite(sx) || !Number.isFinite(sy) || !Number.isFinite(s) || s <= 0) {
    return;
  }
  const safeSeedOffset = Number.isFinite(feature.seedOffset) ? feature.seedOffset : 0;
  const sway = Math.sin(timeMs * 0.0022 + safeSeedOffset * Math.PI * 2);
  const vStretch = Math.max(0.45, sinEff / 0.6428);

  switch (feature.kind) {
    case 'TREE_OAK': {
      const trunkH = 7 * s * vStretch;
      const r = 7.5 * s;
      // Ground shadow
      ctx.fillStyle = 'rgba(15, 25, 12, 0.28)';
      ctx.beginPath();
      ctx.ellipse(sx + 2 * s, sy + 1 * s, r * 1.15, r * 0.5, 0, 0, Math.PI * 2);
      ctx.fill();

      // Trunk
      ctx.fillStyle = '#5b3e24';
      ctx.fillRect(sx - 1.5 * s, sy - trunkH, 3 * s, trunkH);

      // 4-lobe foliage cluster with wind sway
      const cx = sx + sway * 1.1 * s;
      const cy = sy - trunkH - r * 0.65;
      ctx.fillStyle = '#2d5126';
      ctx.beginPath();
      ctx.arc(cx + r * 0.32, cy + r * 0.15, r * 0.72, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = '#3f6e33';
      ctx.beginPath();
      ctx.arc(cx - r * 0.35, cy + r * 0.12, r * 0.75, 0, Math.PI * 2);
      ctx.arc(cx, cy - r * 0.22, r * 0.85, 0, Math.PI * 2);
      ctx.fill();

      // NW sunlit highlight
      ctx.fillStyle = '#5c9145';
      ctx.beginPath();
      ctx.arc(cx - r * 0.25, cy - r * 0.35, r * 0.45, 0, Math.PI * 2);
      ctx.fill();
      break;
    }

    case 'TREE_PINE': {
      const h = 19 * s * vStretch;
      const w = 8 * s;
      ctx.fillStyle = 'rgba(12, 22, 14, 0.28)';
      ctx.beginPath();
      ctx.ellipse(sx + 2 * s, sy + 1 * s, w * 0.9, w * 0.4, 0, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = '#4d331d';
      ctx.fillRect(sx - 1.3 * s, sy - h * 0.25, 2.6 * s, h * 0.25);

      const tipShift = sway * 1.3 * s;
      for (let tier = 0; tier < 3; tier++) {
        const baseY = sy - h * (0.2 + tier * 0.22);
        const topY = sy - h * (0.58 + tier * 0.22);
        const tw = w * (1 - tier * 0.22);
        const tx = sx + tipShift * ((tier + 1) / 3);

        // NW sunlit half
        ctx.fillStyle = '#355e34';
        ctx.beginPath();
        ctx.moveTo(tx, topY);
        ctx.lineTo(sx - tw, baseY);
        ctx.lineTo(sx, baseY);
        ctx.closePath();
        ctx.fill();

        // SE shaded half
        ctx.fillStyle = '#244225';
        ctx.beginPath();
        ctx.moveTo(tx, topY);
        ctx.lineTo(sx, baseY);
        ctx.lineTo(sx + tw, baseY);
        ctx.closePath();
        ctx.fill();
      }
      break;
    }

    case 'HILL_MOUND': {
      const w = 18 * s;
      const h = 11 * s * vStretch;
      ctx.fillStyle = '#6f7c4a';
      ctx.beginPath();
      ctx.moveTo(sx - w, sy);
      ctx.quadraticCurveTo(sx, sy - h * 1.9, sx + w, sy);
      ctx.closePath();
      ctx.fill();

      ctx.fillStyle = '#8b995e';
      ctx.beginPath();
      ctx.moveTo(sx - w * 0.85, sy);
      ctx.quadraticCurveTo(sx - w * 0.25, sy - h * 1.5, sx, sy);
      ctx.closePath();
      ctx.fill();
      break;
    }

    case 'RIDGE_CREST': {
      const w = 22 * s;
      const h = 22 * s * vStretch;
      ctx.fillStyle = '#6d6962';
      ctx.beginPath();
      ctx.moveTo(sx - w, sy);
      ctx.lineTo(sx - w * 0.35, sy - h * 0.72);
      ctx.lineTo(sx, sy - h);
      ctx.lineTo(sx + w * 0.45, sy - h * 0.68);
      ctx.lineTo(sx + w, sy);
      ctx.closePath();
      ctx.fill();

      // NW sunlit face
      ctx.fillStyle = '#928e85';
      ctx.beginPath();
      ctx.moveTo(sx - w, sy);
      ctx.lineTo(sx - w * 0.35, sy - h * 0.72);
      ctx.lineTo(sx, sy - h);
      ctx.lineTo(sx - w * 0.05, sy);
      ctx.closePath();
      ctx.fill();

      if (feature.scale >= 1.0) {
        ctx.fillStyle = '#e5ebf0';
        ctx.beginPath();
        ctx.moveTo(sx, sy - h);
        ctx.lineTo(sx - w * 0.22, sy - h * 0.68);
        ctx.lineTo(sx + w * 0.2, sy - h * 0.66);
        ctx.closePath();
        ctx.fill();
      }
      break;
    }

    case 'MARKET_HALL': {
      const w = 24 * s;
      const h = 18 * s * vStretch;
      // Stone plinth
      ctx.fillStyle = '#9e9583';
      ctx.fillRect(sx - w * 0.85, sy - h * 0.2, w * 1.7, h * 0.22);

      // Colonnade & half-timbered gallery
      ctx.fillStyle = '#e7dcc2';
      ctx.fillRect(sx - w * 0.75, sy - h * 0.72, w * 1.5, h * 0.54);
      ctx.fillStyle = '#5c4028';
      for (let a = -1; a <= 1; a++) {
        ctx.beginPath();
        ctx.arc(sx + a * w * 0.42, sy - h * 0.24, w * 0.14, Math.PI, 0);
        ctx.fill();
      }

      // Steep terracotta hipped roof
      ctx.fillStyle = '#a8432b';
      ctx.beginPath();
      ctx.moveTo(sx - w * 0.9, sy - h * 0.7);
      ctx.lineTo(sx - w * 0.45, sy - h * 1.25);
      ctx.lineTo(sx + w * 0.45, sy - h * 1.25);
      ctx.lineTo(sx + w * 0.9, sy - h * 0.7);
      ctx.closePath();
      ctx.fill();

      // Gold finial
      ctx.fillStyle = '#f59e0b';
      ctx.beginPath();
      ctx.arc(sx, sy - h * 1.32, Math.max(1.8, 2.5 * s), 0, Math.PI * 2);
      ctx.fill();

      drawWavingBanner(
        ctx,
        sx - w * 0.48,
        sy - h * 1.25,
        8 * s * vStretch,
        7 * s,
        4 * s,
        '#b91c1c',
        '#fbbf24',
        timeMs,
        safeSeedOffset,
      );
      drawWavingBanner(
        ctx,
        sx + w * 0.48,
        sy - h * 1.25,
        8 * s * vStretch,
        7 * s,
        4 * s,
        '#1d4ed8',
        '#fde68a',
        timeMs,
        safeSeedOffset + 0.35,
      );
      break;
    }

    case 'MARKET_STALL': {
      const w = 10 * s;
      const h = 8 * s * vStretch;
      // Timber counter & posts
      ctx.fillStyle = '#785332';
      ctx.fillRect(sx - w * 0.65, sy - h * 0.42, w * 1.3, h * 0.42);
      ctx.fillRect(sx - w * 0.6, sy - h * 0.95, 1.4 * s, h * 0.55);
      ctx.fillRect(sx + w * 0.6 - 1.4 * s, sy - h * 0.95, 1.4 * s, h * 0.55);

      // Striped awning
      ctx.fillStyle = primary;
      ctx.beginPath();
      ctx.moveTo(sx - w * 0.8, sy - h * 0.85);
      ctx.lineTo(sx, sy - h * 1.15);
      ctx.lineTo(sx + w * 0.8, sy - h * 0.85);
      ctx.closePath();
      ctx.fill();

      ctx.fillStyle = secondary;
      ctx.beginPath();
      ctx.moveTo(sx - w * 0.25, sy - h * 0.85);
      ctx.lineTo(sx, sy - h * 1.15);
      ctx.lineTo(sx + w * 0.25, sy - h * 0.85);
      ctx.closePath();
      ctx.fill();
      break;
    }

    case 'CASTLE_KEEP': {
      const w = 22 * s;
      const h = 24 * s * vStretch;

      // Ground shadow
      ctx.fillStyle = 'rgba(15, 23, 42, 0.32)';
      ctx.beginPath();
      ctx.ellipse(sx + 2 * s, sy + 1 * s, w * 1.05, w * 0.42, 0, 0, Math.PI * 2);
      ctx.fill();

      // Main stone bastion
      ctx.fillStyle = '#9ca3af';
      ctx.fillRect(sx - w * 0.72, sy - h * 0.62, w * 1.44, h * 0.62);
      ctx.fillStyle = '#6b7280';
      ctx.fillRect(sx + w * 0.12, sy - h * 0.62, w * 0.6, h * 0.62);

      // Central donjon tower
      ctx.fillStyle = '#cbd5e1';
      ctx.fillRect(sx - w * 0.34, sy - h * 1.04, w * 0.68, h * 0.46);

      // Twin corner turrets + kingdom primary roofs
      for (const side of [-1, 1]) {
        const tx = sx + side * w * 0.68;
        ctx.fillStyle = '#94a3b8';
        ctx.fillRect(tx - w * 0.16, sy - h * 0.84, w * 0.32, h * 0.84);

        ctx.fillStyle = primary;
        ctx.beginPath();
        ctx.moveTo(tx - w * 0.22, sy - h * 0.84);
        ctx.lineTo(tx, sy - h * 1.12);
        ctx.lineTo(tx + w * 0.22, sy - h * 0.84);
        ctx.closePath();
        ctx.fill();
      }

      // Portcullis gate
      ctx.fillStyle = '#3b2919';
      ctx.beginPath();
      ctx.arc(sx, sy - h * 0.18, w * 0.16, Math.PI, 0);
      ctx.lineTo(sx + w * 0.16, sy);
      ctx.lineTo(sx - w * 0.16, sy);
      ctx.closePath();
      ctx.fill();

      // Waving heraldic banner
      drawWavingBanner(
        ctx,
        sx,
        sy - h * 1.04,
        10 * s * vStretch,
        10 * s,
        5.5 * s,
        primary,
        secondary,
        timeMs,
        safeSeedOffset,
      );
      break;
    }

    case 'RAMPART_TOWER': {
      const w = 10 * s;
      const h = 16 * s * vStretch;
      // Short stone wall wings
      ctx.fillStyle = '#7c8594';
      ctx.fillRect(sx - w * 1.15, sy - h * 0.42, w * 2.3, h * 0.42);
      // Cylindrical tower
      ctx.fillStyle = '#aeb6c2';
      ctx.fillRect(sx - w * 0.42, sy - h * 0.86, w * 0.84, h * 0.86);
      // Turret cap
      ctx.fillStyle = primary;
      ctx.beginPath();
      ctx.moveTo(sx - w * 0.52, sy - h * 0.86);
      ctx.lineTo(sx, sy - h * 1.18);
      ctx.lineTo(sx + w * 0.52, sy - h * 0.86);
      ctx.closePath();
      ctx.fill();

      drawWavingBanner(
        ctx,
        sx,
        sy - h * 1.18,
        5.5 * s * vStretch,
        6.5 * s,
        3.5 * s,
        primary,
        secondary,
        timeMs,
        safeSeedOffset,
      );
      break;
    }

    case 'HAMLET_HOUSE': {
      const w = 11 * s;
      const h = 11 * s * vStretch;
      // Plaster wall & stone base
      ctx.fillStyle = '#e5dcc8';
      ctx.fillRect(sx - w * 0.65, sy - h * 0.58, w * 1.3, h * 0.58);
      ctx.fillStyle = '#6b655c';
      ctx.fillRect(sx - w * 0.65, sy - h * 0.16, w * 1.3, h * 0.16);
      // Chimney
      ctx.fillStyle = '#7d6c5d';
      ctx.fillRect(sx + w * 0.25, sy - h * 1.12, w * 0.2, h * 0.4);
      // Pitched thatch/tile roof
      ctx.fillStyle = safeSeedOffset > 0.5 ? '#9c5234' : '#b8964f';
      ctx.beginPath();
      ctx.moveTo(sx - w * 0.8, sy - h * 0.56);
      ctx.lineTo(sx, sy - h * 1.05);
      ctx.lineTo(sx + w * 0.8, sy - h * 0.56);
      ctx.closePath();
      ctx.fill();
      // Amber lit window
      ctx.fillStyle = '#fbbf24';
      ctx.fillRect(sx - w * 0.18, sy - h * 0.42, w * 0.28, h * 0.2);
      break;
    }

    case 'WINDMILL': {
      const w = 11 * s;
      const h = 19 * s * vStretch;
      // Tapered mill tower
      ctx.fillStyle = '#d6cbb4';
      ctx.beginPath();
      ctx.moveTo(sx - w * 0.56, sy);
      ctx.lineTo(sx - w * 0.36, sy - h * 0.78);
      ctx.lineTo(sx + w * 0.36, sy - h * 0.78);
      ctx.lineTo(sx + w * 0.56, sy);
      ctx.closePath();
      ctx.fill();

      // Conical timber cap
      ctx.fillStyle = '#734d2c';
      ctx.beginPath();
      ctx.moveTo(sx - w * 0.45, sy - h * 0.78);
      ctx.lineTo(sx, sy - h * 1.06);
      ctx.lineTo(sx + w * 0.45, sy - h * 0.78);
      ctx.closePath();
      ctx.fill();

      // 4 rotating lattice sails
      const hubX = sx;
      const hubY = sy - h * 0.72;
      const sailLen = 12 * s;
      const baseAngle = timeMs * 0.0018 + safeSeedOffset * Math.PI * 2;

      for (let arm = 0; arm < 4; arm++) {
        const a = baseAngle + (arm * Math.PI) / 2;
        const tipX = hubX + Math.cos(a) * sailLen;
        const tipY = hubY + Math.sin(a) * sailLen;
        const perpX = -Math.sin(a) * 2.8 * s;
        const perpY = Math.cos(a) * 2.8 * s;

        ctx.strokeStyle = '#4a321d';
        ctx.lineWidth = Math.max(1, 1.5 * s);
        ctx.beginPath();
        ctx.moveTo(hubX, hubY);
        ctx.lineTo(tipX, tipY);
        ctx.stroke();

        ctx.fillStyle = 'rgba(248, 244, 232, 0.88)';
        ctx.beginPath();
        ctx.moveTo(hubX + Math.cos(a) * 2.5 * s, hubY + Math.sin(a) * 2.5 * s);
        ctx.lineTo(tipX, tipY);
        ctx.lineTo(tipX + perpX, tipY + perpY);
        ctx.lineTo(hubX + Math.cos(a) * 2.5 * s + perpX, hubY + Math.sin(a) * 2.5 * s + perpY);
        ctx.closePath();
        ctx.fill();
      }

      ctx.fillStyle = '#3b2615';
      ctx.beginPath();
      ctx.arc(hubX, hubY, Math.max(1.4, 2 * s), 0, Math.PI * 2);
      ctx.fill();
      break;
    }

    case 'SITE_PALISADE': {
      const w = 13 * s;
      const h = 8 * s * vStretch;
      ctx.strokeStyle = '#6e4e2e';
      ctx.lineWidth = Math.max(1.2, 2.1 * s);
      for (let i = -3; i <= 3; i++) {
        const px = sx + (i / 3) * w;
        const py = sy - Math.cos((i / 3) * 1.1) * 2 * s;
        ctx.beginPath();
        ctx.moveTo(px, py);
        ctx.lineTo(px, py - h);
        ctx.stroke();
      }
      ctx.strokeStyle = '#8b653d';
      ctx.lineWidth = Math.max(1, 1.6 * s);
      ctx.beginPath();
      ctx.moveTo(sx - w, sy - h * 0.55);
      ctx.lineTo(sx + w, sy - h * 0.55);
      ctx.stroke();
      break;
    }

    case 'SITE_FOUNDATION': {
      const w = 16 * s;
      const h = 11 * s * vStretch;
      // Stamped earth clearing
      ctx.fillStyle = 'rgba(138, 114, 78, 0.7)';
      ctx.beginPath();
      ctx.ellipse(sx, sy, w * 0.9, w * 0.42, 0, 0, Math.PI * 2);
      ctx.fill();

      // Stacked stone blocks
      ctx.fillStyle = '#9ca3af';
      ctx.fillRect(sx - w * 0.55, sy - h * 0.25, w * 0.42, h * 0.25);
      ctx.fillStyle = '#cbd5e1';
      ctx.fillRect(sx - w * 0.45, sy - h * 0.45, w * 0.26, h * 0.22);

      // Timber A-frame hoist
      ctx.strokeStyle = '#6e4a28';
      ctx.lineWidth = Math.max(1.2, 1.8 * s);
      ctx.beginPath();
      ctx.moveTo(sx + w * 0.1, sy);
      ctx.lineTo(sx + w * 0.35, sy - h * 0.95);
      ctx.lineTo(sx + w * 0.6, sy);
      ctx.stroke();

      // Surveyor signpost
      ctx.strokeStyle = '#4b331d';
      ctx.beginPath();
      ctx.moveTo(sx - w * 0.72, sy);
      ctx.lineTo(sx - w * 0.72, sy - h * 0.65);
      ctx.stroke();
      ctx.fillStyle = '#e2c992';
      ctx.fillRect(sx - w * 0.72, sy - h * 0.65, w * 0.28, h * 0.22);
      break;
    }

    case 'CROP_FIELD':
      break;
  }
}

function renderPass6Billboards(
  ctx: CanvasRenderingContext2D,
  cache: PreparedMapCache,
  map: GeneratedMap,
  carte: CarteDto,
  params: ProjectionParams,
  trig: ProjectionTrig,
  timeMs: number,
): void {
  const features = map.features ?? [];
  if (features.length === 0) {
    return;
  }

  const fiefByNumero = new Map<number, FiefDomain>();
  const fiefs = map.fiefs ?? [];
  for (let i = 0; i < fiefs.length; i++) {
    fiefByNumero.set(fiefs[i].numero, fiefs[i]);
  }

  const empByNumero = new Map<number, EmplacementCarteDto>();
  const emplacements = carte?.emplacements ?? [];
  for (let i = 0; i < emplacements.length; i++) {
    empByNumero.set(emplacements[i].numero, emplacements[i]);
  }

  const billboards: MapFeature25D[] = [];
  for (let i = 0; i < features.length; i++) {
    const feat = features[i];
    if (feat && feat.kind !== 'CROP_FIELD' && Number.isFinite(feat.x) && Number.isFinite(feat.y)) {
      billboards.push(feat);
    }
  }

  billboards.sort((a, b) => {
    const dy = a.y - b.y;
    if (Math.abs(dy) > 1e-6) {
      return dy;
    }
    const pa = FEATURE_LAYER_PRIORITY[a.kind] ?? 0;
    const pb = FEATURE_LAYER_PRIORITY[b.kind] ?? 0;
    if (pa !== pb) {
      return pa - pb;
    }
    return (a.id ?? '').localeCompare(b.id ?? '');
  });

  for (let i = 0; i < billboards.length; i++) {
    const f = billboards[i];
    const groundH = sampleGroundHeight(cache, f.x, f.y) * params.zoom;
    const anchor = projectPoint(f.x, f.y, groundH, params);
    if (!anchor.visible) {
      continue;
    }

    const fScale = Number.isFinite(f.scale) && f.scale > 0 ? f.scale : 1;
    const s = anchor.k * params.zoom * fScale;
    const owner = f.ownerNumero ?? null;
    const emp = owner !== null ? empByNumero.get(owner) : undefined;
    const fief = owner !== null ? fiefByNumero.get(owner) : undefined;
    const fallback = owner !== null ? createDefaultRoyaume(owner) : undefined;

    const primary =
      f.primaryColor ??
      emp?.royaume?.couleurPrimaire ??
      fief?.royaume?.couleurPrimaire ??
      fallback?.couleurPrimaire ??
      '#b91c1c';
    const secondary =
      f.secondaryColor ??
      emp?.royaume?.couleurSecondaire ??
      fief?.royaume?.couleurSecondaire ??
      fallback?.couleurSecondaire ??
      '#fbbf24';

    renderBillboardFeature(
      ctx,
      f,
      anchor.screenX,
      anchor.screenY,
      s,
      trig.sinEff,
      primary,
      secondary,
      timeMs,
    );
  }
}

// ============================================================================
// Pass 7: Hover / Selection / Inspection Sightlines, Drag Preview & Pills
// ============================================================================

function drawPillBadge(
  ctx: CanvasRenderingContext2D,
  screenX: number,
  screenY: number,
  text: string,
  accentColor: string | null,
  emphasized: boolean,
): void {
  if (!Number.isFinite(screenX) || !Number.isFinite(screenY)) {
    return;
  }
  ctx.font = emphasized
    ? '600 12px system-ui, -apple-system, sans-serif'
    : '500 11px system-ui, -apple-system, sans-serif';
  const metrics = ctx.measureText(text);
  const textW = Number.isFinite(metrics?.width) ? metrics.width : text.length * 6.5;
  const dotPad = accentColor ? 12 : 0;
  const padX = 8;
  const w = textW + padX * 2 + dotPad;
  const h = 20;
  const x = screenX - w * 0.5;
  const y = screenY - h * 0.5;

  traceRoundedRect(ctx, x, y, w, h, 10);
  ctx.fillStyle = emphasized ? 'rgba(15, 23, 42, 0.92)' : 'rgba(15, 23, 42, 0.78)';
  ctx.fill();
  ctx.strokeStyle = accentColor ?? (emphasized ? '#fbbf24' : 'rgba(226, 232, 240, 0.45)');
  ctx.lineWidth = emphasized ? 1.6 : 1;
  ctx.stroke();

  if (accentColor) {
    ctx.fillStyle = accentColor;
    ctx.beginPath();
    ctx.arc(x + padX + 3.5, screenY, 3.5, 0, Math.PI * 2);
    ctx.fill();
  }

  ctx.fillStyle = '#f8fafc';
  ctx.textAlign = 'left';
  ctx.textBaseline = 'middle';
  ctx.fillText(text, x + padX + dotPad, screenY + 0.5);
}

function drawTargetPulseRing(
  ctx: CanvasRenderingContext2D,
  worldX: number,
  worldY: number,
  color: string,
  baseRadius: number,
  params: ProjectionParams,
  trig: ProjectionTrig,
  timeMs: number,
): void {
  if (
    !Number.isFinite(worldX) ||
    !Number.isFinite(worldY) ||
    !Number.isFinite(baseRadius) ||
    baseRadius <= 0
  ) {
    return;
  }
  const pulse = 1 + 0.08 * Math.sin(timeMs * 0.005);
  const r = baseRadius * pulse;
  const steps = 28;
  const verts: ClipVertex[] = new Array(steps + 1);
  for (let i = 0; i <= steps; i++) {
    const theta = (i / steps) * Math.PI * 2;
    verts[i] = toClipVertex(worldX + Math.cos(theta) * r, worldY + Math.sin(theta) * r, 0, params);
  }
  if (traceClippedPolyline(ctx, verts, params, trig)) {
    ctx.strokeStyle = color;
    ctx.lineWidth = Math.max(1.8, 2.6 * params.zoom);
    ctx.stroke();
  }
}

function renderPass7OverlayAndPills(
  ctx: CanvasRenderingContext2D,
  map: GeneratedMap,
  carte: CarteDto,
  state: RenderMapState,
  params: ProjectionParams,
  trig: ProjectionTrig,
  timeMs: number,
): void {
  const cx = Number.isFinite(carte?.centreX)
    ? carte.centreX
    : Number.isFinite(map.centreX)
      ? map.centreX
      : DEFAULT_CARTE_CENTRE_X;
  const cy = Number.isFinite(carte?.centreY)
    ? carte.centreY
    : Number.isFinite(map.centreY)
      ? map.centreY
      : DEFAULT_CARTE_CENTRE_Y;

  const emplacements = carte?.emplacements ?? [];
  const empByNumero = new Map<number, EmplacementCarteDto>();
  for (let i = 0; i < emplacements.length; i++) {
    empByNumero.set(emplacements[i].numero, emplacements[i]);
  }

  // Hover & selection rings
  const targets: Array<{ target: MapInteractionTarget | null | undefined; color: string }> = [
    { target: state.hoveredTarget, color: 'rgba(250, 204, 21, 0.8)' },
    { target: state.selectedTarget, color: 'rgba(56, 189, 248, 0.95)' },
  ];

  for (let t = 0; t < targets.length; t++) {
    const entry = targets[t];
    if (!entry.target) {
      continue;
    }
    if (entry.target.type === 'MARCHE') {
      drawTargetPulseRing(ctx, cx, cy, entry.color, 34, params, trig, timeMs);
    } else {
      const emp = empByNumero.get(entry.target.numero);
      if (emp && Number.isFinite(emp.coordX) && Number.isFinite(emp.coordY)) {
        drawTargetPulseRing(ctx, emp.coordX, emp.coordY, entry.color, 32, params, trig, timeMs);
      }
    }
  }

  // Drag preview ghost & tether
  if (
    state.dragPreview &&
    Number.isFinite(state.dragPreview.coordX) &&
    Number.isFinite(state.dragPreview.coordY)
  ) {
    const dp = state.dragPreview;
    const orig = empByNumero.get(dp.numero);
    if (orig && Number.isFinite(orig.coordX) && Number.isFinite(orig.coordY)) {
      const clipped = clipSegmentNearPlane(
        toClipVertex(orig.coordX, orig.coordY, 0, params),
        toClipVertex(dp.coordX, dp.coordY, 0, params),
        trig.vMax,
      );
      if (clipped) {
        const a = projectClipVertex(clipped[0], params, trig);
        const b = projectClipVertex(clipped[1], params, trig);
        ctx.setLineDash([5, 4]);
        ctx.strokeStyle = 'rgba(251, 191, 36, 0.85)';
        ctx.lineWidth = 1.8;
        ctx.beginPath();
        ctx.moveTo(a.x, a.y);
        ctx.lineTo(b.x, b.y);
        ctx.stroke();
        ctx.setLineDash([]);
      }
    }
    drawTargetPulseRing(
      ctx,
      dp.coordX,
      dp.coordY,
      'rgba(251, 191, 36, 0.95)',
      26,
      params,
      trig,
      timeMs,
    );
  }

  // Inspection sightlines & distance pills
  if (
    state.inspection &&
    Number.isFinite(state.inspection.coordX) &&
    Number.isFinite(state.inspection.coordY)
  ) {
    const insp = state.inspection;
    const fromX = insp.coordX;
    const fromY = insp.coordY;

    const drawSightline = (toX: number, toY: number, dist: number, color: string): void => {
      if (!Number.isFinite(toX) || !Number.isFinite(toY)) {
        return;
      }
      const clipped = clipSegmentNearPlane(
        toClipVertex(fromX, fromY, 0, params),
        toClipVertex(toX, toY, 0, params),
        trig.vMax,
      );
      if (!clipped) {
        return;
      }
      const a = projectClipVertex(clipped[0], params, trig);
      const b = projectClipVertex(clipped[1], params, trig);
      ctx.setLineDash([6, 5]);
      ctx.strokeStyle = color;
      ctx.lineWidth = 1.6;
      ctx.beginPath();
      ctx.moveTo(a.x, a.y);
      ctx.lineTo(b.x, b.y);
      ctx.stroke();
      ctx.setLineDash([]);

      const midX = (a.x + b.x) * 0.5;
      const midY = (a.y + b.y) * 0.5;
      const safeDist = Number.isFinite(dist) ? dist : euclideanDistance(fromX, fromY, toX, toY);
      drawPillBadge(ctx, midX, midY, `${Math.round(safeDist)} u`, color, false);
    };

    if (insp.targetType === 'EMPLACEMENT') {
      drawSightline(cx, cy, insp.distanceMarche, '#fbbf24');
    }

    const entries = insp.distancesRoyaumes ?? [];
    for (let i = 0; i < entries.length; i++) {
      const entry = entries[i];
      if (insp.targetType === 'EMPLACEMENT' && entry.numero === insp.numero) {
        continue;
      }
      const targetEmp = empByNumero.get(entry.numero);
      if (!targetEmp || !Number.isFinite(targetEmp.coordX) || !Number.isFinite(targetEmp.coordY)) {
        continue;
      }
      const dist = Number.isFinite(entry.distance)
        ? entry.distance
        : euclideanDistance(fromX, fromY, targetEmp.coordX, targetEmp.coordY);
      drawSightline(targetEmp.coordX, targetEmp.coordY, dist, entry.couleurPrimaire ?? '#38bdf8');
    }
  }

  // Labels & pill badges
  if (state.showLabels !== false) {
    const marketPt = projectPoint(cx, cy, 0, params);
    if (marketPt.visible) {
      const marketSelected =
        state.selectedTarget?.type === 'MARCHE' || state.hoveredTarget?.type === 'MARCHE';
      drawPillBadge(
        ctx,
        marketPt.screenX,
        marketPt.screenY + 18,
        'Marché Central',
        '#fbbf24',
        marketSelected,
      );
    }

    for (let i = 0; i < emplacements.length; i++) {
      const emp = emplacements[i];
      const pt = projectPoint(emp.coordX, emp.coordY, 0, params);
      if (!pt.visible) {
        continue;
      }

      const isFocused =
        (state.selectedTarget?.type === 'EMPLACEMENT' &&
          state.selectedTarget.numero === emp.numero) ||
        (state.hoveredTarget?.type === 'EMPLACEMENT' && state.hoveredTarget.numero === emp.numero);

      if (emp.occupe) {
        const royaume = emp.royaume ?? createDefaultRoyaume(emp.numero);
        drawPillBadge(
          ctx,
          pt.screenX,
          pt.screenY + 16,
          `#${emp.numero} · ${royaume.nom}`,
          royaume.couleurPrimaire,
          isFocused,
        );
      } else {
        drawPillBadge(ctx, pt.screenX, pt.screenY + 16, `#${emp.numero} · Libre`, null, isFocused);
      }
    }
  }
}

// ============================================================================
// Public Renderer Entrypoint & Class Wrapper
// ============================================================================

export function renderMap25D(
  ctx: CanvasRenderingContext2D,
  width: number,
  height: number,
  map: GeneratedMap,
  carte: CarteDto,
  state: RenderMapState,
  timeMs = 0,
): void {
  if (!ctx || !Number.isFinite(width) || !Number.isFinite(height) || width <= 0 || height <= 0) {
    return;
  }

  const params = state?.projection ?? createDefaultProjectionParams(width, height, carte);
  if (!isValidProjectionParams(params)) {
    return;
  }

  const mapWidth =
    map && Number.isFinite(map.width) && map.width > 0
      ? map.width
      : carte && Number.isFinite(carte.largeur) && carte.largeur > 0
        ? carte.largeur
        : DEFAULT_CARTE_WIDTH;
  const mapHeight =
    map && Number.isFinite(map.height) && map.height > 0
      ? map.height
      : carte && Number.isFinite(carte.hauteur) && carte.hauteur > 0
        ? carte.hauteur
        : DEFAULT_CARTE_HEIGHT;

  const safeTimeMs = Number.isFinite(timeMs) ? timeMs : 0;
  const trig = buildProjectionTrig(params);
  const safeMap: GeneratedMap = map ?? {
    seed: '',
    archetype: 'VALLEE_ROYALE',
    width: mapWidth,
    height: mapHeight,
    centreX: carte?.centreX ?? DEFAULT_CARTE_CENTRE_X,
    centreY: carte?.centreY ?? DEFAULT_CARTE_CENTRE_Y,
    marketRadius: 75,
    marketFreeZoneRadius: MARKET_FREE_ZONE_RADIUS,
    cells: [],
    fiefs: [],
    routes: [],
    features: [],
    riverPaths: [],
  };

  const cache = getPreparedMapCache(safeMap, mapWidth, mapHeight);

  ctx.save();
  try {
    ctx.setLineDash([]);
    ctx.lineDashOffset = 0;
    renderPass1BackdropAndSlab(
      ctx,
      width,
      height,
      mapWidth,
      mapHeight,
      safeMap.archetype,
      params,
      trig,
    );
    renderPass2Terrain(ctx, cache, params, trig);
    renderPass3Water(
      ctx,
      cache,
      safeMap.riverPaths ?? [],
      safeMap.archetype,
      params,
      trig,
      safeTimeMs,
    );
    renderPass4Fiefs(ctx, cache, safeMap, carte, state, params, trig);
    renderPass5DecalsRoadsAndBridges(ctx, cache, safeMap, carte, state, params, trig);
    renderPass6Billboards(ctx, cache, safeMap, carte, params, trig, safeTimeMs);
    renderPass7OverlayAndPills(ctx, safeMap, carte, state, params, trig, safeTimeMs);
  } finally {
    ctx.setLineDash([]);
    ctx.lineDashOffset = 0;
    ctx.restore();
  }
}

export const worldToScreen = projectPoint;

export function hitTestMap(
  screenX: number,
  screenY: number,
  carte: CarteDto,
  params: ProjectionParams,
  hitRadiusPx = 24,
): MapInteractionTarget | null {
  const anchors = getProjectedAnchors(carte, params);
  return hitTestAnchor(screenX, screenY, anchors, hitRadiusPx);
}

export class MapRenderer25D {
  render(
    ctx: CanvasRenderingContext2D,
    width: number,
    height: number,
    map: GeneratedMap,
    carte: CarteDto,
    state: RenderMapState,
    timeMs = 0,
  ): void {
    renderMap25D(ctx, width, height, map, carte, state, timeMs);
  }

  project(x: number, y: number, h: number, params: ProjectionParams): ProjectedPoint {
    return projectPoint(x, y, h, params);
  }

  unproject(screenX: number, screenY: number, params: ProjectionParams): Point2D | null {
    return screenToWorld(screenX, screenY, params);
  }

  unprojectClamped(
    screenX: number,
    screenY: number,
    params: ProjectionParams,
    width = DEFAULT_CARTE_WIDTH,
    height = DEFAULT_CARTE_HEIGHT,
  ): Point2D {
    return screenToWorldClamped(screenX, screenY, params, width, height);
  }

  getAnchors(carte: CarteDto, params: ProjectionParams): ProjectedAnchorMap {
    return getProjectedAnchors(carte, params);
  }

  hitTest(
    screenX: number,
    screenY: number,
    anchors: ProjectedAnchorMap,
    hitRadiusPx = 24,
  ): MapInteractionTarget | null {
    return hitTestAnchor(screenX, screenY, anchors, hitRadiusPx);
  }
}
