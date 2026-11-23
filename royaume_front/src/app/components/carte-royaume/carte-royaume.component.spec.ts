import { SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { createDefaultCarte } from '../../models/carte.model';
import { MapGeneratorService } from '../../services/map-generator.service';
import {
  createDefaultRoyaume,
  genererEmplacementsCercle,
  togglePlacementOccupe,
} from '../../services/placement-royaumes.util';
import { CarteRoyaumeComponent } from './carte-royaume.component';

describe('CarteRoyaumeComponent', () => {
  let component: CarteRoyaumeComponent;
  let fixture: ComponentFixture<CarteRoyaumeComponent>;
  let mapGenerator: MapGeneratorService;

  beforeEach(async () => {
    const gradientStub = { addColorStop: vi.fn() };
    const ctxStub = {
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

    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockImplementation(
      () => ctxStub as never,
    );

    await TestBed.configureTestingModule({
      imports: [CarteRoyaumeComponent],
    }).compileComponents();

    mapGenerator = TestBed.inject(MapGeneratorService);
    fixture = TestBed.createComponent(CarteRoyaumeComponent);
    component = fixture.componentInstance;

    const emplacements = genererEmplacementsCercle(4, 'spec-seed').map((emp, idx) => ({
      ...emp,
      occupe: idx < 2,
      ...(idx < 2 ? { royaume: createDefaultRoyaume(emp.numero) } : {}),
    }));
    component.seed = 'spec-seed';
    component.archetype = 'VALLEE_ROYALE';
    component.carte = createDefaultCarte(emplacements);

    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('creates the standalone component and generates the initial procedural map', () => {
    expect(component).toBeTruthy();
    expect(component.generatedMap).not.toBeNull();
    expect(component.generatedMap?.archetype).toBe('VALLEE_ROYALE');
    expect(component.generatedMap?.fiefs).toHaveLength(4);
    expect(component.generatedMap?.routes).toHaveLength(4);
  });

  it('reuses cached BaseTerrainMesh when only carte emplacements change, and rebuilds when seed or archetype changes', () => {
    const baseSpy = vi.spyOn(mapGenerator, 'generateBaseTerrain');
    const domainSpy = vi.spyOn(mapGenerator, 'populateMapDomains');

    // Update only occupancy on carte
    const prevCarte = component.carte;
    component.carte = togglePlacementOccupe(prevCarte, 3, true);
    component.ngOnChanges({
      carte: new SimpleChange(prevCarte, component.carte, false),
    });

    expect(baseSpy).not.toHaveBeenCalled();
    expect(domainSpy).toHaveBeenCalledTimes(1);

    // Update archetype -> must rebuild base terrain
    component.archetype = 'LAC_CENTRAL';
    component.ngOnChanges({
      archetype: new SimpleChange('VALLEE_ROYALE', 'LAC_CENTRAL', false),
    });

    expect(baseSpy).toHaveBeenCalledTimes(1);
    expect(domainSpy).toHaveBeenCalledTimes(2);
    expect(component.generatedMap?.archetype).toBe('LAC_CENTRAL');
  });

  it('clamps inclinaison to [0, 60] and emits inclinaisonChange on setInclinaison and right-click drag', () => {
    const emitted: number[] = [];
    component.inclinaisonChange.subscribe((val) => emitted.push(val));

    component.setInclinaison(75);
    expect(component.clampedInclinaison).toBe(60);
    expect(emitted).toContain(60);

    component.setInclinaison(-15);
    expect(component.clampedInclinaison).toBe(0);
    expect(emitted).toContain(0);

    // Right-click vertical drag
    component.setInclinaison(40);
    component.onMouseDown(new MouseEvent('mousedown', { button: 2, clientX: 300, clientY: 300 }));
    component.onMouseMove(new MouseEvent('mousemove', { clientX: 300, clientY: 220 }));
    component.onMouseUp();

    expect(component.inclinaison).toBeGreaterThan(40);
    expect(component.inclinaison).toBeLessThanOrEqual(60);
  });

  it('zooms with wheel and toolbar buttons within [0.5, 2.5] and resets camera view', () => {
    const initialZoom = component.zoom;
    component.zoomIn();
    expect(component.zoom).toBeGreaterThan(initialZoom);

    component.setZoom(99);
    expect(component.zoom).toBe(2.5);

    component.setZoom(0.05);
    expect(component.zoom).toBe(0.5);

    const wheelEvent = new WheelEvent('wheel', { deltaY: -100, cancelable: true });
    component.onWheel(wheelEvent);
    expect(wheelEvent.defaultPrevented).toBe(true);
    expect(component.zoom).toBeGreaterThan(0.5);

    component.panX = 120;
    component.panY = -80;
    component.resetView();
    expect(component.panX).toBe(0);
    expect(component.panY).toBe(0);
    expect(component.inclinaison).toBe(40);
  });

  it('pans on background drag and selects Market or Kingdom on click', () => {
    let marketClicked = false;
    component.marcheSelectionne.subscribe(() => {
      marketClicked = true;
    });

    // Click on Central Market projected screen position
    const marketScreen = component.projectWorldPoint(500, 500, 0);
    component.onMouseDown(
      new MouseEvent('mousedown', {
        button: 0,
        clientX: marketScreen.screenX,
        clientY: marketScreen.screenY,
      }),
    );
    component.onMouseUp();
    expect(marketClicked).toBe(true);
    expect(component.selectedTarget).toEqual({ type: 'MARCHE' });
    expect(component.activeInspection?.targetType).toBe('MARCHE');

    // Background drag pans the camera
    component.onMouseDown(new MouseEvent('mousedown', { button: 0, clientX: 10, clientY: 10 }));
    component.onMouseMove(new MouseEvent('mousemove', { button: 0, clientX: 60, clientY: 45 }));
    component.onMouseUp();
    expect(component.panX).toBe(50);
    expect(component.panY).toBe(35);
  });

  it('supports real-time Drag & Drop of a kingdom emplacement and emits emplacementDeplace', () => {
    const emp1 = component.carte.emplacements[0];
    const startScreen = component.projectWorldPoint(emp1.coordX, emp1.coordY, 0);
    const targetScreen = component.projectWorldPoint(260, 310, 0);

    const movedEvents: Array<{ numero: number; coordX: number; coordY: number }> = [];
    component.emplacementDeplace.subscribe((ev) => movedEvents.push(ev));

    component.onMouseDown(
      new MouseEvent('mousedown', {
        button: 0,
        clientX: startScreen.screenX,
        clientY: startScreen.screenY,
      }),
    );
    expect(component.selectedEmplacementNumero).toBe(emp1.numero);

    component.onMouseMove(
      new MouseEvent('mousemove', {
        button: 0,
        clientX: targetScreen.screenX,
        clientY: targetScreen.screenY,
      }),
    );
    expect(component.dragPreview).not.toBeNull();
    expect(component.dragPreview?.numero).toBe(emp1.numero);

    component.onMouseUp();
    expect(component.dragPreview).toBeNull();
    expect(movedEvents).toHaveLength(1);
    expect(movedEvents[0].numero).toBe(emp1.numero);
    expect(movedEvents[0].coordX).toBeGreaterThanOrEqual(0);
    expect(movedEvents[0].coordX).toBeLessThanOrEqual(1000);
    expect(movedEvents[0].coordY).toBeGreaterThanOrEqual(0);
    expect(movedEvents[0].coordY).toBeLessThanOrEqual(1000);
  });
});
