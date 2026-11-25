import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReglesDuJeuManuscript } from './regles-du-jeu-manuscript.component';

describe('ReglesDuJeuManuscript', () => {
  let component: ReglesDuJeuManuscript;
  let fixture: ComponentFixture<ReglesDuJeuManuscript>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglesDuJeuManuscript],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglesDuJeuManuscript);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
