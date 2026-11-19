package com.example.demo.repository;

import com.example.demo.entity.Compra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    @EntityGraph(attributePaths = "items")
    List<Compra> findByUsuarioUsernameOrderByFechaDesc(String username);

    @Query(value = """
            select * from compras
            where id = :id
              and usuario_id = (select id from usuarios where username = :username)
            for update
            """, nativeQuery = true)
    Optional<Compra> findByIdAndUsuarioUsernameForUpdate(
            @Param("id") Long id,
            @Param("username") String username);

    @Query("""
            select new com.example.demo.dto.VentaVendedorResponse(
                d.compra.id,
                d.compra.fecha,
                d.compra.usuario.username,
                d.productoNombre,
                d.cantidad,
                d.subtotal)
            from DetalleCompra d
            where d.productoId in (
                select p.id from Producto p where p.vendedor.username = :username
            )
            order by d.compra.fecha desc
            """)
    List<com.example.demo.dto.VentaVendedorResponse> findVentasByVendedor(
            @Param("username") String username);
}
