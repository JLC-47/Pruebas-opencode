export interface VentaVendedor {
  compraId: number;
  fecha: string;
  comprador: string;
  producto: string;
  cantidad: number;
  subtotal: number;
}

export interface ResumenVentas {
  totalVendido: number;
  unidadesVendidas: number;
  pedidos: number;
  ventas: VentaVendedor[];
}
