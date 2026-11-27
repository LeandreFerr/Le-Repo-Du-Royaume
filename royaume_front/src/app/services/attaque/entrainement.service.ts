import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})

export class SessionEntrainementService {
  private http = inject(HttpClient);
  private apiUrl = '/api/sessions-entrainement';

  entrainer(royaumeId: number, type: string, quantite:number): Observable<any> {
    return this.http.post<any>(this.apiUrl, {
      royaumeId,
      type,
      quantite,
    });
  }

  getSessions(royaumeId: number, finalisee: boolean): Observable<any[]> {
    const params = new HttpParams().set('finalisee', String(finalisee));
    return this.http.get<any[]>(`${this.apiUrl}/royaume/${royaumeId}/sessions`, { params });
  }

  getGarnison(royaumeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/royaume/${royaumeId}/garnison`);
  }
}