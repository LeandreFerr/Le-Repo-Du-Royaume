
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type Ressource =
  | 'PIERRE'
  | 'BOIS'
  | 'OR'
  | 'NOURRITURE';

export type StatutOffre =
  | 'DISPONIBLE'
  | 'CONCLUE'
  | 'ANNULEE';

export interface QuantiteRessource {
  ressource: Ressource;
  quantite: number;
}

export interface OffreMarcheRequest {
  vendeurId: number;
  propose: QuantiteRessource;
  demande: QuantiteRessource;
}

export interface OffreMarcheResponse {
  id: number;
  datePublication: string;
  statut: StatutOffre;
  vendeurId: number;
  vendeurNom: string;
  propose: QuantiteRessource;
  demande: QuantiteRessource;
}
// =========================================
// SERVICE MARCHE
// =========================================

@Injectable({
  providedIn: 'root'
})
export class MarcheService {

  private apiUrl = 'http://localhost:8080/api/marche';

  constructor(private http: HttpClient) {}

  // =========================================
  // 1. PUBLIER UNE OFFRE
  // =========================================

  publierOffre(
    offre: OffreMarcheRequest
  ): Observable<OffreMarcheResponse> {

    return this.http.post<OffreMarcheResponse>(
      `${this.apiUrl}/offres`,
      offre
    );
  }

  // =========================================
  // 2. RECUPERER TOUTES LES OFFRES
  // =========================================

  recupererOffres(): Observable<OffreMarcheResponse[]> {

    return this.http.get<OffreMarcheResponse[]>(
      `${this.apiUrl}/offres`
    );
  }

  // =========================================
  // 3. RECUPERER UNE OFFRE
  // =========================================

  recupererOffre(
    id: number
  ): Observable<OffreMarcheResponse> {

    return this.http.get<OffreMarcheResponse>(
      `${this.apiUrl}/offres/${id}`
    );
  }

  // =========================================
  // 4. RECUPERER LES OFFRES D'UN VENDEUR
  // =========================================

  recupererOffresRoyaume(
    vendeurId: number
  ): Observable<OffreMarcheResponse[]> {

    return this.http.get<OffreMarcheResponse[]>(
      `${this.apiUrl}/offres/vendeur/${vendeurId}`
    );
  }

  // =========================================
  // 5. ANNULER UNE OFFRE
  // =========================================

  annulerOffre(id: number): Observable<void> {

    return this.http.delete<void>(
      `${this.apiUrl}/offres/${id}`
    );
  }

  // =========================================
  // 6. ACCEPTER UNE OFFRE
  // =========================================

  accepterOffre(id: number): Observable<void> {

    return this.http.post<void>(
      `${this.apiUrl}/offres/${id}/accepter`,
      {}
    );
  }
}
