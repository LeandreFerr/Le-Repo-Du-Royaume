import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CarteRoyaumeComponent } from '../../components/carte-royaume/carte-royaume.component';
import {
  ARCHETYPES_CARTE,
  ArchetypeCarte,
  ArchetypeCarteMetadata,
  CarteDto,
  DEFAULT_CARTE_CENTRE_X,
  DEFAULT_CARTE_CENTRE_Y,
  EmplacementCarteDto,
  GeneratedMap,
  InspectionInfo,
  MAX_EMPLACEMENTS,
  MIN_EMPLACEMENTS,
  RoyaumeResume,
  createDefaultCarte,
} from '../../models/carte.model';
import {
  buildInspectionInfo,
  calculerDistanceMarche,
  createDefaultRoyaume,
  genererEmplacementsCercle,
  genererSnippetJavaCarte,
  togglePlacementOccupe,
  updatePlacementCoords,
} from '../../services/placement-royaumes.util';
import { randomSeedString } from '../../services/prng.util';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, CarteRoyaumeComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css',
})
export class AdminComponent implements OnInit {
  readonly archetypes: readonly ArchetypeCarteMetadata[] = ARCHETYPES_CARTE;
  readonly minEmplacements = MIN_EMPLACEMENTS;
  readonly maxEmplacements = MAX_EMPLACEMENTS;

  seed = 'royaume-1';
  archetype: ArchetypeCarte = 'VALLEE_ROYALE';
  inclinaison = 40;
  nombreEmplacements = 6;
  autoriserDeplacement = true;
  showLabels = true;
  showBorders = true;
  showRoutes = true;

  carte: CarteDto = createDefaultCarte();
  selectedEmplacementNumero: number | null = 1;
  inspectingMarche = false;
  showJavaSnippet = false;
  seedCopied = false;
  javaCopied = false;
  lastGeneratedMap: GeneratedMap | null = null;

  ngOnInit(): void {
    const initial = genererEmplacementsCercle(this.nombreEmplacements, this.seed).map(
      (emp, index) => ({
        ...emp,
        occupe: index < 4,
        royaume: createDefaultRoyaume(emp.numero),
      }),
    );
    this.carte = createDefaultCarte(initial);
    this.selectedEmplacementNumero = this.carte.emplacements[0]?.numero ?? null;
  }

  get currentArchetypeMeta(): ArchetypeCarteMetadata {
    return (
      this.archetypes.find((a) => a.id === this.archetype) ?? this.archetypes[0]
    );
  }

  get occupiedCount(): number {
    return this.carte.emplacements.filter((e) => e.occupe).length;
  }

  get freeCount(): number {
    return this.carte.emplacements.length - this.occupiedCount;
  }

  get selectedEmplacement(): EmplacementCarteDto | null {
    if (this.selectedEmplacementNumero === null) {
      return null;
    }
    return (
      this.carte.emplacements.find((e) => e.numero === this.selectedEmplacementNumero) ?? null
    );
  }

  get inspectionInfo(): InspectionInfo | null {
    if (this.inspectingMarche) {
      return buildInspectionInfo({ type: 'MARCHE' }, this.carte);
    }
    if (this.selectedEmplacementNumero !== null) {
      return buildInspectionInfo(
        { type: 'EMPLACEMENT', numero: this.selectedEmplacementNumero },
        this.carte,
      );
    }
    return null;
  }

  get javaSnippet(): string {
    return genererSnippetJavaCarte(this.carte, this.archetype, this.seed);
  }

  getRoyaumeInfo(emp: EmplacementCarteDto): RoyaumeResume {
    return emp.royaume ?? createDefaultRoyaume(emp.numero);
  }

  getDistanceToMarket(emp: EmplacementCarteDto): number {
    return Math.round(
      calculerDistanceMarche(
        emp,
        this.carte?.centreX ?? DEFAULT_CARTE_CENTRE_X,
        this.carte?.centreY ?? DEFAULT_CARTE_CENTRE_Y,
      ),
    );
  }

  onSeedChange(newSeed: string): void {
    this.seed = newSeed;
  }

  generateRandomSeed(): void {
    this.seed = randomSeedString();
    this.disposerEnCercle();
  }

  async copySeedToClipboard(): Promise<void> {
    if (typeof navigator !== 'undefined' && navigator.clipboard?.writeText) {
      try {
        await navigator.clipboard.writeText(String(this.seed));
      } catch {
        // Ignore clipboard permission errors in headless browser environments
      }
    }
    this.seedCopied = true;
    setTimeout(() => {
      this.seedCopied = false;
    }, 1800);
  }

  async copyJavaToClipboard(): Promise<void> {
    if (typeof navigator !== 'undefined' && navigator.clipboard?.writeText) {
      try {
        await navigator.clipboard.writeText(this.javaSnippet);
      } catch {
        // Ignore clipboard permission errors in headless browser environments
      }
    }
    this.javaCopied = true;
    setTimeout(() => {
      this.javaCopied = false;
    }, 1800);
  }

  selectArchetype(arch: ArchetypeCarte): void {
    this.archetype = arch;
  }

  setInclinaison(angle: number): void {
    this.inclinaison = Math.max(0, Math.min(60, Math.round(angle)));
  }

  onInclinaisonChange(angle: number): void {
    this.inclinaison = Math.max(0, Math.min(60, Math.round(angle)));
  }

  onNombreEmplacementsChange(countValue: number | string): void {
    const parsed = Math.round(Number(countValue));
    const safeCount = Math.max(
      MIN_EMPLACEMENTS,
      Math.min(
        MAX_EMPLACEMENTS,
        Number.isFinite(parsed) ? parsed : this.nombreEmplacements,
      ),
    );
    this.nombreEmplacements = safeCount;
    this.disposerEnCercle();
  }

  disposerEnCercle(): void {
    const emplacements = genererEmplacementsCercle(
      this.nombreEmplacements,
      this.seed,
      this.carte.emplacements,
      {
        largeur: this.carte.largeur,
        hauteur: this.carte.hauteur,
        centreX: this.carte.centreX,
        centreY: this.carte.centreY,
      },
    ).map((emp) => ({
      ...emp,
      royaume: emp.royaume ?? createDefaultRoyaume(emp.numero),
    }));

    this.carte = {
      ...this.carte,
      emplacements,
    };

    if (
      this.selectedEmplacementNumero !== null &&
      !emplacements.some((e) => e.numero === this.selectedEmplacementNumero)
    ) {
      this.selectedEmplacementNumero = emplacements[0]?.numero ?? null;
    }
  }

  onToggleOccupe(numero: number, occupe?: boolean): void {
    this.carte = togglePlacementOccupe(this.carte, numero, occupe);
  }

  onCoordChange(numero: number, axis: 'x' | 'y', rawValue: number | string): void {
    const emp = this.carte.emplacements.find((e) => e.numero === numero);
    if (!emp) {
      return;
    }
    const parsed = Number(rawValue);
    const nextX = axis === 'x' ? (Number.isFinite(parsed) ? parsed : emp.coordX) : emp.coordX;
    const nextY = axis === 'y' ? (Number.isFinite(parsed) ? parsed : emp.coordY) : emp.coordY;
    this.carte = updatePlacementCoords(this.carte, numero, nextX, nextY);
  }

  selectEmplacement(numero: number): void {
    this.inspectingMarche = false;
    this.selectedEmplacementNumero = numero;
  }

  selectMarcheCentral(): void {
    this.inspectingMarche = true;
    this.selectedEmplacementNumero = null;
  }

  onEmplacementSelectionneDepuisCarte(emp: EmplacementCarteDto | null): void {
    if (emp) {
      this.inspectingMarche = false;
      this.selectedEmplacementNumero = emp.numero;
    } else if (!this.inspectingMarche) {
      this.selectedEmplacementNumero = null;
    }
  }

  onMarcheSelectionneDepuisCarte(): void {
    this.inspectingMarche = true;
    this.selectedEmplacementNumero = null;
  }

  onEmplacementDeplaceDepuisCarte(event: {
    numero: number;
    coordX: number;
    coordY: number;
  }): void {
    this.inspectingMarche = false;
    this.selectedEmplacementNumero = event.numero;
    this.carte = updatePlacementCoords(
      this.carte,
      event.numero,
      event.coordX,
      event.coordY,
    );
  }

  onCarteGeneree(map: GeneratedMap): void {
    this.lastGeneratedMap = map;
  }

  toggleJavaSnippet(): void {
    this.showJavaSnippet = !this.showJavaSnippet;
  }
}