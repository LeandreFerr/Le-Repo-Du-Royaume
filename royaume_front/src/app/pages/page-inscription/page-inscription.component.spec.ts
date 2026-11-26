import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PageInscription } from './page-inscription.component';

describe('PageInscription', () => {
  let component: PageInscription;
  let fixture: ComponentFixture<PageInscription>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageInscription],
    }).compileComponents();

    fixture = TestBed.createComponent(PageInscription);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
