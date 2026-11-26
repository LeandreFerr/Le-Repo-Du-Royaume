import { Routes } from '@angular/router';
import { PartieCreation } from './components/carte-royaume/partie-creation/partie-creation.component';
import { PageLogin } from './pages/page-login/page-login.component';
import { AdminComponent } from './pages/admin/admin.component';
import { PlayerComponent } from './pages/player/player.component';

export const routes: Routes = [
  { path: 'login', component: PageLogin },
  { path: 'parties/creation', component: PartieCreation },
  { path: 'player', component: PlayerComponent },
  { path: 'admin', component: AdminComponent },
  { path: 'carte-sandbox', redirectTo: 'admin', pathMatch: 'full' },
  { path: '', redirectTo: 'player', pathMatch: 'full' },
];



