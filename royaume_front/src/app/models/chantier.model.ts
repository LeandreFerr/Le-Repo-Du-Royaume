export interface Chantier {

    id: number;

    ouvriersMinimum: number;
    ouvriersAssignes: number;

    tempsBaseSecondes: number;

    amelioration: boolean;

    dateDebut: string;
    dateFinPrevue: string;

}