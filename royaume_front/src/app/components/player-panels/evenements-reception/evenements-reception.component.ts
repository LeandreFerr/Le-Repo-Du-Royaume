import { Component } from '@angular/core';
import { DecimalPipe } from '@angular/common';

interface RessourceEchange {
  nom: string;
  quantite: number;
  icone: string;
}

interface PropositionEchange {
  id: number;
  expediteur: string;
  dateReception: string;
  ressourcesOffertes: RessourceEchange[];
  ressourcesDemandees: RessourceEchange[];
  statut: 'EN_ATTENTE' | 'ACCEPTEE' | 'REFUSEE';
}

interface MessageReception {
  id: number;
  expediteur: string;
  contenu: string;
  dateReception: string;
  lu: boolean;
}
@Component({
  selector: 'app-evenements-reception',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './evenements-reception.component.html',
  styleUrl: './evenements-reception.component.css',
})
export class EvenementsReception {

  // ========================================
  // PROPOSITIONS D'ECHANGE FICTIVES
  // ========================================

  propositions: PropositionEchange[] = [

    {
      id: 1,
      expediteur: 'Royaume de Valoria',
      dateReception: '27/11/2026 à 13h45',

      ressourcesOffertes: [
        {
          nom: 'Bois',
          quantite: 250,
          icone: 'img/resources/bois.svg'
        },
        {
          nom: 'Or',
          quantite: 50,
          icone: 'img/resources/or.svg'
        }
      ],

      ressourcesDemandees: [
        {
          nom: 'Pierre',
          quantite: 150,
          icone: 'img/resources/pierre.svg'
        }
      ],

      statut: 'EN_ATTENTE'
    },

    {
      id: 2,
      expediteur: 'Royaume d’Astoria',
      dateReception: '27/11/2026 à 14h20',

      ressourcesOffertes: [
        {
          nom: 'Nourriture',
          quantite: 400,
          icone: 'img/resources/nourriture.svg'
        }
      ],

      ressourcesDemandees: [
        {
          nom: 'Bois',
          quantite: 200,
          icone: 'img/resources/bois.svg'
        },
        {
          nom: 'Or',
          quantite: 30,
          icone: 'img/resources/or.svg'
        }
      ],

      statut: 'EN_ATTENTE'
    }

  ];


  // ========================================
  // MESSAGES RECUS FICTIFS
  // ========================================

  messages: MessageReception[] = [

    {
      id: 1,
      expediteur: 'Seigneur d’Astoria',
      contenu:
        'Salutations, Votre Majesté. Notre royaume souhaiterait établir des relations commerciales durables avec votre domaine. Nous espérons pouvoir collaborer prochainement.',
      dateReception: '27/11/2026 à 14h30',
      lu: false
    },

    {
      id: 2,
      expediteur: 'Seigneur de Valoria',
      contenu:
        'Votre royaume semble prospérer. Nous vous adressons nos félicitations et espérons que nos deux peuples pourront entretenir des relations amicales.',
      dateReception: '27/11/2026 à 12h15',
      lu: true
    },

    {
      id: 3,
      expediteur: 'Conseiller du royaume de Norhaven',
      contenu:
        'Nous avons entendu parler de votre développement territorial. Ce serait un honneur de pouvoir échanger avec votre cour.',
      dateReception: '26/11/2026 à 18h45',
      lu: false
    }

  ];


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

    proposition.statut = 'ACCEPTEE';

    console.log('Proposition acceptée :', proposition);

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

    proposition.statut = 'REFUSEE';

    console.log('Proposition refusée :', proposition);

  }


  // ========================================
  // MARQUER UN MESSAGE COMME LU
  // ========================================

  marquerCommeLu(id: number): void {

    const message = this.messages.find(
      m => m.id === id
    );

    if (!message || message.lu) {
      return;
    }

    message.lu = true;

    console.log('Message marqué comme lu :', message);

  }

}
