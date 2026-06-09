import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MchatPublicInfo {
  nombreNino: string;
  apellidoNino: string;
  yaCompletado: boolean;
}

export interface MchatSubmit {
  p1: boolean;  p2: boolean;  p3: boolean;  p4: boolean;  p5: boolean;
  p6: boolean;  p7: boolean;  p8: boolean;  p9: boolean;  p10: boolean;
  p11: boolean; p12: boolean; p13: boolean; p14: boolean; p15: boolean;
  p16: boolean; p17: boolean; p18: boolean; p19: boolean; p20: boolean;
}

export interface MchatRespuestasResponse {
  id: number;
  p1: boolean;  p2: boolean;  p3: boolean;  p4: boolean;  p5: boolean;
  p6: boolean;  p7: boolean;  p8: boolean;  p9: boolean;  p10: boolean;
  p11: boolean; p12: boolean; p13: boolean; p14: boolean; p15: boolean;
  p16: boolean; p17: boolean; p18: boolean; p19: boolean; p20: boolean;
  scoreTotal: number;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class MchatService {
  private readonly http = inject(HttpClient);
  private readonly publicBase = '/api/public/mchat';
  private readonly internalBase = '/api/mchat';

  getFormulario(token: string): Observable<MchatPublicInfo> {
    return this.http.get<MchatPublicInfo>(`${this.publicBase}/${token}`);
  }

  submitRespuestas(token: string, dto: MchatSubmit): Observable<void> {
    return this.http.post<void>(`${this.publicBase}/${token}`, dto);
  }

  getRespuestasByPaciente(pacienteId: number): Observable<MchatRespuestasResponse> {
    return this.http.get<MchatRespuestasResponse>(`${this.internalBase}/paciente/${pacienteId}/respuestas`);
  }

  upsertRespuestasByPaciente(pacienteId: number, dto: MchatSubmit): Observable<MchatRespuestasResponse> {
    return this.http.put<MchatRespuestasResponse>(`${this.internalBase}/paciente/${pacienteId}/respuestas`, dto);
  }
}
