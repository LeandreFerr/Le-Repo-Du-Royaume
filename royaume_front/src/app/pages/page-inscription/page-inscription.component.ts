import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { UtilisateurService } from '../../services/utilisateur/utilisateur.service';
import { SessionService } from '../../services/session/session.service';

@Component({
  selector: 'app-page-inscription',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './page-inscription.component.html',
  styleUrl: './page-inscription.component.css'
})
export class PageInscription {

  erreur = '';

  formInscription;

  constructor(
    private fb: FormBuilder,
    private utilisateurService: UtilisateurService,
    private session: SessionService,
    private router: Router
  ) {
    this.formInscription = this.fb.group({
      pseudo: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  inscrire(): void {

    if (this.formInscription.invalid) {
      this.formInscription.markAllAsTouched();
      return;
    }

    const { pseudo, email, password } = this.formInscription.getRawValue();

    this.utilisateurService
      .inscrire({ pseudo: pseudo!, email: email!, password: password! })
      .subscribe({
        next: (utilisateur) => {
          this.erreur = '';
          this.session.enregistrer(utilisateur);
          this.router.navigate(['/']);
        },

        error: (err: HttpErrorResponse) => {
          this.erreur = err.status === 409
            ? (err.error?.message ?? 'Pseudo ou email déjà utilisé.')
            : 'Inscription impossible, réessayez.';
        }
      });
  }
}