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
}
