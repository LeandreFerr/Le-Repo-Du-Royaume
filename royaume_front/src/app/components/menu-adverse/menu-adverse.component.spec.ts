import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MenuAdverse } from './menu-adverse.component';

describe('MenuAdverse', () => {
  let component: MenuAdverse;
  let fixture: ComponentFixture<MenuAdverse>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuAdverse],
    }).compileComponents();

    fixture = TestBed.createComponent(MenuAdverse);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
