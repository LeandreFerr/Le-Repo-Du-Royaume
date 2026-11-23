import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  NgZone,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ArchetypeCarte,
  BaseTerrainMesh,
  CarteDto,
  DEFAULT_CARTE_CENTRE_X,
  DEFAULT_CARTE_CENTRE_Y,
  DEFAULT_CARTE_HEIGHT,
  DEFAULT_CARTE_WIDTH,
  EmplacementCarteDto,
  GeneratedMap,
  InspectionInfo,
  Point2D,
  createDefaultCarte,
} from '../../models/carte.model';
import { MapGeneratorService } from '../../services/map-generator.service';
import {
  buildInspectionInfo,
  genererEmplacementsCercle,
  updatePlacementCoords,
} from '../../services/placement-royaumes.util';
import {
  DEFAULT_TILT_DEG,
  DragPreviewState,
  MAX_TILT_DEG,
  MIN_TILT_DEG,
  MapInteractionTarget,
  MapRenderer25D,
  PERSPECTIVE_FOCAL_LENGTH,
  ProjectionParams,
  RenderMapState,
  hitTestMap,
  screenToWorldClamped,
  worldToScreen,
} from './map-renderer-25d';

type ActiveGesture =
  | {
      mode: 'PAN';
      startScreenX: number;
      startScreenY: number;
      startPanX: number;
      startPanY: number;
      moved: boolean;
      clickedBackground: boolean;
    }
  | {
      mode: 'TILT';
      startScreenY: number;
      startTilt: number;
      moved: boolean;
    }
  | {
      mode: 'EMPLACEMENT';
      numero: number;
      startScreenX: number;
      startScreenY: number;
      moved: boolean;
    };

@Component({
  selector: 'app-carte-royaume',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './carte-royaume.component.html',
  styleUrl: './carte-royaume.component.css',
})
export class CarteRoyaumeComponent implements OnInit, OnChanges, AfterViewInit, OnDestroy {
  private readonly mapGenerator = inject(MapGeneratorService);
  private readonly ngZone = inject(NgZone);
  private readonly renderer = new MapRenderer25D();

  @ViewChild('mapCanvas', { static: true })
  canvasRef!: ElementRef<HTMLCanvasElement>;

  @ViewChild('viewportContainer', { static: true })
  containerRef!: ElementRef<HTMLDivElement>;

  @Input() carte: CarteDto = createDefaultCarte(genererEmplacementsCercle(6, 'royaume-1'));
  @Input() archetype: ArchetypeCarte = 'VALLEE_ROYALE';
  @Input() seed: string | number = 'royaume-1';
  @Input() inclinaison: number = DEFAULT_TILT_DEG;
  @Input() autoriserDeplacement = true;
  @Input() selectedEmplacementNumero: number | null = null;
  @Input() showLabels = true;
  @Input() showBorders = true;
  @Input() showRoutes = true;
  @Input() highlightMarketZone = false;

  @Output() readonly inclinaisonChange = new EventEmitter<number>();
  @Output() readonly emplacementSelectionne = new EventEmitter<EmplacementCarteDto | null>();
  @Output() readonly emplacementDeplace = new EventEmitter<{
    numero: number;
    coordX: number;
    coordY: number;
  }>();
  @Output() readonly marcheSelectionne = new EventEmitter<{ centreX: number; centreY: number }>();
  @Output() readonly carteGeneree = new EventEmitter<GeneratedMap>();

  zoom = 0.85;
  panX = 0;
  panY = 0;
  viewportWidth = 900;
  viewportHeight = 650;

  hoveredTarget: MapInteractionTarget | null = null;
  selectedTarget: MapInteractionTarget | null = null;
  dragPreview: DragPreviewState | null = null;
  hoveredWorldCoords: Point2D | null = null;

  generatedMap: GeneratedMap | null = null;
  private baseTerrain: BaseTerrainMesh | null = null;
  private lastBaseKey: string | null = null;
  private lastProjectedPointsJson: string | null = null;

  private activeGesture: ActiveGesture | null = null;
  private rafId: number | null = null;

  ngOnInit(): void {
    this.inclinaison = this.clampTilt(this.inclinaison);
    this.syncSelectedTargetFromInput();
    this.ensureMapGenerated(true);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['inclinaison']) {
      this.inclinaison = this.clampTilt(this.inclinaison);
    }

    if (changes['selectedEmplacementNumero']) {
      this.syncSelectedTargetFromInput();
    }

    const needBaseRebuild =
      Boolean(changes['seed']) ||
      Boolean(changes['archetype']) ||
      !this.baseTerrain ||
      this.getBaseKey() !== this.lastBaseKey;

    if (needBaseRebuild || changes['carte']) {
      this.ensureMapGenerated(needBaseRebuild);
      this.renderSingleFrame(0);
    }
  }

  ngAfterViewInit(): void {
    this.updateCanvasSize();
    this.startAnimationLoop();
  }

  ngOnDestroy(): void {
    this.stopAnimationLoop();
  }

  get clampedInclinaison(): number {
    return this.clampTilt(this.inclinaison);
  }

  get zoomPercentage(): number {
    return Math.round(this.zoom * 100);
  }

  get activeInspection(): InspectionInfo | null {
    return buildInspectionInfo(this.selectedTarget, this.carte);
  }

  getProjectionParams(): ProjectionParams {
    const cx = Number.isFinite(this.carte?.centreX)
      ? this.carte.centreX
      : DEFAULT_CARTE_CENTRE_X;
    const cy = Number.isFinite(this.carte?.centreY)
      ? this.carte.centreY
      : DEFAULT_CARTE_CENTRE_Y;

    return {
      centreX: cx,
      centreY: cy,
      zoom: this.zoom,
      panX: this.panX,
      panY: this.panY,
      tiltDeg: this.clampTilt(this.inclinaison),
      screenCenterX: this.viewportWidth * 0.5,
      screenCenterY: this.viewportHeight * 0.5,
      focalLength: PERSPECTIVE_FOCAL_LENGTH,
    };
  }

  setInclinaison(angle: number): void {
    const next = this.clampTilt(Math.round(angle));
    if (next !== this.inclinaison) {
      this.inclinaison = next;
      this.inclinaisonChange.emit(this.inclinaison);
    } else {
      this.inclinaison = next;
    }
    this.renderSingleFrame(0);
  }

  zoomIn(): void {
    this.setZoom(this.zoom * 1.18);
  }

  zoomOut(): void {
    this.setZoom(this.zoom / 1.18);
  }

  setZoom(nextZoom: number): void {
    if (!Number.isFinite(nextZoom)) {
      return;
    }
    this.zoom = Math.max(0.5, Math.min(2.5, Number(nextZoom.toFixed(3))));
    this.renderSingleFrame(0);
  }

  resetView(): void {
    const minDim = Math.min(this.viewportWidth, this.viewportHeight);
    this.zoom = Math.max(0.5, Math.min(2.5, Number((minDim / 1050).toFixed(2)) || 0.85));
    this.panX = 0;
    this.panY = 0;
    this.setInclinaison(DEFAULT_TILT_DEG);
  }

  selectMarche(): void {
    this.selectedTarget = { type: 'MARCHE' };
    this.selectedEmplacementNumero = null;
    this.emplacementSelectionne.emit(null);
    this.marcheSelectionne.emit({
      centreX: this.carte?.centreX ?? DEFAULT_CARTE_CENTRE_X,
      centreY: this.carte?.centreY ?? DEFAULT_CARTE_CENTRE_Y,
    });
    this.renderSingleFrame(0);
  }

  selectEmplacementByNumero(numero: number | null): void {
    if (numero === null) {
      this.selectedTarget = null;
      this.selectedEmplacementNumero = null;
      this.emplacementSelectionne.emit(null);
      this.renderSingleFrame(0);
      return;
    }
    const found = (this.carte?.emplacements ?? []).find((e) => e.numero === numero) ?? null;
    this.selectedEmplacementNumero = found ? found.numero : null;
    this.selectedTarget = found ? { type: 'EMPLACEMENT', numero: found.numero } : null;
    this.emplacementSelectionne.emit(found);
    this.renderSingleFrame(0);
  }

  onContextMenu(event: MouseEvent): void {
    event.preventDefault();
  }

  onWheel(event: WheelEvent): void {
    event.preventDefault();
    const factor = event.deltaY < 0 ? 1.12 : 1 / 1.12;
    this.setZoom(this.zoom * factor);
  }

  onMouseDown(event: MouseEvent): void {
    const pos = this.getCanvasRelativeCoords(event);

    // Right click or Alt + Left click -> Tilt 2.5D
    if (event.button === 2 || (event.button === 0 && event.altKey)) {
      event.preventDefault();
      this.activeGesture = {
        mode: 'TILT',
        startScreenY: pos.y,
        startTilt: this.clampTilt(this.inclinaison),
        moved: false,
      };
      return;
    }

    if (event.button !== 0) {
      return;
    }

    const params = this.getProjectionParams();
    const hit = hitTestMap(pos.x, pos.y, this.carte, params, 28);

    if (hit?.type === 'EMPLACEMENT') {
      const emp = (this.carte?.emplacements ?? []).find((e) => e.numero === hit.numero) ?? null;
      this.selectedTarget = { type: 'EMPLACEMENT', numero: hit.numero };
      this.selectedEmplacementNumero = hit.numero;
      this.emplacementSelectionne.emit(emp);

      if (this.autoriserDeplacement && emp) {
        this.activeGesture = {
          mode: 'EMPLACEMENT',
          numero: hit.numero,
          startScreenX: pos.x,
          startScreenY: pos.y,
          moved: false,
        };
      } else {
        this.activeGesture = {
          mode: 'PAN',
          startScreenX: pos.x,
          startScreenY: pos.y,
          startPanX: this.panX,
          startPanY: this.panY,
          moved: false,
          clickedBackground: false,
        };
      }
      this.renderSingleFrame(0);
      return;
    }

    if (hit?.type === 'MARCHE') {
      this.selectMarche();
      this.activeGesture = {
        mode: 'PAN',
        startScreenX: pos.x,
        startScreenY: pos.y,
        startPanX: this.panX,
        startPanY: this.panY,
        moved: false,
        clickedBackground: false,
      };
      return;
    }

    this.activeGesture = {
      mode: 'PAN',
      startScreenX: pos.x,
      startScreenY: pos.y,
      startPanX: this.panX,
      startPanY: this.panY,
      moved: false,
      clickedBackground: true,
    };
  }

  onMouseMove(event: MouseEvent): void {
    const pos = this.getCanvasRelativeCoords(event);
    const params = this.getProjectionParams();
    const mapWidth = this.carte?.largeur ?? DEFAULT_CARTE_WIDTH;
    const mapHeight = this.carte?.hauteur ?? DEFAULT_CARTE_HEIGHT;

    const worldPt = screenToWorldClamped(pos.x, pos.y, params, mapWidth, mapHeight);
    this.hoveredWorldCoords = {
      x: Math.round(worldPt.x),
      y: Math.round(worldPt.y),
    };

    if (!this.activeGesture) {
      this.hoveredTarget = hitTestMap(pos.x, pos.y, this.carte, params, 28);
      return;
    }

    if (this.activeGesture.mode === 'TILT') {
      const dy = pos.y - this.activeGesture.startScreenY;
      if (Math.abs(dy) > 1) {
        this.activeGesture.moved = true;
      }
      const nextTilt = this.clampTilt(Math.round(this.activeGesture.startTilt - dy * 0.25));
      if (nextTilt !== this.inclinaison) {
        this.inclinaison = nextTilt;
        this.inclinaisonChange.emit(this.inclinaison);
      }
      this.renderSingleFrame(0);
      return;
    }

    if (this.activeGesture.mode === 'PAN') {
      const dx = pos.x - this.activeGesture.startScreenX;
      const dy = pos.y - this.activeGesture.startScreenY;
      if (Math.hypot(dx, dy) > 3) {
        this.activeGesture.moved = true;
      }
      if (this.activeGesture.moved) {
        this.panX = this.activeGesture.startPanX + dx;
        this.panY = this.activeGesture.startPanY + dy;
        this.renderSingleFrame(0);
      }
      return;
    }

    if (this.activeGesture.mode === 'EMPLACEMENT') {
      const dx = pos.x - this.activeGesture.startScreenX;
      const dy = pos.y - this.activeGesture.startScreenY;
      if (Math.hypot(dx, dy) > 2 || this.activeGesture.moved) {
        this.activeGesture.moved = true;
        const clamped = screenToWorldClamped(pos.x, pos.y, params, mapWidth, mapHeight);
        this.dragPreview = {
          numero: this.activeGesture.numero,
          coordX: Math.round(clamped.x),
          coordY: Math.round(clamped.y),
        };
        this.renderSingleFrame(0);
      }
    }
  }

  onMouseUp(): void {
    if (!this.activeGesture) {
      return;
    }

    const gesture = this.activeGesture;
    this.activeGesture = null;

    if (gesture.mode === 'EMPLACEMENT' && gesture.moved && this.dragPreview) {
      const { numero, coordX, coordY } = this.dragPreview;
      this.dragPreview = null;
      this.carte = updatePlacementCoords(this.carte, numero, coordX, coordY);
      this.ensureMapGenerated(false);
      this.emplacementDeplace.emit({ numero, coordX, coordY });
      const updatedEmp =
        (this.carte?.emplacements ?? []).find((e) => e.numero === numero) ?? null;
      if (updatedEmp) {
        this.emplacementSelectionne.emit(updatedEmp);
      }
      this.renderSingleFrame(0);
      return;
    }

    if (gesture.mode === 'PAN' && !gesture.moved && gesture.clickedBackground) {
      this.selectedTarget = null;
      this.selectedEmplacementNumero = null;
      this.emplacementSelectionne.emit(null);
      this.renderSingleFrame(0);
    }

    this.dragPreview = null;
  }

  onMouseLeave(): void {
    this.hoveredTarget = null;
    this.hoveredWorldCoords = null;
    if (this.activeGesture) {
      this.onMouseUp();
    }
  }

  projectWorldPoint(x: number, y: number, h = 0) {
    return worldToScreen(x, y, h, this.getProjectionParams());
  }

  renderSingleFrame(timeMs = 0): void {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas || !this.generatedMap) {
      return;
    }
    this.updateCanvasSize();
    const ctx = canvas.getContext('2d');
    if (!ctx) {
      return;
    }

    const dpr = this.getDevicePixelRatio();
    const projection = this.getProjectionParams();
    const anchors = this.renderer.getAnchors(this.carte, projection);
    const anchorsJson = JSON.stringify(anchors);
    if (anchorsJson !== this.lastProjectedPointsJson) {
      this.lastProjectedPointsJson = anchorsJson;
      canvas.setAttribute('data-projected-points', anchorsJson);
    }

    ctx.save();
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    const state: RenderMapState = {
      projection,
      hoveredTarget: this.hoveredTarget,
      selectedTarget: this.selectedTarget,
      inspection: this.activeInspection,
      dragPreview: this.dragPreview,
      highlightMarketZone: this.highlightMarketZone || this.dragPreview !== null,
      showLabels: this.showLabels,
      showBorders: this.showBorders,
      showRoutes: this.showRoutes,
    };

    this.renderer.render(
      ctx,
      this.viewportWidth,
      this.viewportHeight,
      this.generatedMap,
      this.carte,
      state,
      timeMs,
    );
    ctx.restore();
  }

  private ensureMapGenerated(forceBaseRebuild: boolean): void {
    if (!this.carte) {
      this.carte = createDefaultCarte();
    }

    const width =
      Number.isFinite(this.carte.largeur) && this.carte.largeur > 0
        ? this.carte.largeur
        : DEFAULT_CARTE_WIDTH;
    const height =
      Number.isFinite(this.carte.hauteur) && this.carte.hauteur > 0
        ? this.carte.hauteur
        : DEFAULT_CARTE_HEIGHT;
    const baseKey = this.getBaseKey();

    if (forceBaseRebuild || !this.baseTerrain || this.lastBaseKey !== baseKey) {
      this.baseTerrain = this.mapGenerator.generateBaseTerrain(
        this.seed,
        this.archetype,
        width,
        height,
      );
      this.lastBaseKey = baseKey;
    }

    this.generatedMap = this.mapGenerator.populateMapDomains(this.baseTerrain, this.carte);
    this.carteGeneree.emit(this.generatedMap);
  }

  private getBaseKey(): string {
    const w = this.carte?.largeur ?? DEFAULT_CARTE_WIDTH;
    const h = this.carte?.hauteur ?? DEFAULT_CARTE_HEIGHT;
    return `${String(this.seed)}|${this.archetype}|${w}|${h}`;
  }

  private syncSelectedTargetFromInput(): void {
    if (
      this.selectedEmplacementNumero !== null &&
      this.selectedEmplacementNumero !== undefined
    ) {
      this.selectedTarget = {
        type: 'EMPLACEMENT',
        numero: this.selectedEmplacementNumero,
      };
    } else if (this.selectedTarget?.type === 'EMPLACEMENT') {
      this.selectedTarget = null;
    }
  }

  private clampTilt(value: number): number {
    if (!Number.isFinite(value)) {
      return DEFAULT_TILT_DEG;
    }
    return Math.max(MIN_TILT_DEG, Math.min(MAX_TILT_DEG, value));
  }

  private getCanvasRelativeCoords(event: MouseEvent): Point2D {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas) {
      return { x: event.offsetX ?? 0, y: event.offsetY ?? 0 };
    }
    const rect = canvas.getBoundingClientRect();
    if (rect.width > 0 && rect.height > 0) {
      return {
        x: event.clientX - rect.left,
        y: event.clientY - rect.top,
      };
    }
    return {
      x: event.offsetX ?? event.clientX ?? 0,
      y: event.offsetY ?? event.clientY ?? 0,
    };
  }

  private getDevicePixelRatio(): number {
    const raw = typeof window !== 'undefined' ? window.devicePixelRatio : 1;
    return Number.isFinite(raw) && raw > 0 ? Math.min(raw, 2) : 1;
  }

  private updateCanvasSize(): void {
    const container = this.containerRef?.nativeElement;
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas) {
      return;
    }

    const cssWidth =
      container && container.clientWidth > 0 ? container.clientWidth : this.viewportWidth;
    const cssHeight =
      container && container.clientHeight > 0 ? container.clientHeight : this.viewportHeight;

    this.viewportWidth = Math.max(300, Math.round(cssWidth));
    this.viewportHeight = Math.max(260, Math.round(cssHeight));

    const dpr = this.getDevicePixelRatio();
    const targetW = Math.round(this.viewportWidth * dpr);
    const targetH = Math.round(this.viewportHeight * dpr);

    if (canvas.width !== targetW || canvas.height !== targetH) {
      canvas.width = targetW;
      canvas.height = targetH;
    }
  }

  private startAnimationLoop(): void {
    this.stopAnimationLoop();
    if (typeof requestAnimationFrame === 'undefined') {
      this.renderSingleFrame(0);
      return;
    }
    this.ngZone.runOutsideAngular(() => {
      const tick = (timeMs: number): void => {
        this.renderSingleFrame(timeMs);
        this.rafId = requestAnimationFrame(tick);
      };
      this.rafId = requestAnimationFrame(tick);
    });
  }

  private stopAnimationLoop(): void {
    if (this.rafId !== null && typeof cancelAnimationFrame !== 'undefined') {
      cancelAnimationFrame(this.rafId);
      this.rafId = null;
    }
  }
}
