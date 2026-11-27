
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MessageRequest {
  contenu: string;
  partieId: number;
  expediteurId: number;
  destinataireId: number;
}

export interface MessageResponse {
  id: number;
  contenu: string;
  dateEnvoi: string;
  lu: boolean;
  partieId: number;
  expediteurId: number;
  expediteurPseudo: string;
  destinataireId: number;
  destinatairePseudo: string;
}

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  private apiUrl = '/api/messages';

  constructor(private http: HttpClient) {}

  envoyerMessage(request: MessageRequest): Observable<MessageResponse> {

    return this.http.post<MessageResponse>(
      `${this.apiUrl}/envoyer`,
      request
    );
  }
}
