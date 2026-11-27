import { TestBed } from '@angular/core/testing';
import { Entrainement } from './entrainement.service';

describe('Entrainement', () => {
  let service: Entrainement;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Entrainement);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
