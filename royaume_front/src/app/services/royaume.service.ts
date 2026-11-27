import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { RoyaumeCreationRequest } from '../models/royaume-creation-request';
import { RoyaumeResponse } from '../models/royaume-response';

@Injectable({
  providedIn: 'root'
})
export class RoyaumeService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/royaumes';

  creer(request: RoyaumeCreationRequest): Observable<RoyaumeResponse> {
    return this.http.post<RoyaumeResponse>(this.apiUrl, request);
  }
}
