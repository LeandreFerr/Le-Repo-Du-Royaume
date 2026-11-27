import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { PartieResponse } from '../../models/partie-response';
import { Partie } from '../../services/partie.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-page-lobby',
  imports: [RouterLink],
  templateUrl: './page-lobby.component.html',
  styleUrl: './page-lobby.component.css'
})
export class PageLobby implements OnInit {

  private readonly partieService = inject(Partie);

  readonly parties = signal<PartieResponse[]>([]);
  readonly chargement = signal(false);
  readonly messageErreur = signal<string | null>(null);
  private readonly router = inject(Router);

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