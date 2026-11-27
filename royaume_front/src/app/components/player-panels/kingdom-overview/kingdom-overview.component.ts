import {
  Component,
  Input
} from '@angular/core';

import { DecimalPipe } from '@angular/common';

import { Royaume } from '../../../models/royaume.model';
import { PalierNiveau } from '../../../models/palier-niveau.model';
import { Ressource } from '../../../models/ressource.model';
import { TypeBatiment } from '../../../models/batiment.model';

@Component({
  selector: 'app-kingdom-overview',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './kingdom-overview.component.html',
  styleUrl: './kingdom-overview.component.css',
})
export class KingdomOverviewComponent {

  @Input({ required: true }) royaume!: Royaume;

  @Input({ required: true }) paliers!: PalierNiveau[];

  @Input() vitesseJeu = 1;

  getProductionParHeure(ressource: Ressource): number {
    const batiments = this.royaume?.batiments ?? [];
    const totalBase = batiments
      .filter(
        (b) =>
          b.type === TypeBatiment.PRODUCTION &&
          !b.enConstruction &&
          b.pointsDeVie > 0 &&
          b.typeRessource === ressource
      )
      .reduce((somme, b) => somme + (b.productionBaseHeure ?? 0) * b.niveau, 0);

    const vitesse = Number.isFinite(this.vitesseJeu) && this.vitesseJeu > 0 ? this.vitesseJeu : 1;
    return totalBase * vitesse;
  }

  get croissancePopulationPct(): number {
    const vitesse = Number.isFinite(this.vitesseJeu) && this.vitesseJeu > 0 ? this.vitesseJeu : 1;
    return (this.royaume?.bonusCroissance ?? 0) * 100 * vitesse;
  }

  get productionPopulationHeure(): number {
    const vitesse = Number.isFinite(this.vitesseJeu) && this.vitesseJeu > 0 ? this.vitesseJeu : 1;
    return (this.royaume?.populationDisponible ?? 0) * (this.royaume?.bonusCroissance ?? 0) * vitesse;
  }

  get populationTotale(): number {

    return this.royaume.populationDisponible
      + this.royaume.populationOuvriere
      + this.royaume.populationEnEntrainement
      + this.royaume.populationMilitaire;

  }

  get repartitionPopulation() {

    return [
      {
        nom: 'Disponibles',
        quantite: this.royaume.populationDisponible,
        couleur: '#53785c'
      },
      {
        nom: 'Ouvriers',
        quantite: this.royaume.populationOuvriere,
        couleur: '#b39553'
      },
      {
        nom: 'En entraînement',
        quantite: this.royaume.populationEnEntrainement,
        couleur: '#8998a0'
      },
      {
        nom: 'Militaires',
        quantite: this.royaume.populationMilitaire,
        couleur: '#a25343'
      }
    ];

  }

  getPourcentagePopulation(quantite: number): number {

    if (this.populationTotale === 0) {
      return 0;
    }

    return (quantite / this.populationTotale) * 100;

  }

  readonly nomsRessources: Record<Ressource, string> = {

    [Ressource.BOIS]: 'Bois',
    [Ressource.PIERRE]: 'Pierre',
    [Ressource.NOURRITURE]: 'Nourriture',
    [Ressource.OR]: 'Or'

  };

  readonly iconesRessources: Record<Ressource, string> = {

    [Ressource.BOIS]: 'img/resources/bois.svg',
    [Ressource.PIERRE]: 'img/resources/pierre.svg',
    [Ressource.NOURRITURE]: 'img/resources/nourriture.svg',
    [Ressource.OR]: 'img/resources/or.svg'

  };

}