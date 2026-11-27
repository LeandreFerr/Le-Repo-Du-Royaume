import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { PopulationResponse } from '../models/population-response';

@Injectable({
  providedIn: 'root'
})
export class PopulationService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/populations';

  trouverToutes(): Observable<PopulationResponse[]> {
    return this.http.get<PopulationResponse[]>(
      this.apiUrl
    );
  }
}