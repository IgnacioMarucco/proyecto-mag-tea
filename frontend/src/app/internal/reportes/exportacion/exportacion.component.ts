import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ReportesService } from '../../../core/services/reporte.service';

type ExportKey = 'ratones' | 'pacientes' | 'pools' | 'completo';
type Format = 'csv' | 'xlsx';

@Component({
  selector: 'app-exportacion',
  templateUrl: './exportacion.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExportacionComponent {
  private readonly service = inject(ReportesService);

  readonly loading = signal<Record<ExportKey, Record<Format, boolean>>>({
    ratones:   { csv: false, xlsx: false },
    pacientes: { csv: false, xlsx: false },
    pools:     { csv: false, xlsx: false },
    completo:  { csv: false, xlsx: false },
  });

  readonly exports: { key: ExportKey; label: string; desc: string; filename: string }[] = [
    {
      key: 'ratones',
      label: 'Modelos animales (ratones)',
      desc: '1 fila por ratón — vocalizaciones, test de 3 cámaras, microscopía e identificación del pool.',
      filename: 'ratones',
    },
    {
      key: 'pacientes',
      label: 'Pacientes',
      desc: '1 fila por paciente — M-CHAT (familia y seguimiento), CARS, Vineland y valor de anticuerpos. Los pacientes control tienen vacías las columnas de tests.',
      filename: 'pacientes',
    },
    {
      key: 'pools',
      label: 'Composición de pools',
      desc: 'Tabla puente pool ↔ paciente. Usarla para cruzar con ratones.csv via VLOOKUP por pool_codigo.',
      filename: 'pool_composicion',
    },
  ];

  descargar(key: ExportKey, filename: string, format: Format): void {
    if (this.loading()[key][format]) return;
    this.loading.update(l => ({ ...l, [key]: { ...l[key], [format]: true } }));

    const obs$ =
      key === 'ratones'   && format === 'csv'  ? this.service.exportarRatones()             :
      key === 'ratones'   && format === 'xlsx' ? this.service.exportarRatonesXlsx()          :
      key === 'pacientes' && format === 'csv'  ? this.service.exportarPacientes()            :
      key === 'pacientes' && format === 'xlsx' ? this.service.exportarPacientesXlsx()        :
      key === 'pools'     && format === 'csv'  ? this.service.exportarPoolComposicion()      :
      key === 'pools'     && format === 'xlsx' ? this.service.exportarPoolComposicionXlsx()  :
                                                 this.service.exportarCompletoXlsx();

    const ext = format === 'xlsx' ? '.xlsx' : '.csv';
    obs$.pipe(finalize(() => this.loading.update(l => ({ ...l, [key]: { ...l[key], [format]: false } })))).subscribe({
      next: blob => {
        const date = new Date().toISOString().slice(0, 10).replace(/-/g, '_');
        this.triggerDownload(blob, `${filename}_${date}${ext}`);
      },
    });
  }

  private triggerDownload(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a   = document.createElement('a');
    a.href     = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }
}
