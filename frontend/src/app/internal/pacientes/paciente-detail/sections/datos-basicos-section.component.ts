import {
  ChangeDetectionStrategy, Component, ElementRef, Injector,
  afterNextRender, effect, inject, input, output, signal, viewChild,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { PacienteService } from '../../../../core/services/paciente.service';
import { StorageService } from '../../../../core/services/storage.service';
import { PacienteResponse } from '../../../../core/models/paciente.model';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';
import { EdadPipe } from '../../../../core/pipes/edad.pipe';
import { FechaPipe } from '../../../../core/pipes/fecha.pipe';
import { CriteriosSectionComponent } from './criterios-section.component';
import { ConfirmModalComponent } from '../../../../shared/confirm-modal/confirm-modal.component';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { ToastService } from '../../../../core/services/toast.service';
import { map, switchMap } from 'rxjs';

const ALLOWED_TYPES = ['application/pdf', 'image/jpeg', 'image/png'];
const MAX_SIZE_MB   = 10;

@Component({
  selector: 'app-datos-basicos-section',
  imports: [RouterLink, StatusBadgeComponent, EdadPipe, FechaPipe, CriteriosSectionComponent, ConfirmModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './datos-basicos-section.component.html',
})
export class DatosBasicosSectionComponent {
  private readonly injector = inject(Injector);
  private readonly service  = inject(PacienteService);
  private readonly storage  = inject(StorageService);
  private readonly toast    = inject(ToastService);

  paciente      = input.required<PacienteResponse>();
  updated       = output<PacienteResponse>();

  activeTab     = signal<'datos' | 'criterios' | 'consentimiento'>('datos');
  notesExpanded = signal(false);
  isOverflowing = signal(false);

  showConfirmModal  = signal(false);
  confirming        = signal(false);
  confirmError      = signal<string | null>(null);

  pendingDocumentoId   = signal<number | null>(null);
  pendingFileName      = signal<string | null>(null);
  uploading            = signal(false);
  uploadError          = signal<string | null>(null);
  replacingDocument    = signal(false);

  private readonly notesEl       = viewChild<ElementRef<HTMLParagraphElement>>('notesEl');
  private readonly fileInputRef  = viewChild<ElementRef<HTMLInputElement>>('fileInput');
  private readonly replaceInputRef = viewChild<ElementRef<HTMLInputElement>>('replaceInput');

  readonly criteriosEstadoLabels: Record<string, string> = {
    APTO: 'Apto para el protocolo',
    EXCLUIDO: 'Excluido del protocolo',
    INCOMPLETO: 'Criterios incompletos',
  };
  readonly criteriosEstadoColors: Record<string, string> = {
    APTO:       'bg-accent-light text-accent',
    EXCLUIDO:   'bg-danger-light text-danger',
    INCOMPLETO: 'bg-warning/10 text-warning',
  };

  constructor() {
    effect(() => {
      this.paciente();
      this.notesExpanded.set(false);
      this.isOverflowing.set(false);
      this.pendingDocumentoId.set(null);
      this.pendingFileName.set(null);
      this.uploadError.set(null);
      afterNextRender(() => {
        const el = this.notesEl()?.nativeElement;
        if (el) this.isOverflowing.set(el.scrollHeight > el.clientHeight);
      }, { injector: this.injector });
    });
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    if (!this.validateFile(file)) return;
    this.uploadFile(file);
  }

  onReplaceFile(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    if (!this.validateFile(file)) return;
    this.replacingDocument.set(true);
    this.uploadError.set(null);
    this.storage.getPresignedUpload({ bucket: 'consentimientos', nombreOriginal: file.name, mimeType: file.type, tamanio: file.size }).pipe(
      switchMap(res => this.storage.uploadToMinIO(res.presignedUrl, file).pipe(
        switchMap(() => this.service.patchConsentimiento(this.paciente().codigoNumerico, { consentimientoFirmado: true, documentoId: res.documento.id }))
      ))
    ).subscribe({
      next: p => {
        this.toast.show('Documento actualizado');
        this.updated.emit(p);
        this.replacingDocument.set(false);
        if (this.replaceInputRef()?.nativeElement) this.replaceInputRef()!.nativeElement.value = '';
      },
      error: err => {
        this.uploadError.set(extractErrorMessage(err, 'Error al actualizar el documento'));
        this.replacingDocument.set(false);
      },
    });
  }

  clearPendingFile(): void {
    this.pendingDocumentoId.set(null);
    this.pendingFileName.set(null);
    this.uploadError.set(null);
    if (this.fileInputRef()?.nativeElement) this.fileInputRef()!.nativeElement.value = '';
  }

  confirmConsent(): void {
    this.confirming.set(true);
    this.confirmError.set(null);
    this.service.patchConsentimiento(this.paciente().codigoNumerico, {
      consentimientoFirmado: true,
      documentoId: this.pendingDocumentoId() ?? undefined,
    }).subscribe({
      next:  p   => { this.toast.show('Consentimiento confirmado'); this.updated.emit(p); this.showConfirmModal.set(false); this.confirming.set(false); },
      error: err => { this.confirmError.set(extractErrorMessage(err, 'Error al guardar')); this.confirming.set(false); },
    });
  }

  downloadDocument(id: number): void {
    this.storage.getPresignedDownload(id).subscribe({
      next: ({ presignedUrl }) => window.open(presignedUrl, '_blank'),
      error: () => this.toast.show('No se pudo obtener el enlace de descarga'),
    });
  }

  getCriterioValue(key: string): boolean {
    return !!(this.paciente() as unknown as Record<string, unknown>)[key];
  }

  private validateFile(file: File): boolean {
    this.uploadError.set(null);
    if (!ALLOWED_TYPES.includes(file.type)) {
      this.uploadError.set('Solo se permiten archivos PDF, JPG o PNG.');
      return false;
    }
    if (file.size > MAX_SIZE_MB * 1024 * 1024) {
      this.uploadError.set(`El archivo no puede superar los ${MAX_SIZE_MB} MB.`);
      return false;
    }
    return true;
  }

  private uploadFile(file: File): void {
    this.uploading.set(true);
    this.uploadError.set(null);
    this.storage.getPresignedUpload({ bucket: 'consentimientos', nombreOriginal: file.name, mimeType: file.type, tamanio: file.size }).pipe(
      switchMap(res => this.storage.uploadToMinIO(res.presignedUrl, file).pipe(map(() => res)))
    ).subscribe({
      next: res => {
        this.pendingDocumentoId.set(res.documento.id);
        this.pendingFileName.set(file.name);
        this.uploading.set(false);
      },
      error: err => {
        this.uploadError.set(extractErrorMessage(err, 'Error al subir el archivo'));
        this.uploading.set(false);
      },
    });
  }
}
