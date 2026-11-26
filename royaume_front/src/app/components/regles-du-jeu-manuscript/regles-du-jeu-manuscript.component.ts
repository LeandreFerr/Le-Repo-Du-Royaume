
import { Component, HostListener } from '@angular/core';
import { CommonModule, registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';

registerLocaleData(localeFr, 'fr-FR');
interface Peuple {
  titre: string;
  image: string;
  description: string;
  infos: string;
  batiment: string;
  bonusmalus: string;
}

interface Niveau {
  nom: string;
  or: number;
  pierre: number;
  bois: number;
  nourriture: number;
  population: number;
}

@Component({
  selector: 'app-regles-du-jeu-manuscript',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './regles-du-jeu-manuscript.component.html',
  styleUrls: ['./regles-du-jeu-manuscript.component.css']
})

export class ReglesDuJeuManuscriptComponent {

  // ==========================================
  // CHEMINS DES IMAGES
  // ==========================================

  readonly images = '/assets/imago/';

  readonly ressources = {
    or: '/assets/imago/OR.png',
    bois: '/assets/imago/BOIS.png',
    pierre: '/assets/imago/PIERRE.png',
    nourriture: '/assets/imago/NOURRITURE.png'
  };


  // ==========================================
  // NAVIGATION ENTRE LES PAGES
  // ==========================================

  pageActuelle = 0;

  readonly nombrePages = 4;

  pageSuivante(): void {
    if (this.pageActuelle < this.nombrePages - 1) {
      this.pageActuelle++;
    }
  }

  pagePrecedente(): void {
    if (this.pageActuelle > 0) {
      this.pageActuelle--;
    }
  }


  // ==========================================
  // MODALE DES PEUPLES
  // ==========================================

  modalPeuple = false;

  peupleSelectionne: Peuple | null = null;

  peuples: Record<string, Peuple> = {

    licornes: {
      titre: 'Les licornes',
      image: '/assets/imago/licornes.png',

      description: 'Un peu fofolles, les licornes.',

      infos: 'Compétence spéciale : Peter des arc-en-ciel ',

      batiment: 'Réduisent les stats de combat de l\'adversaire de 10%.',

      bonusmalus: '✦ Bonus contre les princesses - Malus face aux simples d\'esprit ✦'
    },


    gobelins: {
      titre: 'Les gobelins',
      image: '/assets/imago/gobelins.png',

      description: 'Pas très beaux, leur agressivité au combat n\'a nul égal.',

      infos: 'Compétence spéciale : Vol sans Recel.',

      batiment: 'Pillage de 5% des réserves ennemies après une attaque extérieure.',

      bonusmalus: '✦ Bonus à l\'encontre des palmidées - Vulnerable aux princesses et à leur charme ✦'
    },


    teuteux: {
      titre: 'Les Teuteux',
      image: '/assets/imago/teuteux.png',

      description: 'Pas les plus intelligents, ils s\'en sortent généralement grâce à leur apparente inconséquence.',

      infos: 'Compétence spéciale : Mange ses crottes de nez.',

      batiment: 'Booste la production de nourriture de 50% et accorde un bonus de taux de croissance de la population (ils sont rassasiés)',

      bonusmalus: '✦ Bonus contre les mammifères cornus - Malus face aux canards ✦'
    },


    princesses: {
      titre: 'Les princesses en feu',
      image: '/assets/imago/princess.png',

      description: 'Rock\'n\'Roll on fire, baby !',

      infos: 'Compétence spéciale : Rot Enflammé - Crache du feu',

      batiment: 'Les flammes incendient les installations rivales, réduisant ainsi leur production de 30%.',

      bonusmalus: '✦ Puissantes face aux gobelins - Pas bien préparées pour les licornes ✦'
    },


    canards: {
      titre: 'Les canards',
      image: '/assets/imago/canard.png',

      description: '*Kouakkkkkk*',

      infos: 'Compétence spéciale : Danse des canards coin-coin.',

      batiment: 'Procure un bonus offensif de 25%.',

      bonusmalus: '✦ Bonus en face d\'un teuteu - Malus si un gobelin lui barre la route ✦'
    }

  };


  ouvrirModal(peuple: string): void {

    const donnees = this.peuples[peuple];

    if (!donnees) {
      return;
    }

    this.peupleSelectionne = donnees;

    this.modalPeuple = true;
  }


  fermerModal(): void {

    this.modalPeuple = false;

    this.peupleSelectionne = null;
  }


  // ==========================================
  // MODALE DES NIVEAUX DU ROYAUME
  // ==========================================

  modalNiveaux = false;

/*
  ouvrirNiveaux(): void {

    this.modalNiveaux = true;
  }
*/
ouvrirNiveaux(): void {
  console.log('Clic sur le bouton des niveaux');

  this.modalNiveaux = true;

  console.log('État de la modale :', this.modalNiveaux);
}
  fermerNiveaux(): void {

    this.modalNiveaux = false;
  }


  // ==========================================
  // NIVEAUX DU ROYAUME
  // ==========================================

  niveaux: Niveau[] = [

    {
      nom: 'Niveau II — Le hameau',
      or: 100,
      pierre: 80,
      bois: 60,
      nourriture: 90,
      population: 100
    },

    {
      nom: 'Niveau III — Le village',
      or: 200,
      pierre: 100,
      bois: 100,
      nourriture: 1300,
      population: 200
    },

    {
      nom: 'Niveau IV — La bourgade',
      or: 300,
      pierre: 150,
      bois: 150,
      nourriture: 2000,
      population: 300
    },

    {
      nom: 'Niveau V - La cité',
      or: 400,
      pierre: 250,
      bois: 250,
      nourriture: 2800,
      population: 400
    },

    {
      nom: 'Niveau VI - La ville fortifiée',
      or: 500,
      pierre: 350,
      bois: 350,
      nourriture: 3600,
      population: 500
    },

    {
      nom: 'Niveau VII - La forteresse',
      or: 600,
      pierre: 500,
      bois: 500,
      nourriture: 4400,
      population: 600
    },

    {
      nom: 'Niveau VIII - Le duché',
      or: 700,
      pierre: 650,
      bois: 650,
      nourriture: 5200,
      population: 700
    },

    {
      nom: 'Niveau IX - La ville-rempart',
      or: 800,
      pierre: 800,
      bois: 800,
      nourriture: 6100,
      population: 800
    },

    {
      nom: 'Niveau X - L\'apogée',
      or: 1000,
      pierre: 1000,
      bois: 1000,
      nourriture: 7000,
      population: 1000
    }

  ];


  // ==========================================
  // FERMETURE DES MODALES AVEC ÉCHAP
  // ==========================================

  @HostListener('document:keydown', ['$event'])

  gererClavier(event: KeyboardEvent): void {

    if (event.key === 'Escape') {

      if (this.modalPeuple) {
        this.fermerModal();
      }

      if (this.modalNiveaux) {
        this.fermerNiveaux();
      }

    }

  }

}
