import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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
  private apiUrl = 'http://localhost:8080/api/attaques';

  constructor(private http: HttpClient) {}
  
  listerToutes(): Observable<AttaqueResponse[]> {
    return this.http.get<AttaqueResponse[]>(this.apiUrl);
  }
}