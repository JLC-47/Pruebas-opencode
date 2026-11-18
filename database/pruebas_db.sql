-- Esquema de productos-api para MySQL 8.
-- Las tablas se crean una sola vez; los datos de demostracion los carga
-- InicializadorDatos cuando app.seed.enabled=true (perfiles dev/test).

CREATE DATABASE IF NOT EXISTS pruebas_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE pruebas_db;

CREATE TABLE IF NOT EXISTS productos (
  id          BIGINT         NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(120)   NOT NULL,
  precio      DECIMAL(12, 2) NOT NULL,
  stock       INT            NOT NULL,
  categoria   VARCHAR(60)    NULL,
  descripcion VARCHAR(300)   NULL,
  imagen_url  VARCHAR(500)   NULL,
  PRIMARY KEY (id),
  KEY idx_productos_nombre (nombre),
  CONSTRAINT chk_productos_precio CHECK (precio >= 0),
  CONSTRAINT chk_productos_stock CHECK (stock >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS usuarios (
  id       BIGINT      NOT NULL AUTO_INCREMENT,
  username VARCHAR(60) NOT NULL,
  password VARCHAR(100) NOT NULL,
  rol      VARCHAR(20) NOT NULL,
  enabled  BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuarios_username (username),
  CONSTRAINT chk_usuarios_rol CHECK (rol IN ('ADMIN', 'USER'))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT      NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  revoked    BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_refresh_tokens_hash (token_hash),
  KEY idx_refresh_tokens_usuario (usuario_id),
  KEY idx_refresh_tokens_caducidad (expires_at, revoked),
  CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS compras (
  id         BIGINT         NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT         NOT NULL,
  fecha      DATETIME(6)    NOT NULL,
  total      DECIMAL(12, 2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_compras_usuario_fecha (usuario_id, fecha),
  CONSTRAINT fk_compras_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS compra_detalles (
  id               BIGINT         NOT NULL AUTO_INCREMENT,
  compra_id        BIGINT         NOT NULL,
  producto_id      BIGINT         NOT NULL,
  producto_nombre  VARCHAR(120)   NOT NULL,
  cantidad         INT            NOT NULL,
  precio_unitario  DECIMAL(12, 2) NOT NULL,
  subtotal         DECIMAL(12, 2) NOT NULL,
  cantidad_devuelta INT           NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_compra_detalles_compra (compra_id),
  CONSTRAINT fk_compra_detalles_compra FOREIGN KEY (compra_id) REFERENCES compras (id),
  CONSTRAINT chk_compra_detalles_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_compra_detalles_devuelta CHECK (
    cantidad_devuelta IS NULL OR (cantidad_devuelta >= 0 AND cantidad_devuelta <= cantidad)
  )
) ENGINE = InnoDB;
