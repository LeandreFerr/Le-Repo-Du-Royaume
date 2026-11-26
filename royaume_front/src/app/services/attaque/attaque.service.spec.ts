import { TestBed } from '@angular/core/testing';
import { Attaque } from '../attaque/attaque.service';

describe('Attaque', () => {
  let service: Attaque;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Attaque);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
