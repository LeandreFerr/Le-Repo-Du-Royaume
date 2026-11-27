
import { DecimalPipe } from '@angular/common';
import {
  Component,
  ElementRef,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { Ressource } from '../../../models/ressource.model';
import { MessageResponse } from '../../../models/message.model';

import {
  PropositionEchangeService
} from '../../../services/proposition_echange/proposition-echange.service';

import {
  MessageService
} from '../../../services/message/message.service';

// ========================================
// INTERFACES
// ========================================

interface RessourceEchange {
  nom: Ressource;
  quantite: number;
}
interface PropositionEchange {
  id: number;
  expediteur: string;
  dateReception: string;
  ressourcesOffertes: RessourceEchange[];
  ressourcesDemandees: RessourceEchange[];
  statut: 'EN_ATTENTE' | 'ACCEPTE' | 'REFUSE' | 'ANNULE';
}

interface MessageReception {
  id: number;
  expediteur: string;
  contenu: string;
  dateReception: string;
  lu: boolean;
}

// ========================================
// COMPOSANT
// ========================================

@Component({
  selector: 'app-evenements-reception',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './evenements-reception.component.html',
  styleUrl: './evenements-reception.component.css',
})
export class EvenementsReception implements OnChanges {

  @Input({ required: true })
  destinataireId!: number;

  private readonly messageService = inject(MessageService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly propositionService = inject(PropositionEchangeService);

  @ViewChild('messagesSection')
  messagesSection!: ElementRef<HTMLElement>;

  // ========================================
  // DONNÉES
  // ========================================

  messages: MessageReception[] = [];
  propositions: PropositionEchange[] = [];

  chargementMessages = false;
  erreurMessages: string | null = null;

  chargementPropositions = false;
  erreurPropositions: string | null = null;

  // ========================================
  // RESSOURCES ET ICÔNES
  // ========================================

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

  // ========================================
  // INITIALISATION
  // ========================================

  ngOnChanges(changes: SimpleChanges): void {

    if (changes['destinataireId'] && this.destinataireId > 0) {
      this.chargerMessages();
      this.chargerPropositions();
    }

  }

  // ========================================
  // FORMATAGE DATE
  // ========================================

  private formaterDate(date: string): string {

    return new Date(date).toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

  }

  // ========================================
  // CHARGER LES MESSAGES
  // ========================================

  chargerMessages(): void {

    if (!this.destinataireId || this.destinataireId <= 0) {
      console.error('Identifiant du destinataire invalide');
      return;
    }

    this.chargementMessages = true;
    this.erreurMessages = null;

    this.messageService.recevoirMessages(this.destinataireId)
      .subscribe({

        next: (response: MessageResponse[]) => {

          console.log('Messages reçus depuis Spring Boot :', response);

          this.messages = response.map(message => ({
            id: message.id,
            expediteur: message.expediteurPseudo,
            contenu: message.contenu,
            dateReception: this.formaterDate(message.dateEnvoi),
            lu: message.lu
          }));

          this.chargementMessages = false;

          console.log('Messages affichés :', this.messages);

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error('Erreur chargement messages :', error);

          this.erreurMessages =
            'Impossible de récupérer les messages.';

          this.chargementMessages = false;

        }

      });

  }

  // ========================================
  // CHARGER LES PROPOSITIONS
  // ========================================
chargerPropositions(): void {

  if (!this.destinataireId || this.destinataireId <= 0) {
    return;
  }

  this.chargementPropositions = true;
  this.erreurPropositions = null;

  this.propositionService
    .recupererPropositionsDestinataire(this.destinataireId)
    .subscribe({

      next: (response) => {
        console.log('Propositions reçues :', response);

response.forEach(p => {
  console.log('Ressource offerte :', p.offerte.ressource);
  console.log('Ressource demandée :', p.demandee.ressource);
});

console.log('Icônes disponibles :', this.iconesRessources);
        this.propositions = response.map(p => ({

          id: p.id,
          expediteur: p.expediteurNom,
          dateReception: this.formaterDate(p.dateCreation),

          ressourcesOffertes: [{
            nom: p.offerte.ressource as Ressource,
            quantite: p.offerte.quantite
          }],

          ressourcesDemandees: [{
            nom: p.demandee.ressource as Ressource,
            quantite: p.demandee.quantite
          }],

          statut: p.statut

        }));

        this.chargementPropositions = false;
        this.cdr.detectChanges();

      },

      error: (error) => {

        console.error('Erreur chargement propositions :', error);

        this.erreurPropositions =
          'Impossible de récupérer les propositions d’échange.';

        this.chargementPropositions = false;

      }

    });
}

  // ========================================
  // STATISTIQUES
  // ========================================

  get propositionsEnAttente(): number {

    return this.propositions.filter(
      p => p.statut === 'EN_ATTENTE'
    ).length;

  }

  get messagesNonLus(): number {

    return this.messages.filter(
      m => !m.lu
    ).length;

  }

  // ========================================
  // ACCEPTER UNE PROPOSITION
  // ========================================

  accepterProposition(id: number): void {

    const proposition = this.propositions.find(
      p => p.id === id
    );

    if (!proposition || proposition.statut !== 'EN_ATTENTE') {
      return;
    }

    this.propositionService.accepterProposition(id).subscribe({

      next: (response) => {

        proposition.statut = response.statut;

        this.cdr.detectChanges();

        console.log('Proposition acceptée en BDD :', response);

      },

      error: (error) => {

        console.error('Erreur acceptation proposition :', error);

        this.erreurPropositions =
          error.error?.message ?? 'Impossible d’accepter cet échange.';

      }

    });

  }

  // ========================================
  // REFUSER UNE PROPOSITION
  // ========================================

  refuserProposition(id: number): void {

    const proposition = this.propositions.find(
      p => p.id === id
    );

    if (!proposition || proposition.statut !== 'EN_ATTENTE') {
      return;
    }

    this.propositionService.refuserProposition(id).subscribe({

      next: (response) => {

        proposition.statut = response.statut;

        this.cdr.detectChanges();

        console.log('Proposition refusée en BDD :', response);

      },

      error: (error) => {

        console.error('Erreur refus proposition :', error);

        this.erreurPropositions =
          error.error?.message ?? 'Impossible de refuser cet échange.';

      }

    });

  }

  // ========================================
  // MARQUER UN MESSAGE COMME LU
  // ========================================

  marquerCommeLu(id: number): void {

    const message = this.messages.find(m => m.id === id);

    if (!message || message.lu) {
      return;
    }

    this.messageService.marquerCommeLu(id)
      .subscribe({

        next: () => {

          message.lu = true;

          console.log('Message marqué comme lu en BDD :', id);

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(
            'Erreur lors du marquage comme lu :',
            error
          );

        }

      });

  }

  // ========================================
  // NAVIGATION VERS LES MESSAGES
  // ========================================

  allerAuxMessages(): void {

    this.messagesSection?.nativeElement.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    });

  }

}
