import {
  PropositionEchangeService,
  Ressource
} from '../../services/proposition_echange/proposition-echange.service';
import {
  Component,
  HostListener,
  OnInit
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  MessageService,
  MessageResponse
} from '../../services/message/message.service';
import {
  UtilisateurService,
  UtilisateurResponse
} from '../../services/utilisateur/utilisateur.service';

import { AttaqueAdverse } from '../attaque-adverse/attaque-adverse.component';

@Component({
  selector: 'app-menu-adverse',
  standalone: true,
  imports: [CommonModule, FormsModule, AttaqueAdverse],
  templateUrl: './menu-adverse.component.html',
  styleUrls: ['./menu-adverse.component.css']
})

export class MenuAdverseComponent implements OnInit {

  // ID du joueur adverse
  idAdverse = 2;
  idPartie = 1;
  idExpediteur = 1;
  attaquantId = 1;
  defenseurId = 2;

// Message
contenuMessage = '';
envoiEnCours = false;
messageSucces = '';
erreurMessage = '';

messageEnvoi = '';


// Echange

envoiEchangeEnCours = false;
messageEchange = '';

  // Informations du joueur adverse
  utilisateurAdverse: UtilisateurResponse | null = null;

  pseudoAdverse = '';

  chargementAdverse = false;

  erreurAdverse = '';
constructor(
  private utilisateurService: UtilisateurService,
  private messageService: MessageService,
  private propositionEchangeService: PropositionEchangeService
) {}

  ngOnInit(): void {
    this.chargerUtilisateurAdverse();
  }

  // =====================================================
  // RÉCUPÉRATION DU JOUEUR ADVERSE
  // =====================================================

  chargerUtilisateurAdverse(): void {

    this.chargementAdverse = true;
    this.erreurAdverse = '';

    this.utilisateurService
      .getUtilisateurById(this.idAdverse)
      .subscribe({

        next: (utilisateur) => {

          this.utilisateurAdverse = utilisateur;
          this.pseudoAdverse = utilisateur.pseudo;

          this.chargementAdverse = false;

        },

        error: (error) => {

          console.error(
            'Erreur récupération utilisateur adverse :',
            error
          );

          this.erreurAdverse =
            'Impossible de récupérer le royaume adverse.';

          this.chargementAdverse = false;

        }

      });

  }

  // =====================================================
  // GESTION DES FENÊTRES MODALES
  // =====================================================

  modalEchange = false;
  modalMessage = false;
  modalAttaque = false;

  // =====================================================
  // FERMETURE DES MODALES
  // =====================================================

  fermerModales(): void {

    this.modalEchange = false;
    this.modalMessage = false;
    this.modalAttaque = false;

  }

  // =====================================================
  // OUVERTURE DES MODALES
  // =====================================================

  ouvrirEchange(): void {
    this.fermerModales();
    this.modalEchange = true;
  }

  ouvrirMessage(): void {
    this.fermerModales();
    this.modalMessage = true;
  }

  ouvrirAttaque(): void {
    this.fermerModales();
    this.modalAttaque = true;
  }

  // =====================================================
  // FERMETURE AVEC ÉCHAP
  // =====================================================

  @HostListener('document:keydown', ['$event'])
  gererClavier(event: KeyboardEvent): void {

    if (
      this.modalEchange ||
      this.modalMessage ||
      this.modalAttaque
    ) {

      if (event.key === 'Escape') {
        this.fermerModales();
      }

    }

  }

  envoyerMessage(): void {

  if (!this.contenuMessage.trim()) {
    this.erreurMessage = 'Écris un message avant de l’envoyer.';
    return;
  }

  if (!this.utilisateurAdverse) {
    this.erreurMessage = 'Le destinataire est introuvable.';
    return;
  }

  this.envoiEnCours = true;
  this.erreurMessage = '';
  this.messageSucces = '';

  const request = {
    contenu: this.contenuMessage.trim(),
    partieId: this.idPartie,
    expediteurId: this.idExpediteur,
    destinataireId: this.idAdverse
  };

  this.messageService.envoyerMessage(request).subscribe({

    next: (response) => {

      console.log('Message envoyé :', response);

      this.messageSucces = 'Message envoyé avec succès !';
      this.contenuMessage = '';
      this.envoiEnCours = false;

    },

    error: (error) => {

      console.error('Erreur envoi message :', error);

      this.erreurMessage =
        error.error || 'Impossible d’envoyer le message.';

      this.envoiEnCours = false;

    }

  });

}

// =====================================================
// PROPOSER UN ÉCHANGE AU ROYAUME ADVERSE
// =====================================================

proposerEchange(
  event: Event,
  ressourceDemandee: string,
  quantiteDemandee: string,
  ressourceProposee: string,
  quantiteProposee: string
): void {

  event.preventDefault();

  if (this.envoiEchangeEnCours) {
    return;
  }

  const quantiteD = Number(quantiteDemandee);
  const quantiteP = Number(quantiteProposee);

  if (
    !ressourceDemandee ||
    !ressourceProposee ||
    quantiteD <= 0 ||
    quantiteP <= 0
  ) {
    this.messageEchange = 'Veuillez remplir correctement tous les champs.';
    return;
  }

  this.envoiEchangeEnCours = true;
  this.messageEchange = '';

  const proposition = {
    expediteurId: this.idExpediteur,

    offerte: {
      ressource: ressourceProposee as Ressource,
      quantite: quantiteP
    },

    demandee: {
      ressource: ressourceDemandee as Ressource,
      quantite: quantiteD
    }
  };

  this.propositionEchangeService
    .creerProposition(proposition)
    .subscribe({

      next: (response) => {

        console.log('Échange proposé :', response);

        this.messageEchange =
          'Votre proposition a été envoyée au royaume adverse !';

        this.envoiEchangeEnCours = false;

      },

      error: (error) => {

        console.error('Erreur proposition échange :', error);

        this.messageEchange =
          error.error?.message ||
          error.error ||
          'Vous ne possédez pas suffisamment de ressources ou la proposition est impossible.';

        this.envoiEchangeEnCours = false;

      }

    });

}

}
