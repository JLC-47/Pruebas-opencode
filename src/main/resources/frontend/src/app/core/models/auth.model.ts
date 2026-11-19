export interface UsuarioSesion {
  id: number;
  username: string;
  roles: string[];
  nombre?: string;
  bio?: string;
  avatarUrl?: string;
  tema?: string;
  idioma?: string;
  notificacionesActivas?: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegistroRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  refreshToken: string;
  refreshExpiresIn: number;
  usuario: UsuarioSesion;
}

export interface Sesion {
  accessToken: string;
  tokenType: string;
  expiresAt: number;
  refreshToken: string;
  refreshExpiresAt: number;
  usuario: UsuarioSesion;
}
