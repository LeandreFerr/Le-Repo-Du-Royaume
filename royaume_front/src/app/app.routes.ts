import { Routes } from '@angular/router';
import { CarteRoyaumeSandboxComponent } from './components/carte-royaume/carte-royaume-sandbox.component';
import { PageLogin} from './pages/page-login/page-login.component';
import { PageInscription } from './pages/page-inscription/page-inscription.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    component: PageLogin,
  },
  {
    path: 'carte-sandbox',
    component: CarteRoyaumeSandboxComponent,
  },
  {
    path: 'inscription',
    component: PageInscription,
  },


];