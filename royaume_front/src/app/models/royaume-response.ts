import { QuantiteRessource } from './quantite-ressource';
import { ContingentTroupeResponse } from './contingent-troupe-response';
import { BatimentResponse } from './batiment-response';

export interface RoyaumeResponse {
  id: number;
  nom: string;
  peuple: string;
  niveau: number;
  populationDisponible: number;
  populationOuvriere: number;
  populationEnEntrainement: number;
  populationMilitaire: number;
  enRuine: boolean;
  bonusCroissance: number;
  ressources: QuantiteRessource[];
  garnison: ContingentTroupeResponse[];
  batiments: BatimentResponse[];
  x: number | null;
  y: number | null;
}