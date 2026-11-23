// ============================================================================
// Constants & Defaults (Reconciling Java Carte.java 100x100 with R2 1000x1000)
// ============================================================================

export const DEFAULT_CARTE_WIDTH = 1000;
export const DEFAULT_CARTE_HEIGHT = 1000;
export const DEFAULT_CARTE_CENTRE_X = 500;
export const DEFAULT_CARTE_CENTRE_Y = 500;

export const MIN_EMPLACEMENTS = 1;
export const MAX_EMPLACEMENTS = 10;

export const MIN_ROYAUME_NIVEAU = 1;
export const MAX_ROYAUME_NIVEAU = 10;

export const MARKET_TERRAFORM_RADIUS = 75;
export const EMPLACEMENT_TERRAFORM_RADIUS = 85;
export const MARKET_FREE_ZONE_RADIUS = 110;

// ============================================================================
// Archetypes (R3 Step 2)
// ============================================================================

export type ArchetypeCarte =
  | 'VALLEE_ROYALE'
  | 'FORET_NOIRE'
  | 'HAUTES_TERRES'
  | 'CONFLUENCES'
  | 'LAC_CENTRAL';

export interface ArchetypeCarteMetadata {
  id: ArchetypeCarte;
  label: string;
  description: string;
}

export const ARCHETYPES_CARTE: readonly ArchetypeCarteMetadata[] = [
  {
    id: 'VALLEE_ROYALE',
    label: 'Vallée Royale',
    description: 'Vallée fertile bordée de collines douces et traversée de rivières paisibles.',
  },
  {
    id: 'FORET_NOIRE',
    label: 'Forêt Noire',
    description: 'Bois profonds et brumeux ponctués de clairières et de ruisseaux sinueux.',
  },
  {
    id: 'HAUTES_TERRES',
    label: 'Hautes Terres',
    description: 'Crêtes escarpées, cols rocheux, bosquets de conifères et lacs d’altitude.',
  },
  {
    id: 'CONFLUENCES',
    label: 'Confluences',
    description: 'Rivières multiples convergeant autour du bassin central, propice aux ponts.',
  },
  {
    id: 'LAC_CENTRAL',
    label: 'Lac Central',
    description: 'Grand lac annulaire entourant l’îlot du Marché central et ses chaussées.',
  },
] as const;

// ============================================================================
// Backend Mirror DTOs (Verified against origin/Module8-Carte_Emplacements)
// ============================================================================

/**
 * Mirrors Java `royaume.model.Royaume` summary fields (`id`, `nom`, `niveau`)
 * plus frontend-only heraldic palette colors (`couleurPrimaire`, `couleurSecondaire`).
 */
export interface RoyaumeResume {
  id?: number; // positive integer when present (> 0 in Royaume.checkId)
  nom: string; // non-blank string (Royaume.checkNom)
  couleurPrimaire: string; // frontend-only CSS hex color (e.g. '#b91c1c')
  couleurSecondaire: string; // frontend-only CSS hex color (e.g. '#fbbf24')
  niveau?: number; // integer in [1, 10], default 1 (Royaume.checkNiveau)
}

/**
 * Mirrors Java `royaume.model.EmplacementCarte` (`id`, `numero`, `coordX`, `coordY`, `occupe`, `royaume`).
 * Omits `carte` back-reference to prevent circular JSON serialization.
 */
export interface EmplacementCarteDto {
  id?: number; // nullable Integer in Java
  numero: number; // 1..10
  coordX: number; // integer in [0, carte.largeur]
  coordY: number; // integer in [0, carte.hauteur]
  occupe: boolean; // default false
  royaume?: RoyaumeResume; // optional; may be omitted even when occupe=true
}

/**
 * Mirrors Java `royaume.model.Carte` (`largeur`, `hauteur`, `centreX`, `centreY`, `emplacements`).
 * Omits `partie` back-reference; frontend defaults use 1000x1000 / (500, 500) per R2.
 */
export interface CarteDto {
  largeur: number; // > 0, frontend default 1000 (Java default 100)
  hauteur: number; // > 0, frontend default 1000 (Java default 100)
  centreX: number; // frontend default 500 (Java default 50)
  centreY: number; // frontend default 500 (Java default 50)
  emplacements: EmplacementCarteDto[];
}

export function createDefaultCarte(emplacements: EmplacementCarteDto[] = []): CarteDto {
  return {
    largeur: DEFAULT_CARTE_WIDTH,
    hauteur: DEFAULT_CARTE_HEIGHT,
    centreX: DEFAULT_CARTE_CENTRE_X,
    centreY: DEFAULT_CARTE_CENTRE_Y,
    emplacements: [...emplacements],
  };
}

// ============================================================================
// Procedural Map & Domain Types (M2 / M3 / M4)
// ============================================================================

export interface Point2D {
  x: number;
  y: number;
}

export type BiomeType =
  | 'EAU_PROFONDE'
  | 'LAC'
  | 'RIVIERE'
  | 'PLAGE'
  | 'PRAIRIE'
  | 'FORET'
  | 'COLLINE'
  | 'MONTAGNE'
  | 'PAVE_MARCHE';

export interface VoronoiCell {
  id: number;
  center: Point2D;
  polygon: Point2D[]; // unclosed ring of vertices
  neighbors: number[]; // adjacent cell IDs
  elevation: number; // normalized [0, 1]
  moisture: number; // normalized [0, 1]
  isWater: boolean;
  waterKind?: 'LAKE' | 'RIVER';
  biome: BiomeType;
  isBoundary: boolean;
  terraformed: boolean;
  isMarketZone: boolean; // true when d(center, (centreX, centreY)) < 110
  ownerNumero: number | null; // null if isWater or isMarketZone
  geodesicDistance: number; // Infinity before Step 4 or if unreachable
}

export interface BorderSegment {
  a: Point2D;
  b: Point2D;
  dashed: boolean; // false when occupe=true, true when occupe=false
  color: string;
}

export interface FiefDomain {
  numero: number;
  occupe: boolean;
  royaume?: RoyaumeResume;
  center: Point2D;
  cellIds: number[];
  borderSegments: BorderSegment[];
}

export interface BridgeSegment {
  start: Point2D;
  end: Point2D;
  midpoint: Point2D;
  angle: number;
  length: number;
}

export interface TradeRoute {
  emplacementNumero: number;
  from: Point2D;
  to: Point2D;
  path: Point2D[];
  cellIds: number[];
  bridges: BridgeSegment[];
  totalLength: number;
}

export type FeatureKind =
  | 'TREE_OAK'
  | 'TREE_PINE'
  | 'HILL_MOUND'
  | 'RIDGE_CREST'
  | 'MARKET_HALL'
  | 'MARKET_STALL'
  | 'CASTLE_KEEP'
  | 'RAMPART_TOWER'
  | 'HAMLET_HOUSE'
  | 'WINDMILL'
  | 'CROP_FIELD'
  | 'SITE_PALISADE'
  | 'SITE_FOUNDATION';

export interface MapFeature25D {
  id: string;
  kind: FeatureKind;
  x: number;
  y: number; // primary Y-depth sort key in R4
  scale: number;
  rotation?: number;
  primaryColor?: string;
  secondaryColor?: string;
  ownerNumero?: number | null;
  seedOffset: number; // deterministic animation/variation phase
}

/**
 * Phase A (Steps 1–2) cached base terrain snapshot.
 * MUST be treated as immutable by `populateMapDomains(base, carte)` (Steps 3–6),
 * which clones `cells` before applying terraformation, fiefs, routes, and features.
 */
export interface BaseTerrainMesh {
  seed: string;
  archetype: ArchetypeCarte;
  width: number;
  height: number;
  cells: VoronoiCell[];
  riverPaths: Point2D[][];
}

export interface GeneratedMap {
  seed: string;
  archetype: ArchetypeCarte;
  width: number;
  height: number;
  centreX: number;
  centreY: number;
  marketRadius: number; // 75 (terraformed Market plaza)
  marketFreeZoneRadius: number; // 110 (Step 4 Market free zone)
  cells: VoronoiCell[];
  fiefs: FiefDomain[];
  routes: TradeRoute[];
  features: MapFeature25D[];
  riverPaths: Point2D[][];
}

// ============================================================================
// Inspection Contracts (M4 <app-carte-royaume> & Sandbox)
// ============================================================================

export interface InspectionDistanceEntry {
  numero: number;
  nom: string;
  occupe: boolean;
  couleurPrimaire?: string;
  distance: number;
}

export interface InspectionInfo {
  targetType: 'MARCHE' | 'EMPLACEMENT';
  numero?: number;
  label: string;
  coordX: number;
  coordY: number;
  distanceMarche: number;
  distancesRoyaumes: InspectionDistanceEntry[];
}
