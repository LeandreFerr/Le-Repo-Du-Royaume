import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { CarteResponse } from '../../models/carte-response';
import { PartieResponse } from '../../models/partie-response';
import { SessionService } from '../../services/session/session.service';
import { PlayerComponent } from './player.component';

function createStub2DContext(): CanvasRenderingContext2D {
  const gradientStub = { addColorStop: vi.fn() };
  return {
    save: vi.fn(),
    restore: vi.fn(),
    setTransform: vi.fn(),
    clearRect: vi.fn(),
    fillRect: vi.fn(),
    beginPath: vi.fn(),
    closePath: vi.fn(),
    moveTo: vi.fn(),
    lineTo: vi.fn(),
    quadraticCurveTo: vi.fn(),
    bezierCurveTo: vi.fn(),
    arc: vi.fn(),
    arcTo: vi.fn(),
    ellipse: vi.fn(),
    rect: vi.fn(),
    roundRect: vi.fn(),
    fill: vi.fn(),
    stroke: vi.fn(),
    clip: vi.fn(),
    setLineDash: vi.fn(),
    getLineDash: vi.fn(() => []),
    fillText: vi.fn(),
    strokeText: vi.fn(),
    measureText: vi.fn((text: string) => ({ width: text.length * 7 }) as TextMetrics),
    createLinearGradient: vi.fn(() => gradientStub as unknown as CanvasGradient),
    createRadialGradient: vi.fn(() => gradientStub as unknown as CanvasGradient),
    lineDashOffset: 0,
    lineWidth: 1,
    fillStyle: '#000',
    strokeStyle: '#000',
    globalAlpha: 1,
    font: '12px sans-serif',
    textAlign: 'center',
    textBaseline: 'middle',
  } as unknown as CanvasRenderingContext2D;
}

describe('PlayerComponent (Carte reliée au Back, Salon & Interactions)', () => {
  let component: PlayerComponent;
  let fixture: ComponentFixture<PlayerComponent>;
  let httpMock: HttpTestingController;
  let session: SessionService;

  const mockMonRoyaume = {
    id: 10,
    nom: 'Royaume Canard',
    peuple: {
      id: 1,
      nom: 'Canards',
      titreSouverain: 'Grand Canard',
      attraction: 1.5,
      capaciteSpeciale: 'Coin',
      descriptionCapacite: '',
    },
    niveau: 1,
    pseudoJoueur: 'Xavier',
    utilisateurId: 5,
    populationDisponible: 10,
    populationOuvriere: 0,
    populationEnEntrainement: 0,
    populationMilitaire: 0,
    enRuine: false,
    bonusCroissance: 1,
    ressources: [],
    batiments: [],
  };

  const mockAdverseRoyaume = {
    ...mockMonRoyaume,
    id: 20,
    nom: 'Citadelle Licorne',
    pseudoJoueur: 'Kat',
    utilisateurId: 6,
    peuple: {
      id: 3,
      nom: 'Licornes',
      titreSouverain: 'Reine',
      attraction: 1,
      capaciteSpeciale: 'Arc',
      descriptionCapacite: '',
    },
    garnison: [],
    x: 50,
    y: 90,
  };

  const mockCarte: CarteResponse = {
    id: 1,
    largeur: 100,
    hauteur: 100,
    centreX: 50,
    centreY: 50,
    partieId: 4,
    archetype: 'LAC_CENTRAL',
    seed: 'lac-partie-4',
    emplacements: [
      {
        id: 100,
        numero: 1,
        coordX: 50,
        coordY: 10,
        occupe: true,
        carteId: 1,
        royaumeId: 10,
        name: 'Royaume Canard',
      },
      {
        id: 101,
        numero: 2,
        coordX: 50,
        coordY: 90,
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

  beforeEach(async () => {
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockImplementation(
      () => createStub2DContext() as never,
    );

    await TestBed.configureTestingModule({
      imports: [PlayerComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (k: string) => (k === 'royaumeId' ? '10' : null),
              },
            },
          },
        },
      ],
    }).compileComponents();

    session = TestBed.inject(SessionService);
    session.enregistrer({
      id: 5,
      pseudo: 'Xavier',
      email: 'x@test.fr',
      online: true,
      admin: false,
      royaumeId: 10,
      royaumeNom: 'Royaume Canard',
    });

    fixture = TestBed.createComponent(PlayerComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    component.ngOnDestroy();
    session.effacer();
    httpMock.verify();
    vi.restoreAllMocks();
  });

  it('displays waiting room without Start button until all slots are filled, then starts party and unlocks map clicks', () => {
    const waitingPartie: PartieResponse = {
      id: 4,
      nom: 'Partie Test',
      dateCreation: '2026-11-27T17:00:00',
      vitesseJeu: 1,
      maxJoueurs: 2,
      vainqueurId: null,
      vainqueurPseudo: null,
      typeVictoire: null,
      royaumes: [{ ...mockMonRoyaume, garnison: [], x: 50, y: 10 }],
      statut: 'EN_ATTENTE',
    };

    httpMock.expectOne('/api/royaumes/10').flush(mockMonRoyaume);
    httpMock.expectOne('/api/royaumes/10/prochain-palier').flush({
      niveauActuel: 1,
      ameliorationPossible: false,
      prochainPalier: null,
      populationDisponible: 10,
      populationRequise: 20,
      populationManquante: 10,
      populationSuffisante: false,
      ressources: [],
    });
    httpMock.expectOne('/api/parties/royaume/10').flush(waitingPartie);
    httpMock.expectOne('/api/cartes/partie/4').flush(mockCarte);

    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(component.estEnAttente).toBe(true);
    expect(component.estPartieComplete).toBe(false);
    expect(component.archetype).toBe('LAC_CENTRAL');
    expect(component.seed).toBe('lac-partie-4');
    expect(el.querySelector('[data-testid="waiting-room-panel"]')).not.toBeNull();
    expect(el.querySelector('[data-testid="start-partie-btn"]')).toBeNull();

    // Clicking our own kingdom while EN_ATTENTE must remain locked
    component.onEmplacementSelectionne(component.carte.emplacements[0]);
    expect(component.salleChateauOuverte).toBe(false);

    // Simulate 2nd player joining -> party becomes full -> Start button appears
    const fullPartie: PartieResponse = {
      ...waitingPartie,
      royaumes: [
        { ...mockMonRoyaume, garnison: [], x: 50, y: 10 },
        mockAdverseRoyaume,
      ],
    };
    const fullCarte: CarteResponse = {
      ...mockCarte,
      emplacements: [
        mockCarte.emplacements[0],
        {
          ...mockCarte.emplacements[1],
          occupe: true,
          royaumeId: 20,
          name: 'Citadelle Licorne',
        },
      ],
    };

    component.onRoyaumeModifie();
    httpMock.expectOne('/api/royaumes/10').flush(mockMonRoyaume);
    httpMock.expectOne('/api/royaumes/10/prochain-palier').flush({
      niveauActuel: 1,
      ameliorationPossible: false,
      prochainPalier: null,
      populationDisponible: 10,
      populationRequise: 20,
      populationManquante: 10,
      populationSuffisante: false,
      ressources: [],
    });
    httpMock.expectOne('/api/parties/royaume/10').flush(fullPartie);
    httpMock.expectOne('/api/cartes/partie/4').flush(fullCarte);
    fixture.detectChanges();

    const startBtn = el.querySelector('[data-testid="start-partie-btn"]') as HTMLButtonElement | null;
    expect(startBtn).not.toBeNull();

    // Click Start -> calls PATCH /api/parties/4/demarrer
    component.demarrerPartie();
    const startReq = httpMock.expectOne('/api/parties/4/demarrer');
    expect(startReq.request.method).toBe('PATCH');
    startReq.flush({ ...fullPartie, statut: 'EN_COURS' });
    httpMock.expectOne('/api/cartes/partie/4').flush(fullCarte);
    fixture.detectChanges();

    expect(component.estEnAttente).toBe(false);
    expect(el.querySelector('[data-testid="waiting-room-panel"]')).toBeNull();
    expect(el.querySelector('[data-testid="partie-en-cours-badge"]')).not.toBeNull();

    // Click our own kingdom -> opens TP Pikachu (Salle du Château)
    component.onEmplacementSelectionne(component.carte.emplacements[0]);
    expect(component.salleChateauOuverte).toBe(true);
    expect(component.menuAdverseOuvert).toBe(false);

    // Click opponent kingdom -> opens MenuAdverse
    component.onEmplacementSelectionne(component.carte.emplacements[1]);
    expect(component.menuAdverseOuvert).toBe(true);
    expect(component.cibleAdverse?.id).toBe(20);
    expect(component.salleChateauOuverte).toBe(false);

    // Click Central Market -> opens PlaceDuMarche
    component.onMarcheSelectionne();
    expect(component.marcheOuvert).toBe(true);
    expect(component.menuAdverseOuvert).toBe(false);
  });
});
