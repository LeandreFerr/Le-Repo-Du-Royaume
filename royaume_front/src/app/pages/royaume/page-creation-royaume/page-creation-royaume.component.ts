import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';

import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  ActivatedRoute,
  Router,
  RouterLink
} from '@angular/router';

import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { PopulationResponse } from '../../../models/population-response';


import { RoyaumeCreationRequest } from '../../../models/royaume-creation-request';

import { PopulationService } from '../../../services/population.service';

import { RoyaumeService } from '../../../services/royaume.service';

import { UtilisateurResponse, UtilisateurService } from '../../../services/utilisateur/utilisateur.service';
import { SessionService } from '../../../services/session/session.service';

@Component({
  selector: 'app-page-creation-royaume',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './page-creation-royaume.component.html',
  styleUrl: './page-creation-royaume.component.css'
})
export class PageCreationRoyaume implements OnInit {

  private readonly formBuilder =
    inject(NonNullableFormBuilder);

  private readonly populationService =
    inject(PopulationService);

  private readonly royaumeService =
    inject(RoyaumeService);

  private readonly utilisateurService =
    inject(UtilisateurService);

  private readonly session =
    inject(SessionService);

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  /*
   * Valeur provisoire pour le test.
   * Elle sera remplacée plus tard par l'utilisateur connecté.
   */
  // private readonly utilisateurIdService = 2;


  // = utilisateurService.UtisateurConnecté() 

  private get utilisateurConnecte(): UtilisateurResponse | null {
    return this.session.utilisateur();
  }

  private get utilisateurId(): number {
    return this.utilisateurConnecte?.id ?? 0;
  }

  readonly populations =
    signal<PopulationResponse[]>([]);

  readonly chargementPopulations =
    signal(false);

  readonly creationEnCours =
    signal(false);

  readonly messageErreur =
    signal<string | null>(null);

  partieId = 0;

  readonly creationForm =
    this.formBuilder.group({
      nom: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(100)
        ]
      ],

      peupleId: [
        0,
        [
          Validators.required,
          Validators.min(1)
        ]
      ]
    });

  ngOnInit(): void {
    this.recupererPartieId();
    this.chargerPopulations();
  }

  private recupererPartieId(): void {
    const parametrePartieId =
      this.route.snapshot.paramMap.get('partieId');

    const partieId = Number(parametrePartieId);

    if (
      !parametrePartieId ||
      !Number.isInteger(partieId) ||
      partieId <= 0
    ) {
      this.messageErreur.set(
        'L’identifiant de la partie est invalide.'
      );

      return;
    }

    this.partieId = partieId;
  }

  chargerPopulations(): void {
    if (this.chargementPopulations()) {
      return;
    }

    this.chargementPopulations.set(true);
    this.messageErreur.set(null);

    this.populationService
      .trouverToutes()
      .pipe(
        finalize(() => {
          this.chargementPopulations.set(false);
        })
      )
      .subscribe({
        next: (populations: PopulationResponse[]) => {
          this.populations.set(populations);
        },

        error: (erreur: HttpErrorResponse) => {
          console.error(
            'Erreur de chargement des peuples :',
            erreur
          );

          this.populations.set([]);
          this.messageErreur.set(
            'Impossible de charger les peuples disponibles.'
          );
        }
      });
  }

  creerRoyaume(): void {
    if (this.utilisateurConnecte === null) {
      this.router.navigate(['/login']);
      return;
    }

    if (this.partieId <= 0) {
      this.messageErreur.set(
        'La partie sélectionnée est invalide.'
      );

      return;
    }

    if (this.creationForm.invalid) {
      this.creationForm.markAllAsTouched();
      return;
    }

    const request: RoyaumeCreationRequest = {
      nom:
        this.creationForm.controls.nom.value.trim(),

      peupleId:
        this.creationForm.controls.peupleId.value,

      partieId:
        this.partieId,

      utilisateurId:
        this.utilisateurId
    };

    this.creationEnCours.set(true);
    this.messageErreur.set(null);

    this.royaumeService
      .creer(request)
      .pipe(
        finalize(() => {
          this.creationEnCours.set(false);
        })
      )
      .subscribe({
        next: (royaume) => {
          console.log('Royaume créé :', royaume);

          // 1. On navigue tout de suite, quoi qu'il arrive
          this.router.navigate(['/lobby']);

          // 2. On rafraîchit la session en tâche de fond
          this.utilisateurService
            .getUtilisateurById(this.utilisateurId)
            .subscribe({
              next: (utilisateur) => {
                this.session.enregistrer(utilisateur);
              },
              error: (erreur) => {
                console.error(
                  'Rafraîchissement de session impossible :',
                  erreur
                );
              }
            });
        },

        error: (erreur: HttpErrorResponse) => {
          console.error(
            'Erreur de création du royaume :',
            erreur
          );

          this.messageErreur.set(
            this.extraireMessageErreur(erreur)
          );
        }
      });
  }

  private extraireMessageErreur(
    erreur: HttpErrorResponse
  ): string {

    if (
      typeof erreur.error === 'string' &&
      erreur.error.trim() !== ''
    ) {
      return erreur.error;
    }

    if (
      erreur.error &&
      typeof erreur.error.message === 'string'
    ) {
      return erreur.error.message;
    }

    if (erreur.status === 409) {
      return 'Le royaume ne peut pas être créé. Le nom est peut-être déjà utilisé ou cet utilisateur possède déjà un royaume.';
    }

    if (erreur.status === 404) {
      return 'La partie, le peuple, l’utilisateur ou la carte est introuvable.';
    }

    if (erreur.status === 0) {
      return 'Impossible de contacter le serveur.';
    }

    return 'La création du royaume a échoué.';
  }
}
