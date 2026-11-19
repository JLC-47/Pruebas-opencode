import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { RolNombre, RolResponse, UsuarioAdmin, UsuarioAdminRequest } from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/admin';

  listarUsuarios(): Observable<UsuarioAdmin[]> {
    return this.http.get<UsuarioAdmin[]>(`${this.baseUrl}/usuarios`);
  }

  obtenerUsuario(id: number): Observable<UsuarioAdmin> {
    return this.http.get<UsuarioAdmin>(`${this.baseUrl}/usuarios/${id}`);
  }

  crearUsuario(request: UsuarioAdminRequest): Observable<UsuarioAdmin> {
    return this.http.post<UsuarioAdmin>(`${this.baseUrl}/usuarios`, request);
  }

  actualizarUsuario(id: number, request: UsuarioAdminRequest): Observable<UsuarioAdmin> {
    return this.http.put<UsuarioAdmin>(`${this.baseUrl}/usuarios/${id}`, request);
  }

  cambiarRol(id: number, rol: RolNombre): Observable<UsuarioAdmin> {
    return this.http.put<UsuarioAdmin>(`${this.baseUrl}/usuarios/${id}/rol`, { rol });
  }

  cambiarEstado(id: number, enabled: boolean): Observable<UsuarioAdmin> {
    const params = new HttpParams().set('enabled', enabled);
    return this.http.put<UsuarioAdmin>(`${this.baseUrl}/usuarios/${id}/estado`, null, { params });
  }

  eliminarUsuario(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/usuarios/${id}`);
  }

  listarRoles(): Observable<RolResponse[]> {
    return this.http.get<RolResponse[]>(`${this.baseUrl}/roles`);
  }
}
