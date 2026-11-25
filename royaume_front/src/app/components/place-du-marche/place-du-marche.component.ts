

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import {
  MarcheService,
  OffreMarcheResponse
} from '../../services/marche/marche-service.service';

@Component({
  selector: 'app-place-du-marche',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './place-du-marche.component.html',
  styleUrls: ['./place-du-marche.component.css']
})
export class PlaceDuMarcheComponent implements OnInit {

  constructor(
    private marcheService: MarcheService
  ) {}

  // IDENTIFICATION DU JOUEUR
  idUtilisateurEnCours = 2;

  // DONNEES
  offres: OffreMarcheResponse[] = [];

  chargement = false;
  message = '';

  // Evite les doubles opérations
  offresEnCours = new Set<number>();

  
ngOnInit(): void {
  console.log('INITIALISATION PLACE DU MARCHE');
  this.chargerOffres();
}
  // =========================================
  // CHARGEMENT DES ANNONCES
  // =========================================


chargerOffres(): void {

  console.log('DEBUT CHARGEMENT DES OFFRES');

  this.chargement = true;
  this.message = '';

  this.marcheService.recupererOffres().subscribe({

    next: (offres) => {

      console.log('REPONSE BACKEND :', offres);

      this.offres = [...offres];

      console.log('OFFRES RECUES :', this.offres.length);
      console.log('OFFRES DISPONIBLES :', this.offresDisponibles.length);

      this.chargement = false;

    },

    error: (error) => {

      console.error('ERREUR HTTP MARCHE :', error);

      this.message = 'Impossible de charger les annonces.';

      this.chargement = false;

    },

    complete: () => {
      console.log('CHARGEMENT TERMINE');
    }

  });

}

/*
  chargerOffres(): void {

    this.chargement = true;
    this.message = '';

    this.marcheService.recupererOffres().subscribe({

      next: (offres) => {

        console.log('Offres reçues du serveur :', offres);

        this.offres = [...offres].sort(
          (a, b) =>
            new Date(b.datePublication).getTime() -
            new Date(a.datePublication).getTime()
        );

        console.log('Nombre d\'annonces :', this.offres.length);

        this.chargement = false;
      },

      error: (error) => {

        console.error('Erreur chargement annonces :', error);

        this.message = 'Impossible de charger les annonces.';

        this.chargement = false;
      }

    });
  }
*/
  // =========================================
  // FILTRAGE
  // =========================================

  get offresDisponibles(): OffreMarcheResponse[] {

    return this.offres.filter(
      offre => offre.statut === 'DISPONIBLE'
    );

  }

  get offresConclues(): OffreMarcheResponse[] {

    return this.offres.filter(
      offre => offre.statut === 'CONCLUE'
    );

  }

  // =========================================
  // IDENTIFICATION
  // =========================================

  estMonOffre(offre: OffreMarcheResponse): boolean {

    return Number(offre.vendeurId) ===
      Number(this.idUtilisateurEnCours);

  }

  // =========================================
  // ACCEPTER UNE OFFRE
  // =========================================

  accepterOffre(offre: OffreMarcheResponse): void {

    if (
      offre.statut !== 'DISPONIBLE' ||
      this.estMonOffre(offre) ||
      this.offresEnCours.has(offre.id)
    ) {
      return;
    }

    if (!confirm(
      `Voulez-vous accepter l'annonce de ${offre.vendeurNom} ?`
    )) {
      return;
    }

    this.offresEnCours.add(offre.id);

    this.marcheService.accepterOffre(offre.id).subscribe({

      next: () => {

        this.message = 'Offre acceptée avec succès !';

        this.chargerOffres();

        this.offresEnCours.delete(offre.id);

      },

      error: (error) => {

        console.error('Erreur acceptation :', error);

        this.message = 'Impossible d’accepter cette offre.';

        this.offresEnCours.delete(offre.id);

      }

    });

  }

  // =========================================
  // ANNULER UNE OFFRE
  // =========================================

  annulerOffre(offre: OffreMarcheResponse): void {

    if (
      offre.statut !== 'DISPONIBLE' ||
      !this.estMonOffre(offre) ||
      this.offresEnCours.has(offre.id)
    ) {
      return;
    }

    if (!confirm('Voulez-vous vraiment annuler cette annonce ?')) {
      return;
    }

    this.offresEnCours.add(offre.id);

    this.marcheService.annulerOffre(offre.id).subscribe({

      next: () => {

        this.message = 'Annonce annulée avec succès !';

        this.chargerOffres();

        this.offresEnCours.delete(offre.id);

      },

      error: (error) => {

        console.error(
          'Erreur lors de l’annulation :',
          error
        );

        this.message =
          error.error?.message ||
          'Impossible d’annuler cette annonce.';

        this.offresEnCours.delete(offre.id);

      }

    });

  }

  // =========================================
  // FORMATAGE DATE
  // =========================================

  formaterDate(date: string): string {

    return new Date(date).toLocaleString('fr-FR', {
      dateStyle: 'short',
      timeStyle: 'short'
    });

  }

}
