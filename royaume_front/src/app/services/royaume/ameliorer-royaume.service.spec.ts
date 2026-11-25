import { TestBed } from '@angular/core/testing';
import { AmeliorerRoyaume } from './ameliorer-royaume.service';

describe('AmeliorerRoyaume', () => {
  let service: AmeliorerRoyaume;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AmeliorerRoyaume);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
