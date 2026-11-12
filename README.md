# Productos API

Aplicacion web de catalogo y compras construida con Spring Boot y Angular. Incluye paginacion, busqueda, validacion de datos, autenticacion JWT con roles, carrito de compras, checkout con descuento transaccional de stock, historial de pedidos, manejo centralizado de errores, documentacion OpenAPI, pruebas automatizadas con cobertura y una coleccion de Postman lista para ejecutar.

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
  src/app/features/   Productos, carrito, compras y login
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

2. Configura las credenciales con variables de entorno (o deja los valores por defecto):

| Variable | Valor por defecto |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/pruebas_db?...` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `root` |
| `JPA_DDL_AUTO` | `update` |
| `JPA_SHOW_SQL` | `false` |
| `SERVER_PORT` | `8080` |
| `JWT_SECRET` | Secreto Base64 de desarrollo incluido en `application.yaml` |
| `JWT_ACCESS_TOKEN_MINUTES` | `20` |
| `JWT_REFRESH_TOKEN_DAYS` | `7` |
| `APP_SEED_ENABLED` | `true` |
| `APP_SEED_ADMIN_USERNAME` / `APP_SEED_ADMIN_PASSWORD` | `admin` / `admin123` |
| `APP_SEED_USER_USERNAME` / `APP_SEED_USER_PASSWORD` | `user` / `user123` |

En produccion define siempre un `JWT_SECRET` propio de al menos 256 bits codificado en Base64.

Ejemplo en PowerShell:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "tu_password"
mvn spring-boot:run
```

## Perfiles de configuracion

| Perfil | Comportamiento |
| --- | --- |
| `dev` | Muestra SQL, `ddl-auto=update`, logging DEBUG de la aplicacion |
| `prod` | `ddl-auto=validate`, sin SQL en logs, health sin detalles |
| `test` | H2 en memoria, `ddl-auto=create-drop` |

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

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

4. Inicia Spring Boot desde la raiz del proyecto:

   ```bash
   mvn spring-boot:run
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
   mvn spring-boot:run
   ```

2. Terminal 2, frontend:

   ```bash
   cd src/main/resources/frontend
   npm ci
   npm start
   ```

3. Abre `http://localhost:4200`. El proxy envia `/api`, `/actuator`, `/v3/api-docs` y `/swagger-ui` a `http://localhost:8080`.

## Autenticacion y roles

La API usa JWT stateless. El login devuelve un access token de 20 minutos y un refresh token de 7 dias; cada refresh rota el token y revoca el anterior. Los refresh tokens se guardan como hash SHA-256 en la base de datos.

| Metodo | Ruta | Descripcion | Respuestas |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | Inicia sesion con `username` y `password` | 200, 400, 401 |
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
| `GET`, `POST /api/compras` | Si | Si | No |
| `/api/auth/login`, `/refresh`, `/logout` | - | - | Si |
| `/actuator/health`, `/actuator/info` | - | - | Si |
| `/v3/api-docs/**`, `/swagger-ui/**` | - | - | Si |

Usuarios de demostracion creados por `InicializadorDatos` cuando `app.seed.enabled=true`:

| Usuario | Contrasena | Rol |
| --- | --- | --- |
| `admin` | `admin123` | `ADMIN` |
| `user` | `user123` | `USER` |

Deshabilita el seed en produccion con `APP_SEED_ENABLED=false` o cambia las credenciales mediante las variables `APP_SEED_*`.

## Frontend Angular

El frontend vive en `src/main/resources/frontend` y se sirve desde el mismo Spring Boot. Usa componentes standalone, signals, routing con lazy loading, guards e interceptores funcionales. El interceptor agrega el bearer token, normaliza los errores y, ante un `401`, intenta renovar la sesion y reintenta la solicitud una vez.

### Rutas

| Ruta | Componente | Acceso |
| --- | --- | --- |
| `/` | Redireccion | Redirige a `/productos` |
| `/login` | `LoginComponent` | Publico |
| `/productos` | `ProductoListComponent` | `authGuard` (USER o ADMIN) |
| `/carrito` | `CarritoComponent` | `authGuard` (USER o ADMIN) |
| `/mis-compras` | `MisComprasComponent` | `authGuard` (USER o ADMIN) |
| `/productos/nuevo` | `ProductoFormComponent` | `adminGuard` (ADMIN) |
| `/productos/:id/editar` | `ProductoFormComponent` | `adminGuard` (ADMIN) |
| `**` | `NotFoundComponent` | Publico |

La sesion se guarda en `localStorage`, se restaura al recargar y se renueva automaticamente 60 segundos antes de expirar. Si el refresh falla, se limpia la sesion y se redirige a `/login` conservando la URL original en `returnUrl`.

### Desarrollo

Para trabajar con recarga automatica usa la **Opcion B** del paso a paso. El proxy de `proxy.conf.json` envia `/api`, `/actuator`, `/v3/api-docs` y `/swagger-ui` a `http://localhost:8080`.

### Compilar para Spring Boot

```bash
cd src/main/resources/frontend
npm run build
```

El resultado se escribe directamente en `src/main/resources/static`. Luego inicia la aplicacion con `mvn spring-boot:run` y abre `http://localhost:8080`.

Las rutas profundas como `/productos/5/editar`, `/carrito` y `/login` se redirigen a `index.html` mediante `SpaController`, y Angular resuelve la vista. La carpeta `static` es un artefacto de build: esta en `.gitignore` y se regenera con `npm run build` o en el CI.

## Carrito y compras

El catálogo permite agregar productos al carrito, ajustar cantidades, quitar artículos y confirmar la compra. El carrito se persiste en `localStorage` bajo la clave `productos.cart` y se limpia al cerrar sesión o al completar una compra.

Al confirmar, el backend valida cada producto y su stock, consolida artículos duplicados y descuenta el inventario dentro de una sola transacción. Si un producto no existe se devuelve `404`; si no hay stock suficiente o el carrito está vacío, `400`/`409` según corresponda. El historial muestra cada pedido con sus artículos y total.

## Endpoints

| Metodo | Ruta | Descripcion | Acceso | Respuestas |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/login` | Inicia sesion | Publico | 200, 400, 401 |
| POST | `/api/auth/refresh` | Renueva la sesion | Publico | 200, 400, 401 |
| POST | `/api/auth/logout` | Revoca el refresh token | Publico | 204, 400, 401 |
| GET | `/api/productos` | Lista paginada con busqueda por nombre | USER/ADMIN | 200, 400, 401 |
| GET | `/api/productos/{id}` | Obtiene un producto por id | USER/ADMIN | 200, 401, 404 |
| POST | `/api/productos` | Crea un producto | ADMIN | 201, 400, 401, 403 |
| PUT | `/api/productos/{id}` | Actualiza un producto | ADMIN | 200, 400, 401, 403, 404 |
| DELETE | `/api/productos/{id}` | Elimina un producto | ADMIN | 204, 401, 403, 404 |
| POST | `/api/compras` | Confirma el carrito y descuenta stock | USER/ADMIN | 201, 400, 401, 404, 409 |
| GET | `/api/compras` | Lista las compras del usuario autenticado | USER/ADMIN | 200, 401 |
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

La suite del backend incluye **104 pruebas** y no requiere MySQL: usa H2 en memoria con el perfil `test`. El frontend agrega **54 pruebas** con Vitest.

| Clase | Tipo | Pruebas | Que cubre |
| --- | --- | ---: | --- |
| `ProductoServiceTest` | Unitaria con Mockito | 18 | CRUD, paginacion, busqueda, ordenamiento y parametros invalidos |
| `ProductoControllerTest` | Web con MockMvc | 17 | Codigos HTTP, validacion, paginacion, errores y metadatos |
| `ProductoApiIntegrationTest` | Integracion | 9 | Flujo CRUD autenticado, H2, paginacion, busqueda, health, OpenAPI y 404 |
| `AuthApiIntegrationTest` | Integracion | 12 | Login, refresh con rotacion, logout, errores 401/403 y permisos por rol |
| `CompraApiIntegrationTest` | Integracion | 8 | Checkout, descuento de stock, duplicados, historial, 400/404/409 y 401 |
| `ProductoRepositoryTest` | Persistencia con `@DataJpaTest` | 6 | Guardado, actualizacion, borrado, busqueda y paginacion |
| `GlobalExceptionHandlerTest` | Unitaria | 11 | Todos los `ProblemDetail` y errores de validacion |
| `ProductoRequestValidationTest` | Unitaria (Bean Validation) | 14 | Reglas de nombre, precio y stock |
| `ProductoResponseTest` | Unitaria | 3 | Mapeo entidad -> DTO, incluidos valores nulos |
| `PageResponseTest` | Unitaria | 3 | Mapeo de paginas de Spring Data |
| `OpenApiConfigTest` | Unitaria | 1 | Metadatos de la documentacion OpenAPI |
| `SpaControllerTest` | Unitaria | 1 | Redireccion de rutas del SPA al `index.html` |
| `DemoApplicationTest` | Smoke test | 1 | Carga del contexto de Spring |

### Pruebas destacadas

| Clase | Prueba | Que valida |
| --- | --- | --- |
| `AuthApiIntegrationTest` | `loginDeAdminDevuelveTokensYRoles` | Login real y payload de tokens |
| `AuthApiIntegrationTest` | `refreshRotaElTokenYRevocaElAnterior` | Rotacion y replay de refresh token |
| `AuthApiIntegrationTest` | `logoutRevocaElRefreshToken` | Logout e invalidez posterior |
| `AuthApiIntegrationTest` | `usuarioNormalPuedeConsultarPeroNoEscribir` | `USER` recibe 403 al escribir |
| `AuthApiIntegrationTest` | `adminPuedeEscribirProductos` | `ADMIN` puede escribir |
| `AuthApiIntegrationTest` | `endpointsDeProductosRequierenAutenticacion` | Proteccion de rutas |
| `CompraApiIntegrationTest` | `compraDescuentaStockYRegistraElHistorial` | Checkout real y persistencia del pedido |
| `CompraApiIntegrationTest` | `compraSinStockNoModificaElInventario` | Transaccion y respuesta 409 |
| `CompraApiIntegrationTest` | `carritoVacioDevuelve400` | Validacion del carrito |
| `ProductoApiIntegrationTest` | `healthEndpointDevuelveUp` | Actuator responde `UP` |
| `ProductoApiIntegrationTest` | `documentacionOpenApiEstaDisponible` | `/v3/api-docs` expone el titulo y las rutas |
| `ProductoServiceTest` | `listarEnviaElOrdenamientoSolicitadoAlRepositorio` | Que `sort` y `direction` lleguen al `Pageable` |
| `ProductoControllerTest` | `listarUsaValoresPorDefectoCuandoNoSeEnvianParametros` | Valores por defecto `page=0`, `size=10`, `sort=id`, `direction=asc` |
| `ProductoRepositoryTest` | `eliminaUnProductoExistente` | Borrado persistente |
| `GlobalExceptionHandlerTest` | `errorInesperadoDevuelve500` | Respuesta `ProblemDetail` para errores no controlados |
| `SpaControllerTest` | `redirigeLasRutasDelFrontendAlIndex` | Forward de rutas del SPA a `index.html` |
| `DemoApplicationTest` | `contextLoads` | La aplicacion arranca con el perfil `test` |

### Pruebas del frontend

| Archivo | Pruebas | Que cubre |
| --- | ---: | --- |
| `producto.service.spec.ts` | 6 | Listado con filtros y paginacion, obtener, crear, actualizar y eliminar |
| `auth.service.spec.ts` | 5 | Login, restauracion, refresh compartido, fallo de refresh y logout |
| `api.interceptor.spec.ts` | 6 | Cabeceras, bearer token, retry tras 401, refresh fallido y errores |
| `auth.guard.spec.ts` | 4 | Proteccion de rutas y redirecciones por rol |
| `producto-list.component.spec.ts` | 6 | Carga, error, paginacion, ordenamiento, carrito y acciones solo para ADMIN |
| `producto-form.component.spec.ts` | 4 | Validacion, creacion, categoria/descripcion, edicion y actualizacion |
| `cart.service.spec.ts` | 7 | Persistencia, limites de stock, quitar, vaciar y checkout |
| `carrito.component.spec.ts` | 4 | Listado, cantidades, eliminacion y confirmacion de compra |
| `mis-compras.component.spec.ts` | 3 | Historial, estado vacio y errores |
| `login.component.spec.ts` | 5 | Validacion, login, `returnUrl`, error y credenciales demo |
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
   Tests run: 104, Failures: 0, Errors: 0, Skipped: 0
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

El workflow `.github/workflows/ci.yml` se ejecuta en cada `push` y `pull request`:

1. Descarga el codigo del repositorio.
2. **Job `pruebas`**: configura Java 21 (Temurin) con cache de Maven, ejecuta `mvn -B verify` con las 104 pruebas y valida la cobertura, y publica `target/site/jacoco` como artefacto descargable.
3. **Job `frontend`**: configura Node 24 con cache de npm, ejecuta `npm ci`, `npm run test:ci` con las 54 pruebas de Vitest y compila con `npm run build`.

Para verlo, sube el proyecto a GitHub y abre la pestana **Actions** del repositorio. Para reproducir la ejecucion localmente:

```bash
mvn -B verify
cd src/main/resources/frontend
npm ci
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
