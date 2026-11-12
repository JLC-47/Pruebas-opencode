export interface ItemCompraRequest {
  productoId: number;
  cantidad: number;
}

export interface CompraRequest {
  items: ItemCompraRequest[];
}

export interface CompraItem {
  productoId: number;
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Compra {
  id: number;
  fecha: string;
  total: number;
  items: CompraItem[];
}
