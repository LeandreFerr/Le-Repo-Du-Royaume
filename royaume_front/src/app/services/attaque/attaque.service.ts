import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ContingentTroupeResponse {
  type: string;
  quantite: number;
}

export interface QuantiteRessourceDTO {
  ressource: string;
  quantite: number;
}

export interface AttaqueResponse {
  id: number;
  attaquantId: number;
  attaquantNom: string;
  defenseurId: number;
  defenseurNom: string;
  troupesEnvoyees: ContingentTroupeResponse[];
  butin: QuantiteRessourceDTO[];
  dateDepart: string;
  dateImpact: string;
  dateRetourPrevue: string | null;
  tempsTrajet: string;
  victoireAttaquant: boolean;
  statut: 'ALLER' | 'RETOUR' | 'TERMINEE';
}

@Injectable({
  providedIn: 'root',
})
export class AttaqueService {
  private apiUrl = '/api/attaques';

  constructor(private http: HttpClient) {}

  lancer(request: {
  attaquantId: number;
  defenseurId: number;
  troupes: Record<string, number>;
}) {
  return this.http.post<AttaqueResponse>(
    `${this.apiUrl}`,
    request
  );
}

trouverTroupesDisponibles(royaumeId: number) {
  return this.http.get<ContingentTroupeResponse[]>(
    `${this.apiUrl}/royaume/${royaumeId}/troupes-disponibles`
  );
}
  
  listerToutes(): Observable<AttaqueResponse[]> {
    return this.http.get<AttaqueResponse[]>(this.apiUrl);
  }
}