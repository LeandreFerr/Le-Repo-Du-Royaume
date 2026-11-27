import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CarteResponse } from '../../models/carte-response';
import {
  ARCHETYPES_CARTE,
  ArchetypeCarte,
  CarteDto,
  DEFAULT_CARTE_CENTRE_X,
  DEFAULT_CARTE_CENTRE_Y,
  DEFAULT_CARTE_HEIGHT,
  DEFAULT_CARTE_WIDTH,
  EmplacementCarteDto,
  RoyaumeResume,
} from '../../models/carte.model';
import { Royaume } from '../../models/royaume.model';
import { RoyaumeResponse } from '../../models/royaume-response';
import { createDefaultRoyaume } from '../placement-royaumes.util';

export interface CarteAdapteeResultat {
  carte: CarteDto;
  archetype: ArchetypeCarte;
  seed: string;
}

const VALID_ARCHETYPES = new Set<string>(ARCHETYPES_CARTE.map((a) => a.id));

const PALETTE_PAR_PEUPLE: Record<
  string,
  { couleurPrimaire: string; couleurSecondaire: string }
> = {
  canards: { couleurPrimaire: '#eab308', couleurSecondaire: '#1d4ed8' },
  gobelins: { couleurPrimaire: '#15803d', couleurSecondaire: '#f59e0b' },
  licornes: { couleurPrimaire: '#9333ea', couleurSecondaire: '#fbcfe8' },
  princesses: { couleurPrimaire: '#e11d48', couleurSecondaire: '#fde047' },
  teuteus: { couleurPrimaire: '#92400e', couleurSecondaire: '#d97706' },
};

export function extraireNomPeuple(
  peuple: Royaume['peuple'] | RoyaumeResponse['peuple'] | undefined | null,
): string | undefined {
  if (!peuple) {
    return undefined;
  }
  if (typeof peuple === 'string') {
    return peuple;
  }
  return peuple.nom;
}

export function getPalettePourPeuple(
  peupleNom: string | undefined,
  numeroFallback: number,
): { couleurPrimaire: string; couleurSecondaire: string } {
  if (peupleNom) {
    const key = peupleNom.trim().toLowerCase();
    for (const [prefix, palette] of Object.entries(PALETTE_PAR_PEUPLE)) {
      if (key.includes(prefix) || prefix.includes(key)) {
        return palette;
      }
    }
  }
  const fallback = createDefaultRoyaume(numeroFallback);
  return {
    couleurPrimaire: fallback.couleurPrimaire,
    couleurSecondaire: fallback.couleurSecondaire,
  };
}

export function normalizeArchetype(raw?: string | null): ArchetypeCarte {
  if (raw && VALID_ARCHETYPES.has(raw.trim())) {
    return raw.trim() as ArchetypeCarte;
  }
  return 'VALLEE_ROYALE';
}

export function adapterCarteBackVersFront(
  carteBack: CarteResponse,
  royaumes: readonly (Royaume | RoyaumeResponse)[] = [],
  monRoyaumeId?: number | null,
): CarteAdapteeResultat {
  const sourceW = carteBack.largeur > 0 ? carteBack.largeur : 100;
  const sourceH = carteBack.hauteur > 0 ? carteBack.hauteur : 100;
  const scaleX = DEFAULT_CARTE_WIDTH / sourceW;
  const scaleY = DEFAULT_CARTE_HEIGHT / sourceH;

  const royaumesParId = new Map<number, Royaume | RoyaumeResponse>();
  for (const r of royaumes) {
    if (r?.id != null) {
      royaumesParId.set(r.id, r);
    }
  }

  const emplacements: EmplacementCarteDto[] = [...(carteBack.emplacements ?? [])]
    .sort((a, b) => a.numero - b.numero)
    .map((emp) => {
      const coordX = Math.max(
        0,
        Math.min(DEFAULT_CARTE_WIDTH, Math.round(emp.coordX * scaleX)),
      );
      const coordY = Math.max(
        0,
        Math.min(DEFAULT_CARTE_HEIGHT, Math.round(emp.coordY * scaleY)),
      );

      let royaumeResume: RoyaumeResume | undefined;
      if (emp.occupe && emp.royaumeId != null) {
        const info = royaumesParId.get(emp.royaumeId);
        const peupleNom = extraireNomPeuple(info?.peuple);
        const palette = getPalettePourPeuple(peupleNom, emp.numero);
        const estMonRoyaume = monRoyaumeId != null && emp.royaumeId === monRoyaumeId;
        const nomBase = info?.nom ?? emp.name ?? `Royaume #${emp.numero}`;

        royaumeResume = {
          id: emp.royaumeId,
          nom: estMonRoyaume ? `★ ${nomBase}` : nomBase,
          couleurPrimaire: estMonRoyaume ? '#fbbf24' : palette.couleurPrimaire,
          couleurSecondaire: estMonRoyaume ? palette.couleurPrimaire : palette.couleurSecondaire,
          niveau: info?.niveau ?? 1,
          peupleNom,
          pseudoJoueur: info?.pseudoJoueur,
          utilisateurId: info?.utilisateurId ?? null,
          estMonRoyaume,
          enRuine: info?.enRuine ?? false,
        };
      }

      return {
        id: emp.id ?? undefined,
        numero: emp.numero,
        coordX,
        coordY,
        occupe: emp.occupe,
        royaume: royaumeResume,
      };
    });

  const centreX = Number.isFinite(carteBack.centreX)
    ? Math.round(carteBack.centreX * scaleX)
    : DEFAULT_CARTE_CENTRE_X;
  const centreY = Number.isFinite(carteBack.centreY)
    ? Math.round(carteBack.centreY * scaleY)
    : DEFAULT_CARTE_CENTRE_Y;

  const carte: CarteDto = {
    largeur: DEFAULT_CARTE_WIDTH,
    hauteur: DEFAULT_CARTE_HEIGHT,
    centreX,
    centreY,
    emplacements,
  };

  const archetype = normalizeArchetype(carteBack.archetype);
  const seed =
    carteBack.seed && carteBack.seed.trim().length > 0
      ? carteBack.seed.trim()
      : `partie-${carteBack.partieId ?? carteBack.id ?? 1}`;

  return { carte, archetype, seed };
}

@Injectable({
  providedIn: 'root',
})
export class CarteApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/cartes';

  trouverParPartieId(partieId: number): Observable<CarteResponse> {
    return this.http.get<CarteResponse>(`${this.apiUrl}/partie/${partieId}`);
  }
}
