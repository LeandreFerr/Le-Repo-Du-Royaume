import { TestBed } from '@angular/core/testing';
import { PropositionEchangeService } from './proposition-echange.service';

describe('PropositionEchangeService', () => {
  let service: PropositionEchangeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PropositionEchangeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
