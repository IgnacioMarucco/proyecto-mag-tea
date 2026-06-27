import { EstadoFormulario } from '../../core/models/formulario-interes.model';

export const ESTADO_FORMULARIO_LABELS: Record<EstadoFormulario, string> = {
  PENDIENTE:  'Pendiente',
  CONTACTADO: 'Contactado',
  ADMITIDO:   'Admitido',
  DESCARTADO: 'Descartado',
};
export const ESTADO_FORMULARIO_COLORS: Record<EstadoFormulario, string> = {
  PENDIENTE:  'bg-warning/10 text-warning',
  CONTACTADO: 'bg-primary-light text-primary',
  ADMITIDO:   'bg-accent-light text-accent',
  DESCARTADO: 'bg-background text-text-muted border border-border',
};
