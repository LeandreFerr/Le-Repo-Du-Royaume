import { TestBed } from '@angular/core/testing';
import { AttaqueService } from './attaque.service';

describe('AttaqueService', () => {
  let service: AttaqueService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AttaqueService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
