import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session/session.service';

export const playerGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);

  const utilisateur = session.utilisateur();
  if (!utilisateur) {
    return true;
  }
  return router.createUrlTree([utilisateur.admin ? '/admin' : '/player']);
};