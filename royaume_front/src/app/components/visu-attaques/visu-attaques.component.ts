import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { DatePipe } from '@angular/common';

import {
  AttaqueService,
  AttaqueResponse,
} from '../../services/attaque/attaque.service';

@Component({
  selector: 'app-visu-attaques',
  imports: [DatePipe],
  styleUrl: './visu-attaques.component.css',
  templateUrl: './visu-attaques.component.html',
})
export class VisuAttaques implements OnInit{
  private attaqueService = inject(AttaqueService);

  // Temporaire en attendant l'authenti
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
    this.chargerAttaques();
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

