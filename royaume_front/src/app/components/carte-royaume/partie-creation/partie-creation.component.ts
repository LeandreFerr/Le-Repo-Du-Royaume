import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { PartieResponse } from '../../../models/partie-response';
import { PartieCreationRequest } from '../../../models/partie-creation-request';
import { Partie } from '../../../services/partie.service';


@Component({
  selector: 'app-partie-creation',
  imports: [ReactiveFormsModule],
  templateUrl: './partie-creation.component.html',
  styleUrl: './partie-creation.component.css'
})
export class PartieCreation {

  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly partieService = inject(Partie);

  popupOuverte = false;
  chargement = false;
  messageErreur: string | null = null;
  partieCreee: PartieResponse | null = null;

  creationForm = this.formBuilder.group({
    nom: [
      '',
      [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100)
      ]
    ],

    maxJoueurs: [
      2,
      [
        Validators.required,
        Validators.min(2),
        Validators.max(10)
      ]
    ]
  });

  ouvrirPopup(): void {
    this.popupOuverte = true;
    this.messageErreur = null;
  }

  fermerPopup(): void {
    if (this.chargement) {
      return;
    }

    this.popupOuverte = false;
    this.messageErreur = null;

    this.creationForm.reset({
      nom: '',
      maxJoueurs: 2
    });
  }

  creerPartie(): void {
    if (this.creationForm.invalid) {
      this.creationForm.markAllAsTouched();
      return;
    }

    const request: PartieCreationRequest = {
      nom: this.creationForm.controls.nom.value.trim(),
      vitesseJeu: 1,
      maxJoueurs: this.creationForm.controls.maxJoueurs.value
    };

    this.chargement = true;
    this.messageErreur = null;

    this.partieService.creer(request).subscribe({
      next: (partie: PartieResponse) => {
        this.partieCreee = partie;
        this.chargement = false;
        this.popupOuverte = false;

        this.creationForm.reset({
          nom: '',
          maxJoueurs: 2
        });
      },

      error: (erreur: HttpErrorResponse) => {
        this.chargement = false;
        this.messageErreur = this.extraireMessageErreur(erreur);
      }
    });
  }

  private extraireMessageErreur(erreur: HttpErrorResponse): string {
    if (typeof erreur.error === 'string' && erreur.error.trim() !== '') {
      return erreur.error;
    }

    if (
      erreur.error &&
      typeof erreur.error.message === 'string'
    ) {
      return erreur.error.message;
    }

    if (erreur.status === 0) {
      return 'Impossible de contacter le serveur.';
    }

    return 'La création de la partie a échoué.';
  }
}