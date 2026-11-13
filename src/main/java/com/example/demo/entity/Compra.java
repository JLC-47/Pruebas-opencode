package com.example.demo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
@Getter
@Setter
@NoArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Instant fecha;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> items = new ArrayList<>();

    public void agregarItem(DetalleCompra item) {
        item.setCompra(this);
        items.add(item);
    }

    public int getUnidadesTotales() {
        return items.stream().mapToInt(DetalleCompra::getCantidad).sum();
    }

    public int getUnidadesDevueltas() {
        return items.stream().mapToInt(DetalleCompra::getCantidadDevueltaSegura).sum();
    }

    public EstadoCompra getEstado() {
        int devueltas = getUnidadesDevueltas();
        if (devueltas <= 0) {
            return EstadoCompra.COMPLETADA;
        }
        return devueltas >= getUnidadesTotales()
                ? EstadoCompra.DEVUELTA_TOTAL
                : EstadoCompra.DEVUELTA_PARCIAL;
    }

    public BigDecimal getTotalDevuelto() {
        return items.stream()
                .map(item -> item.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(item.getCantidadDevueltaSegura())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
