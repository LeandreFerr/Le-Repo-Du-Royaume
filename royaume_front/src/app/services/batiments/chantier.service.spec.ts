import { TestBed } from '@angular/core/testing';
import { Chantier } from './chantier.service';

describe('Chantier', () => {
  let service: Chantier;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Chantier);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
