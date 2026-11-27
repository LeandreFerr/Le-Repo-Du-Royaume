import { QuantiteRessource } from './quantite-ressource';
import { ContingentTroupeResponse } from './contingent-troupe-response';
import { BatimentResponse } from './batiment-response';
import { PopulationResponse } from './population-response';

export interface RoyaumeResponse {
  id: number;
  nom: string;
  peuple: string | PopulationResponse | null;
  pseudoJoueur?: string;
  utilisateurId?: number | null;
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
