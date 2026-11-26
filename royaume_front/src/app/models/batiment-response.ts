
export interface BatimentResponse {
  id: number;
  nom: string;
  niveau: number;
  pointsDeVie: number;
  pointsDeVieMax: number;
  enConstruction: boolean;
  montantReparationEnCours: number;
  royaumeId: number;

  typeRessource: string | null;
  productionBaseHeure: number | null;

  bonusPropre: number | null;

  niveauPalierDebloque: number | null;
  bonusDefenseRoyaume: number | null;
}
