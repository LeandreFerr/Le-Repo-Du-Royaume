import { Routes } from '@angular/router';
import { CarteRoyaumeSandboxComponent } from './components/carte-royaume/carte-royaume-sandbox.component';
import { PageLogin} from './pages/page-login/page-login.component';

export const routes: Routes = [
  {
    path: 'login',
    component: PageLogin,
  },
  {
    path: 'carte-sandbox',
    component: CarteRoyaumeSandboxComponent,
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
   {
    path: 'pikachu/menus-salle-du-chateau',
    loadComponent: () =>
      import('./components/pikachu/menus-salle-du-chateau/menus-salle-du-chateau.component')
        .then(m => m.MenusSalleDuChateauComponent)
  }
];
/*
import { Routes } from '@angular/router';

import { PageLoginComponent } from './pages/page-login/page-login.component';

import {
  MenusSalleDuChateauComponent
} from './components/pikachu/menus-salle-du-chateau/menus-salle-du-chateau.component';

export const routes: Routes = [

  // Page d'accueil : connexion
  {
    path: '',
    component: PageLoginComponent
  },

  // Salle du château
  {
    path: 'salle-du-chateau',
    component: MenusSalleDuChateauComponent
  },

  // Route inconnue
  {
    path: '**',
    redirectTo: ''
  }

];*/