/*
import { Component, HostListener, OnDestroy } from '@angular/core';

@Component({
  selector: 'app-mini-jeu',
  templateUrl: './mini-jeu.component.html',
  styleUrls: ['./mini-jeu.component.css']
})
export class MiniJeuComponent implements OnDestroy {

  // ETAT DU JEU
  demarre = false;

  nomGobelin = '';
  nomPersonnage = 'gobelin';

  personnagesDisponibles = ['gobelin', 'licorne'];

  posX = 0;
  posY = 0;

  mouvement = 30;

  direction = 'Down';

  positionMaxX = 610;
  positionMaxY = 610;

  // FOND ET SPRITES

  get fondEau(): string {
    return `assets/pikachu/img/water-${this.nomPersonnage}.png`;
  }

  get spriteActuel(): string {
    return `assets/pikachu/img/${this.nomPersonnage}${this.direction}.png`;
  }

  // MODALES
  modalEchange = false;
  modalAmelioration = false;
  modalAttaque = false;

  messageEchange = '';
  messageAmelioration = '';
  messageAttaque = '';

  constructor() {}

  ngOnDestroy(): void {
    // Rien à nettoyer : les événements clavier
    // sont gérés par HostListener.
  }

  // MODIFICATION DU NOM
  modifierNom(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.nomGobelin = input.value;
  }

  // DEMARRAGE DU JEU
  demarrerJeu(): void {

    const nom = this.nomGobelin.trim().toLowerCase();

    if (!nom) {
      return;
    }

    if (!this.personnagesDisponibles.includes(nom)) {
      alert(`Le personnage '${nom}' n'existe pas.`);
      return;
    }

    this.nomPersonnage = nom;

    this.posX = 0;
    this.posY = 0;
    this.direction = 'Down';

    this.demarre = true;
  }

  // GESTION DU CLAVIER
  @HostListener('document:keydown', ['$event'])
  gererClavier(event: KeyboardEvent): void {

    if (!this.demarre) {
      return;
    }

    // Ne pas déplacer le personnage lorsqu'une modale est ouverte
    if (
      this.modalEchange ||
      this.modalAmelioration ||
      this.modalAttaque
    ) {

      if (event.key === 'Escape') {
        this.fermerModales();
      }

      return;
    }

    // ESPACE : INTERACTION
    if (event.code === 'Space') {
      event.preventDefault();
      this.interaction();
      return;
    }

    const touche = event.key.toLowerCase();

    // RETOUR AU MENU
    if (
      this.posX === 0 &&
      this.posY === 0 &&
      (event.key === 'ArrowUp' || touche === 'z')
    ) {
      event.preventDefault();
      this.retourMenu();
      return;
    }

    let nouvellePosX = this.posX;
    let nouvellePosY = this.posY;

    // DEPLACEMENTS

    if (event.key === 'ArrowDown' || touche === 's') {
      this.direction = 'Down';
      nouvellePosY += this.mouvement;
    }

    else if (event.key === 'ArrowRight' || touche === 'd') {
      this.direction = 'Right';
      nouvellePosX += this.mouvement;
    }

    else if (event.key === 'ArrowLeft' || touche === 'q') {
      this.direction = 'Left';
      nouvellePosX -= this.mouvement;
    }

    else if (event.key === 'ArrowUp' || touche === 'z') {
      this.direction = 'Up';
      nouvellePosY -= this.mouvement;
    }

    else {
      return;
    }

    event.preventDefault();

    const dansLaMap =
      nouvellePosX >= 0 &&
      nouvellePosX <= this.positionMaxX &&
      nouvellePosY >= 0 &&
      nouvellePosY <= this.positionMaxY;

    if (
      dansLaMap &&
      !this.estSurLaTable(nouvellePosX, nouvellePosY) &&
      !this.estSurLaTapisserie(nouvellePosX, nouvellePosY)
    ) {
      this.posX = nouvellePosX;
      this.posY = nouvellePosY;
    }
  }

  // COLLISION AVEC LA TABLE
  estSurLaTable(x: number, y: number): boolean {

    const caseX = x / this.mouvement;
    const ligneY = y / this.mouvement;

    if (ligneY === 0 && caseX >= 7 && caseX <= 14) {
      return true;
    }

    if (ligneY === 5 && caseX >= 17 && caseX <= 20) {
      return true;
    }

    if (
      ligneY >= 6 &&
      ligneY <= 9 &&
      (
        (caseX >= 0 && caseX <= 2) ||
        (caseX >= 17 && caseX <= 20)
      )
    ) {
      return true;
    }

    return false;
  }

  // COLLISION AVEC LA TAPISSERIE
  estSurLaTapisserie(x: number, y: number): boolean {

    const caseX = x / this.mouvement;
    const ligneY = y / this.mouvement;

    if (ligneY === 0 && caseX === 1) {
      return true;
    }

    if (
      ligneY >= 1 &&
      ligneY <= 4 &&
      (
        (caseX >= 1 && caseX <= 7) ||
        caseX === 13
      )
    ) {
      return true;
    }

    if (
      (ligneY === 5 || ligneY === 6) &&
      caseX === 13
    ) {
      return true;
    }

    if (
      ligneY >= 7 &&
      ligneY <= 9 &&
      (
        (caseX >= 3 && caseX <= 7) ||
        (caseX >= 13 && caseX <= 17)
      )
    ) {
      return true;
    }

    if (ligneY >= 14) {
      return true;
    }

    return false;
  }

  // VERIFICATION DE PROXIMITE
  estAdjacentAZone(
    casePersonnage: number,
    lignePersonnage: number,
    caseDebut: number,
    caseFin: number,
    ligneDebut: number,
    ligneFin: number
  ): boolean {

    for (let ligne = ligneDebut; ligne <= ligneFin; ligne++) {

      for (let cellule = caseDebut; cellule <= caseFin; cellule++) {

        const distance =
          Math.abs(casePersonnage - cellule) +
          Math.abs(lignePersonnage - ligne);

        if (distance === 1) {
          return true;
        }
      }
    }

    return false;
  }

  // INTERACTIONS AVEC LES ZONES
  interaction(): void {

    const casePersonnage = this.posX / this.mouvement;
    const lignePersonnage = this.posY / this.mouvement;

    const presDeLaTable = this.estAdjacentAZone(
      casePersonnage,
      lignePersonnage,
      7, 14, 0, 0
    );

    const presDeLaCarte = this.estAdjacentAZone(
      casePersonnage,
      lignePersonnage,
      0, 3, 6, 9
    );

    const presDesArmes = this.estAdjacentAZone(
      casePersonnage,
      lignePersonnage,
      17, 20, 5, 9
    );

    if (presDeLaTable) {
      this.modalEchange = true;
    }

    else if (presDeLaCarte) {
      this.modalAmelioration = true;
    }

    else if (presDesArmes) {
      this.modalAttaque = true;
    }
  }

  // FERMETURE DES MODALES
  fermerModales(): void {
    this.modalEchange = false;
    this.modalAmelioration = false;
    this.modalAttaque = false;
  }

  // RETOUR AU MENU
  retourMenu(): void {

    this.demarre = false;

    this.nomGobelin = '';

    this.posX = 0;
    this.posY = 0;

    this.direction = 'Down';

    this.fermerModales();
  }

}
*/ 
import { Component, HostListener } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  MarcheService,
  OffreMarcheRequest,
  Ressource
} from '../../../services/marche/marche-service.service';

import { VisuAttaques } from '../../visu-attaques/visu-attaques.component';

@Component({
  selector: 'app-menus-salle-du-chateau',
  standalone: true,
  imports: [FormsModule, VisuAttaques],
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
// PERSONNAGE
nomPersonnage = 'gobelin';

personnagesDisponibles = ['gobelin', 'licorne'];

posX = 0;
posY = 0;

mouvement = 30;

direction = 'Down';

positionMaxX = 610;
positionMaxY = 610;

// CHEMINS DES IMAGES

get fondEau(): string {
  return `assets/pikachu/img/water-${this.nomPersonnage}.png`;
}

get spriteActuel(): string {
  return `assets/pikachu/img/${this.nomPersonnage}${this.direction}.png`;
}
  
  // État du jeu
  nomGobelin = '';
  demarre = false;

  // Gestion des fenêtres
  modalEchange = false;
  modalAmelioration = false;
  modalVisuAttaques = false;

  // Messages
  messageEchange = '';
  messageAmelioration = '';
  messageVisuAttaques = '';

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

  demarrerJeu(): void {

  const nom = this.nomGobelin.trim().toLowerCase();

  if (!nom) {
    return;
  }

  if (!this.personnagesDisponibles.includes(nom)) {
    alert(`Le personnage '${nom}' n'existe pas.`);
    return;
  }

  this.nomPersonnage = nom;

  this.posX = 0;
  this.posY = 0;

  this.direction = 'Down';

  this.demarre = true;

}

  // Fermeture des fenêtres
  fermerModales(): void {
    this.modalEchange = false;
    this.modalAmelioration = false;
    this.modalVisuAttaques = false;
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

  ouvrirVisuAttaques(): void {
    this.fermerModales();
    this.modalVisuAttaques = true;
  }
  @HostListener('document:keydown', ['$event'])
gererClavier(event: KeyboardEvent): void {

  if (!this.demarre) {
    return;
  }

  // Si une fenêtre est ouverte, on bloque les déplacements
  if (
    this.modalEchange ||
    this.modalAmelioration ||
    this.modalVisuAttaques
  ) {

    if (event.key === 'Escape') {
      this.fermerModales();
    }

    return;
  }

  // ESPACE : INTERACTION
  if (event.code === 'Space') {
    event.preventDefault();
    this.interaction();
    return;
  }

  const touche = event.key.toLowerCase();

  // RETOUR AU MENU AU POINT DE DEPART
  if (
    this.posX === 0 &&
    this.posY === 0 &&
    (event.key === 'ArrowUp' || touche === 'z')
  ) {
    event.preventDefault();
    this.retourMenu();
    return;
  }

  let nouvellePosX = this.posX;
  let nouvellePosY = this.posY;

  // DEPLACEMENTS

  if (event.key === 'ArrowDown' || touche === 's') {
    this.direction = 'Down';
    nouvellePosY += this.mouvement;
  }

  else if (event.key === 'ArrowRight' || touche === 'd') {
    this.direction = 'Right';
    nouvellePosX += this.mouvement;
  }

  else if (event.key === 'ArrowLeft' || touche === 'q') {
    this.direction = 'Left';
    nouvellePosX -= this.mouvement;
  }

  else if (event.key === 'ArrowUp' || touche === 'z') {
    this.direction = 'Up';
    nouvellePosY -= this.mouvement;
  }

  else {
    return;
  }

  event.preventDefault();

  // LIMITES DE LA CARTE
  const dansLaMap =
    nouvellePosX >= 0 &&
    nouvellePosX <= this.positionMaxX &&
    nouvellePosY >= 0 &&
    nouvellePosY <= this.positionMaxY;

  // COLLISIONS
  if (
    dansLaMap &&
    !this.estSurLaTable(nouvellePosX, nouvellePosY) &&
    !this.estSurLaTapisserie(nouvellePosX, nouvellePosY)
  ) {

    this.posX = nouvellePosX;
    this.posY = nouvellePosY;

  }

}
//______________
estSurLaTable(x: number, y: number): boolean {

  const caseX = x / this.mouvement;
  const ligneY = y / this.mouvement;

  if (ligneY === 0 && caseX >= 7 && caseX <= 14) {
    return true;
  }

  if (ligneY === 5 && caseX >= 17 && caseX <= 20) {
    return true;
  }

  if (
    ligneY >= 6 &&
    ligneY <= 9 &&
    (
      (caseX >= 0 && caseX <= 2) ||
      (caseX >= 17 && caseX <= 20)
    )
  ) {
    return true;
  }

  return false;
}


estSurLaTapisserie(x: number, y: number): boolean {

  const caseX = x / this.mouvement;
  const ligneY = y / this.mouvement;

  if (ligneY === 0 && caseX === 1) {
    return true;
  }

  if (
    ligneY >= 1 &&
    ligneY <= 4 &&
    (
      (caseX >= 1 && caseX <= 7) ||
      caseX === 13
    )
  ) {
    return true;
  }

  if (
    (ligneY === 5 || ligneY === 6) &&
    caseX === 13
  ) {
    return true;
  }

  if (
    ligneY >= 7 &&
    ligneY <= 9 &&
    (
      (caseX >= 3 && caseX <= 7) ||
      (caseX >= 13 && caseX <= 17)
    )
  ) {
    return true;
  }

  if (ligneY >= 14) {
    return true;
  }

  return false;
}

estAdjacentAZone(
  casePersonnage: number,
  lignePersonnage: number,
  caseDebut: number,
  caseFin: number,
  ligneDebut: number,
  ligneFin: number
): boolean {

  for (let ligne = ligneDebut; ligne <= ligneFin; ligne++) {

    for (let cellule = caseDebut; cellule <= caseFin; cellule++) {

      const distance =
        Math.abs(casePersonnage - cellule) +
        Math.abs(lignePersonnage - ligne);

      if (distance === 1) {
        return true;
      }
    }
  }

  return false;
}


interaction(): void {

  const casePersonnage = this.posX / this.mouvement;
  const lignePersonnage = this.posY / this.mouvement;

  const presDeLaTable = this.estAdjacentAZone(
    casePersonnage,
    lignePersonnage,
    7, 14, 0, 0
  );

  const presDeLaCarte = this.estAdjacentAZone(
    casePersonnage,
    lignePersonnage,
    0, 3, 6, 9
  );

  const presDesArmes = this.estAdjacentAZone(
    casePersonnage,
    lignePersonnage,
    17, 20, 5, 9
  );

  if (presDeLaTable) {
    this.ouvrirEchange();
  }

  else if (presDeLaCarte) {
    this.ouvrirAmelioration();
  }

  else if (presDesArmes) {
    this.ouvrirVisuAttaques();
  }

}
retourMenu(): void {

  this.demarre = false;

  this.nomGobelin = '';

  this.posX = 0;
  this.posY = 0;

  this.direction = 'Down';

  this.fermerModales();

}
//_______________________________________________________________  🐸
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
/*
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

    this.messageVisuAttaques =
      'Demande d’attaque préparée !';
  }
*/
}
