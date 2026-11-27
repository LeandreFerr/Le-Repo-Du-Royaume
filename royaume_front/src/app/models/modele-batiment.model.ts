import { TypeBatiment } from './batiment.model';
import { Ressource, QuantiteRessource } from './ressource.model';
export interface ModeleBatiment {

  id: number;

  nom: string;
  description: string;

  type: TypeBatiment;
  typeRessource?: Ressource;

  icone: string;

  //quantiteMax: number | null;

  niveauRoyaumeRequis: number;

  coutConstruction: QuantiteRessource[];

  tempsConstructionSecondes: number;

  ouvriersMinimum: number;

  // Paramètres nécessaires à la création
  pointsDeVie?: number;

  productionBaseHeure?: number;

}