import { QuantiteRessource } from './ressource.model';

export interface AmeliorationBatiment {

  batimentId: number;

  niveauActuel: number;

  niveauSuivant: number;

  niveauChateauRequis: number;

  coutAmelioration: QuantiteRessource[];

  tempsConstructionSecondes: number;

  ouvriersMinimum: number;

}