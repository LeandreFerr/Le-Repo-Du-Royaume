import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AdminComponent } from './admin.component';


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

describe('AdminComponent (Bac Ã  sable Royaume)', () => {
  let fixture: ComponentFixture<AdminComponent>;
  let sandbox: AdminComponent;

  beforeEach(async () => {
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockImplementation(
      () => createStub2DContext() as never,
    );

    await TestBed.configureTestingModule({
      imports: [AdminComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminComponent);
    sandbox = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('creates the sandbox app with default 6 circular placements and VALLEE_ROYALE archetype', () => {
    expect(sandbox).toBeTruthy();
    expect(sandbox.archetype).toBe('VALLEE_ROYALE');
    expect(sandbox.inclinaison).toBe(40);
    expect(sandbox.carte.emplacements).toHaveLength(6);
    expect(sandbox.occupiedCount).toBe(4);
    expect(sandbox.freeCount).toBe(2);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Cartographie du Royaume');
  });

  it('switches between all 5 Age-of-Empires archetypes and updates seed', () => {
    sandbox.selectArchetype('LAC_CENTRAL');
    fixture.detectChanges();
    expect(sandbox.archetype).toBe('LAC_CENTRAL');
    expect(sandbox.currentArchetypeMeta.label).toBe('Lac Central');

    const prevSeed = sandbox.seed;
    sandbox.generateRandomSeed();
    expect(sandbox.seed).not.toBe(prevSeed);
    expect(sandbox.seed).toMatch(/^royaume-[0-9a-f]{8}$/);
  });

  it('adjusts 2.5D inclinaison via presets and clamps to [0, 60]', () => {
    sandbox.setInclinaison(0);
    expect(sandbox.inclinaison).toBe(0);

    sandbox.setInclinaison(55);
    expect(sandbox.inclinaison).toBe(55);

    sandbox.setInclinaison(95);
    expect(sandbox.inclinaison).toBe(60);

    sandbox.setInclinaison(-10);
    expect(sandbox.inclinaison).toBe(0);
  });

  it('updates placement count N (1..10), toggles occupancy, and edits X/Y coordinates', () => {
    sandbox.onNombreEmplacementsChange(8);
    expect(sandbox.carte.emplacements).toHaveLength(8);

    const emp5 = sandbox.carte.emplacements.find((e) => e.numero === 5)!;
    expect(emp5.occupe).toBe(false);

    sandbox.onToggleOccupe(5, true);
    const updatedEmp5 = sandbox.carte.emplacements.find((e) => e.numero === 5)!;
    expect(updatedEmp5.occupe).toBe(true);
    expect(updatedEmp5.royaume?.nom).toBe('Brumelac');

    sandbox.onCoordChange(5, 'x', 725);
    sandbox.onCoordChange(5, 'y', 280);
    const movedEmp5 = sandbox.carte.emplacements.find((e) => e.numero === 5)!;
    expect(movedEmp5.coordX).toBe(725);
    expect(movedEmp5.coordY).toBe(280);
  });

  it('computes inspection details for a selected kingdom and for the Central Market', () => {
    sandbox.selectEmplacement(1);
    const empInfo = sandbox.inspectionInfo;
    expect(empInfo).not.toBeNull();
    expect(empInfo?.targetType).toBe('EMPLACEMENT');
    expect(empInfo?.numero).toBe(1);
    expect(empInfo?.distancesRoyaumes).toHaveLength(5);

    sandbox.selectMarcheCentral();
    const marketInfo = sandbox.inspectionInfo;
    expect(marketInfo).not.toBeNull();
    expect(marketInfo?.targetType).toBe('MARCHE');
    expect(marketInfo?.distanceMarche).toBe(0);
    expect(marketInfo?.distancesRoyaumes).toHaveLength(6);
  });

  it('generates and toggles the Java Carte.java snippet for the backend team', () => {
    expect(sandbox.showJavaSnippet).toBe(false);
    sandbox.toggleJavaSnippet();
    fixture.detectChanges();
    expect(sandbox.showJavaSnippet).toBe(true);
    expect(sandbox.javaSnippet).toContain('Carte carte = new Carte(1000, 1000, 500, 500);');
    expect(sandbox.javaSnippet).toContain('carte.ajouterEmplacement(emp1);');
  });
});

