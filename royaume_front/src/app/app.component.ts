import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { ReglesDuJeuManuscriptComponent } from './components/regles-du-jeu-manuscript/regles-du-jeu-manuscript.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet,  ReglesDuJeuManuscriptComponent
],
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
