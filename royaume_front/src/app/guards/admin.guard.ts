import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session/session.service';

export const adminGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);

  const utilisateur = session.utilisateur();

  if (!utilisateur) {
    return router.createUrlTree(['/login']);      // 1. pas connecté
  }
  if (!utilisateur.admin) {
    return router.createUrlTree(['/player']);     // 2. connecté mais pas admin
  }
  return true;
};