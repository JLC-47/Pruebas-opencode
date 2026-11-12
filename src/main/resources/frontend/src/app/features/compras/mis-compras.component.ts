import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Compra, CompraItem } from '../../core/models/compra.model';
import { CompraService } from '../../core/services/compra.service';

@Component({
  selector: 'app-mis-compras',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './mis-compras.component.html',
  styleUrl: './mis-compras.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MisComprasComponent implements OnInit {
  private readonly compraService = inject(CompraService);
  private readonly route = inject(ActivatedRoute);

  readonly compras = signal<Compra[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly compraConfirmada = signal<number | null>(null);

  ngOnInit(): void {
    const compra = Number(this.route.snapshot.queryParamMap.get('compra'));
    this.compraConfirmada.set(Number.isFinite(compra) && compra > 0 ? compra : null);
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.compraService
      .listar()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (compras) => this.compras.set(compras),
        error: (error: Error) => this.error.set(error.message),
      });
  }

  unidades(items: CompraItem[]): number {
    return items.reduce((total, item) => total + item.cantidad, 0);
  }
}
