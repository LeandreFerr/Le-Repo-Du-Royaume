import { Batiment, TypeBatiment } from '../models/batiment.model';
import { Ressource } from '../models/ressource.model';

export const MOCK_BATIMENTS: Batiment[] = [

  {
    id: 1,
    nom: 'Château',
    type: TypeBatiment.CHATEAU,
    niveau: 3,
    pointsDeVie: 9000,
    pointsDeVieMax: 9000,
    enConstruction: false,
    montantReparationEnCours: 0,
    chantier: null,
    niveauPalierDebloque: 3,
    bonusDefenseRoyaume: 0.1
  },

  {
    id: 2,
    nom: 'Scierie',
    type: TypeBatiment.PRODUCTION,
    niveau: 2,
    pointsDeVie: 450,
    pointsDeVieMax: 500,
    enConstruction: false,
    montantReparationEnCours: 0,
    chantier: null,
    typeRessource: Ressource.BOIS,
    productionBaseHeure: 60
  },

  {
    id: 3,
    nom: 'Carrière',
    type: TypeBatiment.PRODUCTION,
    niveau: 1,
    pointsDeVie: 500,
    pointsDeVieMax: 500,
    enConstruction: false,
    montantReparationEnCours: 0,
    chantier: null,
    typeRessource: Ressource.PIERRE,
    productionBaseHeure: 40
  },

  {
    id: 4,
    nom: 'Ferme',
    type: TypeBatiment.PRODUCTION,
    niveau: 2,
    pointsDeVie: 400,
    pointsDeVieMax: 400,
    enConstruction: false,
    montantReparationEnCours: 0,
    chantier: null,
    typeRessource: Ressource.NOURRITURE,
    productionBaseHeure: 120
  },

  {
    id: 5,
    nom: 'Mine d’or',
    type: TypeBatiment.PRODUCTION,
    niveau: 1,
    pointsDeVie: 500,
    pointsDeVieMax: 500,
    enConstruction: false,
    montantReparationEnCours: 0,
    chantier: null,
    typeRessource: Ressource.OR,
    productionBaseHeure: 25
  },

  {
    id: 6,
    nom: 'Maison',
    type: TypeBatiment.MAISON,
    niveau: 1,
    pointsDeVie: 200,
    pointsDeVieMax: 200,
    enConstruction: false,
    montantReparationEnCours: 0,
    chantier: null,
    bonusPropre: 0.02
  },

  {
    id: 7,
    nom: 'Maison',
    type: TypeBatiment.MAISON,
    niveau: 1,
    pointsDeVie: 0,
    pointsDeVieMax: 200,
    enConstruction: true,
    montantReparationEnCours: 0,
    bonusPropre: 0.02,

    chantier: {
      id: 1,
      ouvriersMinimum: 5,
      ouvriersAssignes: 10,
      tempsBaseSecondes: 3600,
      amelioration: false,

      dateDebut: new Date(
        Date.now() - 30 * 60 * 1000
      ).toISOString(),

      dateFinPrevue: new Date(
        Date.now() + 30 * 60 * 1000
      ).toISOString()
    }
  }

];