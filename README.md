# Productos API

Aplicacion web de catalogo y compras construida con Spring Boot y Angular. Incluye paginacion, busqueda, validacion de datos, autenticacion JWT con roles, registro de clientes, carrito de compras, checkout con descuento transaccional de stock, historial de pedidos, devoluciones parciales o totales que reponen el inventario, manejo centralizado de errores, notificaciones y confirmaciones elegantes, historial de busquedas, documentacion OpenAPI, pruebas automatizadas con cobertura y una coleccion de Postman lista para ejecutar.

## Tecnologias

- Java 21
- Spring Boot 4.1.2 (Spring MVC, Spring Data JPA, Spring Security, Bean Validation, Actuator)
- JWT con JJWT 0.12 y refresh tokens con rotacion
- MySQL 8 (produccion/desarrollo) y H2 (pruebas)
- Springdoc OpenAPI 3.2 + Swagger UI con esquema `bearerAuth`
- JUnit 6, Mockito, MockMvc, AssertJ y JaCoCo
- Angular 22 (standalone, signals, routing con lazy loading)
- Vitest 5 para las pruebas del frontend
- Node.js 24 y npm
- Maven
- GitHub Actions (CI)
- Docker y Docker Compose
- Prettier (formato del frontend)
- Postman

## Estructura del proyecto

```
src/main/java/com/example/demo
  config/       Seguridad, OpenAPI, datos seed y redireccion del SPA
  controller/   Endpoints REST y de autenticacion
  dto/          Contratos de entrada/salida y respuestas paginadas
  entity/       Entidades JPA (productos, usuarios, refresh tokens, compras)
  exception/    Excepciones y manejo global de errores
  repository/   Repositorios Spring Data
  security/     JwtService, filtro JWT y UserDetailsService
  service/      Logica de negocio, autenticacion y compras
src/main/resources/frontend
  src/app/core/       Modelos, servicios, guards e interceptor
  src/app/features/   Productos, carrito, compras, login y registro
  src/app/shared/     Componentes compartidos (404)
  src/app/app.routes.ts  Rutas con lazy loading y guards
  proxy.conf.json     Proxy de desarrollo hacia Spring Boot
src/main/resources/static  Build de Angular servido por Spring Boot
src/test/java/com/example/demo
  config/       Pruebas de OpenAPI y redireccion del SPA
  controller/   Pruebas de la capa web con MockMvc
  dto/          Pruebas unitarias de validacion y mapeo de DTOs
  exception/    Pruebas unitarias del manejo global de errores
  repository/   Pruebas de persistencia con @DataJpaTest
  service/      Pruebas unitarias con Mockito
  DemoApplicationTest.java
  ProductoApiIntegrationTest.java
  CompraApiIntegrationTest.java
.github/workflows/ci.yml  CI de backend y frontend
database/       Script de creacion de la base de datos
postman/        Coleccion de Postman
```

## Requisitos

- JDK 21
- Maven 3.9+
- Node.js 24 y npm 11 (solo para desarrollar o compilar el frontend)
- MySQL 8 (H2 se usa automaticamente en las pruebas)

## Configuracion de la base de datos

1. Crea la base de datos y los datos de ejemplo:

```bash
mysql -u root -p < database/pruebas_db.sql
```

2. Configura las credenciales con variables de entorno (o deja los valores por defecto). El script `database/pruebas_db.sql` crea el esquema completo (productos, usuarios, refresh tokens, compras y detalles); los datos de demostracion los carga la aplicacion.

| Variable | Valor por defecto |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/pruebas_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `root` |
| `JPA_DDL_AUTO` | `update` |
| `JPA_SHOW_SQL` | `false` |
| `SERVER_PORT` | `8080` |
| `JWT_SECRET` | Secreto de desarrollo por defecto; obligatorio en `prod` y recomendable sobrescribir siempre |
| `JWT_ACCESS_TOKEN_MINUTES` | `20` |
| `JWT_REFRESH_TOKEN_DAYS` | `7` |
| `APP_SEED_ENABLED` | `false` (actívalo en `dev`/`test` para cargar usuarios y catalogo) |
| `APP_SEED_ADMIN_USERNAME` / `APP_SEED_ADMIN_PASSWORD` | `admin` / `admin123` |
| `APP_SEED_USER_USERNAME` / `APP_SEED_USER_PASSWORD` | `user` / `user123` |

En produccion define siempre un `JWT_SECRET` propio de al menos 256 bits codificado en Base64: `application-prod.yaml` lo exige y la aplicacion falla en el arranque si no esta definido. Los mensajes de error del servlet y los detalles del health check no se exponen por defecto.

Ejemplo en PowerShell:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "tu_password"
$env:JWT_SECRET = "Y2xhdmUtc3VwZXItc2VndXJhLXBhcmEtZGVzYXJyb2xsby1sb2NhbC0yMDI2LXByb2R1Y3Rvcy1hcGk="
mvn spring-boot:run
```

## Perfiles de configuracion

| Perfil | Comportamiento |
| --- | --- |
| `dev` | Muestra SQL, `ddl-auto=update`, logging DEBUG, secreto JWT de desarrollo y seed activado |
| `prod` | `ddl-auto=validate`, sin SQL en logs, health sin detalles, seed desactivado, exige `JWT_SECRET` |
| `test` | H2 en memoria, `ddl-auto=create-drop`, seed activado con credenciales fijas |

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Sin perfil activo la aplicacion usa MySQL, el secreto JWT de desarrollo y el seed desactivado; activa `dev` o `APP_SEED_ENABLED=true` para cargar usuarios y catalogo.

## Paso a paso para correr el proyecto

### Opcion A: aplicacion completa en un solo puerto (recomendada)

1. Verifica las herramientas:

   ```bash
   java -version
   mvn -version
   node -v
   npm -v
   ```

2. Crea la base de datos MySQL (solo la primera vez):

   ```bash
   mysql -u root -p < database/pruebas_db.sql
   ```

   Si tus credenciales no son `root/root`, define `DB_USERNAME` y `DB_PASSWORD` como se explica en [Configuracion de la base de datos](#configuracion-de-la-base-de-datos).

3. Compila el frontend para que Spring Boot lo sirva:

   ```bash
   cd src/main/resources/frontend
   npm ci
   npm run build
   cd ../../../..
   ```

4. Inicia Spring Boot desde la raiz del proyecto con el perfil `dev` (incluye el secreto JWT de desarrollo y crea los usuarios de ejemplo):

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. Abre `http://localhost:8080`. Seras redirigido a `/login`.

6. Inicia sesion con un usuario de demostracion:

   | Usuario | Contrasena | Permisos |
   | --- | --- | --- |
   | `admin` | `admin123` | Consultar, crear, editar, eliminar y comprar |
   | `user` | `user123` | Consultar productos, comprar e historial |

   Swagger UI queda en `http://localhost:8080/swagger-ui.html` y el health check en `http://localhost:8080/actuator/health`.

### Opcion B: desarrollo con recarga automatica (dos terminales)

1. Terminal 1, backend:

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. Terminal 2, frontend:

   ```bash
   cd src/main/resources/frontend
   npm ci
   npm start
   ```

3. Abre `http://localhost:4200`. El proxy envia `/api`, `/actuator`, `/v3/api-docs` y `/swagger-ui` a `http://localhost:8080`.

### Opcion C: aplicacion completa con Docker Compose

1. Copia la plantilla de variables y ajusta los valores:

   ```bash
   cp .env.example .env
   ```

   `JWT_SECRET` debe ser Base64 de al menos 32 bytes. El valor incluido en `.env.example` es solo para desarrollo local.

2. Construye y levanta MySQL y la aplicacion:

   ```bash
   docker compose up --build -d
   ```

3. Espera a que ambos health checks esten sanos (`docker compose ps`) y abre `http://localhost:8080`. MySQL queda enlazado a `127.0.0.1:3306`; el contenedor usa el usuario `DB_USERNAME` en lugar de `root`.

4. Para detener y eliminar los contenedores: `docker compose down` (añade `-v` para borrar el volumen de datos).

## Autenticacion y roles

La API usa JWT stateless. El login devuelve un access token de 20 minutos y un refresh token de 7 dias; cada refresh rota el token y revoca el anterior. Los refresh tokens se guardan como hash SHA-256 en la base de datos.

| Metodo | Ruta | Descripcion | Respuestas |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | Inicia sesion con `username` y `password` | 200, 400, 401 |
| POST | `/api/auth/register` | Registra un cliente con rol `USER` | 201, 400, 409 |
| POST | `/api/auth/refresh` | Renueva la sesion con `refreshToken` | 200, 400, 401 |
| POST | `/api/auth/logout` | Revoca el refresh token | 204, 400, 401 |

Ejemplo de login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 1200,
  "refreshToken": "eU9...",
  "refreshExpiresIn": 604800,
  "usuario": {
    "id": 1,
    "username": "admin",
    "roles": ["ADMIN"]
  }
}
```

Ejemplo de registro:

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "nuevo.cliente",
  "password": "claveSegura123"
}
```

El registro siempre asigna el rol `USER`; no es posible elegir `ADMIN` desde el cliente. Si el usuario ya existe responde `409 Conflict`. La pantalla `/registro` inicia sesion automaticamente al crear la cuenta y permite mostrar u ocultar la contrasena con el boton del ojo.

Envia el access token en las rutas protegidas:

```http
GET /api/productos
Authorization: Bearer <accessToken>
```

### Permisos

| Recurso | `USER` | `ADMIN` | Publico |
| --- | --- | --- | --- |
| `GET /api/productos/**` | Si | Si | No |
| `POST`, `PUT`, `DELETE /api/productos/**` | No | Si | No |
| `GET`, `POST /api/compras` y `/api/compras/{id}/devoluciones` | Si | Si | No |
| `/api/auth/login`, `/register`, `/refresh`, `/logout` | - | - | Si |
| `/actuator/health`, `/actuator/info` | - | - | Si |
| `/v3/api-docs/**`, `/swagger-ui/**` | - | - | Si |

Usuarios de demostracion creados por `InicializadorDatos` cuando `app.seed.enabled=true` (perfiles `dev` y `test`; nunca en `prod`):

| Usuario | Contrasena | Rol |
| --- | --- | --- |
| `admin` | `admin123` | `ADMIN` |
| `user` | `user123` | `USER` |

El seed esta desactivado por defecto fuera de `dev`/`test`. Cambia las credenciales con las variables `APP_SEED_*` y usa contraseñas propias en cualquier entorno compartido.

## Frontend Angular

El frontend vive en `src/main/resources/frontend` y se sirve desde el mismo Spring Boot. Usa componentes standalone, signals, routing con lazy loading, guards e interceptores funcionales. El interceptor agrega el bearer token, normaliza los errores y, ante un `401`, intenta renovar la sesion y reintenta la solicitud una vez.

### Rutas

| Ruta | Componente | Acceso |
| --- | --- | --- |
| `/` | Redireccion | Redirige a `/productos` |
| `/login` | `LoginComponent` | Publico |
| `/registro` | `RegistroComponent` | Publico |
| `/productos` | `ProductoListComponent` | `authGuard` (USER o ADMIN) |
| `/carrito` | `CarritoComponent` | `authGuard` (USER o ADMIN) |
| `/mis-compras` | `MisComprasComponent` | `authGuard` (USER o ADMIN) |
| `/productos/nuevo` | `ProductoFormComponent` | `adminGuard` (ADMIN) |
| `/productos/:id/editar` | `ProductoFormComponent` | `adminGuard` (ADMIN) |
| `**` | `NotFoundComponent` | Publico |

La sesion se guarda en `localStorage`, se restaura al recargar y se renueva automaticamente 60 segundos antes de expirar. Si el refresh falla, se limpia la sesion y se redirige a `/login` conservando la URL original en `returnUrl`.

Todas las peticiones HTTP muestran una capa de carga global con anillos orbitales, barra de progreso y mensaje animado. `LoadingService` mantiene la animacion visible al menos **3 segundos** (`DURACION_MINIMA_CARGA_MS`), espera a que terminen las peticiones concurrentes y permite cambiar ese tiempo en un solo lugar. La interfaz respeta `prefers-reduced-motion` para reducir las animaciones.

### Mensajes y confirmaciones

Las acciones de la aplicacion comunican su resultado con notificaciones flotantes administradas por `NotificationService` y renderizadas por `NotificationCenterComponent`. Existen variantes de exito, error e informacion, con icono, barra de progreso, cierre manual y auto-cierre. Se usan al agregar productos al carrito, crear o actualizar productos, confirmar compras, vaciar el carrito y registrar devoluciones.

Las acciones delicadas usan `ConfirmDialogService` y `ConfirmDialogComponent` en lugar de `window.confirm`. El dialogo es accesible, se cierra con `Escape` o pulsando el fondo, distingue visualmente las acciones peligrosas y devuelve una `Promise<boolean>`. La eliminacion de un producto pide confirmacion con el nombre del producto, advierte que la accion no se puede deshacer y muestra una notificacion al completarse. La capa de carga queda en `z-index: 9000`, el dialogo en `9800` y las notificaciones en `10000` para mantener el orden visual correcto.

### Historial de busquedas

`HistorialBusquedaService` conserva las ultimas **8 busquedas** del catalogo en `localStorage`, bajo la clave `productos.busquedas`. Los terminos se guardan al confirmar el buscador con `Enter` o al salir del campo, se normalizan, se deduplican sin distinguir mayusculas y se ordenan del mas reciente al mas antiguo. Se ignoran las busquedas vacias o de una sola letra.

La fila **Recientes** permite reutilizar un termino con un clic, quitar una busqueda concreta o limpiar todo el historial; al limpiarlo se muestra una notificacion informativa. El historial de compras y devoluciones sigue disponible en **Mis compras**, por lo que no se duplica en el catalogo.

### Desarrollo

Para trabajar con recarga automatica usa la **Opcion B** del paso a paso. El proxy de `proxy.conf.json` envia `/api`, `/actuator`, `/v3/api-docs` y `/swagger-ui` a `http://localhost:8080`.

El formato se comprueba con `npm run format:check` y se aplica con `npm run format` (Prettier, configurado en `.prettierrc`). Ambos comandos se ejecutan desde `src/main/resources/frontend`.

### Compilar para Spring Boot

```bash
cd src/main/resources/frontend
npm run build
```

El resultado se escribe directamente en `src/main/resources/static`. Luego inicia la aplicacion con `mvn spring-boot:run` y abre `http://localhost:8080`.

Las rutas profundas como `/productos/5/editar`, `/carrito`, `/login` y `/registro` se redirigen a `index.html` mediante `SpaController`, y Angular resuelve la vista. La carpeta `static` es un artefacto de build: esta en `.gitignore` y se regenera con `npm run build` o en el CI.

## Carrito y compras

El catálogo permite agregar productos al carrito, ajustar cantidades, quitar artículos y confirmar la compra. El carrito se persiste en `localStorage` bajo la clave `productos.cart` y se limpia al cerrar sesión o al completar una compra.

Al confirmar, el backend valida cada producto y su stock, consolida artículos duplicados y descuenta el inventario dentro de una sola transacción. Si un producto no existe se devuelve `404`; si no hay stock suficiente o el carrito está vacío, `400`/`409` según corresponda. El historial muestra cada pedido con sus artículos, total, estado y unidades devueltas.

Desde **Mis compras** se puede devolver un producto o el pedido completo. Cada devolución valida las unidades compradas, suma de nuevo esas unidades al inventario del producto y actualiza el estado del pedido (`COMPLETADA`, `DEVUELTA_PARCIAL` o `DEVUELTA_TOTAL`). No se pueden devolver productos ajenos al pedido, compras de otro usuario ni más unidades de las compradas.

Ejemplo de devolución parcial:

```http
POST /api/compras/12/devoluciones
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "items": [
    { "productoId": 3, "cantidad": 1 }
  ]
}
```

La respuesta es la compra actualizada con `estado`, `totalDevuelto` y `cantidadDevuelta` por artículo.

## Endpoints

| Metodo | Ruta | Descripcion | Acceso | Respuestas |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/login` | Inicia sesion | Publico | 200, 400, 401 |
| POST | `/api/auth/register` | Registra un cliente con rol USER | Publico | 201, 400, 409 |
| POST | `/api/auth/refresh` | Renueva la sesion | Publico | 200, 400, 401 |
| POST | `/api/auth/logout` | Revoca el refresh token | Publico | 204, 400, 401 |
| GET | `/api/productos` | Lista paginada con busqueda por nombre | USER/ADMIN | 200, 400, 401 |
| GET | `/api/productos/{id}` | Obtiene un producto por id | USER/ADMIN | 200, 401, 404 |
| POST | `/api/productos` | Crea un producto | ADMIN | 201, 400, 401, 403 |
| PUT | `/api/productos/{id}` | Actualiza un producto | ADMIN | 200, 400, 401, 403, 404 |
| DELETE | `/api/productos/{id}` | Elimina un producto | ADMIN | 204, 401, 403, 404 |
| POST | `/api/compras` | Confirma el carrito y descuenta stock | USER/ADMIN | 201, 400, 401, 404, 409 |
| GET | `/api/compras` | Lista las compras del usuario autenticado | USER/ADMIN | 200, 401 |
| POST | `/api/compras/{id}/devoluciones` | Devuelve unidades de un pedido y repone stock | USER/ADMIN | 200, 400, 401, 404 |
| GET | `/v3/api-docs` | Documento OpenAPI en JSON | 200 |
| GET | `/swagger-ui.html` | Swagger UI interactivo | 200 |
| GET | `/actuator/health` | Estado de la aplicacion | 200 |

### Paginacion y busqueda

`GET /api/productos` acepta estos parametros:

| Parametro | Por defecto | Descripcion |
| --- | --- | --- |
| `page` | `0` | Numero de pagina (desde 0) |
| `size` | `10` | Elementos por pagina (1 a 100) |
| `sort` | `id` | Campo de orden: `id`, `nombre`, `precio` o `stock` |
| `direction` | `asc` | Direccion: `asc` o `desc` |
| `nombre` | - | Filtro opcional por nombre, sin distinguir mayusculas |

Ejemplo:

```http
GET /api/productos?page=0&size=5&sort=precio&direction=desc&nombre=lap
```

Respuesta:

```json
{
  "content": [
    {
      "id": 1,
      "nombre": "Laptop",
      "precio": 2500000.00,
      "stock": 10,
      "categoria": "Tecnologia",
      "descripcion": "Portatil de alto rendimiento para trabajo y estudio"
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

Si `page`, `size`, `sort` o `direction` son invalidos se devuelve `400 Bad Request` con el titulo `Parametros invalidos`.

### Ejemplo de creacion

```http
POST /api/productos
Content-Type: application/json
```

```json
{
  "nombre": "Laptop",
  "precio": 2500000.00,
  "stock": 10,
  "categoria": "Tecnologia",
  "descripcion": "Portatil de alto rendimiento para trabajo y estudio"
}
```

Respuesta `201 Created` (incluye el encabezado `Location` con la URL del recurso creado):

```json
{
  "id": 1,
  "nombre": "Laptop",
  "precio": 2500000.00,
  "stock": 10,
  "categoria": "Tecnologia",
  "descripcion": "Portatil de alto rendimiento para trabajo y estudio"
}
```

## Validaciones

- `nombre`: obligatorio, maximo 120 caracteres.
- `precio`: obligatorio, mayor o igual a 0, maximo 2 decimales.
- `stock`: obligatorio, mayor o igual a 0.
- `categoria`: opcional, maximo 60 caracteres.
- `descripcion`: opcional, maximo 300 caracteres.

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

## Documentacion OpenAPI

Con la aplicacion en ejecucion:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

## Pruebas automatizadas

La suite del backend incluye **131 pruebas** y no requiere MySQL: usa H2 en memoria con el perfil `test`. El frontend agrega **99 pruebas** con Vitest, distribuidas en 20 archivos.

Resultados de la ultima ejecucion local (`2026-11-18`):

| Comando | Resultado |
| --- | --- |
| `mvn -B verify` | 131 pruebas, 0 fallos; umbral JaCoCo del 80 % cumplido |
| `npm run test:ci` | 20 archivos, 99 pruebas aprobadas |
| `npm run format:check` | Formato Prettier correcto |
| `npm run build` | Build de Angular generado en `src/main/resources/static` |

| Clase | Tipo | Pruebas | Que cubre |
| --- | --- | ---: | --- |
| `ProductoServiceTest` | Unitaria con Mockito | 20 | CRUD, paginacion, busqueda, ordenamiento, parametros invalidos e imagenes por categoria |
| `ProductoControllerTest` | Web con MockMvc | 17 | Codigos HTTP, validacion, paginacion, errores y metadatos |
| `ProductoApiIntegrationTest` | Integracion | 9 | Flujo CRUD autenticado, H2, paginacion, busqueda, health, OpenAPI y 404 |
| `AuthApiIntegrationTest` | Integracion | 16 | Login, registro con rol forzado, duplicados 409, refresh con rotacion, logout, errores 401/403 y permisos por rol |
| `CompraApiIntegrationTest` | Integracion | 16 | Checkout, descuento y reposicion de stock, devoluciones parciales/totales, duplicados, historial, 400/404/409 y 401 |
| `ProductoRepositoryTest` | Persistencia con `@DataJpaTest` | 6 | Guardado, actualizacion, borrado, busqueda y paginacion |
| `GlobalExceptionHandlerTest` | Unitaria | 11 | Todos los `ProblemDetail` y errores de validacion |
| `ProductoRequestValidationTest` | Unitaria (Bean Validation) | 15 | Reglas de nombre, precio, stock e `imagenUrl` |
| `ProductoResponseTest` | Unitaria | 4 | Mapeo entidad -> DTO, incluidos valores nulos e imagen |
| `PageResponseTest` | Unitaria | 3 | Mapeo de paginas de Spring Data |
| `OpenApiConfigTest` | Unitaria | 1 | Metadatos de la documentacion OpenAPI |
| `SpaControllerTest` | Unitaria | 2 | Redireccion de rutas del SPA, incluida `/registro`, al `index.html` |
| `JwtServiceTest` | Unitaria | 10 | Generacion, `jti`, issuer/audiencia, expiracion, firma, secreto y extraccion de claims |
| `DemoApplicationTest` | Smoke test | 1 | Carga del contexto de Spring |

### Pruebas destacadas

| Clase | Prueba | Que valida |
| --- | --- | --- |
| `AuthApiIntegrationTest` | `loginDeAdminDevuelveTokensYRoles` | Login real y payload de tokens |
| `AuthApiIntegrationTest` | `registroCreaUnClienteConRolUserYDevuelveTokens` | Alta real de cliente, BCrypt y auto-login |
| `AuthApiIntegrationTest` | `registroNoPermiteElegirElRolAdmin` | El rol `ADMIN` enviado por el cliente se ignora |
| `AuthApiIntegrationTest` | `registroDuplicadoDevuelve409` | Conflicto al repetir el nombre de usuario |
| `AuthApiIntegrationTest` | `registroConDatosInvalidosDevuelve400` | Validacion del contrato de registro |
| `AuthApiIntegrationTest` | `refreshRotaElTokenYRevocaElAnterior` | Rotacion y replay de refresh token |
| `AuthApiIntegrationTest` | `logoutRevocaElRefreshToken` | Logout e invalidez posterior |
| `AuthApiIntegrationTest` | `usuarioNormalPuedeConsultarPeroNoEscribir` | `USER` recibe 403 al escribir |
| `AuthApiIntegrationTest` | `adminPuedeEscribirProductos` | `ADMIN` puede escribir |
| `AuthApiIntegrationTest` | `endpointsDeProductosRequierenAutenticacion` | Proteccion de rutas |
| `CompraApiIntegrationTest` | `usuarioCompraYSeDescuentaElStock` | Checkout real y persistencia del pedido |
| `CompraApiIntegrationTest` | `compraSinStockDevuelve409YNoModificaElProducto` | Transaccion y respuesta 409 |
| `CompraApiIntegrationTest` | `compraVaciaDevuelve400` | Validacion del carrito |
| `CompraApiIntegrationTest` | `devolucionTotalReponeElInventarioYCambiaElEstado` | Devolucion total y reposicion de stock |
| `CompraApiIntegrationTest` | `devolucionParcialReponeSoloLasUnidadesDevueltas` | Devoluciones parciales acumuladas |
| `CompraApiIntegrationTest` | `noSePuedeDevolverUnaCompraDeOtroUsuario` | Aislamiento de devoluciones por usuario |
| `ProductoApiIntegrationTest` | `healthEndpointDevuelveUp` | Actuator responde `UP` |
| `ProductoApiIntegrationTest` | `documentacionOpenApiEstaDisponible` | `/v3/api-docs` expone el titulo y las rutas |
| `ProductoServiceTest` | `listarEnviaElOrdenamientoSolicitadoAlRepositorio` | Que `sort` y `direction` lleguen al `Pageable` |
| `ProductoControllerTest` | `listarUsaValoresPorDefectoCuandoNoSeEnvianParametros` | Valores por defecto `page=0`, `size=10`, `sort=id`, `direction=asc` |
| `ProductoRepositoryTest` | `eliminaUnProductoExistente` | Borrado persistente |
| `GlobalExceptionHandlerTest` | `errorInesperadoDevuelve500` | Respuesta `ProblemDetail` para errores no controlados |
| `SpaControllerTest` | `lasRutasDelFrontendSeReenvianAlIndex` | Forward de `/login`, `/registro`, `/productos/**`, `/carrito` y `/mis-compras` a `index.html` |
| `JwtServiceTest` | `rechazaUnTokenFirmadoConOtraClave` | Que un token con firma ajena no sea aceptado |
| `DemoApplicationTest` | `contextLoads` | La aplicacion arranca con el perfil `test` |

### Pruebas del frontend

| Archivo | Pruebas | Que cubre |
| --- | ---: | --- |
| `producto.service.spec.ts` | 6 | Listado con filtros y paginacion, obtener, crear, actualizar y eliminar |
| `auth.service.spec.ts` | 6 | Login, registro, restauracion, refresh compartido, fallo de refresh y logout |
| `api.interceptor.spec.ts` | 6 | Cabeceras, bearer token, retry tras 401, refresh fallido y errores |
| `loading.interceptor.spec.ts` | 2 | Muestra la animacion en cada peticion y la oculta al completar o fallar |
| `loading.service.spec.ts` | 4 | Duracion minima de 3 segundos, concurrencia y reinicio de la espera |
| `loading-overlay.component.spec.ts` | 2 | Render condicional y contenido de la animacion de carga |
| `notification.service.spec.ts` | 3 | Alta de notificaciones, auto-cierre, cierre manual y temporizadores independientes |
| `confirm-dialog.service.spec.ts` | 3 | Respuestas `Promise`, valores por defecto y cancelacion al abrir otro dialogo |
| `notification-center.component.spec.ts` | 3 | Pila vacia, notificacion de exito, cierre manual y alertas accesibles |
| `confirm-dialog.component.spec.ts` | 3 | Render condicional, accion peligrosa, botones y cierre con `Escape` |
| `historial-busqueda.service.spec.ts` | 7 | Persistencia, limite de 8, deduplicacion, eliminacion, limpieza y datos corruptos |
| `auth.guard.spec.ts` | 4 | Proteccion de rutas y redirecciones por rol |
| `producto-list.component.spec.ts` | 12 | Carga, error, paginacion, ordenamiento, carrito, agotado, eliminacion confirmada/cancelada, historial y acciones solo para ADMIN |
| `producto-form.component.spec.ts` | 4 | Validacion, creacion, notificaciones, edicion y actualizacion |
| `cart.service.spec.ts` | 7 | Persistencia, limites de stock, quitar, vaciar y checkout |
| `carrito.component.spec.ts` | 6 | Listado, cantidades, eliminacion, compra notificada, vaciado confirmado/cancelado |
| `mis-compras.component.spec.ts` | 6 | Historial, estado vacio, errores y devoluciones |
| `login.component.spec.ts` | 6 | Validacion, login, `returnUrl`, error, credenciales demo y visibilidad de contrasena |
| `registro.component.spec.ts` | 5 | Validacion, contrasenas distintas, registro, usuario duplicado y visibilidad de contrasenas |
| `app.spec.ts` | 4 | Shell, navegacion, sesion visible, badge del carrito y logout |

### Paso a paso para el frontend

1. Verifica Node y npm:

   ```bash
   node -v
   npm -v
   ```

2. Instala dependencias (solo la primera vez o cuando cambie `package.json`):

   ```bash
   cd src/main/resources/frontend
   npm install
   ```

3. Ejecuta las pruebas en modo CI:

   ```bash
   npm run test:ci
   ```

4. Compila el frontend y verifica que se genere en `src/main/resources/static`:

   ```bash
   npm run build
   ```

5. Inicia Spring Boot y abre `http://localhost:8080`:

   ```bash
   mvn spring-boot:run
   ```

### Paso a paso para ejecutar las pruebas

1. Instala JDK 21 y Maven 3.9 o superior y verifica las versiones:

   ```bash
   java -version
   mvn -version
   ```

2. Ubicate en la raiz del proyecto, donde esta el `pom.xml`.

3. Ejecuta toda la suite de pruebas:

   ```bash
   mvn clean test
   ```

4. Verifica el resultado. Debe aparecer un resumen similar a:

   ```text
    Tests run: 131, Failures: 0, Errors: 0, Skipped: 0
   BUILD SUCCESS
   ```

5. Revisa el detalle por clase en `target/surefire-reports/`.

6. Para ejecutar solo una clase de prueba:

   ```bash
   mvn -Dtest=ProductoServiceTest test
   ```

7. Para ejecutar solo un metodo:

   ```bash
   mvn -Dtest=ProductoServiceTest#listarDevuelveLaPaginaDeProductos test
   ```

8. Para generar el reporte de cobertura y validar el umbral del 80 %:

   ```bash
   mvn verify
   ```

9. Abre el reporte de cobertura en `target/site/jacoco/index.html`. En Windows:

   ```powershell
   start target\site\jacoco\index.html
   ```

No necesitas MySQL: el perfil `test` (`src/test/resources/application-test.yaml`) usa H2 en memoria.

### Como agregar una nueva prueba

1. Elige la capa a probar y crea el archivo en el paquete correspondiente dentro de `src/test/java/com/example/demo`.
2. Para logica de negocio usa Mockito:

   ```java
   @ExtendWith(MockitoExtension.class)
   class MiPruebaServiceTest {
       @Mock
       private ProductoRepository repository;

       @InjectMocks
       private ProductoService service;

       @Test
       void miCaso() {
           // given / when / then
       }
   }
   ```

3. Para endpoints usa `@WebMvcTest` con `MockMvc` y `@MockitoBean`.
4. Para repositorios usa `@DataJpaTest` con `@ActiveProfiles("test")`.
5. Para flujos completos usa `@SpringBootTest` con `@AutoConfigureMockMvc` y `@ActiveProfiles("test")`.
6. Ejecuta `mvn test` y confirma que las pruebas nuevas y existentes siguen en verde.

### Ejecucion automatica en GitHub Actions

El workflow `.github/workflows/ci.yml` se ejecuta en cada `push` y `pull request`, con permisos de solo lectura, cancelacion de ejecuciones anticipadas y tiempos limite por job:

1. Descarga el codigo del repositorio.
2. **Job `pruebas`**: configura Java 21 (Temurin) con cache de Maven, ejecuta `mvn -B clean verify` con las 131 pruebas y valida la cobertura, y publica `target/site/jacoco` como artefacto descargable.
3. **Job `frontend`**: configura Node 24 con cache de npm, ejecuta `npm ci`, `npm run format:check`, `npm run test:ci` con las 99 pruebas de Vitest y compila con `npm run build`.
4. **Job `compose`**: valida la sintaxis de `docker-compose.yml` con `docker compose config --quiet`.
5. `.github/dependabot.yml` propone actualizaciones semanales de Maven, npm y GitHub Actions.

Para verlo, sube el proyecto a GitHub y abre la pestana **Actions** del repositorio. Para reproducir la ejecucion localmente:

```bash
mvn -B clean verify
cd src/main/resources/frontend
npm ci
npm run format:check
npm run test:ci
npm run build
```

### Cobertura

JaCoCo genera el reporte en `target/site/jacoco/index.html` y falla la construccion (`mvn verify`) si la cobertura de lineas baja del 80 %.

## Postman

1. Importa `postman/Pruebas-Productos.postman_collection.json`.
2. Verifica la variable `baseUrl` (por defecto `http://localhost:8080`).
3. Ejecuta la coleccion en orden. El primer request inicia sesion como `admin` y guarda `accessToken` y `refreshToken`; el resto usa el bearer token automaticamente.
4. Luego crea un producto, lo consulta, lo actualiza, lo elimina y valida los casos de error.

La coleccion guarda automaticamente el id del producto creado en la variable `productoId` e incluye pruebas de paginacion y busqueda. Si cambias el usuario seed, ajusta el cuerpo del request `Login admin`.
