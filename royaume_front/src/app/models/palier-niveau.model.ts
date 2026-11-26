
export interface QuantiteRessourceDTO {
  ressource: string;
  quantite: number;
}

export interface PalierNiveau {
  id: number;
  niveau: number;
  populationRequise: number;
  prerequis: QuantiteRessourceDTO[];
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
