import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { BehaviorSubject, switchMap } from 'rxjs';
import { catchError, of } from 'rxjs';
import { ProfesionalService } from '../../../core/services/profesional.service';
import { ROLE_LABELS } from '../../../core/models/profesional.model';

@Component({
  selector: 'app-profesional-list',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './profesional-list.component.html',
})
export class ProfesionalListComponent {
  private readonly service = inject(ProfesionalService);

  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  profesionales = toSignal(
    this.refresh$.pipe(
      switchMap(() => this.service.findAll().pipe(catchError(() => of([]))))
    ),
    { initialValue: [] }
  );

  deleting = signal<number | null>(null);
  readonly roleLabels = ROLE_LABELS;

  confirmDelete(id: number): void {
    if (!confirm('¿Dar de baja este profesional?')) return;

    this.deleting.set(id);
    this.service.delete(id).subscribe({
      next: () => {
        this.deleting.set(null);
        this.refresh$.next();
      },
      error: () => {
        this.deleting.set(null);
        alert('Error al dar de baja el profesional');
      },
    });
  }
}
