import { QuantiteRessource } from './ressource.model';

export interface PalierNiveau {

  id: number;
  niveau: number;
  populationRequise: number;

  prerequis: QuantiteRessource[];

}