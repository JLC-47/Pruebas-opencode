# Productos API

API REST para la gestion de productos construida con Spring Boot. Incluye validacion de datos, manejo centralizado de errores, pruebas automatizadas y una coleccion de Postman lista para ejecutar.

## Tecnologias

- Java 21
- Spring Boot 4.1.2 (Spring MVC, Spring Data JPA, Bean Validation, Actuator)
- MySQL 8 (produccion/desarrollo) y H2 (pruebas)
- JUnit 6, Mockito y MockMvc
- Maven
- Postman

## Estructura del proyecto

```
src/main/java/com/example/demo
  controller/   Endpoints REST
  dto/          Contratos de entrada y salida
  entity/       Entidades JPA
  exception/    Excepciones y manejo global de errores
  repository/   Repositorios Spring Data
  service/      Logica de negocio
src/test/java/com/example/demo
  controller/   Pruebas de la capa web con MockMvc
  service/      Pruebas unitarias con Mockito
  ProductoApiIntegrationTest.java
database/       Script de creacion de la base de datos
postman/        Coleccion de Postman
```

## Requisitos

- JDK 21
- Maven 3.9+
- MySQL 8 (opcional para ejecutar con H2 en pruebas)

## Configuracion de la base de datos

1. Crea la base de datos y los datos de ejemplo:

```bash
mysql -u root -p < database/pruebas_db.sql
```

2. Configura las credenciales con variables de entorno (o deja los valores por defecto):

| Variable | Valor por defecto |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/pruebas_db?...` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `root` |
| `JPA_DDL_AUTO` | `update` |
| `JPA_SHOW_SQL` | `false` |
| `SERVER_PORT` | `8080` |

Ejemplo en PowerShell:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "tu_password"
mvn spring-boot:run
```

## Ejecutar la aplicacion

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints

| Metodo | Ruta | Descripcion | Respuestas |
| --- | --- | --- | --- |
| GET | `/api/productos` | Lista todos los productos | 200 |
| GET | `/api/productos/{id}` | Obtiene un producto por id | 200, 404 |
| POST | `/api/productos` | Crea un producto | 201, 400 |
| PUT | `/api/productos/{id}` | Actualiza un producto | 200, 400, 404 |
| DELETE | `/api/productos/{id}` | Elimina un producto | 204, 404 |
| GET | `/actuator/health` | Estado de la aplicacion | 200 |

### Ejemplo de creacion

```http
POST /api/productos
Content-Type: application/json
```

```json
{
  "nombre": "Laptop",
  "precio": 2500000.00,
  "stock": 10
}
```

Respuesta `201 Created` (incluye el encabezado `Location` con la URL del recurso creado):

```json
{
  "id": 1,
  "nombre": "Laptop",
  "precio": 2500000.00,
  "stock": 10
}
```

## Validaciones

- `nombre`: obligatorio, maximo 120 caracteres.
- `precio`: obligatorio, mayor o igual a 0, maximo 2 decimales.
- `stock`: obligatorio, mayor o igual a 0.

Los errores usan el formato `ProblemDetail` (RFC 7807). Por ejemplo, una solicitud invalida devuelve `400 Bad Request`:

```json
{
  "type": "about:blank",
  "title": "Error de validacion",
  "status": 400,
  "detail": "La solicitud contiene datos invalidos",
  "errors": {
    "nombre": "El nombre es obligatorio",
    "precio": "El precio no puede ser negativo"
  },
  "timestamp": "2026-01-01T00:00:00Z"
}
```

## Pruebas automatizadas

```bash
mvn test
```

La suite incluye 23 pruebas:

- `ProductoServiceTest`: 8 pruebas unitarias de la logica de negocio con Mockito.
- `ProductoControllerTest`: 12 pruebas de la capa web con MockMvc (codigos de estado, validacion y errores).
- `ProductoApiIntegrationTest`: 3 pruebas de integracion con base de datos H2 y el flujo CRUD completo.

Las pruebas usan el perfil `test` (`src/test/resources/application-test.yaml`) y una base de datos H2 en memoria, por lo que no requieren MySQL.

## Postman

1. Importa `postman/Pruebas-Productos.postman_collection.json`.
2. Verifica la variable `baseUrl` (por defecto `http://localhost:8080`).
3. Ejecuta la coleccion en orden: crea un producto, lo consulta, lo actualiza, lo elimina y valida los casos de error.

La coleccion guarda automaticamente el id del producto creado en la variable `productoId`.
