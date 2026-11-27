import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session/session.service';

export const authGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);
  console.log('authGuard, utilisateur =', session.utilisateur());
  return session.utilisateur() ? true : router.createUrlTree(['/login']);
};