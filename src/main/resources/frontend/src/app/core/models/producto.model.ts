export interface Producto {
  id: number;
  nombre: string;
  precio: number;
  stock: number;
  categoria?: string;
  descripcion?: string;
}

export interface ProductoRequest {
  nombre: string;
  precio: number;
  stock: number;
  categoria?: string;
  descripcion?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  errors?: Record<string, string>;
  timestamp?: string;
}

export interface ProductoQuery {
  page: number;
  size: number;
  sort: string;
  direction: 'asc' | 'desc';
  nombre?: string;
}
