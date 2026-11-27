import { HttpClient } from '@angular/common/http';
import { inject, Injectable, Service } from '@angular/core';
import { PartieCreationRequest } from '../models/partie-creation-request';
import { Observable } from 'rxjs';
import { PartieResponse } from '../models/partie-response';
import { StatutPartie } from '../models/statut-partie';

@Injectable({
  providedIn: 'root'
})
export class Partie {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8090/api/parties';

  trouverToutes(): Observable<PartieResponse[]> {
    return this.http.get<PartieResponse[]>(this.apiUrl);
  }

  trouverParId(id: number): Observable<PartieResponse> {
    return this.http.get<PartieResponse>(
      `${this.apiUrl}/${id}`
    );
  }

  trouverParStatut(
    statut: StatutPartie
  ): Observable<PartieResponse[]> {
    return this.http.get<PartieResponse[]>(
      `${this.apiUrl}/statut/${statut}`
    );
  }

  creer(
    request: PartieCreationRequest
  ): Observable<PartieResponse> {
    return this.http.post<PartieResponse>(
      this.apiUrl,
      request
    );
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }

  demarrer(id: number): Observable<PartieResponse> {
    return this.http.patch<PartieResponse>(
      `${this.apiUrl}/${id}/demarrer`,
      null
    );
  }

  terminer(id: number): Observable<PartieResponse> {
    return this.http.patch<PartieResponse>(
      `${this.apiUrl}/${id}/terminer`,
      null
    );
  }
}
