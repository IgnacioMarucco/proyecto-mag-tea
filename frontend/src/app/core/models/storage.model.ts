export interface PresignedUploadRequest {
  bucket: string;
  nombreOriginal: string;
  mimeType: string;
  tamanio: number;
}

export interface DocumentoResponse {
  id: number;
  bucket: string;
  clave: string;
  nombreOriginal: string;
  mimeType: string | null;
  tamanio: number | null;
  createdAt: string;
}

export interface PresignedUploadResponse {
  documento: DocumentoResponse;
  presignedUrl: string;
  expiresAt: string;
}
