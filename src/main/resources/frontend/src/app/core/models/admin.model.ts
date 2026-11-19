export type RolNombre = 'ADMIN' | 'VENDEDOR' | 'USER';

export interface UsuarioAdmin {
  id: number;
  username: string;
  rol: RolNombre;
  enabled: boolean;
  nombre: string | null;
  avatarUrl: string | null;
}

export interface UsuarioAdminRequest {
  username: string;
  password?: string;
  rol: RolNombre;
  enabled: boolean;
}

export interface RolResponse {
  nombre: RolNombre;
  descripcion: string;
  permisos: string[];
}
