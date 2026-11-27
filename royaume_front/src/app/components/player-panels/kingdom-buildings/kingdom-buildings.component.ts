import {
  Component,
  Input,
  inject,
  OnInit,
  OnDestroy,
  ChangeDetectorRef
} from '@angular/core';

import { DecimalPipe } from '@angular/common';

import { Royaume } from '../../../models/royaume.model';
import { Batiment, TypeBatiment } from '../../../models/batiment.model';
import {
  Ressource,
  QuantiteRessource
} from '../../../models/ressource.model';

import { ChantierConstructionResponse } from '../../../models/chantier.model';
import { ModeleBatiment } from '../../../models/modele-batiment.model';
import { AmeliorationBatiment } from '../../../models/amelioration-batiment.model';
import { ReparationBatiment } from '../../../models/reparation-batiment.model';

import {
  RoyaumeService,
  BatimentResponse
} from '../../../services/royaume/royaume-service.service';

@Component({
  selector: 'app-kingdom-buildings',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './kingdom-buildings.component.html',
  styleUrl: './kingdom-buildings.component.css'
})
export class KingdomBuildingsComponent implements OnInit, OnDestroy {

  @Input({ required: true }) royaume!: Royaume;

  private readonly royaumeService = inject(RoyaumeService);
  private readonly cdr = inject(ChangeDetectorRef);


  // ========================================
  // PROPRIÉTÉS
  // ========================================
  chantiers: ChantierConstructionResponse[] =[];
  catalogueOuvert: boolean = false;
  catalogueBatiments: ModeleBatiment[] = [];
  batimentSelectionne: ModeleBatiment | null = null;

  ouvriersAssignes: number = 0;
  constructionEnCours: boolean = false;
  erreurConstruction: string | null = null;

  filtreActif: string = 'TOUS';

  maintenant: number = Date.now();
  private intervalleChantiers?: ReturnType<typeof setInterval>;


  // ========================================
  // PROPRIÉTÉS D'AMÉLIORATION
  // ========================================

  batimentAmeliorationId: number | null = null;
  ameliorationSelectionnee: AmeliorationBatiment | null = null;

  ouvriersAmelioration: number = 0;
  ameliorationEnCours: boolean = false;
  erreurAmelioration: string | null = null;

  // ========================================
  // PROPRIÉTÉS DE RÉPARATION
  // ========================================

  batimentReparationId: number | null = null;

  reparationSelectionnee: ReparationBatiment | null = null;

  montantReparation: number = 0;

  ouvriersReparation: number = 0;

  reparationEnCours: boolean = false;

  erreurReparation: string | null = null;

  // ========================================
  // FILTRES
  // ========================================

  readonly filtres = [
    { nom: 'Tous', valeur: 'TOUS' },
    { nom: 'Production', valeur: TypeBatiment.PRODUCTION },
    { nom: 'Habitation', valeur: TypeBatiment.MAISON },
    { nom: 'Château', valeur: TypeBatiment.CHATEAU }
  ];


  // ========================================
  // ICÔNES ET NOMS
  // ========================================

  readonly iconesProduction: Record<Ressource, string> = {
    [Ressource.BOIS]: '/img/buildings/scierie.svg',
    [Ressource.PIERRE]: '/img/buildings/carriere.svg',
    [Ressource.NOURRITURE]: '/img/buildings/ferme.svg',
    [Ressource.OR]: '/img/buildings/mine.svg'
  };

  readonly nomsRessources: Record<Ressource, string> = {
    [Ressource.BOIS]: 'Bois',
    [Ressource.PIERRE]: 'Pierre',
    [Ressource.NOURRITURE]: 'Nourriture',
    [Ressource.OR]: 'Or'
  };

  readonly iconesRessources: Record<Ressource, string> = {
    [Ressource.BOIS]: '/img/resources/bois.svg',
    [Ressource.PIERRE]: '/img/resources/pierre.svg',
    [Ressource.NOURRITURE]: '/img/resources/nourriture.svg',
    [Ressource.OR]: '/img/resources/or.svg'
  };


  // ========================================
  // CYCLE DE VIE DU COMPOSANT
  // ========================================

  ngOnInit(): void {

    this.maintenant = Date.now();
    this.chargerChantiers();
    this.intervalleChantiers = setInterval(() => {

      this.maintenant = Date.now();

      this.cdr.detectChanges();

    }, 1000);

  }


  ngOnDestroy(): void {

    clearInterval(this.intervalleChantiers);

  }


  // ========================================
  // STATISTIQUES
  // ========================================

  get nombreBatiments(): number {

    return this.royaume.batiments.length;

  }


  get nombreChantiers(): number {

    return this.chantiersActifs.length;

  }

get chantiersActifs(): Batiment[] {

  const batimentIds = new Set(
    this.chantiers.map(chantier => chantier.batimentId)
  );

  return this.royaume.batiments.filter(
    batiment => batimentIds.has(batiment.id)
  );

}/*
  get chantiersActifs(): Batiment[] {
 
    return this.royaume.batiments.filter(batiment =>
      batiment.enConstruction 
    );

  }
*/

  // ========================================
  // FILTRAGE DES BÂTIMENTS
  // ========================================

  changerFiltre(filtre: string): void {

    this.fermerAmelioration();

    this.fermerReparation();

    this.filtreActif = filtre;

  }


  get batimentsFiltres(): Batiment[] {

    if (this.filtreActif === 'TOUS') {
      return this.royaume.batiments;
    }

    return this.royaume.batiments.filter(batiment =>
      batiment.type === this.filtreActif
    );

  }


  // ========================================
  // AFFICHAGE DES BÂTIMENTS
  // ========================================

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


  getPourcentageVie(batiment: Batiment): number {

    if (batiment.pointsDeVieMax <= 0) {
      return 0;
    }

    const pourcentage =
      (batiment.pointsDeVie / batiment.pointsDeVieMax) * 100;

    return Math.max(0, Math.min(pourcentage, 100));

  }


  // ========================================
  // CATALOGUE DE CONSTRUCTION
  // ========================================

  ouvrirCatalogue(): void {

    this.fermerAmelioration();

    this.fermerReparation();

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


  fermerCatalogue(): void {

    this.catalogueOuvert = false;
    this.batimentSelectionne = null;
    this.ouvriersAssignes = 0;
    this.erreurConstruction = null;

  }


  selectionnerBatiment(batiment: ModeleBatiment): void {

    this.erreurConstruction = null;

    if (this.batimentSelectionne?.id === batiment.id) {

      this.batimentSelectionne = null;
      this.ouvriersAssignes = 0;

    } else {

      this.batimentSelectionne = batiment;

      this.ouvriersAssignes = this.peutConstruire(batiment)
        ? batiment.ouvriersMinimum
        : 0;

    }

  }


  // ========================================
  // LIMITES DE CONSTRUCTION
  // ========================================

  getQuantiteBatiment(modele: ModeleBatiment): number {

    return this.royaume.batiments.filter(batiment =>

      batiment.type === modele.type &&
      batiment.typeRessource === modele.typeRessource

    ).length;

  }


  limiteConstructionAtteinte(modele: ModeleBatiment): boolean {

    if (modele.quantiteMax === null) {
      return false;
    }

    return this.getQuantiteBatiment(modele) >= modele.quantiteMax;

  }


  // ========================================
  // COÛTS ET PRÉREQUIS DE CONSTRUCTION
  // ========================================

  getStockRessource(ressource: Ressource): number {

    return this.royaume.ressources.find(stock =>
      stock.ressource === ressource
    )?.quantite ?? 0;

  }


  ressourceSuffisante(cout: QuantiteRessource): boolean {

    return this.getStockRessource(cout.ressource) >= cout.quantite;

  }


  ressourcesSuffisantes(modele: ModeleBatiment): boolean {

    return modele.coutConstruction.every(cout =>
      this.ressourceSuffisante(cout)
    );

  }


  niveauSuffisant(modele: ModeleBatiment): boolean {

    return this.royaume.niveau >= modele.niveauRoyaumeRequis;

  }


  ouvriersSuffisants(modele: ModeleBatiment): boolean {

    return this.royaume.populationDisponible >= modele.ouvriersMinimum;

  }


  peutConstruire(modele: ModeleBatiment): boolean {

    return !this.royaume.enRuine

      && !this.limiteConstructionAtteinte(modele)

      && this.ressourcesSuffisantes(modele)

      && this.niveauSuffisant(modele)

      && this.ouvriersSuffisants(modele);

  }


  // ========================================
  // SÉLECTION DES OUVRIERS
  // ========================================

  get ouvriersMaximum(): number {

    return this.royaume.populationDisponible;

  }


  modifierOuvriers(event: Event): void {

    const input = event.target as HTMLInputElement;

    const nombre = Number(input.value);

    if (
      !this.batimentSelectionne ||
      !this.peutConstruire(this.batimentSelectionne) ||
      !Number.isInteger(nombre)
    ) {
      return;
    }

    this.ouvriersAssignes = Math.max(

      this.batimentSelectionne.ouvriersMinimum,

      Math.min(nombre, this.ouvriersMaximum)

    );

  }


  get populationRestante(): number {

    return Math.max(
      0,
      this.royaume.populationDisponible - this.ouvriersAssignes
    );

  }


  // ========================================
  // ESTIMATION DE LA DURÉE
  // ========================================

  getDureeConstruction(modele: ModeleBatiment): number {

    if (this.ouvriersAssignes <= 0) {
      return modele.tempsConstructionSecondes;
    }

    return Math.ceil(
      modele.tempsConstructionSecondes / this.ouvriersAssignes
    );

  }


  formaterDuree(secondes: number): string {

    const heures = Math.floor(secondes / 3600);

    const minutes = Math.floor((secondes % 3600) / 60);

    const secondesRestantes = secondes % 60;

    const elements: string[] = [];

    if (heures > 0) {
      elements.push(`${heures} h`);
    }

    if (minutes > 0) {
      elements.push(`${minutes} min`);
    }

    if (secondesRestantes > 0 || elements.length === 0) {
      elements.push(`${secondesRestantes} s`);
    }

    return elements.join(' ');

  }


  // ========================================
  // DEMANDE DE CONSTRUCTION
  // ========================================
// ========================================
// DEMANDE DE CONSTRUCTION
// ========================================

peutLancerConstruction(modele: ModeleBatiment): boolean {

  return this.peutConstruire(modele)

    && !this.constructionEnCours

    && Number.isInteger(this.ouvriersAssignes)

    && this.ouvriersAssignes >= modele.ouvriersMinimum

    && this.ouvriersAssignes <= this.ouvriersMaximum;

}

lancerConstruction(modele: ModeleBatiment): void {

  if (!this.peutLancerConstruction(modele)) {
    return;
  }

  this.constructionEnCours = true;
  this.erreurConstruction = null;

  // ========================================
  // CONSTRUCTION D'UNE MAISON
  // ========================================

  if (modele.type === TypeBatiment.MAISON) {

    this.royaumeService.construireMaison({

      royaumeId: this.royaume.id,

      nom: modele.nom,

      ouvriersAssignes: this.ouvriersAssignes

    }).subscribe({

      next: () => this.constructionReussie(),

      error: erreur => this.constructionEchouee(erreur)

    });

    return;
  }

  // ========================================
  // CONSTRUCTION D'UN BÂTIMENT DE PRODUCTION
  // ========================================

  if (modele.type === TypeBatiment.PRODUCTION) {

    if (
      !modele.typeRessource ||
      modele.pointsDeVie == null ||
      modele.productionBaseHeure == null
    ) {

      this.constructionEnCours = false;

      this.erreurConstruction =
        'Les caractéristiques de ce bâtiment sont incomplètes.';

      return;
    }

    this.royaumeService.construireBatimentProduction({

      royaumeId: this.royaume.id,

      nom: modele.nom,

      pointsDeVie: modele.pointsDeVie,

      typeRessource: modele.typeRessource,

      productionBaseHeure: modele.productionBaseHeure,

      ouvriersAssignes: this.ouvriersAssignes

    }).subscribe({

      next: () => this.constructionReussie(),

      error: erreur => this.constructionEchouee(erreur)

    });

    return;
  }

  this.constructionEnCours = false;

  this.erreurConstruction =
    'Ce type de bâtiment ne peut pas être construit.';

}

// ========================================
// RÉSULTAT DE LA CONSTRUCTION
// ========================================

private constructionReussie(): void {

  this.constructionEnCours = false;

  this.filtreActif = 'TOUS';

  this.fermerCatalogue();

  this.rafraichirRoyaume();

}


private constructionEchouee(erreur: any): void {

  this.constructionEnCours = false;

  this.erreurConstruction = erreur.error?.message
    ?? erreur.message
    ?? 'Impossible de lancer la construction.';

}


  // ========================================
  // AFFICHAGE DYNAMIQUE DES CHANTIERS
  // ========================================

  getProgressionChantier(chantier: ChantierConstructionResponse ): number {

    const debut = new Date(chantier.dateDebut).getTime();

    const fin = new Date(chantier.dateFinPrevue).getTime();

    if (
      !Number.isFinite(debut) ||
      !Number.isFinite(fin) ||
      fin <= debut
    ) {
      return 0;
    }

    const progression =
      ((this.maintenant - debut) / (fin - debut)) * 100;

    return Math.max(0, Math.min(progression, 100));

  }


  getTempsRestantChantier(chantier: ChantierConstructionResponse ): number {

    const fin = new Date(chantier.dateFinPrevue).getTime();

    if (!Number.isFinite(fin)) {
      return 0;
    }

    const secondesRestantes = Math.ceil(
      (fin - this.maintenant) / 1000
    );

    return Math.max(0, secondesRestantes);

  }


  // ========================================
  // OUVERTURE DE L'AMÉLIORATION
  // ========================================

  ouvrirAmelioration(batiment: Batiment): void {

  if (this.batimentAmeliorationId === batiment.id) {

    this.fermerAmelioration();
    return;

  }

  this.fermerReparation();

  this.fermerAmelioration();

  this.batimentAmeliorationId = batiment.id;

  this.royaumeService.getAmeliorationBatiment(
    batiment.id
  ).subscribe({

    next: amelioration => {

      if (this.batimentAmeliorationId !== batiment.id) {
        return;
      }

      this.ameliorationSelectionnee = amelioration;

      this.ouvriersAmelioration =
        this.peutAmeliorer(batiment, amelioration)
          ? amelioration.ouvriersMinimum
          : 0;

    },

    error: erreur => {

      if (this.batimentAmeliorationId !== batiment.id) {
        return;
      }

      this.erreurAmelioration = erreur.message
        ?? 'Impossible de récupérer cette amélioration.';

    }

  });

}


  fermerAmelioration(): void {

    this.batimentAmeliorationId = null;

    this.ameliorationSelectionnee = null;

    this.ouvriersAmelioration = 0;

    this.erreurAmelioration = null;

  }


  // ========================================
  // PRÉREQUIS D'AMÉLIORATION
  // ========================================

  get niveauChateau(): number {

    const chateau = this.royaume.batiments.find(
      batiment => batiment.type === TypeBatiment.CHATEAU
    );

    return chateau?.niveauPalierDebloque ?? 0;

  }


  niveauChateauSuffisant(
    amelioration: AmeliorationBatiment
  ): boolean {

    return this.niveauChateau >=
      amelioration.niveauChateauRequis;

  }


  ressourcesAmeliorationSuffisantes(
    amelioration: AmeliorationBatiment
  ): boolean {

    return amelioration.coutAmelioration.every(
      cout => this.ressourceSuffisante(cout)
    );

  }


  ouvriersAmeliorationSuffisants(
    amelioration: AmeliorationBatiment
  ): boolean {

    return this.royaume.populationDisponible >=
      amelioration.ouvriersMinimum;

  }


  peutAmeliorer(
    batiment: Batiment,
    amelioration: AmeliorationBatiment
  ): boolean {

    return !this.royaume.enRuine

      && !batiment.enConstruction

      && batiment.type !== TypeBatiment.CHATEAU

      && batiment.niveau < 10

      && batiment.pointsDeVie > 0

      && amelioration.batimentId === batiment.id

      && amelioration.niveauActuel === batiment.niveau

      && this.niveauChateauSuffisant(amelioration)

      && this.ressourcesAmeliorationSuffisantes(amelioration)

      && this.ouvriersAmeliorationSuffisants(amelioration);

  }


  // ========================================
  // OUVRIERS D'AMÉLIORATION
  // ========================================

  modifierOuvriersAmelioration(event: Event): void {

    const input = event.target as HTMLInputElement;

    const nombre = Number(input.value);

    if (
      !this.ameliorationSelectionnee ||
      !Number.isInteger(nombre) ||
      this.ouvriersMaximum < this.ameliorationSelectionnee.ouvriersMinimum
    ) {
      return;
    }

    this.ouvriersAmelioration = Math.max(

      this.ameliorationSelectionnee.ouvriersMinimum,

      Math.min(nombre, this.ouvriersMaximum)

    );

  }


  get populationRestanteAmelioration(): number {

    return Math.max(

      0,

      this.royaume.populationDisponible -
      this.ouvriersAmelioration

    );

  }


  getDureeAmelioration(
    amelioration: AmeliorationBatiment
  ): number {

    if (this.ouvriersAmelioration <= 0) {

      return amelioration.tempsConstructionSecondes;

    }

    return Math.ceil(

      amelioration.tempsConstructionSecondes /
      this.ouvriersAmelioration

    );

  }


  // ========================================
  // DEMANDE D'AMÉLIORATION
  // ========================================

  peutLancerAmelioration(
    batiment: Batiment,
    amelioration: AmeliorationBatiment
  ): boolean {

    return this.peutAmeliorer(batiment, amelioration)

      && !this.ameliorationEnCours

      && Number.isInteger(this.ouvriersAmelioration)

      && this.ouvriersAmelioration >=
      amelioration.ouvriersMinimum

      && this.ouvriersAmelioration <=
      this.ouvriersMaximum;

  }

// ========================================
// DEMANDE D'AMÉLIORATION
// ========================================

lancerAmelioration(batiment: Batiment): void {

  const amelioration = this.ameliorationSelectionnee;

  if (
    !amelioration ||
    !this.peutLancerAmelioration(batiment, amelioration)
  ) {
    return;
  }

  this.ameliorationEnCours = true;

  this.erreurAmelioration = null;

  this.royaumeService.ameliorerBatiment(

    batiment.id,

    this.ouvriersAmelioration

  ).subscribe({

    next: () => {

      this.ameliorationEnCours = false;

      this.fermerAmelioration();

      this.rafraichirRoyaume();

    },

    error: erreur => {

      this.ameliorationEnCours = false;

      this.erreurAmelioration = erreur.error?.message
        ?? erreur.message
        ?? 'Impossible de lancer cette amélioration.';

    }

  });

}

  // ========================================
  // OUVERTURE DE LA RÉPARATION
  // ========================================

  ouvrirReparation(batiment: Batiment): void {

    if (this.batimentReparationId === batiment.id) {

      this.fermerReparation();
      return;

    }

    this.fermerAmelioration();
    this.fermerReparation();

    this.batimentReparationId = batiment.id;

    this.royaumeService.getReparationBatiment(
      batiment.id
    ).subscribe({

      next: reparation => {

        if (this.batimentReparationId !== batiment.id) {
          return;
        }

        this.reparationSelectionnee = reparation;

        this.montantReparation = this.pointsDeVieManquants;

        this.ouvriersReparation =
          this.ouvriersReparationSuffisants(reparation)
            ? reparation.ouvriersMinimum
            : 0;

      },

      error: erreur => {

        if (this.batimentReparationId !== batiment.id) {
          return;
        }

        this.erreurReparation = erreur.message
          ?? 'Impossible de récupérer cette réparation.';

      }

    });

  }


  fermerReparation(): void {

    this.batimentReparationId = null;

    this.reparationSelectionnee = null;

    this.montantReparation = 0;

    this.ouvriersReparation = 0;

    this.erreurReparation = null;

  }


  // ========================================
  // POINTS DE VIE
  // ========================================

  get pointsDeVieManquants(): number {

    if (!this.reparationSelectionnee) {
      return 0;
    }

    return Math.max(

      0,

      this.reparationSelectionnee.pointsDeVieMax -
      this.reparationSelectionnee.pointsDeVieActuels

    );

  }


  get pointsDeVieApresReparation(): number {

    if (!this.reparationSelectionnee) {
      return 0;
    }

    return Math.min(

      this.reparationSelectionnee.pointsDeVieMax,

      this.reparationSelectionnee.pointsDeVieActuels +
      this.montantReparation

    );

  }


  get pourcentageVieApresReparation(): number {

    if (
      !this.reparationSelectionnee ||
      this.reparationSelectionnee.pointsDeVieMax <= 0
    ) {
      return 0;
    }

    return (

      this.pointsDeVieApresReparation /
      this.reparationSelectionnee.pointsDeVieMax

    ) * 100;

  }


  modifierMontantReparation(event: Event): void {

    const input = event.target as HTMLInputElement;

    const montant = Number(input.value);

    if (
      !this.reparationSelectionnee ||
      !Number.isInteger(montant) ||
      this.pointsDeVieManquants <= 0
    ) {
      return;
    }

    this.montantReparation = Math.max(

      1,

      Math.min(montant, this.pointsDeVieManquants)

    );

    this.erreurReparation = null;

  }


  // ========================================
  // PRÉREQUIS DE RÉPARATION
  // ========================================

  ouvriersReparationSuffisants(
    reparation: ReparationBatiment
  ): boolean {

    return this.royaume.populationDisponible >=
      reparation.ouvriersMinimum;

  }


  peutReparer(
    batiment: Batiment,
    reparation: ReparationBatiment
  ): boolean {

    return !batiment.enConstruction

      && batiment.id === reparation.batimentId

      && batiment.pointsDeVie < batiment.pointsDeVieMax

      && this.pointsDeVieManquants > 0

      && this.ouvriersReparationSuffisants(reparation);

  }


  // ========================================
  // AFFECTATION DES OUVRIERS
  // ========================================

  modifierOuvriersReparation(event: Event): void {

    const input = event.target as HTMLInputElement;

    const nombre = Number(input.value);

    if (
      !this.reparationSelectionnee ||
      !Number.isInteger(nombre) ||
      this.ouvriersMaximum <
      this.reparationSelectionnee.ouvriersMinimum
    ) {
      return;
    }

    this.ouvriersReparation = Math.max(

      this.reparationSelectionnee.ouvriersMinimum,

      Math.min(nombre, this.ouvriersMaximum)

    );

    this.erreurReparation = null;

  }


  get populationRestanteReparation(): number {

    return Math.max(

      0,

      this.royaume.populationDisponible -
      this.ouvriersReparation

    );

  }


  // ========================================
  // ESTIMATION DE LA DURÉE
  // ========================================

  getDureeReparation(
    reparation: ReparationBatiment
  ): number {

    if (this.ouvriersReparation <= 0) {

      return reparation.tempsReparationSecondes;

    }

    return Math.ceil(

      reparation.tempsReparationSecondes /
      this.ouvriersReparation

    );

  }


  // ========================================
  // DEMANDE DE RÉPARATION
  // ========================================

  peutLancerReparation(
    batiment: Batiment,
    reparation: ReparationBatiment
  ): boolean {

    return this.peutReparer(batiment, reparation)

      && !this.reparationEnCours

      && Number.isInteger(this.montantReparation)

      && this.montantReparation > 0

      && this.montantReparation <= this.pointsDeVieManquants

      && Number.isInteger(this.ouvriersReparation)

      && this.ouvriersReparation >=
      reparation.ouvriersMinimum

      && this.ouvriersReparation <=
      this.ouvriersMaximum;

  }

// ========================================
// DEMANDE DE RÉPARATION
// ========================================

lancerReparation(batiment: Batiment): void {

  const reparation = this.reparationSelectionnee;

  if (
    !reparation ||
    !this.peutLancerReparation(batiment, reparation)
  ) {
    return;
  }

  this.reparationEnCours = true;

  this.erreurReparation = null;

  this.royaumeService.reparerBatiment(

    batiment.id,

    this.montantReparation,

    this.ouvriersReparation

  ).subscribe({

    next: () => {

      this.reparationEnCours = false;

      this.fermerReparation();

      this.rafraichirRoyaume();

    },

    error: erreur => {

      this.reparationEnCours = false;

      this.erreurReparation = erreur.error?.message
        ?? erreur.message
        ?? 'Impossible de lancer cette réparation.';

    }

  });

}


  // ========================================
  // TYPE DE CHANTIER
  // ========================================


  // ========================================
// CHARGEMENT DES CHANTIERS
// ========================================

chargerChantiers(): void {

  this.royaumeService.getChantiersRoyaume(
    this.royaume.id
  ).subscribe({

    next: chantiers => {

      this.chantiers = chantiers;

      this.cdr.detectChanges();

    },

    error: erreur => {

      console.error(
        'Erreur lors du chargement des chantiers',
        erreur
      );

    }

  });

}

// ========================================
// RAFRAÎCHISSEMENT DU ROYAUME
// ========================================

private rafraichirRoyaume(): void {

  this.royaumeService.getMonRoyaume(
    this.royaume.id
  ).subscribe({

    next: royaume => {

      this.royaume = royaume;

      this.chargerChantiers();

      this.cdr.detectChanges();

    },

    error: erreur => {

      console.error(
        'Erreur lors du rafraîchissement du royaume',
        erreur
      );

    }

  });

}

ajouterOuvriersChantier(
  chantier: ChantierConstructionResponse,
  nombre: number
): void {

  this.royaumeService.ajouterOuvriers(
    chantier.id,
    nombre
  ).subscribe({

    next: chantierActualise => {

      this.chantiers = this.chantiers.map(c =>
        c.id === chantierActualise.id
          ? chantierActualise
          : c
      );

      this.rafraichirRoyaume();

    },

    error: erreur => {

      console.error(
        'Erreur lors de l’ajout des ouvriers',
        erreur
      );

    }

  });

}

retirerOuvriersChantier(
  chantier: ChantierConstructionResponse,
  nombre: number
): void {

  this.royaumeService.retirerOuvriers(
    chantier.id,
    nombre
  ).subscribe({

    next: chantierActualise => {

      this.chantiers = this.chantiers.map(c =>
        c.id === chantierActualise.id
          ? chantierActualise
          : c
      );

      this.rafraichirRoyaume();

    },

    error: erreur => {

      console.error(
        'Erreur lors du retrait des ouvriers',
        erreur
      );

    }

  });

}
getTypeChantier(
  batiment: Batiment,
  chantier: ChantierConstructionResponse
): string {

  if (chantier.amelioration) {
    return 'Amélioration';
  }

  if ((batiment.montantReparationEnCours ?? 0) > 0) {
    return 'Réparation';
  }

  return 'Construction';
}

getNomBatimentChantier(
  chantier: ChantierConstructionResponse
): string {

  const batiment = this.royaume.batiments.find(
    b => b.id === chantier.batimentId
  );

  return batiment?.nom ?? `Bâtiment #${chantier.batimentId}`;

}


getTypeChantierParId(
  chantier: ChantierConstructionResponse
): string {

  const batiment = this.royaume.batiments.find(
    b => b.id === chantier.batimentId
  );

  if (batiment?.montantReparationEnCours > 0) {
    return 'Réparation';
  }

  return 'Construction';

}

}