-- Base de datos para el proyecto productos-api (MySQL)

CREATE DATABASE IF NOT EXISTS pruebas_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE pruebas_db;

-- Tabla que corresponde a la entidad Producto
CREATE TABLE IF NOT EXISTS productos (
  id     BIGINT         NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120)   NOT NULL,
  precio DECIMAL(12, 2) NOT NULL,
  stock  INT            NOT NULL,
  PRIMARY KEY (id)
);

-- Datos de ejemplo (idempotente: no duplica si ya existen)
INSERT INTO productos (nombre, precio, stock)
SELECT 'Laptop', 2500000.00, 10
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = 'Laptop');

INSERT INTO productos (nombre, precio, stock)
SELECT 'Mouse', 45000.00, 50
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = 'Mouse');

INSERT INTO productos (nombre, precio, stock)
SELECT 'Teclado', 120000.00, 30
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = 'Teclado');
