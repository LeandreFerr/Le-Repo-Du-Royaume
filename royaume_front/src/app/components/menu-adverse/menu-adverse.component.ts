import {
  PropositionEchangeService,
  Ressource,
} from '../../services/proposition_echange/proposition-echange.service';
import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MessageService } from '../../services/message/message.service';
import {
  UtilisateurService,
  UtilisateurResponse,
} from '../../services/utilisateur/utilisateur.service';
import { AttaqueAdverse } from '../attaque-adverse/attaque-adverse.component';

export interface RoyaumeAdverseCible {
  id: number;
  nom: string;
  pseudoJoueur?: string;
  utilisateurId?: number | null;
  peupleNom?: string;
  niveau?: number;
}

@Component({
  selector: 'app-menu-adverse',
  standalone: true,
  imports: [CommonModule, FormsModule, AttaqueAdverse],
  templateUrl: './menu-adverse.component.html',
  styleUrls: ['./menu-adverse.component.css'],
})
export class MenuAdverseComponent implements OnInit, OnChanges {
  @Input() monRoyaumeId = 1;
  @Input() monUtilisateurId = 1;
  @Input() partieId = 1;
  @Input() royaumeAdverse: RoyaumeAdverseCible | null = null;
  @Input() modeModal = false;

  @Output() fermer = new EventEmitter<void>();
  @Output() actionEffectuee = new EventEmitter<void>();

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

  // Modales
  modalEchange = false;
  modalMessage = false;
  modalAttaque = false;

  constructor(
    private utilisateurService: UtilisateurService,
    private messageService: MessageService,
    private propositionEchangeService: PropositionEchangeService,
  ) {}

  ngOnInit(): void {
    this.synchroniserCible();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes['monRoyaumeId'] ||
      changes['monUtilisateurId'] ||
      changes['partieId'] ||
      changes['royaumeAdverse']
    ) {
      this.synchroniserCible();
    }
  }

  private synchroniserCible(): void {
    this.attaquantId = this.monRoyaumeId;
    this.idExpediteur = this.monUtilisateurId;
    this.idPartie = this.partieId;

    if (this.royaumeAdverse) {
      this.defenseurId = this.royaumeAdverse.id;
      this.pseudoAdverse =
        this.royaumeAdverse.pseudoJoueur || this.royaumeAdverse.nom;
      if (this.royaumeAdverse.utilisateurId) {
        this.idAdverse = this.royaumeAdverse.utilisateurId;
        this.chargerUtilisateurAdverse();
      } else {
        this.utilisateurAdverse = null;
        this.idAdverse = 0;
      }
    } else {
      this.chargerUtilisateurAdverse();
    }
  }

  chargerUtilisateurAdverse(): void {
    if (!this.idAdverse || this.idAdverse <= 0) {
      return;
    }

    this.chargementAdverse = true;
    this.erreurAdverse = '';

    this.utilisateurService.getUtilisateurById(this.idAdverse).subscribe({
      next: (utilisateur) => {
        this.utilisateurAdverse = utilisateur;
        this.pseudoAdverse = utilisateur.pseudo;
        this.chargementAdverse = false;
      },
      error: (error) => {
        console.error('Erreur récupération utilisateur adverse :', error);
        this.erreurAdverse = 'Impossible de récupérer le royaume adverse.';
        this.chargementAdverse = false;
      },
    });
  }

  fermerModales(): void {
    this.modalEchange = false;
    this.modalMessage = false;
    this.modalAttaque = false;
  }

  fermerMenuPrincipal(): void {
    this.fermerModales();
    this.fermer.emit();
  }

  ouvrirEchange(): void {
    this.fermerModales();
    this.messageEchange = '';
    this.modalEchange = true;
  }

  ouvrirMessage(): void {
    this.fermerModales();
    this.messageSucces = '';
    this.erreurMessage = '';
    this.modalMessage = true;
  }

  ouvrirAttaque(): void {
    this.fermerModales();
    this.modalAttaque = true;
  }

  @HostListener('document:keydown', ['$event'])
  gererClavier(event: KeyboardEvent): void {
    if (event.key !== 'Escape') {
      return;
    }
    if (this.modalEchange || this.modalMessage || this.modalAttaque) {
      this.fermerModales();
    } else if (this.modeModal) {
      this.fermerMenuPrincipal();
    }
  }

  envoyerMessage(): void {
    if (!this.contenuMessage.trim()) {
      this.erreurMessage = 'Écris un message avant de l’envoyer.';
      return;
    }

    const destinataireId = this.utilisateurAdverse?.id ?? this.idAdverse;
    if (!destinataireId || destinataireId <= 0) {
      this.erreurMessage = 'Le destinataire est introuvable pour ce royaume.';
      return;
    }

    this.envoiEnCours = true;
    this.erreurMessage = '';
    this.messageSucces = '';

    const request = {
      contenu: this.contenuMessage.trim(),
      partieId: this.idPartie,
      expediteurId: this.idExpediteur,
      destinataireId,
    };

    this.messageService.envoyerMessage(request).subscribe({
      next: (response) => {
        console.log('Message envoyé :', response);
        this.messageSucces = 'Message envoyé avec succès !';
        this.contenuMessage = '';
        this.envoiEnCours = false;
        this.actionEffectuee.emit();
      },
      error: (error) => {
        console.error('Erreur envoi message :', error);
        this.erreurMessage =
          error.error?.message ||
          (typeof error.error === 'string' ? error.error : null) ||
          'Impossible d’envoyer le message.';
        this.envoiEnCours = false;
      },
    });
  }

  proposerEchange(
    event: Event,
    ressourceDemandee: string,
    quantiteDemandee: string,
    ressourceProposee: string,
    quantiteProposee: string,
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
      expediteurId: this.attaquantId,
      destinataireId: this.defenseurId,
      offerte: {
        ressource: ressourceProposee as Ressource,
        quantite: quantiteP,
      },
      demandee: {
        ressource: ressourceDemandee as Ressource,
        quantite: quantiteD,
      },
    };

    this.propositionEchangeService.creerProposition(proposition).subscribe({
      next: (response) => {
        console.log('Échange proposé :', response);
        this.messageEchange =
          'Votre proposition a été envoyée au royaume adverse !';
        this.envoiEchangeEnCours = false;
        this.actionEffectuee.emit();
      },
      error: (error) => {
        console.error('Erreur proposition échange :', error);
        this.messageEchange =
          error.error?.message ||
          (typeof error.error === 'string' ? error.error : null) ||
          'Vous ne possédez pas suffisamment de ressources ou la proposition est impossible.';
        this.envoiEchangeEnCours = false;
      },
    });
  }
}
