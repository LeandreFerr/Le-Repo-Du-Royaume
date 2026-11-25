import { Component, Input, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';

import { Royaume } from '../../../models/royaume.model';
import { Batiment, TypeBatiment } from '../../../models/batiment.model';
import { Ressource } from '../../../models/ressource.model';
import { Chantier } from '../../../models/chantier.model';
import { RoyaumeService } from '../../../services/royaume.service';
import { ModeleBatiment } from '../../../models/modele-batiment.model';

@Component({
  selector: 'app-kingdom-buildings',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './kingdom-buildings.component.html',
  styleUrl: './kingdom-buildings.component.css',
})
export class KingdomBuildingsComponent {

  @Input({ required: true }) royaume!: Royaume;

  // Service

  private readonly royaumeService = inject(RoyaumeService);


  // Catalogue de construction

  catalogueOuvert: boolean = false;

  catalogueBatiments: ModeleBatiment[] = [];

  batimentSelectionne: ModeleBatiment | null = null;


  // Ouverture du catalogue

  ouvrirCatalogue(): void {

    this.catalogueOuvert = true;

    this.royaumeService.getCatalogueBatiments().subscribe({

      next: catalogue => {
        this.catalogueBatiments = catalogue;
      },

      error: erreur => {
        console.error(
          'Erreur lors du chargement du catalogue',
          erreur
        );
      }

    });

  }


  // Fermeture du catalogue

  fermerCatalogue(): void {

    this.catalogueOuvert = false;

    this.batimentSelectionne = null;

  }


  // Sélection d'un bâtiment

  selectionnerBatiment(batiment: ModeleBatiment): void {

    if (this.batimentSelectionne?.id === batiment.id) {

      this.batimentSelectionne = null;

    } else {

      this.batimentSelectionne = batiment;

    }

  }

  // Filtres

  readonly filtres = [
    { nom: 'Tous', valeur: 'TOUS' },
    { nom: 'Production', valeur: TypeBatiment.PRODUCTION },
    { nom: 'Habitation', valeur: TypeBatiment.MAISON },
    { nom: 'Château', valeur: TypeBatiment.CHATEAU }
  ];

  filtreActif: string = 'TOUS';


  changerFiltre(filtre: string): void {

    this.filtreActif = filtre;

  }


  // Nombre total de bâtiments

  get nombreBatiments(): number {

    return this.royaume.batiments.length;

  }


  // Bâtiments filtrés

  get batimentsFiltres(): Batiment[] {

    if (this.filtreActif === 'TOUS') {
      return this.royaume.batiments;
    }

    return this.royaume.batiments.filter(
      batiment => batiment.type === this.filtreActif
    );

  }


  // Chantiers actifs

  get chantiersActifs(): Batiment[] {

    return this.royaume.batiments.filter(
      batiment => batiment.enConstruction
        && batiment.chantier !== null
    );

  }


  get nombreChantiers(): number {

    return this.chantiersActifs.length;

  }


  // Points de vie

  getPourcentageVie(batiment: Batiment): number {

    if (batiment.pointsDeVieMax <= 0) {
      return 0;
    }

    return Math.max(0, Math.min(
      (batiment.pointsDeVie / batiment.pointsDeVieMax) * 100,
      100
    ));

  }


  // Etat du bâtiment

  getEtatBatiment(batiment: Batiment): string {

    if (batiment.enConstruction) {
      return 'En travaux';
    }

    if (batiment.pointsDeVie <= 0) {
      return 'Détruit';
    }

    if (batiment.pointsDeVie < batiment.pointsDeVieMax) {
      return 'Endommagé';
    }

    return 'Opérationnel';

  }


  // Icônes des ressources

  readonly iconesProduction: Record<Ressource, string> = {

    [Ressource.BOIS]: '/img/buildings/scierie.svg',
    [Ressource.PIERRE]: '/img/buildings/carriere.svg',
    [Ressource.NOURRITURE]: '/img/buildings/ferme.svg',
    [Ressource.OR]: '/img/buildings/mine.svg'

  };

  // Icône du bâtiment

  getIconeBatiment(batiment: Batiment): string {

    switch (batiment.type) {

      case TypeBatiment.CHATEAU:
        return '/img/buildings/chateau.svg';

      case TypeBatiment.MAISON:
        return '/img/buildings/maison.svg';

      case TypeBatiment.PRODUCTION:
        return batiment.typeRessource
          ? this.iconesProduction[batiment.typeRessource]
          : '';

      default:
        return '';

    }

  }

  getDescriptionBatiment(batiment: Batiment): string {

    switch (batiment.type) {

      case TypeBatiment.CHATEAU:
        return 'Siège de la couronne';

      case TypeBatiment.MAISON:
        return 'Croissance démographique';

      case TypeBatiment.PRODUCTION:
        return 'Bâtiment de production';

      default:
        return 'Infrastructure du domaine';

    }

  }

  getProgressionChantier(chantier: Chantier): number {

    const debut = new Date(chantier.dateDebut).getTime();

    const fin = new Date(chantier.dateFinPrevue).getTime();

    const maintenant = Date.now();

    if (!Number.isFinite(debut)
      || !Number.isFinite(fin)
      || fin <= debut) {

      return 0;

    }

    const progression =
      ((maintenant - debut) / (fin - debut)) * 100;

    return Math.max(0, Math.min(progression, 100));

  }

}