package com.example.demo.repository;

import com.example.demo.entity.Compra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    @EntityGraph(attributePaths = "items")
    List<Compra> findByUsuarioUsernameOrderByFechaDesc(String username);
}
