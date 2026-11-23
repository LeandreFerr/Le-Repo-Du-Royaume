import { Injectable } from '@angular/core';
import { Delaunay } from 'd3-delaunay';
import {
  ArchetypeCarte,
  BaseTerrainMesh,
  BiomeType,
  BorderSegment,
  BridgeSegment,
  CarteDto,
  DEFAULT_CARTE_HEIGHT,
  DEFAULT_CARTE_WIDTH,
  EMPLACEMENT_TERRAFORM_RADIUS,
  EmplacementCarteDto,
  FiefDomain,
  GeneratedMap,
  MARKET_FREE_ZONE_RADIUS,
  MARKET_TERRAFORM_RADIUS,
  MapFeature25D,
  Point2D,
  TradeRoute,
  VoronoiCell,
} from '../models/carte.model';
import { createDefaultRoyaume, euclideanDistance } from './placement-royaumes.util';
import { createSeededRng, createSimplex2D, fbm2D, mulberry32 } from './prng.util';

export const VORONOI_POINT_COUNT = 1400;
export const LLOYD_RELAXATION_PASSES = 2;
export const MARKET_PAVE_RADIUS = 45;
export const TERRAFORM_BLEND_MARGIN = 20;
export const TARGET_PLATEAU_ELEVATION = 0.42;
export const EMPLACEMENT_FEATURE_EXCLUSION_RADIUS = 55;
export const ROUTE_FEATURE_EXCLUSION_RADIUS = 14;
export const UNOCCUPIED_BORDER_COLOR = '#7a7368';

const MAX_CACHE_SIZE = 12;
const VERTEX_EPS_SQ = 1e-6;
const BOUNDARY_EPS = 1e-2;

const FALLBACK_STALL_PALETTES: readonly { primary: string; secondary: string }[] = [
  { primary: '#b45309', secondary: '#fef3c7' },
  { primary: '#1d4ed8', secondary: '#e0f2fe' },
  { primary: '#15803d', secondary: '#dcfce7' },
  { primary: '#991b1b', secondary: '#fee2e2' },
  { primary: '#7e22ce', secondary: '#f3e8ff' },
  { primary: '#0f766e', secondary: '#ccfbf1' },
] as const;

export interface BiomeClassifyOptions {
  isMarketPlaza?: boolean;
  nearWater?: boolean;
}

interface ArchetypeNatureProfile {
  forestTreesBase: number;
  forestExtraProb: number;
  prairieTreeProb: number;
  pineShare: number;
  hillProb: number;
  ridgeProb: number;
}

interface HeapItem {
  idx: number;
  dist: number;
  owner: number;
}

interface AStarHeapItem {
  idx: number;
  f: number;
  h: number;
  id: number;
}

interface SegmentAabb {
  ax: number;
  ay: number;
  bx: number;
  by: number;
  minX: number;
  maxX: number;
  minY: number;
  maxY: number;
}

interface Disk2D {
  cx: number;
  cy: number;
  r: number;
}

function normalizeSeedString(seed: string | number): string {
  if (typeof seed === 'number') {
    return Object.is(seed, -0) ? '0' : String(seed);
  }
  return String(seed ?? '');
}

function cleanZero(value: number): number {
  return value === 0 ? 0 : value;
}

function clamp01(value: number): number {
  if (!Number.isFinite(value)) {
    return 0;
  }
  const clamped = value < 0 ? 0 : value > 1 ? 1 : value;
  return cleanZero(clamped);
}

function clampRange(value: number, min: number, max: number): number {
  if (!Number.isFinite(value)) {
    return cleanZero(min);
  }
  const clamped = value < min ? min : value > max ? max : value;
  return cleanZero(clamped);
}

function assertValidArchetype(archetype: ArchetypeCarte): void {
  switch (archetype) {
    case 'VALLEE_ROYALE':
    case 'FORET_NOIRE':
    case 'HAUTES_TERRES':
    case 'CONFLUENCES':
    case 'LAC_CENTRAL':
      return;
    default: {
      const exhaustiveCheck: never = archetype;
      throw new RangeError(`Unsupported map archetype: ${String(exhaustiveCheck)}`);
    }
  }
}

function assertValidDimensions(width: number, height: number): void {
  if (!Number.isFinite(width) || width <= 0 || !Number.isFinite(height) || height <= 0) {
    throw new RangeError(
      `Map dimensions must be positive finite numbers, received width=${width}, height=${height}.`,
    );
  }
}

function unclosePolygon(raw: Delaunay.Polygon): Point2D[] {
  let end = raw.length;
  if (
    end > 1 &&
    Math.abs(raw[0][0] - raw[end - 1][0]) < 1e-9 &&
    Math.abs(raw[0][1] - raw[end - 1][1]) < 1e-9
  ) {
    end -= 1;
  }
  const ring: Point2D[] = [];
  for (let k = 0; k < end; k++) {
    const pt = raw[k];
    ring.push({ x: cleanZero(pt[0]), y: cleanZero(pt[1]) });
  }
  return ring;
}

function computePolygonCentroid(ring: readonly Point2D[], fallback: Point2D): Point2D {
  const m = ring.length;
  if (m === 0) {
    return { x: fallback.x, y: fallback.y };
  }
  if (m < 3) {
    let sx = 0;
    let sy = 0;
    for (const p of ring) {
      sx += p.x;
      sy += p.y;
    }
    return { x: cleanZero(sx / m), y: cleanZero(sy / m) };
  }

  let twiceArea = 0;
  let sumCx = 0;
  let sumCy = 0;
  let avgX = 0;
  let avgY = 0;

  for (let k = 0; k < m; k++) {
    const p0 = ring[k];
    const p1 = ring[(k + 1) % m];
    const cross = p0.x * p1.y - p1.x * p0.y;
    twiceArea += cross;
    sumCx += (p0.x + p1.x) * cross;
    sumCy += (p0.y + p1.y) * cross;
    avgX += p0.x;
    avgY += p0.y;
  }

  if (!Number.isFinite(twiceArea) || Math.abs(twiceArea) < 1e-6) {
    return { x: cleanZero(avgX / m), y: cleanZero(avgY / m) };
  }

  const cx = sumCx / (3 * twiceArea);
  const cy = sumCy / (3 * twiceArea);
  if (!Number.isFinite(cx) || !Number.isFinite(cy)) {
    return { x: cleanZero(avgX / m), y: cleanZero(avgY / m) };
  }
  return { x: cleanZero(cx), y: cleanZero(cy) };
}

function cloneCells(cells: readonly VoronoiCell[], resetDomainFields = false): VoronoiCell[] {
  return cells.map((c) => {
    const clone: VoronoiCell = {
      id: c.id,
      center: { x: c.center.x, y: c.center.y },
      polygon: c.polygon.map((p) => ({ x: p.x, y: p.y })),
      neighbors: [...c.neighbors],
      elevation: c.elevation,
      moisture: c.moisture,
      isWater: c.isWater,
      ...(c.waterKind !== undefined ? { waterKind: c.waterKind } : {}),
      biome: c.biome,
      isBoundary: c.isBoundary,
      terraformed: resetDomainFields ? false : c.terraformed,
      isMarketZone: resetDomainFields ? false : c.isMarketZone,
      ownerNumero: resetDomainFields ? null : c.ownerNumero,
      geodesicDistance: resetDomainFields ? Number.POSITIVE_INFINITY : c.geodesicDistance,
    };
    return clone;
  });
}

function cloneRiverPaths(paths: readonly (readonly Point2D[])[]): Point2D[][] {
  return paths.map((path) => path.map((p) => ({ x: p.x, y: p.y })));
}

function cloneBaseTerrainMesh(base: BaseTerrainMesh): BaseTerrainMesh {
  return {
    seed: base.seed,
    archetype: base.archetype,
    width: base.width,
    height: base.height,
    cells: cloneCells(base.cells, false),
    riverPaths: cloneRiverPaths(base.riverPaths),
  };
}

/**
 * Pure biome classification helper shared between Step 2 (archetype shaping)
 * and Step 3 (terraformation + 20px transition rings).
 */
export function classifyCellBiome(
  cell: Pick<VoronoiCell, 'elevation' | 'moisture' | 'isWater' | 'waterKind' | 'terraformed'>,
  archetype: ArchetypeCarte,
  options?: BiomeClassifyOptions,
): BiomeType {
  if (options?.isMarketPlaza) {
    return 'PAVE_MARCHE';
  }
  if (cell.terraformed) {
    return 'PRAIRIE';
  }
  if (cell.isWater) {
    if (cell.waterKind === 'RIVER') {
      return 'RIVIERE';
    }
    return cell.elevation < 0.14 ? 'EAU_PROFONDE' : 'LAC';
  }
  if (cell.elevation >= 0.78) {
    return 'MONTAGNE';
  }
  if (cell.elevation >= 0.64) {
    return 'COLLINE';
  }
  if (options?.nearWater && cell.elevation < 0.36) {
    return 'PLAGE';
  }

  let forestThreshold: number;
  switch (archetype) {
    case 'FORET_NOIRE':
      forestThreshold = 0.48;
      break;
    case 'HAUTES_TERRES':
      forestThreshold = 0.56;
      break;
    default:
      forestThreshold = 0.54;
      break;
  }
  return cell.moisture >= forestThreshold ? 'FORET' : 'PRAIRIE';
}

function pointToSegmentDistance(
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
  const t = clamp01(((px - ax) * dx + (py - ay) * dy) / lenSq);
  const projX = ax + t * dx;
  const projY = ay + t * dy;
  return Math.hypot(px - projX, py - projY);
}

function distToPolyline(pt: Point2D, polyline: readonly Point2D[]): number {
  let minDist = Number.POSITIVE_INFINITY;
  for (let i = 0; i < polyline.length - 1; i++) {
    const d = pointToSegmentDistance(
      pt.x,
      pt.y,
      polyline[i].x,
      polyline[i].y,
      polyline[i + 1].x,
      polyline[i + 1].y,
    );
    if (d < minDist) {
      minDist = d;
    }
  }
  return minDist;
}

function projectToBoundary(
  cx: number,
  cy: number,
  angle: number,
  width: number,
  height: number,
  inset: number,
): Point2D {
  const cos = Math.cos(angle);
  const sin = Math.sin(angle);
  const minX = inset;
  const maxX = Math.max(inset, width - inset);
  const minY = inset;
  const maxY = Math.max(inset, height - inset);

  let tMin = Number.POSITIVE_INFINITY;
  if (Math.abs(cos) > 1e-9) {
    const tx = cos > 0 ? (maxX - cx) / cos : (minX - cx) / cos;
    if (tx > 0 && tx < tMin) {
      tMin = tx;
    }
  }
  if (Math.abs(sin) > 1e-9) {
    const ty = sin > 0 ? (maxY - cy) / sin : (minY - cy) / sin;
    if (ty > 0 && ty < tMin) {
      tMin = ty;
    }
  }
  if (!Number.isFinite(tMin)) {
    return { x: cx, y: cy };
  }
  return {
    x: clampRange(cx + cos * tMin, minX, maxX),
    y: clampRange(cy + sin * tMin, minY, maxY),
  };
}

function pushOutOfCenter(pt: Point2D, cx: number, cy: number, minRadius: number): Point2D {
  const dx = pt.x - cx;
  const dy = pt.y - cy;
  const dist = Math.hypot(dx, dy);
  if (dist >= minRadius) {
    return pt;
  }
  if (dist < 1e-6) {
    return { x: cx + minRadius, y: cy };
  }
  const scale = minRadius / dist;
  return {
    x: cx + dx * scale,
    y: cy + dy * scale,
  };
}

function findNearestCellIndex(cells: readonly VoronoiCell[], x: number, y: number): number {
  let bestIdx = -1;
  let bestDistSq = Number.POSITIVE_INFINITY;
  for (let i = 0; i < cells.length; i++) {
    const dx = cells[i].center.x - x;
    const dy = cells[i].center.y - y;
    const d2 = dx * dx + dy * dy;
    if (
      d2 < bestDistSq ||
      (d2 === bestDistSq && bestIdx !== -1 && cells[i].id < cells[bestIdx].id)
    ) {
      bestDistSq = d2;
      bestIdx = i;
    }
  }
  return bestIdx;
}

class MinHeap<T> {
  private readonly items: T[] = [];

  constructor(private readonly isBefore: (a: T, b: T) => boolean) {}

  get size(): number {
    return this.items.length;
  }

  push(item: T): void {
    this.items.push(item);
    this.siftUp(this.items.length - 1);
  }

  pop(): T | undefined {
    const n = this.items.length;
    if (n === 0) {
      return undefined;
    }
    const top = this.items[0];
    const last = this.items.pop()!;
    if (n > 1) {
      this.items[0] = last;
      this.siftDown(0);
    }
    return top;
  }

  private siftUp(index: number): void {
    let i = index;
    while (i > 0) {
      const parent = (i - 1) >> 1;
      if (this.isBefore(this.items[i], this.items[parent])) {
        const tmp = this.items[i];
        this.items[i] = this.items[parent];
        this.items[parent] = tmp;
        i = parent;
      } else {
        break;
      }
    }
  }

  private siftDown(index: number): void {
    let i = index;
    const n = this.items.length;
    while (true) {
      const left = i * 2 + 1;
      const right = left + 1;
      let smallest = i;

      if (left < n && this.isBefore(this.items[left], this.items[smallest])) {
        smallest = left;
      }
      if (right < n && this.isBefore(this.items[right], this.items[smallest])) {
        smallest = right;
      }
      if (smallest === i) {
        break;
      }
      const tmp = this.items[i];
      this.items[i] = this.items[smallest];
      this.items[smallest] = tmp;
      i = smallest;
    }
  }
}

function isBetterFiefPath(
  distA: number,
  ownerA: number,
  distB: number,
  ownerB: number | null,
): boolean {
  if (ownerB === null) {
    return true;
  }
  return distA < distB || (distA === distB && ownerA < ownerB);
}

function pointsMatch(p: Point2D, q: Point2D): boolean {
  const dx = p.x - q.x;
  const dy = p.y - q.y;
  return dx * dx + dy * dy <= VERTEX_EPS_SQ;
}

function hasMatchingEdge(poly: readonly Point2D[], a: Point2D, b: Point2D): boolean {
  const m = poly.length;
  if (m < 2) {
    return false;
  }
  for (let k = 0; k < m; k++) {
    const p0 = poly[k];
    const p1 = poly[(k + 1) % m];
    if (
      (pointsMatch(p0, a) && pointsMatch(p1, b)) ||
      (pointsMatch(p0, b) && pointsMatch(p1, a))
    ) {
      return true;
    }
  }
  return false;
}

function canonicalEdgeKey(a: Point2D, b: Point2D): string {
  const ax = Math.round(a.x * 1000);
  const ay = Math.round(a.y * 1000);
  const bx = Math.round(b.x * 1000);
  const by = Math.round(b.y * 1000);
  const k1 = `${ax},${ay}`;
  const k2 = `${bx},${by}`;
  return k1 <= k2 ? `${k1}|${k2}` : `${k2}|${k1}`;
}

function isOnMapBounds(a: Point2D, b: Point2D, width: number, height: number): boolean {
  return (
    (a.x <= BOUNDARY_EPS && b.x <= BOUNDARY_EPS) ||
    (a.x >= width - BOUNDARY_EPS && b.x >= width - BOUNDARY_EPS) ||
    (a.y <= BOUNDARY_EPS && b.y <= BOUNDARY_EPS) ||
    (a.y >= height - BOUNDARY_EPS && b.y >= height - BOUNDARY_EPS)
  );
}

function lerpPoint(a: Point2D, b: Point2D, t: number): Point2D {
  return {
    x: cleanZero(a.x + (b.x - a.x) * t),
    y: cleanZero(a.y + (b.y - a.y) * t),
  };
}

function intersectSegments(
  a: Point2D,
  b: Point2D,
  c: Point2D,
  d: Point2D,
): Point2D | null {
  const rX = b.x - a.x;
  const rY = b.y - a.y;
  const sX = d.x - c.x;
  const sY = d.y - c.y;
  const denom = rX * sY - rY * sX;
  if (Math.abs(denom) < 1e-9) {
    return null;
  }
  const qpx = c.x - a.x;
  const qpy = c.y - a.y;
  const t = (qpx * sY - qpy * sX) / denom;
  const u = (qpx * rY - qpy * rX) / denom;
  if (t >= -1e-6 && t <= 1 + 1e-6 && u >= -1e-6 && u <= 1 + 1e-6) {
    const tc = clamp01(t);
    return {
      x: cleanZero(a.x + tc * rX),
      y: cleanZero(a.y + tc * rY),
    };
  }
  return null;
}

function clipPolylineOutsideDisk(
  polyline: readonly Point2D[],
  disk: Disk2D,
): Point2D[][] {
  if (polyline.length < 2) {
    return [];
  }
  const rSq = disk.r * disk.r;
  const segmentsOut: Array<[Point2D, Point2D]> = [];

  for (let i = 0; i < polyline.length - 1; i++) {
    const p = polyline[i];
    const q = polyline[i + 1];
    const dx = q.x - p.x;
    const dy = q.y - p.y;
    const fx = p.x - disk.cx;
    const fy = p.y - disk.cy;

    const a = dx * dx + dy * dy;
    if (a <= 1e-12) {
      continue;
    }
    const b = 2 * (fx * dx + fy * dy);
    const c = fx * fx + fy * fy - rSq;
    const disc = b * b - 4 * a * c;

    if (disc <= 0) {
      // No crossing (or tangent): outside iff midpoint is outside
      const midX = (p.x + q.x) * 0.5 - disk.cx;
      const midY = (p.y + q.y) * 0.5 - disk.cy;
      if (midX * midX + midY * midY >= rSq - 1e-6) {
        segmentsOut.push([{ x: p.x, y: p.y }, { x: q.x, y: q.y }]);
      }
      continue;
    }

    const sqrtDisc = Math.sqrt(disc);
    const t1 = (-b - sqrtDisc) / (2 * a);
    const t2 = (-b + sqrtDisc) / (2 * a);

    const intervals: Array<[number, number]> = [];
    if (t1 > 0) {
      const end = Math.min(1, t1);
      if (end > 1e-6) {
        intervals.push([0, end]);
      }
    }
    if (t2 < 1) {
      const start = Math.max(0, t2);
      if (1 - start > 1e-6) {
        intervals.push([start, 1]);
      }
    }

    for (const [s0, s1] of intervals) {
      const pt0 = lerpPoint(p, q, s0);
      const pt1 = lerpPoint(p, q, s1);
      if (Math.hypot(pt1.x - pt0.x, pt1.y - pt0.y) > 1e-4) {
        segmentsOut.push([pt0, pt1]);
      }
    }
  }

  // Merge contiguous segments back into continuous polylines
  const result: Point2D[][] = [];
  let current: Point2D[] = [];
  for (const [s0, s1] of segmentsOut) {
    if (current.length === 0) {
      current = [s0, s1];
    } else {
      const tail = current[current.length - 1];
      if (Math.hypot(tail.x - s0.x, tail.y - s0.y) <= 1e-3) {
        current.push(s1);
      } else {
        if (current.length >= 2) {
          result.push(current);
        }
        current = [s0, s1];
      }
    }
  }
  if (current.length >= 2) {
    result.push(current);
  }
  return result;
}

function clipRiverPathsAgainstDisks(
  riverPaths: readonly (readonly Point2D[])[],
  disks: readonly Disk2D[],
): Point2D[][] {
  let current = cloneRiverPaths(riverPaths);
  for (const disk of disks) {
    const next: Point2D[][] = [];
    for (const path of current) {
      const clipped = clipPolylineOutsideDisk(path, disk);
      for (const piece of clipped) {
        next.push(piece);
      }
    }
    current = next;
  }
  return current;
}

const MIN_CLIPPED_RIVER_FRAGMENT_LENGTH = 25;
const ORPHAN_RIVER_WATER_CHECK_RADIUS = 30;

function computePolylineLength(poly: readonly Point2D[]): number {
  let total = 0;
  for (let i = 0; i < poly.length - 1; i++) {
    total += Math.hypot(poly[i + 1].x - poly[i].x, poly[i + 1].y - poly[i].y);
  }
  return total;
}

function getPolylineArcMidpoint(poly: readonly Point2D[], totalLength: number): Point2D {
  const half = totalLength * 0.5;
  let acc = 0;
  for (let i = 0; i < poly.length - 1; i++) {
    const segLen = Math.hypot(poly[i + 1].x - poly[i].x, poly[i + 1].y - poly[i].y);
    if (segLen > 1e-9 && acc + segLen >= half) {
      return lerpPoint(poly[i], poly[i + 1], (half - acc) / segLen);
    }
    acc += segLen;
  }
  const midIdx = Math.floor(poly.length * 0.5);
  return { x: cleanZero(poly[midIdx].x), y: cleanZero(poly[midIdx].y) };
}

function pruneOrphanRiverStubs(
  riverPaths: Point2D[][],
  cells: readonly VoronoiCell[],
): Point2D[][] {
  if (cells.length === 0) {
    return riverPaths;
  }
  const kept: Point2D[][] = [];
  for (const path of riverPaths) {
    if (path.length < 2) {
      continue;
    }
    const totalLen = computePolylineLength(path);
    if (totalLen >= MIN_CLIPPED_RIVER_FRAGMENT_LENGTH) {
      kept.push(path);
      continue;
    }
    const mid = getPolylineArcMidpoint(path, totalLen);
    const nearestIdx = findNearestCellIndex(cells, mid.x, mid.y);
    if (nearestIdx < 0 || cells[nearestIdx].terraformed) {
      continue;
    }
    let hasWaterWithinRadius = false;
    for (const c of cells) {
      if (
        c.isWater &&
        Math.hypot(c.center.x - mid.x, c.center.y - mid.y) <= ORPHAN_RIVER_WATER_CHECK_RADIUS
      ) {
        hasWaterWithinRadius = true;
        break;
      }
    }
    if (hasWaterWithinRadius) {
      kept.push(path);
    }
  }
  return kept;
}

function getTraversalCostFactor(cell: VoronoiCell): number {
  if (cell.isWater) {
    return 4.5;
  }
  switch (cell.biome) {
    case 'PAVE_MARCHE':
    case 'PRAIRIE':
    case 'PLAGE':
      return 1.0;
    case 'FORET':
      return 1.35;
    case 'COLLINE':
      return 1.8;
    case 'MONTAGNE':
      return 3.5;
    case 'RIVIERE':
    case 'LAC':
    case 'EAU_PROFONDE':
      return 4.5;
    default:
      return 1.0;
  }
}

function makeBridgeSegment(start: Point2D, end: Point2D): BridgeSegment | null {
  const dx = end.x - start.x;
  const dy = end.y - start.y;
  const length = Math.hypot(dx, dy);
  if (length <= 1e-3) {
    return null;
  }
  const angle = cleanZero(Math.atan2(dy, dx));
  return {
    start: { x: cleanZero(start.x), y: cleanZero(start.y) },
    end: { x: cleanZero(end.x), y: cleanZero(end.y) },
    midpoint: {
      x: cleanZero((start.x + end.x) * 0.5),
      y: cleanZero((start.y + end.y) * 0.5),
    },
    angle,
    length,
  };
}

function isPointInConvexPolygon(px: number, py: number, poly: readonly Point2D[]): boolean {
  const m = poly.length;
  if (m < 3) {
    return false;
  }
  let hasPos = false;
  let hasNeg = false;
  for (let i = 0; i < m; i++) {
    const a = poly[i];
    const b = poly[(i + 1) % m];
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
  return true;
}

function getArchetypeNatureProfile(archetype: ArchetypeCarte): ArchetypeNatureProfile {
  switch (archetype) {
    case 'FORET_NOIRE':
      return {
        forestTreesBase: 2,
        forestExtraProb: 0.45,
        prairieTreeProb: 0.35,
        pineShare: 0.65,
        hillProb: 0.55,
        ridgeProb: 0.8,
      };
    case 'HAUTES_TERRES':
      return {
        forestTreesBase: 1,
        forestExtraProb: 0,
        prairieTreeProb: 0.06,
        pineShare: 0.9,
        hillProb: 0.85,
        ridgeProb: 0.95,
      };
    case 'CONFLUENCES':
      return {
        forestTreesBase: 1,
        forestExtraProb: 0.35,
        prairieTreeProb: 0.15,
        pineShare: 0.3,
        hillProb: 0.6,
        ridgeProb: 0.8,
      };
    case 'LAC_CENTRAL':
      return {
        forestTreesBase: 1,
        forestExtraProb: 0.35,
        prairieTreeProb: 0.16,
        pineShare: 0.35,
        hillProb: 0.6,
        ridgeProb: 0.85,
      };
    case 'VALLEE_ROYALE':
    default:
      return {
        forestTreesBase: 1,
        forestExtraProb: 0.45,
        prairieTreeProb: 0.18,
        pineShare: 0.25,
        hillProb: 0.65,
        ridgeProb: 0.85,
      };
  }
}

@Injectable({ providedIn: 'root' })
export class MapGeneratorService {
  private readonly baseCache = new Map<string, BaseTerrainMesh>();

  /**
   * Phase A (Steps 1–2): Generates or retrieves from LRU cache a deterministic base Voronoi
   * terrain mesh shaped according to the selected Age-of-Empires archetype.
   */
  generateBaseTerrain(
    seed: string | number,
    archetype: ArchetypeCarte,
    width = DEFAULT_CARTE_WIDTH,
    height = DEFAULT_CARTE_HEIGHT,
  ): BaseTerrainMesh {
    assertValidArchetype(archetype);
    assertValidDimensions(width, height);
    const cached = this.getOrCreateBaseTerrain(seed, archetype, width, height);
    return cloneBaseTerrainMesh(cached);
  }

  /**
   * Phase B (Steps 3–6): Applies terraformation, Market free zone & geodesic Voronoi fiefs,
   * A* trade routes with bridges, and deterministic 2.5D features on a cloned copy of `base`.
   */
  populateMapDomains(base: BaseTerrainMesh, carte: CarteDto): GeneratedMap {
    const width = Number.isFinite(base.width) && base.width > 0 ? base.width : carte.largeur;
    const height = Number.isFinite(base.height) && base.height > 0 ? base.height : carte.hauteur;
    const centreX = Number.isFinite(carte.centreX) ? carte.centreX : width * 0.5;
    const centreY = Number.isFinite(carte.centreY) ? carte.centreY : height * 0.5;

    const cells = cloneCells(base.cells, true);
    const idToIndex = new Map<number, number>();
    for (let i = 0; i < cells.length; i++) {
      idToIndex.set(cells[i].id, i);
    }

    const sortedEmps = [...carte.emplacements].sort((a, b) => a.numero - b.numero);

    // ------------------------------------------------------------------------
    // Step 3: Order-independent max-weight smoothstep terraformation
    // ------------------------------------------------------------------------
    const marketCellIdx = findNearestCellIndex(cells, centreX, centreY);
    const pendingDryBlendIndices: number[] = [];
    for (let i = 0; i < cells.length; i++) {
      const c = cells[i];
      const baseElev = c.elevation;
      const baseMoisture = c.moisture;
      const baseIsWater = c.isWater;
      const baseWaterKind = c.waterKind;

      const dMarket = euclideanDistance(c.center.x, c.center.y, centreX, centreY);
      let wMarket = 0;
      if (dMarket <= MARKET_TERRAFORM_RADIUS) {
        wMarket = 1;
      } else if (dMarket < MARKET_TERRAFORM_RADIUS + TERRAFORM_BLEND_MARGIN) {
        const u = (dMarket - MARKET_TERRAFORM_RADIUS) / TERRAFORM_BLEND_MARGIN;
        wMarket = 1 - u * u * (3 - 2 * u);
      }

      let wEmpMax = 0;
      for (const emp of sortedEmps) {
        const dEmp = euclideanDistance(c.center.x, c.center.y, emp.coordX, emp.coordY);
        if (dEmp <= EMPLACEMENT_TERRAFORM_RADIUS) {
          wEmpMax = 1;
          break;
        }
        if (dEmp < EMPLACEMENT_TERRAFORM_RADIUS + TERRAFORM_BLEND_MARGIN) {
          const u = (dEmp - EMPLACEMENT_TERRAFORM_RADIUS) / TERRAFORM_BLEND_MARGIN;
          wEmpMax = Math.max(wEmpMax, 1 - u * u * (3 - 2 * u));
        }
      }

      const w = Math.max(wMarket, wEmpMax);
      if (w === 0) {
        c.terraformed = false;
        continue;
      }

      if (w === 1) {
        c.terraformed = true;
        c.isWater = false;
        delete c.waterKind;
        c.elevation = TARGET_PLATEAU_ELEVATION;
        c.moisture = Math.max(baseMoisture, 0.45);
        const isMarketPlaza =
          dMarket <= MARKET_PAVE_RADIUS ||
          (i === marketCellIdx && dMarket <= MARKET_TERRAFORM_RADIUS);
        c.biome = classifyCellBiome(c, base.archetype, { isMarketPlaza });
        continue;
      }

      // Smooth 20px transition ring (0 < w < 1)
      c.terraformed = false;
      c.elevation = clamp01(baseElev * (1 - w) + TARGET_PLATEAU_ELEVATION * w);

      if (baseIsWater && baseWaterKind === 'RIVER') {
        c.isWater = true;
        c.waterKind = 'RIVER';
        c.biome = classifyCellBiome(c, base.archetype);
      } else if (baseIsWater) {
        if (c.elevation >= 0.34) {
          c.isWater = false;
          delete c.waterKind;
          c.biome = classifyCellBiome(c, base.archetype, { nearWater: true });
        } else {
          c.isWater = true;
          c.waterKind = 'LAKE';
          c.biome = classifyCellBiome(c, base.archetype);
        }
      } else {
        pendingDryBlendIndices.push(i);
      }
    }

    // Classify dry transition-ring cells after all Step 3 isWater updates complete
    for (const idx of pendingDryBlendIndices) {
      const c = cells[idx];
      const nearLake = c.neighbors.some((nId) => {
        const nIdx = idToIndex.get(nId);
        if (nIdx === undefined) {
          return false;
        }
        const nb = cells[nIdx];
        return nb.isWater && nb.waterKind !== 'RIVER';
      });
      c.biome = classifyCellBiome(c, base.archetype, { nearWater: nearLake });
    }

    const terraformDisks: Disk2D[] = [
      { cx: centreX, cy: centreY, r: MARKET_TERRAFORM_RADIUS },
      ...sortedEmps.map((emp) => ({
        cx: emp.coordX,
        cy: emp.coordY,
        r: EMPLACEMENT_TERRAFORM_RADIUS,
      })),
    ];
    const riverPaths = pruneOrphanRiverStubs(
      clipRiverPathsAgainstDisks(base.riverPaths, terraformDisks),
      cells,
    );

    // ------------------------------------------------------------------------
    // Step 4: Market Free Zone (d < 110) & Geodesic Voronoi Fief Partitioning
    // ------------------------------------------------------------------------
    for (const c of cells) {
      c.isMarketZone =
        euclideanDistance(c.center.x, c.center.y, centreX, centreY) < MARKET_FREE_ZONE_RADIUS;
      c.ownerNumero = null;
      c.geodesicDistance = Number.POSITIVE_INFINITY;
    }

    const fiefs = this.partitionGeodesicFiefs(
      cells,
      idToIndex,
      sortedEmps,
      width,
      height,
    );

    // ------------------------------------------------------------------------
    // Step 5: A* Trade Routes & Dual Bridge Detection
    // ------------------------------------------------------------------------
    const routes = this.computeTradeRoutes(
      cells,
      idToIndex,
      sortedEmps,
      centreX,
      centreY,
      riverPaths,
    );

    // ------------------------------------------------------------------------
    // Step 6: Deterministic 2.5D Feature Population
    // ------------------------------------------------------------------------
    const features = this.populateFeatures25D(
      base.seed,
      base.archetype,
      width,
      height,
      centreX,
      centreY,
      cells,
      idToIndex,
      sortedEmps,
      fiefs,
      routes,
    );

    return {
      seed: base.seed,
      archetype: base.archetype,
      width,
      height,
      centreX,
      centreY,
      marketRadius: MARKET_TERRAFORM_RADIUS,
      marketFreeZoneRadius: MARKET_FREE_ZONE_RADIUS,
      cells,
      fiefs,
      routes,
      features,
      riverPaths,
    };
  }

  /**
   * Convenience full pipeline running Phase A (cached) followed by Phase B.
   */
  generateMap(
    carte: CarteDto,
    seed: string | number,
    archetype: ArchetypeCarte,
  ): GeneratedMap {
    assertValidArchetype(archetype);
    const width = Number.isFinite(carte.largeur) && carte.largeur > 0 ? carte.largeur : DEFAULT_CARTE_WIDTH;
    const height = Number.isFinite(carte.hauteur) && carte.hauteur > 0 ? carte.hauteur : DEFAULT_CARTE_HEIGHT;
    const base = this.getOrCreateBaseTerrain(seed, archetype, width, height);
    return this.populateMapDomains(base, carte);
  }

  private getOrCreateBaseTerrain(
    seed: string | number,
    archetype: ArchetypeCarte,
    width: number,
    height: number,
  ): BaseTerrainMesh {
    const normSeed = normalizeSeedString(seed);
    const cacheKey = `${normSeed.length}:${normSeed}|${archetype}|${width}|${height}`;

    const existing = this.baseCache.get(cacheKey);
    if (existing) {
      this.baseCache.delete(cacheKey);
      this.baseCache.set(cacheKey, existing);
      return existing;
    }

    const built = this.buildBaseTerrain(normSeed, archetype, width, height);
    if (this.baseCache.size >= MAX_CACHE_SIZE) {
      const oldestKey = this.baseCache.keys().next().value;
      if (oldestKey !== undefined) {
        this.baseCache.delete(oldestKey);
      }
    }
    this.baseCache.set(cacheKey, built);
    return built;
  }

  private buildBaseTerrain(
    seedStr: string,
    archetype: ArchetypeCarte,
    width: number,
    height: number,
  ): BaseTerrainMesh {
    // ------------------------------------------------------------------------
    // Step 1: 1400 deterministic points + 2 Lloyd passes via d3-delaunay
    // ------------------------------------------------------------------------
    const rngVoronoi = createSeededRng(seedStr, 'voronoi');
    const marginX = Math.min(0.5, width * 0.25);
    const marginY = Math.min(0.5, height * 0.25);
    const spanX = Math.max(0, width - 2 * marginX);
    const spanY = Math.max(0, height - 2 * marginY);

    let points: Array<[number, number]> = [];
    for (let i = 0; i < VORONOI_POINT_COUNT; i++) {
      points.push([
        cleanZero(marginX + rngVoronoi() * spanX),
        cleanZero(marginY + rngVoronoi() * spanY),
      ]);
    }

    for (let pass = 0; pass < LLOYD_RELAXATION_PASSES; pass++) {
      const delaunay = Delaunay.from(points);
      const voronoi = delaunay.voronoi([0, 0, width, height]);
      const nextPoints: Array<[number, number]> = new Array(points.length);

      for (let i = 0; i < points.length; i++) {
        const raw = voronoi.cellPolygon(i) as Delaunay.Polygon | null;
        if (!raw || raw.length < 3) {
          nextPoints[i] = points[i];
          continue;
        }
        const ring = unclosePolygon(raw);
        const centroid = computePolygonCentroid(ring, {
          x: points[i][0],
          y: points[i][1],
        });
        nextPoints[i] = [
          clampRange(centroid.x, marginX, width - marginX),
          clampRange(centroid.y, marginY, height - marginY),
        ];
      }
      points = nextPoints;
    }

    const finalDelaunay = Delaunay.from(points);
    const finalVoronoi = finalDelaunay.voronoi([0, 0, width, height]);

    const neighborSets: Set<number>[] = Array.from(
      { length: points.length },
      () => new Set<number>(),
    );
    for (let i = 0; i < points.length; i++) {
      for (const j of finalVoronoi.neighbors(i)) {
        if (j !== i && j >= 0 && j < points.length) {
          neighborSets[i].add(j);
          neighborSets[j].add(i);
        }
      }
    }

    const cells: VoronoiCell[] = new Array(points.length);
    for (let i = 0; i < points.length; i++) {
      const raw = finalVoronoi.cellPolygon(i) as Delaunay.Polygon | null;
      let polygon: Point2D[];
      if (raw && raw.length >= 3) {
        polygon = unclosePolygon(raw);
      } else {
        const px = points[i][0];
        const py = points[i][1];
        const eps = Math.min(1, width * 0.05, height * 0.05);
        polygon = [
          { x: clampRange(px - eps, 0, width), y: clampRange(py - eps, 0, height) },
          { x: clampRange(px + eps, 0, width), y: clampRange(py - eps, 0, height) },
          { x: clampRange(px + eps, 0, width), y: clampRange(py + eps, 0, height) },
          { x: clampRange(px - eps, 0, width), y: clampRange(py + eps, 0, height) },
        ];
      }

      const isBoundary = polygon.some(
        (v) =>
          v.x <= BOUNDARY_EPS ||
          v.x >= width - BOUNDARY_EPS ||
          v.y <= BOUNDARY_EPS ||
          v.y >= height - BOUNDARY_EPS,
      );

      cells[i] = {
        id: i,
        center: { x: points[i][0], y: points[i][1] },
        polygon,
        neighbors: Array.from(neighborSets[i]).sort((a, b) => a - b),
        elevation: 0.5,
        moisture: 0.5,
        isWater: false,
        biome: 'PRAIRIE',
        isBoundary,
        terraformed: false,
        isMarketZone: false,
        ownerNumero: null,
        geodesicDistance: Number.POSITIVE_INFINITY,
      };
    }

    // ------------------------------------------------------------------------
    // Step 2: Simplex / fBm fields + 5 AoE archetypes + river carving
    // ------------------------------------------------------------------------
    const elevNoise = createSimplex2D(createSeededRng(seedStr, 'elevation'));
    const moistNoise = createSimplex2D(createSeededRng(seedStr, 'moisture'));
    const detailNoise = createSimplex2D(createSeededRng(seedStr, 'detail'));
    const rngRivers = createSeededRng(seedStr, 'rivers');

    const cx = width * 0.5;
    const cy = height * 0.5;
    const minDim = Math.min(width, height);
    const scale = minDim / 1000;

    for (const cell of cells) {
      const x = cell.center.x;
      const y = cell.center.y;
      const ux = x / width;
      const uy = y / height;

      const rawElev = 0.5 + 0.5 * fbm2D(elevNoise, ux * 3.2, uy * 3.2, 4, 0.5, 2.0);
      const rawMoist =
        0.5 + 0.5 * fbm2D(moistNoise, ux * 2.8 + 13.7, uy * 2.8 - 7.3, 4, 0.5, 2.0);
      const ridge =
        1 - Math.abs(fbm2D(detailNoise, ux * 3.6 + 5.1, uy * 3.6 + 9.4, 3, 0.5, 2.1));
      const d = Math.hypot(x - cx, y - cy);
      const dNorm = d / (minDim * 0.5);

      switch (archetype) {
        case 'VALLEE_ROYALE': {
          const valleyRim =
            0.2 * Math.pow(Math.max(0, dNorm - 0.55) / 0.65, 1.5) -
            0.06 * Math.exp(-(dNorm * dNorm) / 0.18);
          cell.elevation = clamp01(0.41 + 0.3 * rawElev + valleyRim);
          cell.moisture = clamp01(0.5 + 0.32 * rawMoist);
          break;
        }
        case 'FORET_NOIRE': {
          cell.elevation = clamp01(
            0.35 + 0.25 * rawElev + 0.08 * Math.max(0, dNorm - 0.85),
          );
          cell.moisture = clamp01(0.64 + 0.28 * rawMoist);
          if (cell.elevation < 0.23 && d > 115 * scale) {
            cell.isWater = true;
            cell.waterKind = 'LAKE';
          }
          break;
        }
        case 'HAUTES_TERRES': {
          let elev = clamp01(0.47 + 0.24 * rawElev + 0.28 * (ridge - 0.45));
          if (d < 80 * scale) {
            elev = Math.min(elev, 0.72);
          }
          cell.elevation = elev;
          cell.moisture = clamp01(0.41 + 0.3 * rawMoist);
          if (cell.elevation < 0.22 && d > 115 * scale) {
            cell.isWater = true;
            cell.waterKind = 'LAKE';
          }
          break;
        }
        case 'CONFLUENCES': {
          cell.elevation = clamp01(
            0.38 + 0.28 * rawElev + 0.1 * Math.max(0, dNorm - 0.8),
          );
          cell.moisture = clamp01(0.54 + 0.3 * rawMoist);
          break;
        }
        case 'LAC_CENTRAL': {
          const angle = Math.atan2(y - cy, x - cx);
          const wobbleIn =
            fbm2D(
              detailNoise,
              Math.cos(angle) * 1.8 + 4.1,
              Math.sin(angle) * 1.8 + 4.1,
              2,
              0.5,
              2.0,
            ) *
            9 *
            scale;
          const wobbleOut =
            fbm2D(
              detailNoise,
              Math.cos(angle) * 2.2 - 3.7,
              Math.sin(angle) * 2.2 + 2.9,
              2,
              0.5,
              2.0,
            ) *
            20 *
            scale;

          const rInner = clampRange(92 * scale + wobbleIn, 82 * scale, 106 * scale);
          const rOuter = clampRange(235 * scale + wobbleOut, 210 * scale, 262 * scale);
          const mid = 0.5 * (rInner + rOuter);
          const halfSpan = Math.max(1e-6, 0.5 * (rOuter - rInner));

          if (d <= rInner) {
            cell.elevation = clamp01(0.42 + 0.1 * rawElev);
            cell.isWater = false;
          } else if (d < rOuter) {
            const t = clamp01(1 - Math.abs(d - mid) / halfSpan);
            cell.elevation = clamp01(0.24 - 0.19 * t + 0.03 * (rawElev - 0.5));
            cell.isWater = true;
            cell.waterKind = 'LAKE';
          } else {
            const shoreBlend = clamp01((d - rOuter) / (45 * scale));
            const outerElev =
              0.4 + 0.28 * rawElev + 0.14 * Math.max(0, dNorm - 0.85);
            cell.elevation = clamp01(0.31 * (1 - shoreBlend) + outerElev * shoreBlend);
            cell.isWater = false;
          }

          cell.moisture = clamp01(
            0.5 +
              0.28 * rawMoist +
              0.15 * Math.exp(-Math.pow((d - mid) / (90 * scale), 2)),
          );
          break;
        }
      }
    }

    // River carving
    const riverPaths: Point2D[][] = [];
    const baseAngle = rngRivers() * Math.PI * 2;
    const inset = 12 * scale;

    const carveRiverLeg = (startPt: Point2D, endPt: Point2D, rSafe: number): void => {
      const guideCount = 24;
      const guidePts: Point2D[] = [];
      const dx = endPt.x - startPt.x;
      const dy = endPt.y - startPt.y;
      const len = Math.hypot(dx, dy);
      const nx = len > 1e-6 ? -dy / len : 0;
      const ny = len > 1e-6 ? dx / len : 0;
      const phase = rngRivers() * Math.PI * 2;
      const amp = (28 + rngRivers() * 24) * scale;

      for (let k = 0; k <= guideCount; k++) {
        const t = k / guideCount;
        const envelope = Math.sin(t * Math.PI);
        const lat =
          envelope *
          (amp * Math.sin(t * Math.PI * 2.5 + phase) +
            16 * scale * fbm2D(detailNoise, t * 2.4 + phase, phase * 0.3, 2, 0.5, 2.0));
        let pt: Point2D = {
          x: clampRange(startPt.x + dx * t + nx * lat, 0, width),
          y: clampRange(startPt.y + dy * t + ny * lat, 0, height),
        };
        if (k > 0 && k < guideCount) {
          pt = pushOutOfCenter(pt, cx, cy, rSafe);
          pt = {
            x: clampRange(pt.x, 0, width),
            y: clampRange(pt.y, 0, height),
          };
        }
        guidePts.push(pt);
      }

      // One smoothing pass on intermediate guide points, re-enforcing R_safe
      for (let k = 1; k < guidePts.length - 1; k++) {
        const avg: Point2D = {
          x: 0.25 * guidePts[k - 1].x + 0.5 * guidePts[k].x + 0.25 * guidePts[k + 1].x,
          y: 0.25 * guidePts[k - 1].y + 0.5 * guidePts[k].y + 0.25 * guidePts[k + 1].y,
        };
        const pushed = pushOutOfCenter(avg, cx, cy, rSafe);
        guidePts[k] = {
          x: clampRange(pushed.x, 0, width),
          y: clampRange(pushed.y, 0, height),
        };
      }

      const startIdx = finalDelaunay.find(startPt.x, startPt.y);
      const endIdx = finalDelaunay.find(endPt.x, endPt.y);
      if (startIdx < 0 || endIdx < 0 || startIdx === endIdx) {
        return;
      }

      const n = cells.length;
      const gScore = new Float64Array(n).fill(Number.POSITIVE_INFINITY);
      const parent = new Int32Array(n).fill(-1);
      const closed = new Uint8Array(n);
      const endCenter = cells[endIdx].center;

      const heap = new MinHeap<AStarHeapItem>((a, b) => a.f < b.f || (a.f === b.f && a.id < b.id));
      gScore[startIdx] = 0;
      const h0 = Math.hypot(
        endCenter.x - cells[startIdx].center.x,
        endCenter.y - cells[startIdx].center.y,
      );
      heap.push({ idx: startIdx, f: h0, h: h0, id: startIdx });

      let found = false;
      while (heap.size > 0) {
        const cur = heap.pop()!;
        if (closed[cur.idx]) {
          continue;
        }
        if (cur.idx === endIdx) {
          found = true;
          break;
        }
        closed[cur.idx] = 1;
        const uCell = cells[cur.idx];

        for (const vIdx of uCell.neighbors) {
          if (closed[vIdx]) {
            continue;
          }
          const vCell = cells[vIdx];
          const stepLen = Math.hypot(
            vCell.center.x - uCell.center.x,
            vCell.center.y - uCell.center.y,
          );
          const guideDist = distToPolyline(vCell.center, guidePts);
          const centerDist = Math.hypot(vCell.center.x - cx, vCell.center.y - cy);
          const centerPenalty = centerDist < 105 * scale ? 1e6 : 0;
          const stepCost =
            stepLen * (1 + 0.08 * guideDist + 0.6 * vCell.elevation) + centerPenalty;
          const tentative = gScore[cur.idx] + stepCost;

          if (tentative < gScore[vIdx]) {
            gScore[vIdx] = tentative;
            parent[vIdx] = cur.idx;
            const hv = Math.hypot(endCenter.x - vCell.center.x, endCenter.y - vCell.center.y);
            heap.push({ idx: vIdx, f: tentative + hv, h: hv, id: vIdx });
          }
        }
      }

      if (!found) {
        return;
      }

      const cellPath: number[] = [];
      let trace = endIdx;
      while (trace !== -1) {
        cellPath.push(trace);
        trace = parent[trace];
      }
      cellPath.reverse();

      for (const cid of cellPath) {
        const c = cells[cid];
        if (!(c.isWater && c.waterKind === 'LAKE')) {
          c.isWater = true;
          c.waterKind = 'RIVER';
        }
      }

      const rawPath = cellPath.map((cid) => ({
        x: cells[cid].center.x,
        y: cells[cid].center.y,
      }));
      if (rawPath.length >= 2) {
        const smoothed: Point2D[] = rawPath.map((pt) => ({ x: pt.x, y: pt.y }));
        for (let k = 1; k < rawPath.length - 1; k++) {
          const avg: Point2D = {
            x: 0.25 * rawPath[k - 1].x + 0.5 * rawPath[k].x + 0.25 * rawPath[k + 1].x,
            y: 0.25 * rawPath[k - 1].y + 0.5 * rawPath[k].y + 0.25 * rawPath[k + 1].y,
          };
          const pushed = pushOutOfCenter(avg, cx, cy, 100 * scale);
          smoothed[k] = {
            x: clampRange(pushed.x, 0, width),
            y: clampRange(pushed.y, 0, height),
          };
        }
        riverPaths.push(smoothed);
      }
    };

    switch (archetype) {
      case 'VALLEE_ROYALE': {
        const p1 = projectToBoundary(cx, cy, baseAngle + Math.PI * 0.85, width, height, inset);
        const p2 = projectToBoundary(cx, cy, baseAngle - Math.PI * 0.15, width, height, inset);
        carveRiverLeg(p1, p2, 125 * scale);

        const midAnchor: Point2D = pushOutOfCenter(
          {
            x: cx + Math.cos(baseAngle + 0.2) * 165 * scale,
            y: cy + Math.sin(baseAngle + 0.2) * 165 * scale,
          },
          cx,
          cy,
          125 * scale,
        );
        const p3 = projectToBoundary(cx, cy, baseAngle + Math.PI * 0.45, width, height, inset);
        carveRiverLeg(p3, midAnchor, 125 * scale);
        break;
      }
      case 'FORET_NOIRE': {
        const p1 = projectToBoundary(cx, cy, baseAngle + Math.PI * 0.72, width, height, inset);
        const p2 = projectToBoundary(cx, cy, baseAngle - Math.PI * 0.28, width, height, inset);
        carveRiverLeg(p1, p2, 125 * scale);
        break;
      }
      case 'HAUTES_TERRES': {
        const source: Point2D = {
          x: clampRange(cx + Math.cos(baseAngle + Math.PI) * 230 * scale, inset, width - inset),
          y: clampRange(cy + Math.sin(baseAngle + Math.PI) * 230 * scale, inset, height - inset),
        };
        const exit = projectToBoundary(cx, cy, baseAngle, width, height, inset);
        carveRiverLeg(source, exit, 125 * scale);
        break;
      }
      case 'CONFLUENCES': {
        const rConf = (145 + rngRivers() * 30) * scale;
        const confPt: Point2D = {
          x: clampRange(cx + Math.cos(baseAngle) * rConf, inset * 2, width - inset * 2),
          y: clampRange(cy + Math.sin(baseAngle) * rConf, inset * 2, height - inset * 2),
        };
        const trib1 = projectToBoundary(cx, cy, baseAngle + 0.55 * Math.PI, width, height, inset);
        const trib2 = projectToBoundary(cx, cy, baseAngle - 0.55 * Math.PI, width, height, inset);
        const trib3 = projectToBoundary(cx, cy, baseAngle + 1.05 * Math.PI, width, height, inset);
        const exit = projectToBoundary(cx, cy, baseAngle, width, height, inset);

        carveRiverLeg(trib1, confPt, 118 * scale);
        carveRiverLeg(trib2, confPt, 118 * scale);
        carveRiverLeg(trib3, confPt, 118 * scale);
        carveRiverLeg(confPt, exit, 118 * scale);
        break;
      }
      case 'LAC_CENTRAL': {
        const rStart = 248 * scale;
        const startOut: Point2D = {
          x: clampRange(cx + Math.cos(baseAngle) * rStart, inset, width - inset),
          y: clampRange(cy + Math.sin(baseAngle) * rStart, inset, height - inset),
        };
        const exitOut = projectToBoundary(cx, cy, baseAngle, width, height, inset);
        carveRiverLeg(startOut, exitOut, 125 * scale);
        break;
      }
    }

    // 2-hop riparian moisture boost around all water cells
    const distFromWater = new Int8Array(cells.length).fill(-1);
    const bfsQueue: number[] = [];
    for (let i = 0; i < cells.length; i++) {
      if (cells[i].isWater) {
        distFromWater[i] = 0;
        bfsQueue.push(i);
      }
    }
    let head = 0;
    while (head < bfsQueue.length) {
      const u = bfsQueue[head++];
      const dHop = distFromWater[u];
      if (dHop >= 2) {
        continue;
      }
      for (const v of cells[u].neighbors) {
        if (distFromWater[v] === -1) {
          distFromWater[v] = dHop + 1;
          bfsQueue.push(v);
        }
      }
    }
    for (let i = 0; i < cells.length; i++) {
      if (distFromWater[i] === 1) {
        cells[i].moisture = clamp01(cells[i].moisture + 0.16);
      } else if (distFromWater[i] === 2) {
        cells[i].moisture = clamp01(cells[i].moisture + 0.08);
      }
    }

    // Classify initial biomes
    for (const c of cells) {
      const nearLake = c.neighbors.some(
        (nId) => cells[nId].isWater && cells[nId].waterKind !== 'RIVER',
      );
      c.biome = classifyCellBiome(c, archetype, { nearWater: nearLake });
    }

    return {
      seed: seedStr,
      archetype,
      width,
      height,
      cells,
      riverPaths,
    };
  }

  private partitionGeodesicFiefs(
    cells: VoronoiCell[],
    idToIndex: ReadonlyMap<number, number>,
    sortedEmps: readonly EmplacementCarteDto[],
    width: number,
    height: number,
  ): FiefDomain[] {
    if (sortedEmps.length === 0) {
      return [];
    }

    const n = cells.length;
    const bestDist = new Float64Array(n).fill(Number.POSITIVE_INFINITY);
    const bestOwner: Array<number | null> = new Array(n).fill(null);

    const heap = new MinHeap<HeapItem>((a, b) =>
      a.dist < b.dist || (a.dist === b.dist && a.owner < b.owner),
    );

    for (const emp of sortedEmps) {
      const seedIdx = findNearestCellIndex(cells, emp.coordX, emp.coordY);
      if (
        seedIdx >= 0 &&
        isBetterFiefPath(0, emp.numero, bestDist[seedIdx], bestOwner[seedIdx])
      ) {
        bestDist[seedIdx] = 0;
        bestOwner[seedIdx] = emp.numero;
        heap.push({ idx: seedIdx, dist: 0, owner: emp.numero });
      }
    }

    while (heap.size > 0) {
      const cur = heap.pop()!;
      if (
        cur.dist > bestDist[cur.idx] ||
        (cur.dist === bestDist[cur.idx] && cur.owner !== bestOwner[cur.idx])
      ) {
        continue;
      }

      const uCell = cells[cur.idx];
      for (const neighborId of uCell.neighbors) {
        const vIdx = idToIndex.get(neighborId);
        if (vIdx === undefined) {
          continue;
        }
        const vCell = cells[vIdx];
        const edgeDist = Math.hypot(
          vCell.center.x - uCell.center.x,
          vCell.center.y - uCell.center.y,
        );
        const weightFactor = vCell.isMarketZone ? 100.0 : vCell.isWater ? 4.0 : 1.0;
        const nextDist = cur.dist + edgeDist * weightFactor;

        if (isBetterFiefPath(nextDist, cur.owner, bestDist[vIdx], bestOwner[vIdx])) {
          bestDist[vIdx] = nextDist;
          bestOwner[vIdx] = cur.owner;
          heap.push({ idx: vIdx, dist: nextDist, owner: cur.owner });
        }
      }
    }

    const fiefCellsMap = new Map<number, number[]>();
    for (const emp of sortedEmps) {
      fiefCellsMap.set(emp.numero, []);
    }

    for (let i = 0; i < n; i++) {
      const c = cells[i];
      c.geodesicDistance = bestDist[i];
      if (c.isWater || c.isMarketZone) {
        c.ownerNumero = null;
        continue;
      }

      let owner = bestOwner[i];
      if (owner === null) {
        // Fallback for disconnected synthetic test graphs
        let bestFallbackDist = Number.POSITIVE_INFINITY;
        for (const emp of sortedEmps) {
          const d = euclideanDistance(c.center.x, c.center.y, emp.coordX, emp.coordY);
          if (
            d < bestFallbackDist ||
            (d === bestFallbackDist && owner !== null && emp.numero < owner)
          ) {
            bestFallbackDist = d;
            owner = emp.numero;
          }
        }
      }

      c.ownerNumero = owner;
      if (owner !== null) {
        fiefCellsMap.get(owner)?.push(c.id);
      }
    }

    const fiefs: FiefDomain[] = [];
    for (const emp of sortedEmps) {
      const dashed = !emp.occupe;
      const color = emp.occupe
        ? (emp.royaume?.couleurPrimaire ?? createDefaultRoyaume(emp.numero).couleurPrimaire)
        : UNOCCUPIED_BORDER_COLOR;
      const ownedIds = fiefCellsMap.get(emp.numero) ?? [];
      const seenEdges = new Set<string>();
      const borderSegments: BorderSegment[] = [];

      for (const cid of ownedIds) {
        const cIdx = idToIndex.get(cid);
        if (cIdx === undefined) {
          continue;
        }
        const c = cells[cIdx];
        const poly = c.polygon;
        if (poly.length < 2) {
          continue;
        }

        for (let e = 0; e < poly.length; e++) {
          const a = poly[e];
          const b = poly[(e + 1) % poly.length];
          if (pointsMatch(a, b)) {
            continue;
          }

          let matchedNeighbor: VoronoiCell | null = null;
          for (const nId of c.neighbors) {
            const nIdx = idToIndex.get(nId);
            if (nIdx === undefined) {
              continue;
            }
            const candidate = cells[nIdx];
            if (hasMatchingEdge(candidate.polygon, a, b)) {
              matchedNeighbor = candidate;
              break;
            }
          }

          const shouldEmit =
            matchedNeighbor !== null
              ? matchedNeighbor.ownerNumero !== emp.numero
              : c.isBoundary && isOnMapBounds(a, b, width, height);

          if (shouldEmit) {
            const key = canonicalEdgeKey(a, b);
            if (!seenEdges.has(key)) {
              seenEdges.add(key);
              borderSegments.push({
                a: { x: a.x, y: a.y },
                b: { x: b.x, y: b.y },
                dashed,
                color,
              });
            }
          }
        }
      }

      fiefs.push({
        numero: emp.numero,
        occupe: emp.occupe,
        ...(emp.royaume ? { royaume: { ...emp.royaume } } : {}),
        center: { x: cleanZero(emp.coordX), y: cleanZero(emp.coordY) },
        cellIds: ownedIds,
        borderSegments,
      });
    }

    return fiefs;
  }

  private computeTradeRoutes(
    cells: readonly VoronoiCell[],
    idToIndex: ReadonlyMap<number, number>,
    sortedEmps: readonly EmplacementCarteDto[],
    centreX: number,
    centreY: number,
    riverPaths: readonly (readonly Point2D[])[],
  ): TradeRoute[] {
    const goalIdx = findNearestCellIndex(cells, centreX, centreY);
    if (goalIdx < 0) {
      return [];
    }

    const goalCenter = cells[goalIdx].center;
    const routes: TradeRoute[] = [];

    for (const emp of sortedEmps) {
      const from: Point2D = { x: cleanZero(emp.coordX), y: cleanZero(emp.coordY) };
      const to: Point2D = { x: cleanZero(centreX), y: cleanZero(centreY) };
      const startIdx = findNearestCellIndex(cells, emp.coordX, emp.coordY);

      let routeIndices: number[];
      if (startIdx < 0) {
        routeIndices = [];
      } else if (startIdx === goalIdx) {
        routeIndices = [startIdx];
      } else {
        routeIndices = this.runAStar(cells, idToIndex, startIdx, goalIdx, goalCenter);
      }

      const cellIds = routeIndices.map((idx) => cells[idx].id);
      const rawPath: Point2D[] = [{ ...from }];
      if (routeIndices.length > 2) {
        for (let k = 1; k < routeIndices.length - 1; k++) {
          const c = cells[routeIndices[k]].center;
          rawPath.push({ x: c.x, y: c.y });
        }
      }
      if (
        Math.hypot(to.x - from.x, to.y - from.y) > 1e-6 ||
        rawPath.length > 1
      ) {
        rawPath.push({ ...to });
      }

      // Deduplicate consecutive identical points
      const path: Point2D[] = [];
      for (const pt of rawPath) {
        if (
          path.length === 0 ||
          Math.hypot(pt.x - path[path.length - 1].x, pt.y - path[path.length - 1].y) > 1e-6
        ) {
          path.push(pt);
        }
      }
      if (path.length === 1 && Math.hypot(to.x - from.x, to.y - from.y) > 1e-6) {
        path.push({ ...to });
      }

      let totalLength = 0;
      for (let i = 0; i < path.length - 1; i++) {
        totalLength += Math.hypot(path[i + 1].x - path[i].x, path[i + 1].y - path[i].y);
      }

      const bridges: BridgeSegment[] = [];
      const routeNodePoint = (idxPos: number): Point2D => {
        if (idxPos === 0) {
          return from;
        }
        if (idxPos === routeIndices.length - 1) {
          return to;
        }
        return cells[routeIndices[idxPos]].center;
      };

      // Mechanism 1: Contiguous polyline BridgeSegment spans along route.path for isWater runs
      let k = 0;
      while (k < routeIndices.length) {
        if (!cells[routeIndices[k]].isWater) {
          k++;
          continue;
        }
        const iStart = k;
        while (k + 1 < routeIndices.length && cells[routeIndices[k + 1]].isWater) {
          k++;
        }
        const iEnd = k;

        const firstWaterPt = routeNodePoint(iStart);
        const lastWaterPt = routeNodePoint(iEnd);
        const bStart =
          iStart > 0
            ? lerpPoint(routeNodePoint(iStart - 1), firstWaterPt, 0.35)
            : from;
        const bEnd =
          iEnd < routeIndices.length - 1
            ? lerpPoint(lastWaterPt, routeNodePoint(iEnd + 1), 0.65)
            : to;

        const rawSpanVerts: Point2D[] = [bStart];
        for (let w = iStart; w <= iEnd; w++) {
          const wp = routeNodePoint(w);
          rawSpanVerts.push({ x: cleanZero(wp.x), y: cleanZero(wp.y) });
        }
        rawSpanVerts.push(bEnd);

        const dedupedVerts: Point2D[] = [];
        for (const pt of rawSpanVerts) {
          if (
            dedupedVerts.length === 0 ||
            Math.hypot(
              pt.x - dedupedVerts[dedupedVerts.length - 1].x,
              pt.y - dedupedVerts[dedupedVerts.length - 1].y,
            ) > 1e-3
          ) {
            dedupedVerts.push(pt);
          }
        }

        const spanVerts: Point2D[] = [];
        if (dedupedVerts.length <= 2) {
          spanVerts.push(...dedupedVerts);
        } else {
          spanVerts.push(dedupedVerts[0]);
          let dropped: Point2D[] = [];
          for (let idx = 1; idx < dedupedVerts.length - 1; idx++) {
            const a = spanVerts[spanVerts.length - 1];
            const b = dedupedVerts[idx];
            const c = dedupedVerts[idx + 1];
            let canDrop = pointToSegmentDistance(b.x, b.y, a.x, a.y, c.x, c.y) < 0.5;
            if (canDrop && dropped.length > 0) {
              for (const dPt of dropped) {
                if (pointToSegmentDistance(dPt.x, dPt.y, a.x, a.y, c.x, c.y) >= 0.5) {
                  canDrop = false;
                  break;
                }
              }
            }
            if (canDrop) {
              dropped.push(b);
            } else {
              spanVerts.push(b);
              dropped = [];
            }
          }
          spanVerts.push(dedupedVerts[dedupedVerts.length - 1]);
        }

        for (let s = 0; s < spanVerts.length - 1; s++) {
          const bridge = makeBridgeSegment(spanVerts[s], spanVerts[s + 1]);
          if (bridge) {
            bridges.push(bridge);
          }
        }
        k++;
      }

      // Mechanism 2: 2D segment-vs-riverPaths intersection with exact crossing check & deduplication
      for (let i = 0; i < path.length - 1; i++) {
        const a = path[i];
        const b = path[i + 1];
        const segLen = Math.hypot(b.x - a.x, b.y - a.y);
        if (segLen <= 1e-6) {
          continue;
        }
        const ux = (b.x - a.x) / segLen;
        const uy = (b.y - a.y) / segLen;

        for (const rPath of riverPaths) {
          for (let r = 0; r < rPath.length - 1; r++) {
            const r0 = rPath[r];
            const r1 = rPath[r + 1];
            const hit = intersectSegments(a, b, r0, r1);
            if (!hit) {
              continue;
            }
            const covered = bridges.some((br) => {
              if (
                pointToSegmentDistance(
                  hit.x,
                  hit.y,
                  br.start.x,
                  br.start.y,
                  br.end.x,
                  br.end.y,
                ) <= 2.0
              ) {
                return true;
              }
              const brHit = intersectSegments(br.start, br.end, r0, r1);
              return (
                brHit !== null &&
                Math.hypot(brHit.x - hit.x, brHit.y - hit.y) <= 4.0
              );
            });
            if (!covered) {
              const bridge = makeBridgeSegment(
                { x: hit.x - ux * 16, y: hit.y - uy * 16 },
                { x: hit.x + ux * 16, y: hit.y + uy * 16 },
              );
              if (bridge) {
                bridges.push(bridge);
              }
            }
          }
        }
      }

      routes.push({
        emplacementNumero: emp.numero,
        from,
        to,
        path,
        cellIds,
        bridges,
        totalLength,
      });
    }

    return routes;
  }

  private runAStar(
    cells: readonly VoronoiCell[],
    idToIndex: ReadonlyMap<number, number>,
    startIdx: number,
    goalIdx: number,
    goalCenter: Point2D,
  ): number[] {
    const n = cells.length;
    const gScore = new Float64Array(n).fill(Number.POSITIVE_INFINITY);
    const parent = new Int32Array(n).fill(-1);
    const closed = new Uint8Array(n);

    const heap = new MinHeap<AStarHeapItem>(
      (a, b) =>
        a.f < b.f ||
        (a.f === b.f && (a.h < b.h || (a.h === b.h && a.id < b.id))),
    );

    const h0 = Math.hypot(
      goalCenter.x - cells[startIdx].center.x,
      goalCenter.y - cells[startIdx].center.y,
    );
    gScore[startIdx] = 0;
    heap.push({ idx: startIdx, f: h0, h: h0, id: cells[startIdx].id });

    let found = false;
    while (heap.size > 0) {
      const cur = heap.pop()!;
      if (closed[cur.idx]) {
        continue;
      }
      if (cur.idx === goalIdx) {
        found = true;
        break;
      }
      closed[cur.idx] = 1;

      const uCell = cells[cur.idx];
      for (const neighborId of uCell.neighbors) {
        const vIdx = idToIndex.get(neighborId);
        if (vIdx === undefined || closed[vIdx]) {
          continue;
        }
        const vCell = cells[vIdx];
        const edgeDist = Math.hypot(
          vCell.center.x - uCell.center.x,
          vCell.center.y - uCell.center.y,
        );
        const tentative = gScore[cur.idx] + edgeDist * getTraversalCostFactor(vCell);
        if (tentative < gScore[vIdx]) {
          gScore[vIdx] = tentative;
          parent[vIdx] = cur.idx;
          const hv = Math.hypot(goalCenter.x - vCell.center.x, goalCenter.y - vCell.center.y);
          heap.push({ idx: vIdx, f: tentative + hv, h: hv, id: vCell.id });
        }
      }
    }

    if (!found) {
      return [startIdx, goalIdx];
    }

    const indices: number[] = [];
    let trace = goalIdx;
    while (trace !== -1) {
      indices.push(trace);
      trace = parent[trace];
    }
    indices.reverse();
    return indices;
  }

  private populateFeatures25D(
    seedStr: string,
    archetype: ArchetypeCarte,
    width: number,
    height: number,
    centreX: number,
    centreY: number,
    cells: readonly VoronoiCell[],
    idToIndex: ReadonlyMap<number, number>,
    sortedEmps: readonly EmplacementCarteDto[],
    fiefs: readonly FiefDomain[],
    routes: readonly TradeRoute[],
  ): MapFeature25D[] {
    const featureRng = createSeededRng(seedStr, 'features');
    // Draw nature base seed upfront so distant natural features stay stable during Drag & Drop
    const natureBaseSeed = Math.floor(featureRng() * 0x100000000) >>> 0;

    const features: MapFeature25D[] = [];
    const pad = Math.min(6, width * 0.1, height * 0.1);
    const natPad = Math.min(8, width * 0.1, height * 0.1);

    const routeAabbs: SegmentAabb[] = [];
    for (const route of routes) {
      for (let i = 0; i < route.path.length - 1; i++) {
        const a = route.path[i];
        const b = route.path[i + 1];
        routeAabbs.push({
          ax: a.x,
          ay: a.y,
          bx: b.x,
          by: b.y,
          minX: Math.min(a.x, b.x) - ROUTE_FEATURE_EXCLUSION_RADIUS,
          maxX: Math.max(a.x, b.x) + ROUTE_FEATURE_EXCLUSION_RADIUS,
          minY: Math.min(a.y, b.y) - ROUTE_FEATURE_EXCLUSION_RADIUS,
          maxY: Math.max(a.y, b.y) + ROUTE_FEATURE_EXCLUSION_RADIUS,
        });
      }
    }

    const isClearOfRoutes = (px: number, py: number): boolean => {
      for (const seg of routeAabbs) {
        if (px < seg.minX || px > seg.maxX || py < seg.minY || py > seg.maxY) {
          continue;
        }
        if (
          pointToSegmentDistance(px, py, seg.ax, seg.ay, seg.bx, seg.by) <
          ROUTE_FEATURE_EXCLUSION_RADIUS
        ) {
          return false;
        }
      }
      return true;
    };

    // ------------------------------------------------------------------------
    // 1. Market Hall & Colored Stalls at (centreX, centreY)
    // ------------------------------------------------------------------------
    features.push({
      id: 'market-hall',
      kind: 'MARKET_HALL',
      x: cleanZero(centreX),
      y: cleanZero(centreY),
      scale: 1.15,
      primaryColor: '#8b5a2b',
      secondaryColor: '#d97706',
      ownerNumero: null,
      seedOffset: featureRng(),
    });

    const activeKingdoms = sortedEmps.filter((e) => e.occupe);
    const stallCount = Math.max(6, Math.min(10, activeKingdoms.length || 6));

    for (let i = 0; i < stallCount; i++) {
      const angle = (2 * Math.PI * i) / stallCount + (featureRng() - 0.5) * 0.14;
      const radius = 34 + featureRng() * 10;
      const sx = clampRange(centreX + Math.cos(angle) * radius, pad, width - pad);
      const sy = clampRange(centreY + Math.sin(angle) * radius, pad, height - pad);

      let primaryColor: string;
      let secondaryColor: string;
      let ownerNumero: number | null;
      if (activeKingdoms.length > 0) {
        const emp = activeKingdoms[i % activeKingdoms.length];
        const royaume = emp.royaume ?? createDefaultRoyaume(emp.numero);
        primaryColor = royaume.couleurPrimaire;
        secondaryColor = royaume.couleurSecondaire;
        ownerNumero = emp.numero;
      } else {
        const fallback = FALLBACK_STALL_PALETTES[i % FALLBACK_STALL_PALETTES.length];
        primaryColor = fallback.primary;
        secondaryColor = fallback.secondary;
        ownerNumero = null;
      }

      features.push({
        id: `market-stall-${i}`,
        kind: 'MARKET_STALL',
        x: sx,
        y: sy,
        scale: 0.9 + featureRng() * 0.15,
        rotation: angle,
        primaryColor,
        secondaryColor,
        ownerNumero,
        seedOffset: featureRng(),
      });
    }

    // ------------------------------------------------------------------------
    // 2. Kingdom Domains (Occupied Fortified vs Unoccupied Clearing)
    // ------------------------------------------------------------------------
    const fiefByNumero = new Map<number, FiefDomain>(fiefs.map((f) => [f.numero, f]));

    for (const emp of sortedEmps) {
      if (emp.occupe) {
        const palette = emp.royaume ?? createDefaultRoyaume(emp.numero);
        const rawNiveau = emp.royaume?.niveau ?? 1;
        const niveau = clampRange(Math.round(rawNiveau), 1, 10);

        features.push({
          id: `emp-${emp.numero}-keep`,
          kind: 'CASTLE_KEEP',
          x: cleanZero(emp.coordX),
          y: cleanZero(emp.coordY),
          scale: 0.95 + (niveau - 1) * 0.03,
          primaryColor: palette.couleurPrimaire,
          secondaryColor: palette.couleurSecondaire,
          ownerNumero: emp.numero,
          seedOffset: featureRng(),
        });

        const towerCount = niveau >= 5 ? 6 : 4;
        for (let t = 0; t < towerCount; t++) {
          const tAngle = (2 * Math.PI * t) / towerCount + Math.PI / 4;
          const tx = clampRange(emp.coordX + Math.cos(tAngle) * 28, pad, width - pad);
          const ty = clampRange(emp.coordY + Math.sin(tAngle) * 28, pad, height - pad);
          features.push({
            id: `emp-${emp.numero}-tower-${t}`,
            kind: 'RAMPART_TOWER',
            x: tx,
            y: ty,
            scale: 0.88 + (niveau - 1) * 0.015,
            primaryColor: palette.couleurPrimaire,
            secondaryColor: palette.couleurSecondaire,
            ownerNumero: emp.numero,
            seedOffset: featureRng(),
          });
        }

        const fief = fiefByNumero.get(emp.numero);
        const dryFiefCells = (fief?.cellIds ?? [])
          .map((cid) => {
            const idx = idToIndex.get(cid);
            return idx !== undefined ? cells[idx] : undefined;
          })
          .filter(
            (c): c is VoronoiCell =>
              c !== undefined && !c.isWater && !c.isMarketZone,
          )
          .sort((a, b) => {
            const da = Math.hypot(a.center.x - emp.coordX, a.center.y - emp.coordY);
            const db = Math.hypot(b.center.x - emp.coordX, b.center.y - emp.coordY);
            return da - db || a.id - b.id;
          });

        const placedEstatePoints: Point2D[] = [];
        const usedEstateCellIds = new Set<number>();

        const isClearOfEstate = (px: number, py: number, minGap: number): boolean => {
          for (const ep of placedEstatePoints) {
            if (Math.hypot(px - ep.x, py - ep.y) < minGap) {
              return false;
            }
          }
          return true;
        };

        // Guaranteed >= 1 WINDMILL per occupied kingdom
        let windmillCount = 0;
        const placeWindmillAt = (wx: number, wy: number): void => {
          features.push({
            id: `emp-${emp.numero}-windmill-${windmillCount}`,
            kind: 'WINDMILL',
            x: cleanZero(wx),
            y: cleanZero(wy),
            scale: 0.95 + featureRng() * 0.12,
            primaryColor: palette.couleurPrimaire,
            secondaryColor: palette.couleurSecondaire,
            ownerNumero: emp.numero,
            seedOffset: featureRng(),
          });
          placedEstatePoints.push({ x: wx, y: wy });
          windmillCount++;
        };

        const targetWindmills = dryFiefCells.length >= 14 ? 2 : 1;

        for (const c of dryFiefCells) {
          if (windmillCount >= targetWindmills) {
            break;
          }
          const dKeep = Math.hypot(c.center.x - emp.coordX, c.center.y - emp.coordY);
          const dMarket = Math.hypot(c.center.x - centreX, c.center.y - centreY);
          if (
            dKeep >= 40 &&
            dKeep <= 180 &&
            dMarket >= MARKET_TERRAFORM_RADIUS &&
            isClearOfRoutes(c.center.x, c.center.y) &&
            isClearOfEstate(c.center.x, c.center.y, 24)
          ) {
            usedEstateCellIds.add(c.id);
            placeWindmillAt(c.center.x, c.center.y);
          }
        }

        // Fallback 1: any dry fief cell >= 32px or furthest dry fief cell
        if (windmillCount === 0 && dryFiefCells.length > 0) {
          const candidate =
            dryFiefCells.find(
              (c) => Math.hypot(c.center.x - emp.coordX, c.center.y - emp.coordY) >= 32,
            ) ?? dryFiefCells[dryFiefCells.length - 1];
          usedEstateCellIds.add(candidate.id);
          placeWindmillAt(candidate.center.x, candidate.center.y);
        }

        // Fallback 2: empty cellIds (e.g., duplicate coordinates or dragged onto Market)
        if (windmillCount === 0) {
          const fallbackPad = Math.min(10, width * 0.1, height * 0.1);
          let chosenX: number | null = null;
          let chosenY: number | null = null;
          for (let a = 0; a < 8; a++) {
            const ang = (2 * Math.PI * a) / 8 + emp.numero * 0.37;
            const cxCand = clampRange(emp.coordX + Math.cos(ang) * 44, fallbackPad, width - fallbackPad);
            const cyCand = clampRange(emp.coordY + Math.sin(ang) * 44, fallbackPad, height - fallbackPad);
            const nIdx = findNearestCellIndex(cells, cxCand, cyCand);
            const nCell = nIdx >= 0 ? cells[nIdx] : undefined;
            if (nCell && !nCell.isWater && !nCell.isMarketZone) {
              chosenX = cxCand;
              chosenY = cyCand;
              break;
            }
          }
          if (chosenX === null || chosenY === null) {
            const ang = (emp.numero * 1.1) % (Math.PI * 2);
            chosenX = clampRange(emp.coordX + Math.cos(ang) * 44, fallbackPad, width - fallbackPad);
            chosenY = clampRange(emp.coordY + Math.sin(ang) * 44, fallbackPad, height - fallbackPad);
          }
          placeWindmillAt(chosenX, chosenY);
        }

        // HAMLET_HOUSEs
        const targetHamlets = Math.min(4, Math.max(0, Math.floor(dryFiefCells.length / 3)));
        let hamletCount = 0;
        for (const c of dryFiefCells) {
          if (hamletCount >= targetHamlets) {
            break;
          }
          if (usedEstateCellIds.has(c.id)) {
            continue;
          }
          const dKeep = Math.hypot(c.center.x - emp.coordX, c.center.y - emp.coordY);
          const dMarket = Math.hypot(c.center.x - centreX, c.center.y - centreY);
          if (
            dKeep >= 38 &&
            dMarket >= MARKET_TERRAFORM_RADIUS &&
            isClearOfRoutes(c.center.x, c.center.y) &&
            isClearOfEstate(c.center.x, c.center.y, 18)
          ) {
            usedEstateCellIds.add(c.id);
            placedEstatePoints.push({ x: c.center.x, y: c.center.y });
            features.push({
              id: `emp-${emp.numero}-hamlet-${hamletCount}`,
              kind: 'HAMLET_HOUSE',
              x: c.center.x,
              y: c.center.y,
              scale: 0.88 + featureRng() * 0.16,
              primaryColor: palette.couleurPrimaire,
              secondaryColor: palette.couleurSecondaire,
              ownerNumero: emp.numero,
              seedOffset: featureRng(),
            });
            hamletCount++;
          }
        }

        // CROP_FIELDs
        const targetCrops = Math.min(5, Math.max(0, Math.floor(dryFiefCells.length / 4)));
        let cropCount = 0;
        const sortedCropCells = [...dryFiefCells].sort((a, b) => {
          const prefA = a.biome === 'PRAIRIE' || a.biome === 'PLAGE' ? 0 : 1;
          const prefB = b.biome === 'PRAIRIE' || b.biome === 'PLAGE' ? 0 : 1;
          return prefA - prefB || a.id - b.id;
        });
        for (const c of sortedCropCells) {
          if (cropCount >= targetCrops) {
            break;
          }
          if (usedEstateCellIds.has(c.id) || c.elevation >= 0.65) {
            continue;
          }
          const dKeep = Math.hypot(c.center.x - emp.coordX, c.center.y - emp.coordY);
          const dMarket = Math.hypot(c.center.x - centreX, c.center.y - centreY);
          if (
            dKeep >= 42 &&
            dMarket >= MARKET_FREE_ZONE_RADIUS &&
            isClearOfRoutes(c.center.x, c.center.y) &&
            isClearOfEstate(c.center.x, c.center.y, 20)
          ) {
            usedEstateCellIds.add(c.id);
            placedEstatePoints.push({ x: c.center.x, y: c.center.y });
            features.push({
              id: `emp-${emp.numero}-crop-${cropCount}`,
              kind: 'CROP_FIELD',
              x: c.center.x,
              y: c.center.y,
              scale: 0.92 + featureRng() * 0.16,
              rotation: featureRng() * Math.PI,
              primaryColor: palette.couleurPrimaire,
              secondaryColor: palette.couleurSecondaire,
              ownerNumero: emp.numero,
              seedOffset: featureRng(),
            });
            cropCount++;
          }
        }
      } else {
        // Unoccupied clearing / foundation
        features.push({
          id: `emp-${emp.numero}-foundation`,
          kind: 'SITE_FOUNDATION',
          x: cleanZero(emp.coordX),
          y: cleanZero(emp.coordY),
          scale: 1.0,
          primaryColor: '#8c785c',
          secondaryColor: '#d6c7ae',
          ownerNumero: emp.numero,
          seedOffset: featureRng(),
        });

        const palisadeCount = 5;
        for (let p = 0; p < palisadeCount; p++) {
          const pAngle = (2 * Math.PI * p) / palisadeCount + 0.3;
          const px = clampRange(emp.coordX + Math.cos(pAngle) * 26, pad, width - pad);
          const py = clampRange(emp.coordY + Math.sin(pAngle) * 26, pad, height - pad);
          features.push({
            id: `emp-${emp.numero}-palisade-${p}`,
            kind: 'SITE_PALISADE',
            x: px,
            y: py,
            scale: 0.9 + featureRng() * 0.12,
            rotation: pAngle,
            primaryColor: '#7c5c3b',
            secondaryColor: '#a8845c',
            ownerNumero: emp.numero,
            seedOffset: featureRng(),
          });
        }
      }
    }

    // ------------------------------------------------------------------------
    // 3. Natural Biome Features with Strict Exclusion Masks
    // ------------------------------------------------------------------------
    const profile = getArchetypeNatureProfile(archetype);
    const orderedCells = [...cells].sort((a, b) => a.id - b.id);

    const passesNaturalExclusion = (
      fx: number,
      fy: number,
      cell: VoronoiCell,
    ): boolean => {
      if (cell.isWater) {
        return false;
      }
      if (
        fx < natPad ||
        fx > width - natPad ||
        fy < natPad ||
        fy > height - natPad
      ) {
        return false;
      }
      if (!isPointInConvexPolygon(fx, fy, cell.polygon)) {
        return false;
      }
      if (Math.hypot(fx - centreX, fy - centreY) < MARKET_TERRAFORM_RADIUS) {
        return false;
      }
      for (const emp of sortedEmps) {
        if (
          Math.hypot(fx - emp.coordX, fy - emp.coordY) <
          EMPLACEMENT_FEATURE_EXCLUSION_RADIUS
        ) {
          return false;
        }
      }
      if (!isClearOfRoutes(fx, fy)) {
        return false;
      }
      return true;
    };

    const pickPointInCell = (cell: VoronoiCell, rng: () => number): Point2D => {
      const ang = rng() * Math.PI * 2;
      const rad = 3 + rng() * 9;
      const candX = cell.center.x + Math.cos(ang) * rad;
      const candY = cell.center.y + Math.sin(ang) * rad;
      if (isPointInConvexPolygon(candX, candY, cell.polygon)) {
        return { x: cleanZero(candX), y: cleanZero(candY) };
      }
      return { x: cleanZero(cell.center.x), y: cleanZero(cell.center.y) };
    };

    for (const cell of orderedCells) {
      if (cell.isWater || cell.terraformed) {
        continue;
      }

      const cellSeed = (natureBaseSeed ^ Math.imul(cell.id + 1, 2654435761)) >>> 0;
      const cellRng = mulberry32(cellSeed);
      let natIdx = 0;

      if (cell.biome === 'FORET') {
        const count =
          profile.forestTreesBase + (cellRng() < profile.forestExtraProb ? 1 : 0);
        for (let k = 0; k < count; k++) {
          const pt = pickPointInCell(cell, cellRng);
          const isPine = cellRng() < profile.pineShare;
          const scale = 0.85 + cellRng() * 0.35;
          const seedOffset = cellRng();
          if (passesNaturalExclusion(pt.x, pt.y, cell)) {
            features.push({
              id: `nat-${cell.id}-${natIdx++}`,
              kind: isPine ? 'TREE_PINE' : 'TREE_OAK',
              x: pt.x,
              y: pt.y,
              scale,
              ownerNumero: cell.ownerNumero,
              seedOffset,
            });
          }
        }
      } else if (cell.biome === 'PRAIRIE') {
        if (cellRng() < profile.prairieTreeProb) {
          const pt = pickPointInCell(cell, cellRng);
          const isPine = cellRng() < profile.pineShare;
          const scale = 0.8 + cellRng() * 0.3;
          const seedOffset = cellRng();
          if (passesNaturalExclusion(pt.x, pt.y, cell)) {
            features.push({
              id: `nat-${cell.id}-${natIdx++}`,
              kind: isPine ? 'TREE_PINE' : 'TREE_OAK',
              x: pt.x,
              y: pt.y,
              scale,
              ownerNumero: cell.ownerNumero,
              seedOffset,
            });
          }
        }
      } else if (cell.biome === 'COLLINE') {
        if (cellRng() < profile.hillProb) {
          const pt = pickPointInCell(cell, cellRng);
          const scale = 0.9 + cellRng() * 0.35;
          const seedOffset = cellRng();
          if (passesNaturalExclusion(pt.x, pt.y, cell)) {
            features.push({
              id: `nat-${cell.id}-${natIdx++}`,
              kind: 'HILL_MOUND',
              x: pt.x,
              y: pt.y,
              scale,
              ownerNumero: cell.ownerNumero,
              seedOffset,
            });
          }
        }
        if ((archetype === 'HAUTES_TERRES' || cell.moisture > 0.55) && cellRng() < 0.22) {
          const pt = pickPointInCell(cell, cellRng);
          const scale = 0.82 + cellRng() * 0.25;
          const seedOffset = cellRng();
          if (passesNaturalExclusion(pt.x, pt.y, cell)) {
            features.push({
              id: `nat-${cell.id}-${natIdx++}`,
              kind: 'TREE_PINE',
              x: pt.x,
              y: pt.y,
              scale,
              ownerNumero: cell.ownerNumero,
              seedOffset,
            });
          }
        }
      } else if (cell.biome === 'MONTAGNE') {
        if (cellRng() < profile.ridgeProb) {
          const pt = pickPointInCell(cell, cellRng);
          const scale = 1.0 + cellRng() * 0.4;
          const seedOffset = cellRng();
          if (passesNaturalExclusion(pt.x, pt.y, cell)) {
            features.push({
              id: `nat-${cell.id}-${natIdx++}`,
              kind: 'RIDGE_CREST',
              x: pt.x,
              y: pt.y,
              scale,
              ownerNumero: cell.ownerNumero,
              seedOffset,
            });
          }
        }
      }
    }

    features.sort((a, b) => a.y - b.y || (a.id < b.id ? -1 : a.id > b.id ? 1 : 0));
    return features;
  }
}
