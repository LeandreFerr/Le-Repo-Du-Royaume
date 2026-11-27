
import { Ressource } from './ressource.model';

export enum TypeBatiment {
    CHATEAU = 'CHATEAU',
    MAISON = 'MAISON',
    PRODUCTION = 'PRODUCTION'
}

export interface Batiment {

    id: number;
    nom: string;
    type: TypeBatiment;

    niveau: number;

    pointsDeVie: number;
    pointsDeVieMax: number;

    enConstruction: boolean;

    montantReparationEnCours: number;

    // Bâtiments de production
    typeRessource?: Ressource;
    productionBaseHeure?: number;

    // Maison
    bonusPropre?: number;

    // Château
    niveauPalierDebloque?: number;
    bonusDefenseRoyaume?: number;
}
