import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class App {
  private readonly router = inject(Router);
  readonly title = 'Le Royaume';

  get isSandboxRoute(): boolean {
    return this.router.url.startsWith('/carte-sandbox');
  }
}

export { App as AppComponent };
