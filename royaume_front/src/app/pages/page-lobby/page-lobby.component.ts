import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { PartieResponse } from '../../models/partie-response';
import { Partie } from '../../services/partie.service';
import { SessionService } from '../../services/session/session.service';
import { BoutonDeconnexion } from '../../components/bouton-deconnexion/bouton-deconnexion.component';

@Component({
  selector: 'app-page-lobby',
  standalone: true,
  imports: [RouterLink, BoutonDeconnexion],
  templateUrl: './page-lobby.component.html',
  styleUrl: './page-lobby.component.css'
})
export class PageLobby implements OnInit {

  private readonly partieService = inject(Partie);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  readonly utilisateur = this.session.utilisateur;

  readonly parties = signal<PartieResponse[]>([]);
  readonly chargement = signal(false);
  readonly messageErreur = signal<string | null>(null);

  ngOnInit(): void {
    this.chargerPartiesEnAttente();
  }

  chargerPartiesEnAttente(): void {
    if (this.chargement()) {
      return;
    }

    this.chargement.set(true);
    this.messageErreur.set(null);

    this.partieService
      .trouverParStatut('EN_ATTENTE')
      .pipe(
        finalize(() => {
          this.chargement.set(false);
        })
      )
      .subscribe({
        next: (parties: PartieResponse[]) => {
          console.log('Parties reçues :', parties);
          this.parties.set(parties);
        },

        error: (erreur) => {
          console.error(
            'Erreur pendant le chargement des parties :',
            erreur
          );

          this.parties.set([]);
          this.messageErreur.set(
            'Impossible de charger les parties en attente.'
          );
        }
      });
  }

  rejoindre(partie: PartieResponse): void {
    this.router.navigate([
      '/parties',
      partie.id,
      'royaume',
      'creation'
    ]);
  }
}