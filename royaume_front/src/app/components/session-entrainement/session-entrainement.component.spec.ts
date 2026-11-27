import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SessionEntrainement } from './session-entrainement.component';

describe('SessionEntrainement', () => {
  let component: SessionEntrainement;
  let fixture: ComponentFixture<SessionEntrainement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SessionEntrainement],
    }).compileComponents();

    fixture = TestBed.createComponent(SessionEntrainement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
