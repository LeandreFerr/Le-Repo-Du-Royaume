import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import {
  ARCHETYPES_CARTE,
  ArchetypeCarte,
  ArchetypeCarteMetadata,
  CarteDto,
  createDefaultCarte,
} from '../../../models/carte.model';
import { PartieCreationRequest } from '../../../models/partie-creation-request';
import { PartieResponse } from '../../../models/partie-response';
import { Partie } from '../../../services/partie.service';
import { genererEmplacementsCercle } from '../../../services/placement-royaumes.util';
import { hashSeed } from '../../../services/prng.util';
import { SessionService } from '../../../services/session/session.service';
import { CarteRoyaumeComponent } from '../carte-royaume.component';

const ARCHETYPE_PREFIX: Record<ArchetypeCarte, string> = {
  VALLEE_ROYALE: 'vallee',
  FORET_NOIRE: 'foret',
  HAUTES_TERRES: 'hautes',
  CONFLUENCES: 'confluences',
  LAC_CENTRAL: 'lac',
};

@Component({
  selector: 'app-partie-creation',
  standalone: true,
  imports: [ReactiveFormsModule, CarteRoyaumeComponent],
  templateUrl: './partie-creation.component.html',
  styleUrl: './partie-creation.component.css',
})
export class PartieCreation implements OnInit {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly partieService = inject(Partie);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  readonly archetypes: readonly ArchetypeCarteMetadata[] = ARCHETYPES_CARTE;

  popupOuverte = true;
  chargement = false;
  messageErreur: string | null = null;
  partieCreee: PartieResponse | null = null;

  creationTimestamp = Date.now();
  seedManuelle = false;
  previewCarte: CarteDto = createDefaultCarte();

  creationForm = this.formBuilder.group({
    nom: [
      '',
      [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
      ],
    ],
    maxJoueurs: [
      2,
      [
        Validators.required,
        Validators.min(2),
        Validators.max(10),
      ],
    ],
    archetype: ['VALLEE_ROYALE' as ArchetypeCarte, [Validators.required]],
    seed: [''],
  });

  ngOnInit(): void {
    this.recalculerSeedParDefaut();
    this.mettreAJourPreview();

    this.creationForm.controls.nom.valueChanges.subscribe(() => {
      this.recalculerSeedParDefaut();
      this.mettreAJourPreview();
    });
    this.creationForm.controls.maxJoueurs.valueChanges.subscribe(() => {
      this.recalculerSeedParDefaut();
      this.mettreAJourPreview();
    });
    this.creationForm.controls.archetype.valueChanges.subscribe(() => {
      this.recalculerSeedParDefaut();
      this.mettreAJourPreview();
    });
  }

  get selectedArchetype(): ArchetypeCarte {
    return this.creationForm.controls.archetype.value;
  }

  get effectiveSeed(): string {
    const val = this.creationForm.controls.seed.value.trim();
    return val.length > 0 ? val : this.calculerSeedAutomatique();
  }

  ouvrirPopup(): void {
    this.popupOuverte = true;
    this.messageErreur = null;
    this.creationTimestamp = Date.now();
    this.seedManuelle = false;
    this.recalculerSeedParDefaut();
    this.mettreAJourPreview();
  }

  fermerPopup(): void {
    if (this.chargement) {
      return;
    }

    this.popupOuverte = false;
    this.messageErreur = null;
    this.seedManuelle = false;
    this.creationForm.reset({
      nom: '',
      maxJoueurs: 2,
      archetype: 'VALLEE_ROYALE',
      seed: '',
    });

    if (this.router.url.includes('/parties/creation')) {
      this.router.navigate(['/lobby']);
    }
  }

  selectArchetype(archetype: ArchetypeCarte): void {
    this.creationForm.controls.archetype.setValue(archetype);
  }

  onSeedInput(): void {
    const val = this.creationForm.controls.seed.value.trim();
    this.seedManuelle = val.length > 0;
    this.mettreAJourPreview();
  }

  regenererSeed(): void {
    this.creationTimestamp = Date.now();
    this.seedManuelle = false;
    this.recalculerSeedParDefaut();
    this.mettreAJourPreview();
  }

  creerPartie(): void {
    if (this.creationForm.invalid) {
      this.creationForm.markAllAsTouched();
      return;
    }

    const request: PartieCreationRequest = {
      nom: this.creationForm.controls.nom.value.trim(),
      vitesseJeu: 1,
      maxJoueurs: this.creationForm.controls.maxJoueurs.value,
      archetype: this.selectedArchetype,
      seed: this.effectiveSeed,
    };

    this.chargement = true;
    this.messageErreur = null;

    this.partieService.creer(request).subscribe({
      next: (partie: PartieResponse) => {
        this.partieCreee = partie;
        this.chargement = false;
        this.popupOuverte = false;

        const royaumeIdExistant = this.session.utilisateur()?.royaumeId;
        if (royaumeIdExistant) {
          this.router.navigate(['/player', royaumeIdExistant]);
        } else {
          this.router.navigate(['/parties', partie.id, 'royaume', 'creation']);
        }
      },

      error: (erreur: HttpErrorResponse) => {
        this.chargement = false;
        this.messageErreur = this.extraireMessageErreur(erreur);
      },
    });
  }

  private calculerSeedAutomatique(): string {
    const nom = this.creationForm.controls.nom.value.trim() || 'nouvelle-partie';
    const maxJoueurs = this.creationForm.controls.maxJoueurs.value || 2;
    const archetype = this.creationForm.controls.archetype.value || 'VALLEE_ROYALE';
    const prefix = ARCHETYPE_PREFIX[archetype] ?? 'royaume';
    const hash = hashSeed(
      `${this.creationTimestamp}|${nom}|${maxJoueurs}|${archetype}`,
    )
      .toString(16)
      .padStart(8, '0');
    return `${prefix}-${hash}`;
  }

  private recalculerSeedParDefaut(): void {
    if (this.seedManuelle) {
      return;
    }
    this.creationForm.controls.seed.setValue(this.calculerSeedAutomatique(), {
      emitEvent: false,
    });
  }

  private mettreAJourPreview(): void {
    const rawCount = Number(this.creationForm.controls.maxJoueurs.value);
    const nbEmplacements = Math.max(
      2,
      Math.min(10, Number.isFinite(rawCount) ? Math.round(rawCount) : 2),
    );
    const emplacements = genererEmplacementsCercle(nbEmplacements, this.effectiveSeed);
    this.previewCarte = createDefaultCarte(emplacements);
  }

  private extraireMessageErreur(erreur: HttpErrorResponse): string {
    if (typeof erreur.error === 'string' && erreur.error.trim() !== '') {
      return erreur.error;
    }

    if (erreur.error && typeof erreur.error.message === 'string') {
      return erreur.error.message;
    }

    if (erreur.status === 0) {
      return 'Impossible de contacter le serveur.';
    }

    return 'La création de la partie a échoué.';
  }
}
