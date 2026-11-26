import { Routes } from '@angular/router';
import { PartieCreation } from './components/carte-royaume/partie-creation/partie-creation.component';
import { PageLogin } from './pages/page-login/page-login.component';
import { AdminComponent } from './pages/admin/admin.component';
import { PlayerComponent } from './pages/player/player.component';
import { PageInscription } from './pages/page-inscription/page-inscription.component';
import { PageLobby } from './pages/page-lobby/page-lobby.component';
import { PageCreationRoyaume } from './pages/royaume/page-creation-royaume/page-creation-royaume.component';


export const routes: Routes = [
  { path: 'login', component: PageLogin },
  { path: 'parties/creation', component: PartieCreation },
  { path: 'player', component: PlayerComponent },
  { path: 'admin', component: AdminComponent },
  { path: 'carte-sandbox', redirectTo: 'admin', pathMatch: 'full' },
  { path: '', redirectTo: 'player', pathMatch: 'full' },
  { path: 'inscription', component: PageInscription },
  {path: 'lobby',component: PageLobby},
  {path: 'parties/:partieId/royaume/creation',component: PageCreationRoyaume}
];



