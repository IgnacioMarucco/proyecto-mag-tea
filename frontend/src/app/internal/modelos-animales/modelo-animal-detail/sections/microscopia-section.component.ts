import { ChangeDetectionStrategy, Component, ElementRef, computed, inject, input, output, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { map, switchMap } from 'rxjs';
import { ModeloAnimalService } from '../../../../core/services/modelo-animal.service';
import { StorageService } from '../../../../core/services/storage.service';
import { ModeloAnimalResponse, ImagenMicroscopiaCreate, ImagenMicroscopiaResponse, TipoImagenMicroscopia } from '../../../../core/models/modelo-animal.model';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { ToastService } from '../../../../core/services/toast.service';

const ALLOWED_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/tiff'];

@Component({
  selector: 'app-microscopia-section',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './microscopia-section.component.html',
})
export class MicroscopiaSectionComponent {
  private readonly service = inject(ModeloAnimalService);
  private readonly storage = inject(StorageService);
  private readonly fb      = inject(FormBuilder);
  private readonly toast   = inject(ToastService);

  modeloAnimal = input.required<ModeloAnimalResponse>();
  updated      = output<ModeloAnimalResponse>();

  saving    = signal(false);
  saveError = signal<string | null>(null);
  editMode  = signal(false);

  form = this.fb.group({
    numCelulasGanglionares: [null as number | null, [Validators.required, Validators.min(0)]],
    numCelulasPurkinje:     [null as number | null, [Validators.required, Validators.min(0)]],
  });

  startEdit(): void {
    const ma = this.modeloAnimal();
    this.form.patchValue({
      numCelulasGanglionares: ma.numCelulasGanglionares ?? null,
      numCelulasPurkinje:     ma.numCelulasPurkinje ?? null,
    });
    this.saveError.set(null);
    this.editMode.set(true);
  }

  cancelEdit(): void {
    this.editMode.set(false);
    this.saveError.set(null);
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (this.saving()) return;
    this.saving.set(true);
    this.saveError.set(null);
    const v = this.form.value;
    this.service.patchMicroscopia(this.modeloAnimal().id, {
      numCelulasGanglionares: Number(v.numCelulasGanglionares),
      numCelulasPurkinje:     Number(v.numCelulasPurkinje),
    }).subscribe({
      next:  ma  => {
        this.toast.show('Microscopía guardada');
        this.updated.emit(ma);
        this.saving.set(false);
        this.editMode.set(false);
      },
      error: err => { this.saveError.set(extractErrorMessage(err, 'Error al guardar')); this.saving.set(false); },
    });
  }

  // Image management state
  readonly fileInputRef = viewChild<ElementRef<HTMLInputElement>>('fileInput');

  showAddForm        = signal(false);
  tipoSeleccionado   = signal<TipoImagenMicroscopia | null>(null);
  modoEntrada        = signal<'archivo' | 'url'>('archivo');
  uploading          = signal(false);
  uploadError        = signal<string | null>(null);
  pendingDocumentoId = signal<number | null>(null);
  pendingFileName    = signal<string | null>(null);
  urlExterna         = signal('');
  descripcion        = signal('');
  addingImage        = signal(false);
  addImageError      = signal<string | null>(null);

  readonly tipoChipClases: Record<TipoImagenMicroscopia, string> = {
    GANGLIONAR: 'shrink-0 px-2 py-0.5 text-xs font-mono font-semibold rounded-full bg-violet-100 text-violet-700',
    PURKINJE:   'shrink-0 px-2 py-0.5 text-xs font-mono font-semibold rounded-full bg-teal-100 text-teal-700',
  };

  canAdd = computed(() => {
    if (!this.tipoSeleccionado() || this.uploading()) return false;
    return this.modoEntrada() === 'archivo'
      ? this.pendingDocumentoId() !== null
      : this.urlExterna().trim().length > 0;
  });

  openAddForm(): void {
    this.showAddForm.set(true);
    this.resetImageForm();
  }

  cancelAddForm(): void {
    this.showAddForm.set(false);
    this.resetImageForm();
  }

  setModoEntrada(modo: 'archivo' | 'url'): void {
    this.modoEntrada.set(modo);
    this.pendingDocumentoId.set(null);
    this.pendingFileName.set(null);
    this.urlExterna.set('');
    this.uploadError.set(null);
    const el = this.fileInputRef()?.nativeElement;
    if (el) el.value = '';
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
      this.uploadError.set('Tipo no permitido. Usar PNG, JPEG o TIFF.');
      return;
    }
    this.uploading.set(true);
    this.uploadError.set(null);
    this.pendingDocumentoId.set(null);
    this.pendingFileName.set(null);

    this.storage.getPresignedUpload({
      bucket: 'microscopia',
      nombreOriginal: file.name,
      mimeType: file.type,
      tamanio: file.size,
    }).pipe(
      switchMap(res => this.storage.uploadToMinIO(res.presignedUrl, file).pipe(map(() => res)))
    ).subscribe({
      next: res => {
        this.pendingDocumentoId.set(res.documento.id);
        this.pendingFileName.set(res.documento.nombreOriginal);
        this.uploading.set(false);
      },
      error: err => {
        this.uploadError.set(extractErrorMessage(err, 'Error al subir el archivo'));
        this.uploading.set(false);
      },
    });
  }

  agregarImagen(): void {
    if (!this.canAdd() || this.addingImage()) return;
    const tipo = this.tipoSeleccionado()!;
    this.addingImage.set(true);
    this.addImageError.set(null);

    const dto: ImagenMicroscopiaCreate = { tipo };
    if (this.modoEntrada() === 'archivo') {
      dto.documentoId = this.pendingDocumentoId()!;
    } else {
      dto.urlExterna = this.urlExterna().trim();
    }
    const desc = this.descripcion().trim();
    if (desc) dto.descripcion = desc;

    this.service.agregarImagen(this.modeloAnimal().id, dto).pipe(
      switchMap(() => this.service.findById(this.modeloAnimal().id))
    ).subscribe({
      next: ma => {
        this.toast.show('Imagen agregada');
        this.updated.emit(ma);
        this.cancelAddForm();
      },
      error: err => {
        this.addImageError.set(extractErrorMessage(err, 'Error al agregar imagen'));
        this.addingImage.set(false);
      },
    });
  }

  eliminarImagen(imagenId: number): void {
    this.service.eliminarImagen(this.modeloAnimal().id, imagenId).pipe(
      switchMap(() => this.service.findById(this.modeloAnimal().id))
    ).subscribe({
      next: ma => { this.toast.show('Imagen eliminada'); this.updated.emit(ma); },
      error: err => this.toast.show(extractErrorMessage(err, 'Error al eliminar imagen')),
    });
  }

  abrirImagen(imagen: ImagenMicroscopiaResponse): void {
    if (imagen.urlExterna) {
      window.open(imagen.urlExterna, '_blank', 'noopener,noreferrer');
    } else if (imagen.documentoId) {
      this.storage.getPresignedDownload(imagen.documentoId).subscribe({
        next: ({ presignedUrl }) => window.open(presignedUrl, '_blank', 'noopener,noreferrer'),
        error: () => this.toast.show('No se pudo obtener el enlace de descarga'),
      });
    }
  }

  private resetImageForm(): void {
    this.tipoSeleccionado.set(null);
    this.modoEntrada.set('archivo');
    this.uploading.set(false);
    this.uploadError.set(null);
    this.pendingDocumentoId.set(null);
    this.pendingFileName.set(null);
    this.urlExterna.set('');
    this.descripcion.set('');
    this.addingImage.set(false);
    this.addImageError.set(null);
    const el = this.fileInputRef()?.nativeElement;
    if (el) el.value = '';
  }
}
