import { RoyaumeResponse } from './royaume-response';
import { StatutPartie } from './statut-partie';

export interface PartieResponse {
  id: number;
  nom: string;
  dateCreation: string;
  vitesseJeu: number;
  maxJoueurs: number;
  vainqueurId: number | null;
  vainqueurPseudo: string | null;
  typeVictoire: string | null;
  royaumes: RoyaumeResponse[];
  statut: StatutPartie;
}