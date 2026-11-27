import { EmplacementCarteResponse } from './emplacement-carte-response';

export interface CarteResponse {
  id: number;
  largeur: number;
  hauteur: number;
  centreX: number;
  centreY: number;
  partieId: number;
  emplacements: EmplacementCarteResponse[];
  emplacementMarche: EmplacementCarteResponse;
  archetype?: string;
  seed?: string;
}
