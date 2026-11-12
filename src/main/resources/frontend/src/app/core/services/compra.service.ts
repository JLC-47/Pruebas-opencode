import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Compra, CompraRequest } from '../models/compra.model';

@Injectable({ providedIn: 'root' })
export class CompraService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/compras';

  comprar(request: CompraRequest): Observable<Compra> {
    return this.http.post<Compra>(this.baseUrl, request);
  }

  listar(): Observable<Compra[]> {
    return this.http.get<Compra[]>(this.baseUrl);
  }
}
