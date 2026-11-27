
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { CarteRoyaumeComponent } from '../../components/carte-royaume/carte-royaume.component';
import { KingdomOverviewComponent } from '../../components/player-panels/kingdom-overview/kingdom-overview.component';
import { KingdomDevelopmentComponent } from '../../components/player-panels/kingdom-development/kingdom-development.component';
import { KingdomBuildingsComponent } from '../../components/player-panels/kingdom-buildings/kingdom-buildings.component';

import {
  CarteDto,
  EmplacementCarteDto,
  createDefaultCarte
} from '../../models/carte.model';

import {
  createDefaultRoyaume,
  genererEmplacementsCercle
} from '../../services/placement-royaumes.util';

import { PalierNiveau } from '../../models/palier-niveau.model';
import { Royaume } from '../../models/royaume.model';

import {
  AmeliorerRoyaumeService
} from '../../services/royaume/ameliorer-royaume.service';

import { BoutonDeconnexion } from '../../components/bouton-deconnexion/bouton-deconnexion.component';

@Component({
  selector: 'app-player',
  standalone: true,
  imports: [
    CarteRoyaumeComponent,
    KingdomOverviewComponent,
    KingdomDevelopmentComponent,
    KingdomBuildingsComponent,
    BoutonDeconnexion
  ],
  templateUrl: './player.component.html',
  styleUrl: './player.component.css'
})
export class PlayerComponent implements OnInit {

  private readonly royaumeService = inject(AmeliorerRoyaumeService);
  private readonly route = inject(ActivatedRoute);

  // Identifiant récupéré depuis l'URL
  royaumeId!: number;

  seed = 'royaume-1';

  carte: CarteDto = createDefaultCarte();

  selectedEmplacementNumero: number | null = null;

activePage: string | null = 'royaume';
  // Données provenant du backend
  royaume: Royaume | null = null;

  paliers: PalierNiveau[] = [];

  chargement = true;
  erreur: string | null = null;

  ngOnInit(): void {

    this.initialiserCarte();

    const id = Number(
      this.route.snapshot.paramMap.get('royaumeId')
    );

   if (!Number.isInteger(id) || id <= 0) {
  this.erreur = 'Identifiant du royaume invalide.';
  this.chargement = false;
  return;
}

    this.royaumeId = id;

    this.chargerDonneesRoyaume();
  }

  // Initialisation visuelle de la carte
  initialiserCarte(): void {

    const emplacements = genererEmplacementsCercle(6, this.seed)
      .map((emp, index) => ({
        ...emp,
        occupe: index < 4,
        royaume: createDefaultRoyaume(emp.numero)
      }));

    this.carte = createDefaultCarte(emplacements);
  }

  // Chargement complet des données
  chargerDonneesRoyaume(): void {

    this.chargerRoyaume();
    this.chargerPaliers();
  }

  // Récupération du royaume depuis Spring
  chargerRoyaume(): void {

  console.log('Début chargement royaume');
  console.log('ID du royaume :', this.royaumeId);

  this.chargement = true;
  this.erreur = null;

  this.royaumeService.obtenirRoyaume(this.royaumeId)
    .subscribe({

      next: (royaume: Royaume) => {

        console.log('Royaume reçu depuis Spring :', royaume);

        this.royaume = royaume;
        this.chargement = false;

        console.log('Chargement terminé');
        console.log('Royaume affichable :', this.royaume);

      },

      error: (error) => {

        console.error('ERREUR HTTP :', error);

        this.erreur =
          error.error?.message ||
          error.message ||
          'Impossible de récupérer votre royaume.';

        this.chargement = false;

      }

    });

}

  // Récupération des informations du prochain palier
  chargerPaliers(): void {

    this.royaumeService.obtenirProchainPalier(this.royaumeId)
      .subscribe({

        next: (response) => {

          this.paliers = response.prochainPalier
            ? [response.prochainPalier]
            : [];

        },

        error: (error) => {

          console.error(
            'Erreur de récupération du prochain palier :',
            error
          );

          this.paliers = [];

        }

      });
  }

  // Appelé après une amélioration du royaume
  onRoyaumeModifie(): void {

    this.chargerDonneesRoyaume();
  }

  onEmplacementSelectionne(
    emplacement: EmplacementCarteDto | null
  ): void {

    this.selectedEmplacementNumero = emplacement?.numero ?? null;

    if (emplacement) {
      console.log('Royaume sélectionné :', emplacement);
    }
  }

  togglePage(page: string): void {

    this.activePage = this.activePage === page
      ? null
      : page;

  }

}
