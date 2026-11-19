export interface Perfil {
  id: number;
  username: string;
  roles: string[];
  nombre: string | null;
  bio: string | null;
  avatarUrl: string | null;
  tema: string;
  idioma: string;
  notificacionesActivas: boolean;
  enabled: boolean;
}

export interface PerfilRequest {
  nombre: string;
  bio: string;
  avatarUrl: string;
  tema: string;
  idioma: string;
  notificacionesActivas: boolean;
}

export interface CambioContrasenaRequest {
  contrasenaActual: string;
  contrasenaNueva: string;
}
