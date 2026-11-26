import { Component, OnInit, inject } from '@angular/core';
import { CarteRoyaumeComponent } from '../../components/carte-royaume/carte-royaume.component';
import { KingdomOverviewComponent } from '../../components/player-panels/kingdom-overview/kingdom-overview.component';
import { KingdomDevelopmentComponent } from '../../components/player-panels/kingdom-development/kingdom-development.component';
import { KingdomBuildingsComponent } from '../../components/player-panels/kingdom-buildings/kingdom-buildings.component';

import {
  CarteDto,
  EmplacementCarteDto,
  createDefaultCarte,
} from '../../models/carte.model';

import {
  createDefaultRoyaume,
  genererEmplacementsCercle,
} from '../../services/placement-royaumes.util';

import { Royaume } from '../../models/royaume.model';
import { PalierNiveau } from '../../models/palier-niveau.model';
import { RoyaumeService } from '../../services/royaume.service';

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
  styleUrl: './player.component.css',
})
export class PlayerComponent implements OnInit {

  private readonly royaumeService = inject(RoyaumeService);

  seed = 'royaume-1';

  carte: CarteDto = createDefaultCarte();

  selectedEmplacementNumero: number | null = null;

  activePage: string | null = null;

  // Données du joueur

  royaume: Royaume | null = null;

  paliers: PalierNiveau[] = [];


  ngOnInit(): void {

    // Initialisation de la carte

    const emplacements = genererEmplacementsCercle(6, this.seed)
      .map((emp, index) => ({
        ...emp,
        occupe: index < 4,
        royaume: createDefaultRoyaume(emp.numero),
      }));

    this.carte = createDefaultCarte(emplacements);

    // Récupération des données du joueur

    this.chargerRoyaume();
    this.chargerPaliers();
  }


  // Récupération du royaume

  chargerRoyaume(): void {

    this.royaumeService.getMonRoyaume().subscribe({
      next: (royaume) => {
        this.royaume = royaume;
      },
      error: (error) => {
        console.error('Erreur de récupération du royaume', error);
      }
    });

  }


  // Récupération des paliers

  chargerPaliers(): void {

    this.royaumeService.getPaliers().subscribe({
      next: (paliers) => {
        this.paliers = paliers;
      },
      error: (error) => {
        console.error('Erreur de récupération des paliers', error);
      }
    });

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

    if (this.activePage === page) {
      this.activePage = null;
      console.log('Fermeture :', page);
    } else {
      this.activePage = page;
      console.log('Ouverture :', page);
    }

  }

}