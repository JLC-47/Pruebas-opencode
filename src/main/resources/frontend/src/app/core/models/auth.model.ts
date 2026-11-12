export interface UsuarioSesion {
  id: number;
  username: string;
  roles: string[];
}

export interface LoginRequest {
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
