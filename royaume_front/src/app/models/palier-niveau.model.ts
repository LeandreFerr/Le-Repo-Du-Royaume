import { QuantiteRessource } from './ressource.model';

// Les deux formes cohabitent : le modele UI (QuantiteRessource) et le DTO du back.
export type QuantiteRessourceDTO = QuantiteRessource;

export interface PalierNiveau {
  id: number;
  niveau: number;
  populationRequise: number;
  prerequis: QuantiteRessource[];
}

export interface RessourcesAmeliorerRoyaumeResponse {
  ressource: string;
  quantitePossedee: number;
  quantiteRequise: number;
  quantiteManquante: number;
  suffisante: boolean;
}

export interface AmeliorerRoyaumeResponse {
  niveauActuel: number;
  prochainPalier: PalierNiveau | null;
  populationDisponible: number;
  populationRequise: number;
  populationManquante: number;
  populationSuffisante: boolean;
  ressources: RessourcesAmeliorerRoyaumeResponse[];
  ameliorationPossible: boolean;
}
