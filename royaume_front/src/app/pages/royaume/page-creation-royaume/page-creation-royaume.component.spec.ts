import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PageCreationRoyaume } from './page-creation-royaume.component';

describe('PageCreationRoyaume', () => {
  let component: PageCreationRoyaume;
  let fixture: ComponentFixture<PageCreationRoyaume>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageCreationRoyaume],
    }).compileComponents();

    fixture = TestBed.createComponent(PageCreationRoyaume);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
