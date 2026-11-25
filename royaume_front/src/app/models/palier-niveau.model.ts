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