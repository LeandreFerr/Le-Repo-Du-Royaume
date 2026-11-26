import { Component, Input } from '@angular/core';
import { DecimalPipe } from '@angular/common';

import { Royaume } from '../../../models/royaume.model';
import { PalierNiveau } from '../../../models/palier-niveau.model';
import { Ressource } from '../../../models/ressource.model';

@Component({
  selector: 'app-kingdom-development',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './kingdom-development.component.html',
  styleUrl: './kingdom-development.component.css',
})
export class KingdomDevelopmentComponent {

  @Input({ required: true }) royaume!: Royaume;

  @Input({ required: true }) paliers!: PalierNiveau[];

  get populationTotale(): number {

    return this.royaume.populationDisponible
      + this.royaume.populationOuvriere
      + this.royaume.populationEnEntrainement
      + this.royaume.populationMilitaire;

  }

  get prochainPalier(): PalierNiveau | undefined {

    return this.paliers.find(
      palier => palier.niveau === this.royaume.niveau + 1
    );

  }

  getQuantiteRessource(ressource: Ressource): number {

    return this.royaume.ressources.find(
      stock => stock.ressource === ressource
    )?.quantite ?? 0;

  }

  // Libellés des ressources

  readonly nomsRessources: Record<Ressource, string> = {
    [Ressource.BOIS]: 'Bois',
    [Ressource.PIERRE]: 'Pierre',
    [Ressource.NOURRITURE]: 'Nourriture',
    [Ressource.OR]: 'Or'
  };


  // Icônes des ressources

  readonly iconesRessources: Record<Ressource, string> = {
    [Ressource.BOIS]: '/img/resources/bois.svg',
    [Ressource.PIERRE]: '/img/resources/pierre.svg',
    [Ressource.NOURRITURE]: '/img/resources/nourriture.svg',
    [Ressource.OR]: '/img/resources/or.svg'
  };

  // Conditions nécessaires au prochain niveau

  get conditionsProgression() {

    const palier = this.prochainPalier;

    if (!palier) {
      return [];
    }

    const conditions = palier.prerequis.map(prerequis => ({

      nom: this.nomsRessources[prerequis.ressource],

      icone: this.iconesRessources[prerequis.ressource],

      actuelle: this.getQuantiteRessource(prerequis.ressource),

      requise: prerequis.quantite

    }));

    // Ajout de la condition de population

    conditions.push({
      nom: 'Population',
      icone: '/img/population.svg',
      actuelle: this.populationTotale,
      requise: palier.populationRequise
    });

    return conditions;

  }


  // Pourcentage d'une condition

  getPourcentage(actuelle: number, requise: number): number {

    if (requise <= 0) {
      return 100;
    }

    return Math.min((actuelle / requise) * 100, 100);

  }


  // Progression globale

  get progressionGlobale(): number {

    const conditions = this.conditionsProgression;

    if (conditions.length === 0) {
      return 0;
    }

    const total = conditions.reduce(
      (somme, condition) => somme + this.getPourcentage(
        condition.actuelle,
        condition.requise
      ),
      0
    );

    return total / conditions.length;

  }


  // Nombre de conditions restantes

  get conditionsRestantes(): number {

    return this.conditionsProgression.filter(
      condition => condition.actuelle < condition.requise
    ).length;

  }


  // Disponibilité de l'amélioration

  get peutAmeliorer(): boolean {

    return !this.royaume.enRuine
      && this.royaume.niveau < 10
      && !!this.prochainPalier
      && this.conditionsRestantes === 0;

  }

}