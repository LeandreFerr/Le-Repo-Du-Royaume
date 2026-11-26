import { Routes } from '@angular/router';
import { PartieCreation } from './components/carte-royaume/partie-creation/partie-creation.component';
import { AdminComponent } from './pages/admin/admin.component';
import { PageInscription } from './pages/page-inscription/page-inscription.component';
import { PageLogin } from './pages/page-login/page-login.component';
import { PlayerComponent } from './pages/player/player.component';
import { PlaceDuMarcheComponent } from './components/place-du-marche/place-du-marche.component';
import { MenuAdverseComponent } from './components/menu-adverse/menu-adverse.component';
import { VisuAttaques } from './components/visu-attaques/visu-attaques.component';


export const routes: Routes = [
  { path: 'login', component: PageLogin },
  { path: 'parties/creation', component: PartieCreation },
  { path: 'player', component: PlayerComponent },
  { path: 'admin', component: AdminComponent },
  { path: 'carte-sandbox', redirectTo: 'admin', pathMatch: 'full' },
  {
    path: 'pikachu/menus-salle-du-chateau',
    loadComponent: () =>
      import('./components/pikachu/menus-salle-du-chateau/menus-salle-du-chateau.component')
        .then(m => m.MenusSalleDuChateauComponent),
  },
  { path: 'place-du-marche', component: PlaceDuMarcheComponent },
  { path: 'menu-adverse', component: MenuAdverseComponent },
  { path: 'attaques', component: VisuAttaques },
  { path: '', redirectTo: 'player', pathMatch: 'full' },
  { path: 'inscription', component: PageInscription },

];
