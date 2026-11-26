import {
  Component,
  HostListener,
  ChangeDetectorRef,
  NgZone
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AmeliorerRoyaumeService } from '../../../services/royaume/ameliorer-royaume.service';
import { CommonModule } from '@angular/common';
import { PalierNiveau } from '../../../models/palier-niveau.model';
import {
  AmeliorerRoyaumeResponse
} from '../../../models/palier-niveau.model';
import {
  MarcheService,
  OffreMarcheRequest,
  Ressource
} from '../../../services/marche/marche-service.service';


import { VisuAttaques } from '../../visu-attaques/visu-attaques.component';

@Component({
  selector: 'app-menus-salle-du-chateau',
  standalone: true,
  imports: [FormsModule, CommonModule, VisuAttaques],
  templateUrl: './menus-salle-du-chateau.component.html',
  styleUrls: ['./menus-salle-du-chateau.component.css']
})
export class MenusSalleDuChateauComponent {

  constructor(
    private marcheService: MarcheService,
  private ameliorerRoyaumeService: AmeliorerRoyaumeService,
  private cdr: ChangeDetectorRef,
  private zone: NgZone
) {}
  // Mémorisation des annonces déjà publiées
private offresPubliees = new Set<string>();

// Publications actuellement en cours
private publicationsEnCours = new Map<string, number>();
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

// =====================================================
// AMELIORATION DU ROYAUME
// =====================================================

modalAmelioration = false;

chargementAmelioration = false;

messageAmelioration = '';

prochainPalier: AmeliorerRoyaumeResponse | null = null;
// =====================================================

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
  //modalAmelioration = false;
  modalAttaque = false;
  readonly Math = Math;
  // Messages
  messageEchange = '';
  //messageAmelioration = '';
  messageAttaque = '';
  modalVisuAttaques = false;

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

  // IDENTIFICATION DE L'ANNONCE
  const signature = JSON.stringify(offre);

  // Vérification d'une éventuelle republication
  const estRepublication =
    this.offresPubliees.has(signature) ||
    (this.publicationsEnCours.get(signature) ?? 0) > 0;

  // Comptabilisation des requêtes en cours
  const nombreEnCours =
    this.publicationsEnCours.get(signature) ?? 0;

  this.publicationsEnCours.set(
    signature,
    nombreEnCours + 1
  );

  // PUBLICATION
  this.marcheService.publierOffre(offre).subscribe({

    next: (response) => {

      console.log('Offre publiée :', response);

      this.zone.run(() => {

        // Mémorisation de la publication réussie
        this.offresPubliees.add(signature);

        // Mise à jour des requêtes en cours
        this.terminerPublication(signature);

        // MESSAGE
        this.messageEchange = estRepublication
          ? 'Offre republiée ! Attention, vous avez publié une annonce identique.'
          : 'Annonce publiée avec succès !';

        // Actualisation immédiate de l'affichage
        this.cdr.detectChanges();

      });

    },

    error: (error) => {

      console.error('Erreur lors de la publication :', error);

      this.zone.run(() => {

        this.terminerPublication(signature);

        this.messageEchange =
          'Erreur lors de la publication de l’annonce.';

        this.cdr.detectChanges();

      });

    }

  });

}

// Décompte des publications en cours
private terminerPublication(signature: string): void {

  const nombreEnCours =
    this.publicationsEnCours.get(signature) ?? 0;

  if (nombreEnCours <= 1) {
    this.publicationsEnCours.delete(signature);
  } else {
    this.publicationsEnCours.set(
      signature,
      nombreEnCours - 1
    );
  }

}
//_______________________________________________________________  🐸

  // =====================================================
  // OUVERTURE DE LA MODALE
  // =====================================================
ouvrirAmelioration(): void {

  this.fermerModales();

  this.modalAmelioration = true;

  // Charger uniquement si les données ne sont pas encore disponibles
  if (!this.prochainPalier) {
    this.chargerProchainPalier();
  }

}


  // =====================================================
  // CHARGEMENT DES INFORMATIONS
  // =====================================================
chargerProchainPalier(): void {

  // Éviter les chargements simultanés
  if (this.chargementAmelioration) {
    return;
  }

  this.chargementAmelioration = true;
  this.messageAmelioration = '';

  // IMPORTANT :
  // Ne plus effacer prochainPalier ici.
  // Les anciennes données restent disponibles pendant le chargement.

  this.ameliorerRoyaumeService
    .obtenirProchainPalier(this.vendeurId)
    .subscribe({

      next: (reponse) => {

        this.zone.run(() => {

          this.prochainPalier = reponse;
          this.chargementAmelioration = false;

          this.cdr.detectChanges();

        });

      },

      error: (error) => {

        console.error(
          'Erreur chargement amélioration :',
          error
        );

        this.zone.run(() => {

          this.chargementAmelioration = false;

          this.messageAmelioration =
            'Impossible de récupérer les informations du prochain palier.';

          this.cdr.detectChanges();

        });

      }

    });

}


  // =====================================================
  // IMAGE DES RESSOURCES
  // =====================================================

  getImageRessource(ressource: string): string {

    return `/assets/imago/${ressource.toUpperCase()}.png`;

  }


  // =====================================================
  // AMELIORATION DU ROYAUME
  // =====================================================

  ameliorerRoyaume(): void {

    if (
      !this.prochainPalier ||
      !this.prochainPalier.ameliorationPossible ||
      this.chargementAmelioration
    ) {
      return;
    }

    this.messageAmelioration = '';
    this.chargementAmelioration = true;

    this.ameliorerRoyaumeService
      .ameliorerRoyaume(this.vendeurId)
      .subscribe({

        next: (message) => {

          this.zone.run(() => {

            this.messageAmelioration = message;

            this.chargementAmelioration = false;

            // Actualiser les informations du prochain niveau
            this.chargerProchainPalier();

            this.cdr.detectChanges();

          });

        },

        error: (error) => {

          console.error(
            'Erreur amélioration :',
            error
          );

          this.zone.run(() => {

            this.chargementAmelioration = false;

            this.messageAmelioration =
              typeof error.error === 'string'
                ? error.error
                : 'Impossible d’améliorer le royaume.';

            // Actualiser les conditions après un éventuel échec
            this.chargerProchainPalier();

            this.cdr.detectChanges();

          });

        }

      });

  }

//________________________________________________________________  🐸

}
