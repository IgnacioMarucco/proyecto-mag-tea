import { HttpErrorResponse } from '@angular/common/http';

export function extractErrorMessage(
  error: HttpErrorResponse,
  defaultMsg = 'Ocurrió un error inesperado'
): string {
  return error.error?.message ?? defaultMsg;
}
