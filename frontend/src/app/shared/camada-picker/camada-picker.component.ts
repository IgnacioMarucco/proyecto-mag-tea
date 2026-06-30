import {
  ChangeDetectionStrategy, Component, computed, effect,
  inject, input, output, signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, map, of } from 'rxjs';
import { CamadaService } from '../../core/services/camada.service';
import { CamadaListItem, CamadaCreate } from '../../core/models/camada.model';
import { extractErrorMessage } from '../utils/error.utils';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-camada-picker',
  imports: [ReactiveFormsModule, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './camada-picker.component.html',
})
export class CamadaPickerComponent {
  private readonly camadaService = inject(CamadaService);
  private readonly fb            = inject(FormBuilder);

  defaultCamadaId = input<number | null>(null);
  showError       = input<boolean>(false);
  camadaIdChange  = output<number | null>();

  camadas          = signal<CamadaListItem[]>([]);
  filterText       = signal('');
  isOpen           = signal(false);
  selectedCamadaId = signal<number | null>(null);

  readonly filteredCamadas = computed(() => {
    const q = this.filterText().toLowerCase().trim();
    return q.length === 0
      ? this.camadas()
      : this.camadas().filter(c => c.nombre.toLowerCase().includes(q));
  });
  readonly selectedCamada = computed(() =>
    this.camadas().find(c => c.id === this.selectedCamadaId()) ?? null
  );

  showModal    = signal(false);
  modalLoading = signal(false);
  modalError   = signal<string | null>(null);

  camadaForm = this.fb.group({
    nombre:          ['', [Validators.required, Validators.maxLength(50)]],
    fechaNacimiento: ['',  Validators.required],
  });

  private inicializado = false;

  constructor() {
    this.loadCamadas();
    effect(() => {
      const id = this.defaultCamadaId();
      if (this.inicializado || id == null) return;
      this.selectedCamadaId.set(id);
      this.inicializado = true;
    });
  }

  private loadCamadas(): void {
    this.camadaService.findAll({ size: 200, sortBy: 'fechaNacimiento', sortDir: 'desc' })
      .pipe(map(r => r.content), catchError(() => of([] as CamadaListItem[])))
      .subscribe(list => this.camadas.set(list));
  }

  select(c: CamadaListItem): void {
    this.selectedCamadaId.set(c.id);
    this.filterText.set('');
    this.isOpen.set(false);
    this.camadaIdChange.emit(c.id);
  }

  clearSelection(): void {
    this.selectedCamadaId.set(null);
    this.camadaIdChange.emit(null);
  }

  onBlur(): void {
    this.isOpen.set(false);
  }

  formatFecha(fecha: string): string {
    return new Date(fecha + 'T00:00:00').toLocaleDateString('es-AR');
  }

  openModal(): void {
    this.camadaForm.reset();
    this.modalError.set(null);
    this.showModal.set(true);
  }

  closeModal(): void { this.showModal.set(false); }

  guardarCamada(): void {
    if (this.camadaForm.invalid) { this.camadaForm.markAllAsTouched(); return; }
    this.modalLoading.set(true);
    this.modalError.set(null);
    const v = this.camadaForm.value;
    const dto: CamadaCreate = { nombre: v.nombre!, fechaNacimiento: v.fechaNacimiento! };
    this.camadaService.create(dto).subscribe({
      next: camada => {
        this.loadCamadas();
        this.selectedCamadaId.set(camada.id);
        this.camadaIdChange.emit(camada.id);
        this.modalLoading.set(false);
        this.showModal.set(false);
      },
      error: err => {
        this.modalError.set(extractErrorMessage(err, 'Error al crear la camada'));
        this.modalLoading.set(false);
      },
    });
  }
}
