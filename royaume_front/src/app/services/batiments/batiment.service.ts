
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { BatimentResponse } from '../../models/batiment-response';

@Injectable({
  providedIn: 'root'
})
export class BatimentService {

  private apiUrl = 'http://localhost:8080/api/batiments';

  constructor(private http: HttpClient) {}

  creerMaison(
    royaumeId: number,
    nom: string,
    ouvriers: number
  ): Observable<BatimentResponse> {

    const params = {
      royaumeId: royaumeId.toString(),
      nom,
      ouvriers: ouvriers.toString()
    };

    return this.http.post<BatimentResponse>(
      `${this.apiUrl}/maisons`,
      null,
      { params }
    );
  }

  creerProduction(
    royaumeId: number,
    nom: string,
    pointsDeVie: number,
    typeRessource: string,
    productionBaseHeure: number,
    ouvriers: number
  ): Observable<BatimentResponse> {

    const params = {
      royaumeId: royaumeId.toString(),
      nom,
      pointsDeVie: pointsDeVie.toString(),
      typeRessource,
      productionBaseHeure: productionBaseHeure.toString(),
      ouvriers: ouvriers.toString()
    };

    return this.http.post<BatimentResponse>(
      `${this.apiUrl}/production`,
      null,
      { params }
    );
  }

  ameliorer(
    batimentId: number,
    ouvriers: number
  ): Observable<BatimentResponse> {

    return this.http.post<BatimentResponse>(
      `${this.apiUrl}/${batimentId}/ameliorer`,
      null,
      { params: { ouvriers: ouvriers.toString() } }
    );
  }

  reparer(
    batimentId: number,
    montant: number,
    ouvriers: number
  ): Observable<BatimentResponse> {

    return this.http.post<BatimentResponse>(
      `${this.apiUrl}/${batimentId}/reparer`,
      null,
      {
        params: {
          montant: montant.toString(),
          ouvriers: ouvriers.toString()
        }
      }
    );
  }
}
