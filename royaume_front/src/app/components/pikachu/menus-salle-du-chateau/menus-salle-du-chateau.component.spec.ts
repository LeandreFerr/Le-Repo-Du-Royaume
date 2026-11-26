import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MenusSalleDuChateauComponent} from './menus-salle-du-chateau.component';

describe('MenusSalleDuChateau', () => {
  let component: MenusSalleDuChateauComponent;
  let fixture: ComponentFixture<MenusSalleDuChateauComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenusSalleDuChateauComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MenusSalleDuChateauComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
