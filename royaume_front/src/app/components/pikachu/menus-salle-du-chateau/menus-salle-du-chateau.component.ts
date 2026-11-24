
import { Component } from '@angular/core';

@Component({
  selector: 'app-menus-salle-du-chateau',
  standalone: true,
  templateUrl: './menus-salle-du-chateau.component.html',
  styleUrls: ['./menus-salle-du-chateau.component.css']
})
export class MenusSalleDuChateauComponent {

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

  // Récupération du nom
  modifierNom(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.nomGobelin = input.value;
  }

  // Démarrage du jeu
  demarrerJeu(audio: HTMLAudioElement): void {

    if (!this.nomGobelin.trim()) {
      return;
    }

    this.demarre = true;

    audio.play().catch(error => {
      console.warn('Lecture audio impossible :', error);
    });

    console.log('Gobelin démarré :', this.nomGobelin);
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

    const annonce = {
      ressourceDemandee,
      quantiteDemandee: Number(quantiteDemandee),
      ressourceProposee,
      quantiteProposee: Number(quantiteProposee)
    };

    console.log('Annonce à envoyer :', annonce);

    this.messageEchange = 'Annonce préparée avec succès !';
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
