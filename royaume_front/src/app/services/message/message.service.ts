
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
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

  private apiUrl = 'http://localhost:8080/api/messages';

  constructor(private http: HttpClient) {}

  // ENVOYER UN MESSAGE
  envoyerMessage(
    request: MessageRequest
  ): Observable<MessageResponse> {

    return this.http.post<MessageResponse>(
      `${this.apiUrl}/envoyer`,
      request
    );
  }

  // RÉCUPÉRER LES MESSAGES D'UN JOUEUR
recevoirMessages(
  destinataireId: number
): Observable<MessageResponse[]> {

  return this.http.get<MessageResponse[]>(
    `${this.apiUrl}/recus/${destinataireId}`
  );
}

  // MARQUER UN MESSAGE COMME LU
  marquerCommeLu(
    messageId: number
  ): Observable<void> {

    return this.http.patch<void>(
      `${this.apiUrl}/${messageId}/lu`,
      {}
    );
  }
}
