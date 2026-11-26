import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PagePartieCreation } from './page-partie-creation.component';

describe('PagePartieCreation', () => {
  let component: PagePartieCreation;
  let fixture: ComponentFixture<PagePartieCreation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PagePartieCreation],
    }).compileComponents();

    fixture = TestBed.createComponent(PagePartieCreation);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
