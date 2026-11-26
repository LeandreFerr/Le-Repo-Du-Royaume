import { Injectable, signal } from '@angular/core';
import { UtilisateurResponse } from '../utilisateur/utilisateur.service';

const CLE = 'utilisateur';

@Injectable({ providedIn: 'root' })
export class SessionService {

  private readonly _utilisateur = signal<UtilisateurResponse | null>(this.lire());

  readonly utilisateur = this._utilisateur.asReadonly();

  enregistrer(utilisateur: UtilisateurResponse): void {
    this._utilisateur.set(utilisateur);
    try {
      sessionStorage.setItem(CLE, JSON.stringify(utilisateur));
    } catch { /* stockage indisponible : la session reste en mémoire */ }
  }

  effacer(): void {
    this._utilisateur.set(null);
    try {
      sessionStorage.removeItem(CLE);
    } catch { /* idem */ }
  }

  private lire(): UtilisateurResponse | null {
    try {
      const brut = sessionStorage.getItem(CLE);
      return brut ? JSON.parse(brut) : null;
    } catch {
      return null;
    }
  }
}