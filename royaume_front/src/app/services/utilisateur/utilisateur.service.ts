import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UtilisateurRequest {
  pseudo: string;
  email: string;
  password: string;
}

export interface UtilisateurResponse {
  id: number;
  pseudo: string;
  email: string;
  online: boolean;
  admin: boolean;
  royaumeId: number | null;
  royaumeNom: string | null;
}



export interface InscriptionRequest {
  pseudo: string;
  email: string;
  password: string;
}


@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {


  //private apiUrl = 'http://localhost:8080/api/utilisateurs';
  private apiUrl = 'http://localhost:8090/api/utilisateurs';

  constructor(private http: HttpClient) { }

  login(pseudo: string, password: string): Observable<UtilisateurResponse> {

    return this.http.post<UtilisateurResponse>(
      `${this.apiUrl}/login`,
      { pseudo, password }
    );
  }

  inscrire(request: InscriptionRequest): Observable<UtilisateurResponse> {
    return this.http.post<UtilisateurResponse>(this.apiUrl, request);
  }

  logout(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/logout`, {});
  }


}