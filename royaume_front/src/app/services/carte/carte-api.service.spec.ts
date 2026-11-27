import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { CarteResponse } from '../../models/carte-response';
import { RoyaumeResponse } from '../../models/royaume-response';
import {
  CarteApiService,
  adapterCarteBackVersFront,
  normalizeArchetype,
} from './carte-api.service';

describe('CarteApiService', () => {
  let service: CarteApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CarteApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('fetches carte by partieId via /api/cartes/partie/{partieId}', () => {
    const mockResponse: CarteResponse = {
      id: 7,
      largeur: 100,
      hauteur: 100,
      centreX: 50,
      centreY: 50,
      partieId: 3,
      archetype: 'LAC_CENTRAL',
      seed: 'lac-123',
      emplacements: [],
      emplacementMarche: {
        id: null,
        numero: 0,
        coordX: 50,
        coordY: 50,
        occupe: false,
        carteId: 7,
        royaumeId: null,
        name: null,
      },
    };

    let result: CarteResponse | undefined;
    service.trouverParPartieId(3).subscribe((res) => (result = res));

    const req = httpMock.expectOne('/api/cartes/partie/3');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);

    expect(result).toEqual(mockResponse);
  });

  it('scales 100x100 backend coordinates to 1000x1000 and enriches kingdoms with people & owner flags', () => {
    const carteBack: CarteResponse = {
      id: 1,
      largeur: 100,
      hauteur: 100,
      centreX: 50,
      centreY: 50,
      partieId: 9,
      archetype: 'FORET_NOIRE',
      seed: 'foret-999',
      emplacements: [
        {
          id: 11,
          numero: 1,
          coordX: 50,
          coordY: 10,
          occupe: true,
          carteId: 1,
          royaumeId: 101,
          name: 'Coin-Land',
        },
        {
          id: 12,
          numero: 2,
          coordX: 90,
          coordY: 50,
          occupe: false,
          carteId: 1,
          royaumeId: null,
          name: null,
        },
      ],
      emplacementMarche: {
        id: null,
        numero: 0,
        coordX: 50,
        coordY: 50,
        occupe: false,
        carteId: 1,
        royaumeId: null,
        name: null,
      },
    };

    const royaumes: RoyaumeResponse[] = [
      {
        id: 101,
        nom: 'Coin-Land',
        peuple: {
          id: 1,
          nom: 'Canards',
          titreSouverain: 'Grand Canard',
          attraction: 1.5,
          capaciteSpeciale: 'Coin',
          descriptionCapacite: '',
        },
        pseudoJoueur: 'Kat',
        utilisateurId: 55,
        niveau: 3,
        populationDisponible: 10,
        populationOuvriere: 2,
        populationEnEntrainement: 0,
        populationMilitaire: 4,
        enRuine: false,
        bonusCroissance: 1,
        ressources: [],
        garnison: [],
        batiments: [],
        x: 50,
        y: 10,
      },
    ];

    const adapted = adapterCarteBackVersFront(carteBack, royaumes, 101);

    expect(adapted.archetype).toBe('FORET_NOIRE');
    expect(adapted.seed).toBe('foret-999');
    expect(adapted.carte.largeur).toBe(1000);
    expect(adapted.carte.centreX).toBe(500);
    expect(adapted.carte.emplacements[0].coordX).toBe(500);
    expect(adapted.carte.emplacements[0].coordY).toBe(100);
    expect(adapted.carte.emplacements[0].royaume?.estMonRoyaume).toBe(true);
    expect(adapted.carte.emplacements[0].royaume?.peupleNom).toBe('Canards');
    expect(adapted.carte.emplacements[0].royaume?.utilisateurId).toBe(55);
    expect(adapted.carte.emplacements[1].occupe).toBe(false);
    expect(adapted.carte.emplacements[1].royaume).toBeUndefined();
  });

  it('normalizes unknown archetypes to VALLEE_ROYALE', () => {
    expect(normalizeArchetype('UNKNOWN')).toBe('VALLEE_ROYALE');
    expect(normalizeArchetype('CONFLUENCES')).toBe('CONFLUENCES');
  });
});
