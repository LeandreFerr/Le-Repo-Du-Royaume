import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MenusSalleDuChateau } from './menus-salle-du-chateau.component';

describe('MenusSalleDuChateau', () => {
  let component: MenusSalleDuChateau;
  let fixture: ComponentFixture<MenusSalleDuChateau>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenusSalleDuChateau],
    }).compileComponents();

    fixture = TestBed.createComponent(MenusSalleDuChateau);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
