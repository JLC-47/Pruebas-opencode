import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ResumenVentas } from '../models/vendedor.model';

@Injectable({ providedIn: 'root' })
export class VendedorService {
  private readonly http = inject(HttpClient);

  obtenerVentas(): Observable<ResumenVentas> {
    return this.http.get<ResumenVentas>('/api/vendedores/ventas');
  }
}
