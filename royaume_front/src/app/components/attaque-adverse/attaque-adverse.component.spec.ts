import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AttaqueAdverse } from './attaque-adverse.component';

describe('AttaqueAdverse', () => {
  let component: AttaqueAdverse;
  let fixture: ComponentFixture<AttaqueAdverse>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AttaqueAdverse],
    }).compileComponents();

    fixture = TestBed.createComponent(AttaqueAdverse);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
