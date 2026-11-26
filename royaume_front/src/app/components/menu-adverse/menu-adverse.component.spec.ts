import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MenuAdverseComponent } from './menu-adverse.component';

describe('MenuAdverseComponent', () => {
  let component: MenuAdverseComponent;
  let fixture: ComponentFixture<MenuAdverseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuAdverseComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MenuAdverseComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
