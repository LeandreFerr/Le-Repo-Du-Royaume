import { TestBed } from '@angular/core/testing';
import { RoyaumeService } from './royaume-service.service';

describe('RoyaumeService', () => {
  let service: RoyaumeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RoyaumeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
