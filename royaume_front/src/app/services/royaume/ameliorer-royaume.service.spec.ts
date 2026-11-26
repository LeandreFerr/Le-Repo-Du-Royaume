import { TestBed } from '@angular/core/testing';
import { AmeliorerRoyaumeService } from './ameliorer-royaume.service';

describe('AmeliorerRoyaumeService', () => {
  let service: AmeliorerRoyaumeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AmeliorerRoyaumeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
