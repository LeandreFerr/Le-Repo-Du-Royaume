import { Routes } from '@angular/router';

import { AdminComponent } from './pages/admin/admin.component';
import { PlayerComponent } from './pages/player/player.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'player',
    pathMatch: 'full',
  },
  {
    path: 'player',
    component: PlayerComponent,
  },
  {
    path: 'admin',
    component: AdminComponent,
  },
  {
    path: '**',
    redirectTo: 'player',
  },
];
