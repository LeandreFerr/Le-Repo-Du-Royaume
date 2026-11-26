import { AmeliorationBatiment } from '../models/amelioration-batiment.model';
import { Ressource } from '../models/ressource.model';

export const MOCK_AMELIORATIONS_BATIMENTS: AmeliorationBatiment[] = [

  // ========================================
  // SCIERIE — Niveau 2 vers niveau 3
  // ========================================

  {
    batimentId: 2,

    niveauActuel: 2,
    niveauSuivant: 3,
    niveauChateauRequis: 3,

    tempsConstructionSecondes: 180,
    ouvriersMinimum: 1,

    coutAmelioration: [
      { ressource: Ressource.BOIS, quantite: 100 },
      { ressource: Ressource.PIERRE, quantite: 180 },
      { ressource: Ressource.NOURRITURE, quantite: 140 },
      { ressource: Ressource.OR, quantite: 80 }
    ]
  },


  // ========================================
  // CARRIÈRE — Niveau 1 vers niveau 2
  // ========================================

  {
    batimentId: 3,

    niveauActuel: 1,
    niveauSuivant: 2,
    niveauChateauRequis: 2,

    tempsConstructionSecondes: 180,
    ouvriersMinimum: 1,

    coutAmelioration: [
      { ressource: Ressource.BOIS, quantite: 80 },
      { ressource: Ressource.PIERRE, quantite: 60 },
      { ressource: Ressource.NOURRITURE, quantite: 100 },
      { ressource: Ressource.OR, quantite: 40 }
    ]
  },


  // ========================================
  // FERME — Niveau 2 vers niveau 3
  // ========================================

  {
    batimentId: 4,

    niveauActuel: 2,
    niveauSuivant: 3,
    niveauChateauRequis: 3,

    tempsConstructionSecondes: 180,
    ouvriersMinimum: 1,

    coutAmelioration: [
      { ressource: Ressource.BOIS, quantite: 180 },
      { ressource: Ressource.PIERRE, quantite: 180 },
      { ressource: Ressource.NOURRITURE, quantite: 140 },
      { ressource: Ressource.OR, quantite: 120 }
    ]
  },


  // ========================================
  // MINE D'OR — Niveau 1 vers niveau 2
  // ========================================

  {
    batimentId: 5,

    niveauActuel: 1,
    niveauSuivant: 2,
    niveauChateauRequis: 2,

    tempsConstructionSecondes: 180,
    ouvriersMinimum: 1,

    coutAmelioration: [
      { ressource: Ressource.BOIS, quantite: 100 },
      { ressource: Ressource.PIERRE, quantite: 80 },
      { ressource: Ressource.NOURRITURE, quantite: 60 },
      { ressource: Ressource.OR, quantite: 40 }
    ]
  },


  // ========================================
  // MAISON — Niveau 1 vers niveau 2
  // ========================================

  {
    batimentId: 6,

    niveauActuel: 1,
    niveauSuivant: 2,
    niveauChateauRequis: 2,

    tempsConstructionSecondes: 120,
    ouvriersMinimum: 1,

    coutAmelioration: [
      { ressource: Ressource.BOIS, quantite: 10 },
      { ressource: Ressource.PIERRE, quantite: 10 },
      { ressource: Ressource.NOURRITURE, quantite: 10 },
      { ressource: Ressource.OR, quantite: 1 }
    ]
  }

];