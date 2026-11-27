import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PageLogin } from './page-login.component';

describe('PageLogin', () => {
  let component: PageLogin;
  let fixture: ComponentFixture<PageLogin>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ providers: [provideRouter([])],
      imports: [PageLogin]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PageLogin);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
