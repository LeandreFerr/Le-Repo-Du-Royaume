import {
  ArchetypeCarte,
  CarteDto,
  DEFAULT_CARTE_CENTRE_X,
  DEFAULT_CARTE_CENTRE_Y,
  DEFAULT_CARTE_HEIGHT,
  DEFAULT_CARTE_WIDTH,
  EmplacementCarteDto,
  InspectionDistanceEntry,
  InspectionInfo,
  MAX_EMPLACEMENTS,
  MIN_EMPLACEMENTS,
  RoyaumeResume,
} from '../models/carte.model';
import { createSeededRng } from './prng.util';

export { MIN_EMPLACEMENTS, MAX_EMPLACEMENTS };

export const R_BASE = 340;
export const MAX_THETA_JITTER_FACTOR = 0.22;
export const MAX_RADIUS_JITTER = 35;

export interface CircularPlacementOptions {
  largeur?: number;
  hauteur?: number;
  centreX?: number;
  centreY?: number;
}

export const DEFAULT_HERALDIC_ROYAUMES: readonly RoyaumeResume[] = [
  {
    nom: 'Valdore',
    couleurPrimaire: '#b91c1c',
    couleurSecondaire: '#fef3c7',
    niveau: 1,
  },
  {
    nom: 'Azurmont',
    couleurPrimaire: '#1d4ed8',
    couleurSecondaire: '#fde68a',
    niveau: 1,
  },
  {
    nom: 'Sylveclair',
    couleurPrimaire: '#15803d',
    couleurSecondaire: '#ecfccb',
    niveau: 1,
  },
  {
    nom: 'Soleilroc',
    couleurPrimaire: '#d97706',
    couleurSecondaire: '#fffbeb',
    niveau: 1,
  },
  {
    nom: 'Brumelac',
    couleurPrimaire: '#7e22ce',
    couleurSecondaire: '#f3e8ff',
    niveau: 1,
  },
  {
    nom: 'Noiregarde',
    couleurPrimaire: '#334155',
    couleurSecondaire: '#f8fafc',
    niveau: 1,
  },
  {
    nom: 'Aigremont',
    couleurPrimaire: '#0f766e',
    couleurSecondaire: '#ccfbf1',
    niveau: 1,
  },
  {
    nom: 'Braisebourg',
    couleurPrimaire: '#c2410c',
    couleurSecondaire: '#ffedd5',
    niveau: 1,
  },
  {
    nom: 'Roseroc',
    couleurPrimaire: '#be185d',
    couleurSecondaire: '#fce7f3',
    niveau: 1,
  },
  {
    nom: 'Aubeclair',
    couleurPrimaire: '#0369a1',
    couleurSecondaire: '#e0f2fe',
    niveau: 1,
  },
] as const;

export function createDefaultRoyaume(numero: number): RoyaumeResume {
  const len = DEFAULT_HERALDIC_ROYAUMES.length;
  const safeNumero = Number.isFinite(numero) ? Math.trunc(numero) : 1;
  const index = (((safeNumero - 1) % len) + len) % len;
  return { ...DEFAULT_HERALDIC_ROYAUMES[index] };
}

function clampCoord(value: number, max: number): number {
  const safeMax = Number.isFinite(max) && max > 0 ? max : 0;
  const rounded = Math.round(value);
  const clamped = Math.max(0, Math.min(safeMax, rounded));
  return clamped === 0 ? 0 : clamped;
}

/**
 * Generates `count` (`1..10`) circular noisy kingdom placements around `(centreX, centreY)`.
 * Preserves existing occupancy/kingdom metadata matched by `numero`.
 */
export function generateCircularPlacements(
  count: number,
  seed: string | number,
  existing?: EmplacementCarteDto[],
  options?: CircularPlacementOptions,
): EmplacementCarteDto[] {
  if (!Number.isInteger(count) || count < MIN_EMPLACEMENTS || count > MAX_EMPLACEMENTS) {
    throw new RangeError(
      `count must be an integer between ${MIN_EMPLACEMENTS} and ${MAX_EMPLACEMENTS}, received ${count}.`,
    );
  }

  const largeur = options?.largeur ?? DEFAULT_CARTE_WIDTH;
  const hauteur = options?.hauteur ?? DEFAULT_CARTE_HEIGHT;
  const centreX = options?.centreX ?? DEFAULT_CARTE_CENTRE_X;
  const centreY = options?.centreY ?? DEFAULT_CARTE_CENTRE_Y;

  if (
    !Number.isFinite(largeur) ||
    largeur <= 0 ||
    !Number.isFinite(hauteur) ||
    hauteur <= 0 ||
    !Number.isFinite(centreX) ||
    !Number.isFinite(centreY)
  ) {
    throw new RangeError('Invalid carte dimensions or center coordinates.');
  }

  const rng = createSeededRng(seed, 'placement');
  const theta0 = rng() * 2 * Math.PI;
  const maxDeltaTheta = (MAX_THETA_JITTER_FACTOR * Math.PI) / count;

  const result: EmplacementCarteDto[] = [];

  for (let i = 0; i < count; i++) {
    const numero = i + 1;
    const deltaTheta = (rng() * 2 - 1) * maxDeltaTheta;
    const theta = (2 * Math.PI * i) / count + theta0 + deltaTheta;
    const deltaR = (rng() * 2 - 1) * MAX_RADIUS_JITTER;
    const r = R_BASE + deltaR;

    const coordX = clampCoord(centreX + r * Math.cos(theta), largeur);
    const coordY = clampCoord(centreY + r * Math.sin(theta), hauteur);

    const byNumero = existing?.find((e) => e.numero === numero);
    const candidateAtIndex = existing?.[i];
    const fallbackByIdx =
      candidateAtIndex && (candidateAtIndex as Partial<EmplacementCarteDto>).numero === undefined
        ? candidateAtIndex
        : undefined;
    const prev = byNumero ?? fallbackByIdx;

    const occupe = prev ? Boolean(prev.occupe) : false;
    const royaume = prev?.royaume
      ? { ...prev.royaume }
      : occupe
        ? createDefaultRoyaume(numero)
        : undefined;

    result.push({
      ...(prev?.id !== undefined ? { id: prev.id } : {}),
      numero,
      coordX,
      coordY,
      occupe,
      ...(royaume ? { royaume } : {}),
    });
  }

  return result;
}

/**
 * Returns an immutable copy of `carte` with the specified placement's coordinates updated,
 * rounded to integers and clamped to `[0, largeur]` x `[0, hauteur]`.
 */
export function updatePlacementCoords(
  carte: CarteDto,
  numero: number,
  x: number,
  y: number,
): CarteDto {
  const emplacements = carte.emplacements.map((emp) => {
    const royaumeCopy = emp.royaume ? { ...emp.royaume } : undefined;
    if (emp.numero !== numero) {
      return {
        ...emp,
        ...(royaumeCopy ? { royaume: royaumeCopy } : {}),
      };
    }

    const nextX = Number.isFinite(x)
      ? clampCoord(x, carte.largeur)
      : clampCoord(emp.coordX, carte.largeur);
    const nextY = Number.isFinite(y)
      ? clampCoord(y, carte.hauteur)
      : clampCoord(emp.coordY, carte.hauteur);

    return {
      ...emp,
      coordX: nextX,
      coordY: nextY,
      ...(royaumeCopy ? { royaume: royaumeCopy } : {}),
    };
  });

  return {
    ...carte,
    emplacements,
  };
}

/**
 * Returns an immutable copy of `carte` with the specified placement's `occupe` flag updated.
 * Preserves any existing `royaume` metadata, and assigns a default heraldic kingdom when
 * transitioning to `occupe = true` without an existing `royaume`.
 */
export function togglePlacementOccupe(
  carte: CarteDto,
  numero: number,
  occupe?: boolean,
): CarteDto {
  const emplacements = carte.emplacements.map((emp) => {
    if (emp.numero !== numero) {
      return {
        ...emp,
        ...(emp.royaume ? { royaume: { ...emp.royaume } } : {}),
      };
    }

    const nextOccupe = occupe !== undefined ? Boolean(occupe) : !emp.occupe;
    const nextRoyaume = emp.royaume
      ? { ...emp.royaume }
      : nextOccupe
        ? createDefaultRoyaume(emp.numero)
        : undefined;

    return {
      ...(emp.id !== undefined ? { id: emp.id } : {}),
      numero: emp.numero,
      coordX: emp.coordX,
      coordY: emp.coordY,
      occupe: nextOccupe,
      ...(nextRoyaume ? { royaume: nextRoyaume } : {}),
    };
  });

  return {
    ...carte,
    emplacements,
  };
}

/**
 * Computes Euclidean distance between two 2D points, returning `0` on non-finite inputs.
 */
export function euclideanDistance(ax: number, ay: number, bx: number, by: number): number {
  if (
    !Number.isFinite(ax) ||
    !Number.isFinite(ay) ||
    !Number.isFinite(bx) ||
    !Number.isFinite(by)
  ) {
    return 0;
  }
  return Math.hypot(bx - ax, by - ay);
}

/**
 * Alias français de `generateCircularPlacements` pour l'interface Bac à sable.
 */
export function genererEmplacementsCercle(
  count: number,
  seed: string | number = 'royaume-1',
  existing?: EmplacementCarteDto[],
  options?: CircularPlacementOptions,
): EmplacementCarteDto[] {
  return generateCircularPlacements(count, seed, existing, options);
}

/**
 * Calcule la distance euclidienne exacte entre un point/emplacement et le Marché central.
 */
export function calculerDistanceMarche(
  point: { coordX: number; coordY: number },
  centreX = DEFAULT_CARTE_CENTRE_X,
  centreY = DEFAULT_CARTE_CENTRE_Y,
): number {
  return euclideanDistance(point.coordX, point.coordY, centreX, centreY);
}

/**
 * Calcule la liste triée des distances entre une cible (emplacement ou marché) et tous les autres royaumes.
 */
export function calculerDistancesRoyaumes(
  source: { coordX: number; coordY: number; numero?: number },
  emplacements: readonly EmplacementCarteDto[],
): InspectionDistanceEntry[] {
  if (!Array.isArray(emplacements)) {
    return [];
  }
  return emplacements
    .filter((emp) => source.numero === undefined || emp.numero !== source.numero)
    .map((emp) => {
      const royaume = emp.royaume ?? createDefaultRoyaume(emp.numero);
      return {
        numero: emp.numero,
        nom: emp.occupe ? royaume.nom : `Emplacement #${emp.numero} (Libre)`,
        occupe: emp.occupe,
        couleurPrimaire: emp.occupe ? royaume.couleurPrimaire : '#94a3b8',
        distance: Math.round(euclideanDistance(source.coordX, source.coordY, emp.coordX, emp.coordY)),
      };
    })
    .sort((a, b) => a.distance - b.distance || a.numero - b.numero);
}

/**
 * Construit la fiche d'inspection complète (`InspectionInfo`) pour un emplacement ou le Marché central.
 */
export function buildInspectionInfo(
  target: { type: 'MARCHE' } | { type: 'EMPLACEMENT'; numero: number } | null | undefined,
  carte: CarteDto,
): InspectionInfo | null {
  if (!target || !carte) {
    return null;
  }

  const cx = Number.isFinite(carte.centreX) ? carte.centreX : DEFAULT_CARTE_CENTRE_X;
  const cy = Number.isFinite(carte.centreY) ? carte.centreY : DEFAULT_CARTE_CENTRE_Y;

  if (target.type === 'MARCHE') {
    return {
      targetType: 'MARCHE',
      label: 'Marché Central',
      coordX: cx,
      coordY: cy,
      distanceMarche: 0,
      distancesRoyaumes: calculerDistancesRoyaumes({ coordX: cx, coordY: cy }, carte.emplacements),
    };
  }

  const emp = (carte.emplacements ?? []).find((e) => e.numero === target.numero);
  if (!emp) {
    return null;
  }

  const royaume = emp.royaume ?? createDefaultRoyaume(emp.numero);
  return {
    targetType: 'EMPLACEMENT',
    numero: emp.numero,
    label: emp.occupe ? `#${emp.numero} · ${royaume.nom}` : `#${emp.numero} · Libre`,
    coordX: emp.coordX,
    coordY: emp.coordY,
    distanceMarche: Math.round(calculerDistanceMarche(emp, cx, cy)),
    distancesRoyaumes: calculerDistancesRoyaumes(emp, carte.emplacements),
  };
}

/**
 * Génère le snippet Java (`Carte.java` / `EmplacementCarte.java`) correspondant à la configuration actuelle.
 */
export function genererSnippetJavaCarte(
  carte: CarteDto,
  archetype: ArchetypeCarte = 'VALLEE_ROYALE',
  seed: string | number = 'royaume-1',
): string {
  const largeur = Number.isFinite(carte?.largeur) ? Math.round(carte.largeur) : DEFAULT_CARTE_WIDTH;
  const hauteur = Number.isFinite(carte?.hauteur) ? Math.round(carte.hauteur) : DEFAULT_CARTE_HEIGHT;
  const centreX = Number.isFinite(carte?.centreX) ? Math.round(carte.centreX) : DEFAULT_CARTE_CENTRE_X;
  const centreY = Number.isFinite(carte?.centreY) ? Math.round(carte.centreY) : DEFAULT_CARTE_CENTRE_Y;
  const emplacements = [...(carte?.emplacements ?? [])].sort((a, b) => a.numero - b.numero);

  const lines: string[] = [
    `// Configuration générée depuis le Bac à sable Royaume Front`,
    `// Seed : "${String(seed)}" | Archétype : ${archetype} | Emplacements : ${emplacements.length}`,
    `Carte carte = new Carte(${largeur}, ${hauteur}, ${centreX}, ${centreY});`,
    ``,
  ];

  for (const emp of emplacements) {
    const royaume = emp.royaume ?? createDefaultRoyaume(emp.numero);
    const comment = emp.occupe ? `// Royaume : ${royaume.nom}` : `// Emplacement libre`;
    lines.push(
      `EmplacementCarte emp${emp.numero} = new EmplacementCarte(${emp.numero}, ${Math.round(emp.coordX)}, ${Math.round(emp.coordY)}, ${emp.occupe}); ${comment}`,
    );
    lines.push(`carte.ajouterEmplacement(emp${emp.numero});`);
  }

  return lines.join('\n');
}
