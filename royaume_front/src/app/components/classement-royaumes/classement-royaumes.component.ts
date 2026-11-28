import { Component, inject, Input } from '@angular/core';
import { RoyaumeService } from '../../services/royaume.service';
import { Royaume } from '../../models/royaume.model';

@Component({
  imports: [],
  selector: 'app-classement-royaumes',
  styleUrl: './classement-royaumes.component.css',
  templateUrl: './classement-royaumes.component.html',
})
export class ClassementRoyaumes {
  private royaumeService = inject (RoyaumeService);

   @Input({ required: true }) royaume!: Royaume;

 



}
