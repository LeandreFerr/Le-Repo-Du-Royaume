
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { AmeliorerRoyaumeResponse } from '../../models/palier-niveau.model';
import { Royaume } from '../../models/royaume.model';

@Injectable({
  providedIn: 'root'
})
export class AmeliorerRoyaumeService {

  private readonly apiUrl = 'http://localhost:8080/api/royaumes';

  constructor(private readonly http: HttpClient) {}

  // Récupérer un royaume depuis le backend
  obtenirRoyaume(royaumeId: number): Observable<Royaume> {

    return this.http.get<Royaume>(
      `${this.apiUrl}/${royaumeId}`
    );
  }

  // Récupérer les informations du prochain palier
  obtenirProchainPalier(
    royaumeId: number
  ): Observable<AmeliorerRoyaumeResponse> {

    return this.http.get<AmeliorerRoyaumeResponse>(
      `${this.apiUrl}/${royaumeId}/prochain-palier`
    );
  }

  // Améliorer le royaume
  ameliorerRoyaume(
    royaumeId: number
  ): Observable<string> {

    return this.http.post(
      `${this.apiUrl}/${royaumeId}/ameliorer`,
      null,
      { responseType: 'text' }
    );
  }
}
