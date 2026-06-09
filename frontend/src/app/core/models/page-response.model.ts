export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface PageParams {
  page?: number;
  size?: number;
  q?: string;
  sortBy?: string;
  sortDir?: string;
}
