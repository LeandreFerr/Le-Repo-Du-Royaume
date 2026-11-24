
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  MarcheService,
  OffreMarcheRequest,
  Ressource
} from '../../../services/marche/marche-service.service';
@Component({
  selector: 'app-menus-salle-du-chateau',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './menus-salle-du-chateau.component.html',
  styleUrls: ['./menus-salle-du-chateau.component.css']
})
export class MenusSalleDuChateauComponent {

  constructor(
    private marcheService: MarcheService
  ) {}
  
//_______________________________________________________________  🐸
//_______________________________________________________________  🐸
  vendeurId = 1;  
//_______________________________________________________________  🐸
//_______________________________________________________________  🐸

  
  // État du jeu
  nomGobelin = '';
  demarre = false;

  // Gestion des fenêtres
  modalEchange = false;
  modalAmelioration = false;
  modalAttaque = false;

  // Messages
  messageEchange = '';
  messageAmelioration = '';
  messageAttaque = '';

  typesRessources: string[] = [
  'PIERRE',
  'BOIS',
  'OR',
  'NOURRITURE'
];

  // Récupération du nom
  modifierNom(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.nomGobelin = input.value;
  }

  // Démarrage du jeu
  // Démarrage du jeu sans audio
demarrerJeu(): void {

  if (!this.nomGobelin.trim()) {
    return;
  }

  this.demarre = true;

  console.log('Personnage démarré :', this.nomGobelin);
}

  // Fermeture des fenêtres
  fermerModales(): void {
    this.modalEchange = false;
    this.modalAmelioration = false;
    this.modalAttaque = false;
  }

  // Ouverture des fenêtres
  ouvrirEchange(): void {
    this.fermerModales();
    this.modalEchange = true;
  }

  ouvrirAmelioration(): void {
    this.fermerModales();
    this.modalAmelioration = true;
  }

  ouvrirAttaque(): void {
    this.fermerModales();
    this.modalAttaque = true;
  }

  // Poster une annonce d'échange
  posterEchange(
  event: Event,
  ressourceDemandee: string,
  quantiteDemandee: string,
  ressourceProposee: string,
  quantiteProposee: string
): void {

  event.preventDefault();

  const offre: OffreMarcheRequest = {
    vendeurId: this.vendeurId,

    propose: {
      ressource: ressourceProposee as Ressource,
      quantite: Number(quantiteProposee)
    },

    demande: {
      ressource: ressourceDemandee as Ressource,
      quantite: Number(quantiteDemandee)
    }
  };

  this.marcheService.publierOffre(offre).subscribe({

    next: (response) => {
      console.log('Offre publiée :', response);

      this.messageEchange = 'Annonce publiée avec succès !';
    },

    error: (error) => {
      console.error('Erreur lors de la publication :', error);

      this.messageEchange = 'Erreur lors de la publication de l’annonce.';
    }

  });
}
  // Amélioration d'un bâtiment
  ameliorerBatiment(
    event: Event,
    batiment: string
  ): void {

    event.preventDefault();

    console.log('Bâtiment à améliorer :', batiment);

    this.messageAmelioration =
      'Demande d’amélioration préparée !';
  }

  // Déclaration d'une attaque
  attaquerRoyaume(
    event: Event,
    royaume: string,
    confirmation: boolean
  ): void {

    event.preventDefault();

    if (!confirmation || !royaume) {
      return;
    }

    console.log('Royaume ciblé :', royaume);

    this.messageAttaque =
      'Demande d’attaque préparée !';
  }

}
