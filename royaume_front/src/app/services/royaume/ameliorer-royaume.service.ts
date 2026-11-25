import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { PalierNiveau } from '../../models/palier-niveau.model';

@Injectable({
  providedIn: 'root'
})
export class AmeliorerRoyaumeService {

  private apiUrl = 'http://localhost:8080/api/royaumes';

  constructor(private http: HttpClient) {}

  // Récupérer le prochain palier
  obtenirProchainPalier(
    royaumeId: number
  ): Observable<PalierNiveau> {

    return this.http.get<PalierNiveau>(
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