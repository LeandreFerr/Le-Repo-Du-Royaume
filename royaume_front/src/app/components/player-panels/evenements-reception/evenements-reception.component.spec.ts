import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EvenementsReception } from './evenements-reception.component';

describe('EvenementsReception', () => {
  let component: EvenementsReception;
  let fixture: ComponentFixture<EvenementsReception>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvenementsReception],
    }).compileComponents();

    fixture = TestBed.createComponent(EvenementsReception);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
