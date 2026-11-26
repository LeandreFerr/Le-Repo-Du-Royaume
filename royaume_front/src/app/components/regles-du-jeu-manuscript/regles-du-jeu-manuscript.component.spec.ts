import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReglesDuJeuManuscriptComponent } from './regles-du-jeu-manuscript.component';

describe('ReglesDuJeuManuscriptComponent', () => {
  let component: ReglesDuJeuManuscriptComponent;
  let fixture: ComponentFixture<ReglesDuJeuManuscriptComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglesDuJeuManuscriptComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglesDuJeuManuscriptComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
