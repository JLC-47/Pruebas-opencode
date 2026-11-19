import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CambioContrasenaRequest, Perfil, PerfilRequest } from '../models/perfil.model';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/perfil';

  obtener(): Observable<Perfil> {
    return this.http.get<Perfil>(this.baseUrl);
  }

  actualizar(request: PerfilRequest): Observable<Perfil> {
    return this.http.put<Perfil>(this.baseUrl, request);
  }

  cambiarContrasena(request: CambioContrasenaRequest): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/password`, request);
  }
}
