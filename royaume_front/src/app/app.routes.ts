import { Routes } from '@angular/router';
import { CarteRoyaumeSandboxComponent } from './components/carte-royaume/carte-royaume-sandbox.component';
import { PageLogin} from './pages/page-login/page-login.component';
<<<<<<< HEAD
=======
import {PlaceDuMarcheComponent} from './components/place-du-marche/place-du-marche.component';
>>>>>>> Modul_6_pikachu

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
<<<<<<< HEAD
];
=======
   {
    path: 'pikachu/menus-salle-du-chateau',
    loadComponent: () =>
      import('./components/pikachu/menus-salle-du-chateau/menus-salle-du-chateau.component')
        .then(m => m.MenusSalleDuChateauComponent)
  },
  {
    path: 'place-du-marche',
    component: PlaceDuMarcheComponent
  }
];
>>>>>>> Modul_6_pikachu
