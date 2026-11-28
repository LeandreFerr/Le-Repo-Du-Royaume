import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClassementRoyaumes } from './classement-royaumes.component';

describe('ClassementRoyaumes', () => {
  let component: ClassementRoyaumes;
  let fixture: ComponentFixture<ClassementRoyaumes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClassementRoyaumes],
    }).compileComponents();

    fixture = TestBed.createComponent(ClassementRoyaumes);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
