import { ModeleBatiment } from '../models/modele-batiment.model';
import { TypeBatiment } from '../models/batiment.model';
import { Ressource } from '../models/ressource.model';

export const MOCK_CATALOGUE_BATIMENTS: ModeleBatiment[] = [

  {
    id: 1,
    nom: 'Scierie',
    description: 'Permet de produire du bois.',
    type: TypeBatiment.PRODUCTION,
    typeRessource: Ressource.BOIS,
    icone: '/img/buildings/scierie.svg',

    quantiteMax: 1,
    niveauRoyaumeRequis: 1,
    tempsConstructionSecondes: 180,
    ouvriersMinimum: 1,

    coutConstruction: [
      { ressource: Ressource.BOIS, quantite: 30 },
      { ressource: Ressource.PIERRE, quantite: 20 },
      { ressource: Ressource.NOURRITURE, quantite: 20 },
      { ressource: Ressource.OR, quantite: 10 }
    ]
  },

  {
    id: 2,
    nom: 'Carrière',
    description: 'Permet de produire de la pierre.',
    type: TypeBatiment.PRODUCTION,
    typeRessource: Ressource.PIERRE,
    icone: '/img/buildings/carriere.svg',

    quantiteMax: 1,
    niveauRoyaumeRequis: 1,
    tempsConstructionSecondes: 180,
    ouvriersMinimum: 1,

    coutConstruction: [
      { ressource: Ressource.BOIS, quantite: 20 },
      { ressource: Ressource.PIERRE, quantite: 30 },
      { ressource: Ressource.NOURRITURE, quantite: 20 },
      { ressource: Ressource.OR, quantite: 10 }
    ]
  },

  {
    id: 3,
    nom: 'Ferme',
    description: 'Permet de produire de la nourriture.',
    type: TypeBatiment.PRODUCTION,
    typeRessource: Ressource.NOURRITURE,
    icone: '/img/buildings/ferme.svg',

    quantiteMax: 1,
    niveauRoyaumeRequis: 1,
    tempsConstructionSecondes: 180,
    ouvriersMinimum: 1,

    coutConstruction: [
      { ressource: Ressource.BOIS, quantite: 30 },
      { ressource: Ressource.PIERRE, quantite: 30 },
      { ressource: Ressource.NOURRITURE, quantite: 20 },
      { ressource: Ressource.OR, quantite: 10 }
    ]
  },

  {
    id: 4,
    nom: 'Mine d’or',
    description: 'Permet de produire de l’or.',
    type: TypeBatiment.PRODUCTION,
    typeRessource: Ressource.OR,
    icone: '/img/buildings/mine.svg',

    quantiteMax: 1,
    niveauRoyaumeRequis: 1,
    tempsConstructionSecondes: 180,
    ouvriersMinimum: 1,

    coutConstruction: [
      { ressource: Ressource.BOIS, quantite: 40 },
      { ressource: Ressource.PIERRE, quantite: 40 },
      { ressource: Ressource.NOURRITURE, quantite: 30 },
      { ressource: Ressource.OR, quantite: 20 }
    ]
  },

  {
    id: 5,
    nom: 'Maison',
    description: 'Favorise la croissance démographique.',
    type: TypeBatiment.MAISON,
    icone: '/img/buildings/maison.svg',

    quantiteMax: null,
    niveauRoyaumeRequis: 1,
    tempsConstructionSecondes: 120,
    ouvriersMinimum: 1,

    coutConstruction: [
      { ressource: Ressource.BOIS, quantite: 10 },
      { ressource: Ressource.PIERRE, quantite: 10 },
      { ressource: Ressource.NOURRITURE, quantite: 10 },
      { ressource: Ressource.OR, quantite: 1 }
    ]
  }

];