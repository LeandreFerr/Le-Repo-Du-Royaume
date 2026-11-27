import { Population } from './population.model';
import { QuantiteRessource } from './ressource.model';
import { Batiment } from './batiment.model';

export interface Royaume {
  id: number;
  nom: string;
  peuple: Population;
  niveau: number;
  pseudoJoueur: string;
  utilisateurId?: number | null;
  populationDisponible: number;
  populationOuvriere: number;
  populationEnEntrainement: number;
  populationMilitaire: number;
  enRuine: boolean;
  bonusCroissance: number;
  ressources: QuantiteRessource[];
  batiments: Batiment[];
  x?: number | null;
  y?: number | null;
}
