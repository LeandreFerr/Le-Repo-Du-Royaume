import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { SessionService } from '../../services/session/session.service';
import { UtilisateurService } from '../../services/utilisateur/utilisateur.service';

@Component({
  selector: 'app-bouton-deconnexion',
  standalone: true,
  templateUrl: './bouton-deconnexion.component.html',
  styleUrl: './bouton-deconnexion.component.css'
})
export class BoutonDeconnexion {

  @Input() variante: 'player' | 'admin' = 'player';

  constructor(
    protected session: SessionService,
    private utilisateurService: UtilisateurService,
    private router: Router
  ) {}

  deconnecter(id: number): void {
    const terminer = () => {
      this.session.effacer();
      this.router.navigate(['/login']);
    };

    // Même si le serveur ne répond pas, on déconnecte côté front.
    this.utilisateurService.logout(id).subscribe({
      next: terminer,
      error: terminer
    });
  }
}