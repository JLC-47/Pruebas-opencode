package com.example.demo.repository;

import com.example.demo.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    @Query(value = "select * from productos where id = :id for update", nativeQuery = true)
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);
}
