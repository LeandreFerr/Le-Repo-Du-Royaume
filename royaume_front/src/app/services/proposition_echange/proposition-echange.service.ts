
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type Ressource =
  | 'PIERRE'
  | 'BOIS'
  | 'OR'
  | 'NOURRITURE';

export type StatutEchange =
  | 'EN_ATTENTE'
  | 'ACCEPTE'
  | 'REFUSE'
  | 'ANNULE';

export interface QuantiteRessource {
  ressource: Ressource;
  quantite: number;
}

export interface PropositionEchangeRequest {
  expediteurId: number;
  offerte: QuantiteRessource;
  demandee: QuantiteRessource;
}

export interface PropositionEchangeResponse {
  id: number;
  dateCreation: string;
  statut: StatutEchange;
  expediteurId: number;
  expediteurNom: string;
  destinataireId: number;
  destinataireNom: string;
  offerte: QuantiteRessource;
  demandee: QuantiteRessource;
}

@Injectable({
  providedIn: 'root'
})
export class PropositionEchangeService {

  private apiUrl =
    'http://localhost:8080/api/propositions-echange';

  constructor(private http: HttpClient) {}

  // Proposer un échange

  creerProposition(
    proposition: PropositionEchangeRequest
  ): Observable<PropositionEchangeResponse> {

    return this.http.post<PropositionEchangeResponse>(
      this.apiUrl,
      proposition
    );
  }

  // Récupérer toutes les propositions

  recupererPropositions():
    Observable<PropositionEchangeResponse[]> {

    return this.http.get<PropositionEchangeResponse[]>(
      this.apiUrl
    );
  }

  // Récupérer les propositions destinées à un royaume

  recupererPropositionsDestinataire(
    destinataireId: number
  ): Observable<PropositionEchangeResponse[]> {

    return this.http.get<PropositionEchangeResponse[]>(
      `${this.apiUrl}/destinataire/${destinataireId}`
    );
  }
}
