import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PageLobby } from './page-lobby.component';

describe('PageLobby', () => {
  let component: PageLobby;
  let fixture: ComponentFixture<PageLobby>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ providers: [provideRouter([])],
      imports: [PageLobby],
    }).compileComponents();

    fixture = TestBed.createComponent(PageLobby);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
