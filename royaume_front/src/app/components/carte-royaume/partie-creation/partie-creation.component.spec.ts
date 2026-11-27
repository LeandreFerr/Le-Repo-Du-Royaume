import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { PartieCreation } from './partie-creation.component';

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

describe('PartieCreation', () => {
  let component: PartieCreation;
  let fixture: ComponentFixture<PartieCreation>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockImplementation(
      () => createStub2DContext() as never,
    );

    await TestBed.configureTestingModule({
      imports: [PartieCreation],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(PartieCreation);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });

  it('should create with live preview, archetype selection, and auto-seeded map', () => {
    expect(component).toBeTruthy();
    expect(component.popupOuverte).toBe(true);
    expect(component.selectedArchetype).toBe('VALLEE_ROYALE');
    expect(component.effectiveSeed).toMatch(/^vallee-[0-9a-f]{8}$/);
    expect(component.previewCarte.emplacements).toHaveLength(2);
  });

  it('updates seed and preview when changing archetype and maxJoueurs unless seed is manual', () => {
    component.selectArchetype('LAC_CENTRAL');
    component.creationForm.controls.maxJoueurs.setValue(4);
    fixture.detectChanges();

    expect(component.effectiveSeed).toMatch(/^lac-[0-9a-f]{8}$/);
    expect(component.previewCarte.emplacements).toHaveLength(4);

    component.creationForm.controls.seed.setValue('ma-seed-custom');
    component.onSeedInput();
    component.selectArchetype('FORET_NOIRE');
    expect(component.effectiveSeed).toBe('ma-seed-custom');
  });

  it('submits archetype & seed and redirects creator to kingdom creation', () => {
    const navSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.creationForm.controls.nom.setValue('Bataille Royale');
    component.creationForm.controls.maxJoueurs.setValue(4);
    component.selectArchetype('CONFLUENCES');
    component.creerPartie();

    const req = httpMock.expectOne('/api/parties');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.archetype).toBe('CONFLUENCES');
    expect(req.request.body.seed).toMatch(/^confluences-[0-9a-f]{8}$/);

    req.flush({
      id: 42,
      nom: 'Bataille Royale',
      dateCreation: '2026-11-27T17:00:00',
      vitesseJeu: 1,
      maxJoueurs: 4,
      vainqueurId: null,
      vainqueurPseudo: null,
      typeVictoire: null,
      royaumes: [],
      statut: 'EN_ATTENTE',
    });

    expect(navSpy).toHaveBeenCalledWith(['/parties', 42, 'royaume', 'creation']);
  });
});
