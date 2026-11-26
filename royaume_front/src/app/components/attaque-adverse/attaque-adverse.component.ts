import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AttaqueService, ContingentTroupeResponse } from '../../services/attaque/attaque.service';

@Component({
  selector: 'app-attaque-adverse',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: './attaque-adverse.component.css',
  templateUrl: './attaque-adverse.component.html',
})

export class AttaqueAdverse implements OnInit {
  @Input({ required: true }) attaquantId!: number;
  @Input({ required: true }) defenseurId!: number;

 troupes = {
  ARCHER: 0,
  CAVALIER: 0,
  LANCIER: 0,
  SPECIAL: 0,
};

stocks: Record<string, number> = {
  ARCHER: 0,
  CAVALIER: 0,
  LANCIER: 0,
  SPECIAL: 0,
};

chargementTroupes = false;
troupesChargees = false;

message = '';
loading = false;

constructor(private attaqueService: AttaqueService, private cdr: ChangeDetectorRef) {}
ngOnInit(): void {
    this.chargerTroupesDisponibles();
  }

 chargerTroupesDisponibles(): void {

  if (!this.troupesChargees) {
    this.chargementTroupes = true;
  }

  this.attaqueService
    .trouverTroupesDisponibles(this.attaquantId)
    .subscribe({

      next: (res) => {

        this.stocks = {
          ARCHER: 0,
          CAVALIER: 0,
          LANCIER: 0,
          SPECIAL: 0
        };

        res.forEach(t => {
          this.stocks[t.type] = t.quantite;
        });

        this.chargementTroupes = false;
        this.troupesChargees = true;

        console.log('Stocks actualisés :', this.stocks);

        this.cdr.detectChanges();
      },

      error: (err) => {

        console.error('Erreur chargement des troupes :', err);

        this.message = 'Impossible de récupérer les troupes disponibles.';
        this.chargementTroupes = false;
        this.troupesChargees = true;

        this.cdr.detectChanges();
      }
    });
}

  verifierQuantite(type: keyof typeof this.troupes, valeur: number): void {
  const disponible = this.stocks[type] ?? 0;
  const quantite = Number(valeur);

  if (!Number.isFinite(quantite) || quantite < 0) {
    this.troupes[type] = 0;
    return;
  }

  this.troupes[type] = Math.min(
    Math.floor(quantite),
    disponible
  );
}

  envoyerAttaque(): void {

    if (this.loading) {
      return;
    }

    const troupesSelectionnees: Record<string, number> = {};

    for (const type of Object.keys(this.troupes)) {

      const quantite = Number(
        this.troupes[type as keyof typeof this.troupes]
      );

      const disponible = this.stocks[type] ?? 0;

      if (
        !Number.isInteger(quantite) ||
        quantite < 0 ||
        quantite > disponible
      ) {
        this.message = `Effectif invalide pour ${type}. Disponible : ${disponible}.`;
        return;
      }

      if (quantite > 0) {
        troupesSelectionnees[type] = quantite;
      }
    }

    if (Object.keys(troupesSelectionnees).length === 0) {
      this.message = 'Rassemble tes troupes avant de partir au combat !';
      return;
    }

    this.loading = true;
    this.message = '';

    this.attaqueService.lancer({
      attaquantId: this.attaquantId,
      defenseurId: this.defenseurId,
      troupes: troupesSelectionnees,
    }).subscribe({

      next: (res) => {

  this.loading = false;

  this.message = '⚔ Attaque lancée ! Que l’ennemi tremble derrière ses murailles !';

  this.troupes = {
    ARCHER: 0,
    CAVALIER: 0,
    LANCIER: 0,
    SPECIAL: 0
  };

  this.cdr.detectChanges();

  this.chargerTroupesDisponibles();
      },

      error: (err) => {
        this.loading = false;
        console.error('Erreur lancement attaque :', err);

        this.message =
          err.error?.message ||
          err.error?.detail ||
          "Erreur lors du lancement de l'attaque.";
      }
    });
  }
}