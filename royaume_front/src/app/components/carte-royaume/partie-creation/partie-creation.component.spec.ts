import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PartieCreation } from './partie-creation.component';

describe('PartieCreation', () => {
  let component: PartieCreation;
  let fixture: ComponentFixture<PartieCreation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PartieCreation],
    }).compileComponents();

    fixture = TestBed.createComponent(PartieCreation);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
