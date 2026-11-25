import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

import { Royaume } from '../models/royaume.model';
import { PalierNiveau } from '../models/palier-niveau.model';

import {
  MOCK_ROYAUME,
  MOCK_PALIERS
} from '../mocks/royaume.mock';


@Injectable({
  providedIn: 'root'
})
export class RoyaumeService {

  getMonRoyaume(): Observable<Royaume> {

    return of(MOCK_ROYAUME);

  }

  getPaliers(): Observable<PalierNiveau[]> {

    return of(MOCK_PALIERS);

  }

}