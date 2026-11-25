import { Component } from '@angular/core';
<<<<<<< HEAD
import { Router } from '@angular/router';
=======
>>>>>>> Modul_6_pikachu
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
<<<<<<< HEAD

import { UtilisateurService, UtilisateurResponse }
  from '../../services/utilisateur/utilisateur.service';
=======
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UtilisateurResponse, UtilisateurService } from '../../services/utilisateur/utilisateur.service';
>>>>>>> Modul_6_pikachu

@Component({
  selector: 'app-page-login',
  standalone: true,
<<<<<<< HEAD
  imports: [ReactiveFormsModule],
=======
  imports: [ReactiveFormsModule,   CommonModule],
>>>>>>> Modul_6_pikachu
  templateUrl: './page-login.component.html',
  styleUrl: './page-login.component.css'
})
export class PageLogin {

  erreur = '';

  formLogin;

  constructor(
    private fb: FormBuilder,
    private utilisateurService: UtilisateurService,
    private router: Router
  ) {
    this.formLogin = this.fb.group({
      pseudo: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  login(): void {

    if (this.formLogin.invalid) {
      this.formLogin.markAllAsTouched();
      return;
    }

    const { pseudo, password } = this.formLogin.getRawValue();

    this.utilisateurService.login(pseudo!, password!).subscribe({

      next: (utilisateur: UtilisateurResponse) => {
        console.log('Utilisateur connecté :', utilisateur);
        this.erreur = '';
        this.router.navigate(['/carte-sandbox']);
      },

      error: () => {
        this.erreur = 'Pseudo ou mot de passe incorrect';
      }

    });
  }
}