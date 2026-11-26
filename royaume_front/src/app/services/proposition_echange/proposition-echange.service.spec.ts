import { TestBed } from '@angular/core/testing';
import { PropositionEchange } from './proposition-echange.service';

describe('PropositionEchange', () => {
  let service: PropositionEchange;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PropositionEchange);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
