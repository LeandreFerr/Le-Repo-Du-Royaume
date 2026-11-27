
import {
  ChangeDetectorRef,
  Component,
  inject,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from '@angular/core';
import { EventEmitter, Output } from '@angular/core';
import { DecimalPipe } from '@angular/common';

import { Ressource } from '../../../models/ressource.model';
import { Royaume } from '../../../models/royaume.model';

import {
  AmeliorerRoyaumeService
} from '../../../services/royaume/ameliorer-royaume.service';

import {
  AmeliorerRoyaumeResponse
} from '../../../models/palier-niveau.model';


interface ConditionProgression {
  nom: string;
  icone: string;
  actuelle: number;
  requise: number;
}

@Component({
  selector: 'app-kingdom-development',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './kingdom-development.component.html',
  styleUrl: './kingdom-development.component.css',
})
export class KingdomDevelopmentComponent implements OnInit, OnChanges {
  @Output() royaumeModifie = new EventEmitter<void>();
  @Input({ required: true }) royaume!: Royaume;

  private readonly ameliorerRoyaumeService =
    inject(AmeliorerRoyaumeService);

  private readonly cdr = inject(ChangeDetectorRef);

  prochainPalier: AmeliorerRoyaumeResponse | null = null;

  chargementAmelioration = false;
  ameliorationEnCours = false;

  messageAmelioration = '';
  erreurAmelioration = '';

  // ========================================
  // INITIALISATION
  // ========================================

  ngOnInit(): void {
    this.chargerProchainPalier();
  }

  ngOnChanges(changes: SimpleChanges): void {

    if (changes['royaume'] && this.royaume?.id) {
      this.chargerProchainPalier();
    }

  }

  // ========================================
  // POPULATION
  // ========================================

  get populationTotale(): number {

    if (!this.royaume) {
      return 0;
    }

    return (
      (this.royaume.populationDisponible ?? 0) +
      (this.royaume.populationOuvriere ?? 0) +
      (this.royaume.populationEnEntrainement ?? 0) +
      (this.royaume.populationMilitaire ?? 0)
    );

  }

  // ========================================
  // PROCHAIN PALIER
  // ========================================

  chargerProchainPalier(): void {

    if (!this.royaume?.id || this.royaume.niveau >= 10) {
      this.prochainPalier = null;
      this.chargementAmelioration = false;
      return;
    }

    this.chargementAmelioration = true;
    this.erreurAmelioration = '';

    this.ameliorerRoyaumeService
      .obtenirProchainPalier(this.royaume.id)
      .subscribe({

        next: (palier) => {

          this.prochainPalier = palier;
          this.chargementAmelioration = false;

          this.cdr.detectChanges();

        },

        error: (erreur) => {

          this.prochainPalier = null;
          this.chargementAmelioration = false;

          this.erreurAmelioration =
            erreur.error?.message ||
            erreur.error ||
            erreur.message ||
            'Impossible de récupérer le prochain palier.';

          this.cdr.detectChanges();

        }

      });

  }

  // ========================================
  // RESSOURCES
  // ========================================

  getQuantiteRessource(ressource: Ressource): number {

    if (!this.royaume?.ressources) {
      return 0;
    }

    return this.royaume.ressources.find(
      stock => stock.ressource === ressource
    )?.quantite ?? 0;

  }

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
  // CONDITIONS DE PROGRESSION
  // ========================================

  get conditionsProgression(): ConditionProgression[] {

    const palier = this.prochainPalier?.prochainPalier;

    if (!palier) {
      return [];
    }

    const conditions: ConditionProgression[] =
      palier.prerequis.map(prerequis => {

        const ressource = prerequis.ressource as Ressource;

        return {

          nom: this.nomsRessources[ressource],
          icone: this.iconesRessources[ressource],
          actuelle: this.getQuantiteRessource(ressource),
          requise: prerequis.quantite

        };

      });

    conditions.push({

      nom: 'Population',
      icone: '/img/population.svg',
      actuelle: this.populationTotale,
      requise: this.prochainPalier!.populationRequise

    });

    return conditions;

  }

  // ========================================
  // POURCENTAGE
  // ========================================

  getPourcentage(actuelle: number, requise: number): number {

    if (requise <= 0) {
      return 100;
    }

    return Math.min((actuelle / requise) * 100, 100);

  }

  // ========================================
  // PROGRESSION GLOBALE
  // ========================================

  get progressionGlobale(): number {

    const conditions = this.conditionsProgression;

    if (conditions.length === 0) {
      return 0;
    }

    const total = conditions.reduce(

      (somme, condition) =>
        somme + this.getPourcentage(
          condition.actuelle,
          condition.requise
        ),

      0

    );

    return total / conditions.length;

  }

  // ========================================
  // CONDITIONS RESTANTES
  // ========================================

  get conditionsRestantes(): number {

    if (!this.prochainPalier) {
      return 0;
    }

    const ressourcesInsuffisantes =
      this.prochainPalier.ressources.filter(
        ressource => !ressource.suffisante
      ).length;

    const populationInsuffisante =
      this.prochainPalier.populationSuffisante ? 0 : 1;

    return ressourcesInsuffisantes + populationInsuffisante;

  }

  // ========================================
  // AUTORISATION D'AMÉLIORATION
  // ========================================

  get peutAmeliorer(): boolean {

    return !this.royaume?.enRuine
      && !this.ameliorationEnCours
      && !this.chargementAmelioration
      && this.royaume?.niveau < 10
      && !!this.prochainPalier
      && this.prochainPalier.ameliorationPossible
      && this.conditionsRestantes === 0;

  }

  // ========================================
  // AMÉLIORATION DU ROYAUME
  // ========================================

ameliorerRoyaume(): void {

  if (!this.peutAmeliorer) {
    return;
  }

  this.ameliorationEnCours = true;

  this.messageAmelioration = '';
  this.erreurAmelioration = '';

  this.ameliorerRoyaumeService
    .ameliorerRoyaume(this.royaume.id)
    .subscribe({

      next: (message: string) => {

        this.ameliorationEnCours = false;

        this.messageAmelioration =
          message || 'Votre royaume a été amélioré.';

        // Demande au parent de récupérer le royaume actualisé
        this.royaumeModifie.emit();

        this.cdr.detectChanges();

      },

      error: (erreur) => {

        this.ameliorationEnCours = false;

        this.erreurAmelioration =
          erreur.error?.message ||
          erreur.error ||
          erreur.message ||
          'Impossible d’améliorer votre royaume.';

        this.cdr.detectChanges();

      }

    });

}



}
