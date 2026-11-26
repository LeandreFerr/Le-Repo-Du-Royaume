
export interface ChantierConstructionResponse {
  id: number;
  batimentId: number;
  royaumeId: number;
  dateDebut: string;
  dateFinPrevue: string;
  ouvriersMinimum: number;
  ouvriersAssignes: number;
  tempsBaseSecondes: number;
  amelioration: boolean;
}
