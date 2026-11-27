
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  AmeliorerRoyaumeResponse
} from '../../models/palier-niveau.model';

@Injectable({
  providedIn: 'root'
})
export class AmeliorerRoyaumeService {

  private apiUrl = '/api/royaumes';

  constructor(private http: HttpClient) {}

  // Récupérer toutes les informations du prochain palier
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
