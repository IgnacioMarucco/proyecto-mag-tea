import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, from } from 'rxjs';
import { DocumentoResponse, PresignedUploadRequest, PresignedUploadResponse } from '../models/storage.model';

const BASE = '/api/v1/storage';

@Injectable({ providedIn: 'root' })
export class StorageService {
  private readonly http = inject(HttpClient);

  getPresignedUpload(dto: PresignedUploadRequest): Observable<PresignedUploadResponse> {
    return this.http.post<PresignedUploadResponse>(`${BASE}/documentos`, dto);
  }

  getPresignedDownload(id: number): Observable<{ presignedUrl: string; expiresAt: string }> {
    return this.http.get<{ presignedUrl: string; expiresAt: string }>(`${BASE}/documentos/${id}/presigned-download`);
  }

  uploadToMinIO(presignedUrl: string, file: File): Observable<void> {
    // No enviar Content-Type: el presigned URL firma solo 'host' (X-Amz-SignedHeaders=host)
    // Agregar headers adicionales invalida la firma y devuelve 403
    return from(
      fetch(presignedUrl, { method: 'PUT', body: file }).then(res => {
        if (!res.ok) throw new Error(`Error al subir el archivo (${res.status})`);
      })
    );
  }
}
