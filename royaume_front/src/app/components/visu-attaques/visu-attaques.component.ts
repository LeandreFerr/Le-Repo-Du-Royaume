import { Component, Input, OnChanges, OnInit, SimpleChanges, inject, signal, computed } from '@angular/core';
import { DatePipe } from '@angular/common';

import {
  AttaqueService,
  AttaqueResponse,
} from '../../services/attaque/attaque.service';
import { SessionService } from '../../services/session/session.service';

@Component({
  selector: 'app-visu-attaques',
  imports: [DatePipe],
  styleUrl: './visu-attaques.component.css',
  templateUrl: './visu-attaques.component.html',
})
export class VisuAttaques implements OnInit, OnChanges {
  private readonly attaqueService = inject(AttaqueService);
  private readonly session = inject(SessionService);

  @Input() royaumeId?: number;

  idUtilisateur = signal<number>(1);

  attaques = signal<AttaqueResponse[]>([]);
  chargement = signal<boolean>(true);
  erreur = signal<string | null>(null);

  attaquesEnCours = computed(() => {
    const userId = this.idUtilisateur();
    return this.attaques().filter(
      (a) =>
        (a.statut === 'ALLER' || a.statut === 'RETOUR') &&
        a.attaquantId === userId
    );
  });

  attaquesPassees = computed(() => {
    const userId = this.idUtilisateur();
    return this.attaques().filter(
      (a) =>
        a.statut === 'TERMINEE' &&
        (a.attaquantId === userId || a.defenseurId === userId)
    );
  });

  ngOnInit(): void {
    this.synchroniserRoyaumeId();
    this.chargerAttaques();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['royaumeId']) {
      this.synchroniserRoyaumeId();
    }
  }

  private synchroniserRoyaumeId(): void {
    const id = this.royaumeId ?? this.session.utilisateur()?.royaumeId ?? 1;
    this.idUtilisateur.set(id);
  }

  chargerAttaques(): void {
    this.chargement.set(true);
    this.erreur.set(null);

    this.attaqueService.listerToutes().subscribe({
      next: (data) => {
        this.attaques.set(data);
        this.chargement.set(false);
      },
      error: (err) => {
        console.error('Erreur chargement attaques', err);
        this.erreur.set('Impossible de charger les attaques.');
        this.chargement.set(false);
      },
    });
  }
}
