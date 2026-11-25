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
    icone: '/img/buildings/scierie.svg'
  },

  {
    id: 2,
    nom: 'Carrière',
    description: 'Permet de produire de la pierre.',
    type: TypeBatiment.PRODUCTION,
    typeRessource: Ressource.PIERRE,
    icone: '/img/buildings/carriere.svg'
  },

  {
    id: 3,
    nom: 'Ferme',
    description: 'Permet de produire de la nourriture.',
    type: TypeBatiment.PRODUCTION,
    typeRessource: Ressource.NOURRITURE,
    icone: '/img/buildings/ferme.svg'
  },

  {
    id: 4,
    nom: 'Mine d’or',
    description: 'Permet de produire de l’or.',
    type: TypeBatiment.PRODUCTION,
    typeRessource: Ressource.OR,
    icone: '/img/buildings/mine.svg'
  },

  {
    id: 5,
    nom: 'Maison',
    description: 'Favorise la croissance démographique.',
    type: TypeBatiment.MAISON,
    icone: '/img/buildings/maison.svg'
  }

];