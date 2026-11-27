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