import { Routes } from '@angular/router';
import { CarteRoyaumeSandboxComponent } from './components/carte-royaume/carte-royaume-sandbox.component';
import { PageLogin} from './pages/page-login/page-login.component';
import { PartieCreation } from './components/carte-royaume/partie-creation/partie-creation.component';

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
    path: 'parties/creation',
    component: PartieCreation
  },
  {
    path: '',
    redirectTo: 'parties/creation',
    pathMatch: 'full'
  }
];