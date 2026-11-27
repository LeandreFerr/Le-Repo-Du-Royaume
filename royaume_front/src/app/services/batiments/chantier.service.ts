
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ChantierConstructionResponse } from '../../models/chantier.model';

@Injectable({
  providedIn: 'root'
})
export class ChantierService {

  private apiUrl = '/api/chantiers';

  constructor(private http: HttpClient) {}

  trouverParBatiment(
    batimentId: number
  ): Observable<ChantierConstructionResponse> {

    return this.http.get<ChantierConstructionResponse>(
      `${this.apiUrl}/batiment/${batimentId}`
    );
  }

  ajouterOuvriers(
    chantierId: number,
    nombre: number
  ): Observable<ChantierConstructionResponse> {

    return this.http.patch<ChantierConstructionResponse>(
      `${this.apiUrl}/${chantierId}/ouvriers/ajouter`,
      null,
      { params: { nombre: nombre.toString() } }
    );
  }

  retirerOuvriers(
    chantierId: number,
    nombre: number
  ): Observable<ChantierConstructionResponse> {

    return this.http.patch<ChantierConstructionResponse>(
      `${this.apiUrl}/${chantierId}/ouvriers/retirer`,
      null,
      { params: { nombre: nombre.toString() } }
    );
  }
}
