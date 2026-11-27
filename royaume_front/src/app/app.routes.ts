import { Routes } from '@angular/router';
import { PartieCreation } from './components/carte-royaume/partie-creation/partie-creation.component';
import { AdminComponent } from './pages/admin/admin.component';
import { PageInscription } from './pages/page-inscription/page-inscription.component';
import { PageLogin } from './pages/page-login/page-login.component';
import { PlayerComponent } from './pages/player/player.component';
import { PlaceDuMarcheComponent } from './components/place-du-marche/place-du-marche.component';
import { MenuAdverseComponent } from './components/menu-adverse/menu-adverse.component';
import { VisuAttaques } from './components/visu-attaques/visu-attaques.component';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import { playerGuard } from './guards/player.guard';


import { inject } from '@angular/core';
import { SessionService } from './services/session/session.service';


// accueil redirige suivant admin ou player
// Si pas de session, alors redirige vers login
const accueil = () => {
  const utilisateur = inject(SessionService).utilisateur();
  if (!utilisateur) {
    return '/login';
  }
  return utilisateur.admin ? '/admin' : '/player';
};



export const routes: Routes = [

  // Publique
  { path: 'login', component: PageLogin, canActivate: [playerGuard] },
  { path: 'inscription', component: PageInscription, canActivate: [playerGuard] },

  { path: 'admin', component: AdminComponent, canActivate: [adminGuard] },


  // user connecté
  { path: 'parties/creation', component: PartieCreation, canActivate: [authGuard] },
  { path: 'player', component: PlayerComponent, canActivate: [authGuard] },
  { path: 'place-du-marche', component: PlaceDuMarcheComponent, canActivate: [authGuard] },
  { path: 'menu-adverse', component: MenuAdverseComponent, canActivate: [authGuard] },
  { path: 'attaques', component: VisuAttaques, canActivate: [authGuard] },

  { path: 'carte-sandbox', redirectTo: 'admin', pathMatch: 'full' },
  {
    path: 'pikachu/menus-salle-du-chateau',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./components/pikachu/menus-salle-du-chateau/menus-salle-du-chateau.component')
        .then(m => m.MenusSalleDuChateauComponent),
  },

  // redirection basique 
  { path: '', redirectTo: accueil, pathMatch: 'full' },

  // URL inconnue
  { path: '**', redirectTo: accueil },


];
