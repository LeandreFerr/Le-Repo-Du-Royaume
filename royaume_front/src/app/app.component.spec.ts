import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { App } from './app.component';
import { routes } from './app.routes';

describe('App', () => {
  let fixture: ComponentFixture<App>;
  let app: App;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter(routes)],
    }).compileComponents();

    fixture = TestBed.createComponent(App);
    app = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates the clean base app without rendering the map component on root', () => {
    expect(app).toBeTruthy();
    expect(app.title).toBe('Le Royaume');

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Le Royaume');
    expect(compiled.querySelector('router-outlet')).not.toBeNull();
    expect(compiled.querySelector('app-carte-royaume')).toBeNull();
    expect(compiled.querySelector('app-carte-royaume-sandbox')).toBeNull();
  });
});
