import { Royaume } from '../models/royaume.model';
import { Ressource } from '../models/ressource.model';
import { PalierNiveau } from '../models/palier-niveau.model';
import { MOCK_BATIMENTS } from './batiments.mock';

// Royaume du joueur

export const MOCK_ROYAUME: Royaume = {

  id: 1,
  nom: 'Valdore',

  peuple: {
    id: 1,
    nom: 'Canards',
    titreSouverain: 'Roi des Canards',
    attraction: 1.0,
    capaciteSpeciale: 'danse des canards'
  },

  niveau: 3,

  populationDisponible: 250,
  populationOuvriere: 100,
  populationEnEntrainement: 20,
  populationMilitaire: 50,

  enRuine: false,
  bonusCroissance: 0.0,

  ressources: [
    {
      ressource: Ressource.BOIS,
      quantite: 125
    },
    {
      ressource: Ressource.PIERRE,
      quantite: 85
    },
    {
      ressource: Ressource.NOURRITURE,
      quantite: 2400
    },
    {
      ressource: Ressource.OR,
      quantite: 160
    }
  ],

  batiments: MOCK_BATIMENTS

};

// Conditions du prochain niveau

export const MOCK_PALIERS: PalierNiveau[] = [

  {
    id: 3,
    niveau: 4,
    populationRequise: 300,

    prerequis: [
      {
        ressource: Ressource.BOIS,
        quantite: 150
      },
      {
        ressource: Ressource.PIERRE,
        quantite: 150
      },
      {
        ressource: Ressource.NOURRITURE,
        quantite: 2000
      },
      {
        ressource: Ressource.OR,
        quantite: 200
      }
    ]
  }

];