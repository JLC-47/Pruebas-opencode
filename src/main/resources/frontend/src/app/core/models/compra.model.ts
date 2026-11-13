export interface ItemCompraRequest {
  productoId: number;
  cantidad: number;
}

export interface CompraRequest {
  items: ItemCompraRequest[];
}

export interface ItemDevolucionRequest {
  productoId: number;
  cantidad: number;
}

export interface DevolucionRequest {
  items: ItemDevolucionRequest[];
}

export type EstadoCompra = 'COMPLETADA' | 'DEVUELTA_PARCIAL' | 'DEVUELTA_TOTAL';

export interface CompraItem {
  productoId: number;
  productoNombre: string;
  cantidad: number;
  cantidadDevuelta: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Compra {
  id: number;
  fecha: string;
  total: number;
  totalDevuelto: number;
  estado: EstadoCompra;
  items: CompraItem[];
}
