export enum Ressource {
  PIERRE = 'PIERRE',
  BOIS = 'BOIS',
  OR = 'OR',
  NOURRITURE = 'NOURRITURE'
}

export interface QuantiteRessource {
  quantite: number;
  ressource: Ressource;
}