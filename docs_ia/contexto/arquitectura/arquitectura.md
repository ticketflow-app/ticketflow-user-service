# Arquitectura — ticketflow-user-service

Microservicio de **usuarios, autenticación y roles** de TicketFlow. Emite el JWT que valida el
API Gateway.

> **Alcance de este documento:** describe **exclusivamente la arquitectura de este servicio**.
> La arquitectura de los demás microservicios de la plataforma (API Gateway, Eureka, Inventario,
> Catálogo, Check-in, Notificaciones, Reseñas, Analítica) está en el repositorio que le
> corresponde a cada uno; aquí solo se mencionan las **dependencias externas** en los puntos
> donde el contrato entre servicios es relevante.
>
> **Fuente de verdad del contrato HTTP:** `docs_ia/contexto/contrato_api.md`.
> **Contexto de negocio, requisitos y calidad:** `docs_ia/contexto/`.

---

## 1. Propósito y alcance

El servicio administra la identidad y la autenticación de los actores de la plataforma:

- Registro de usuarios.
- Inicio de sesión y emisión de tokens.
- Consulta del perfil del usuario autenticado.
- Gestión de roles.

Sus consumidores son:

| Consumidor | Qué consume |
|---|---|
| API Gateway | Valida el JWT emitido por este servicio en cada petición (secreto HS256 compartido). |
| Frontend (vía BFF del gateway) | `POST /register`, `POST /login`, `GET /me`, `PATCH /users/{id}/rol`. |

**Fuera de alcance** (no pertenece a este servicio): catálogo de eventos, disponibilidad y
reservas, emisión de boletos, check-in, notificaciones, reseñas y analítica.

## 2. Responsabilidad y límites

- Es el **dueño único** de los datos de usuario: ninguna otra tabla de la plataforma lo modifica.
- Es la **única autoridad de roles**: el token que emite es la fuente de verdad de los permisos.
- **No** consume datos de otros servicios: es autónomo en cuanto a datos.
- **No** expone endpoints de lectura masiva ni paginación de usuarios (no están en el contrato).
- No emite eventos de dominio: la especificación de plataforma contempla el topic
  `usuarios.registrados`, pero **este servicio no usa Kafka** (ver §12).

## 3. Objetivos arquitectónicos

- Aislar las reglas de negocio de la infraestructura (Spring, JPA, HTTP, hashing).
- Emitir un token verificable **sin consultar a este servicio** en cada petición, para no ser un
  cuello de botella ni un punto único de falla en el camino crítico.
- Mantener la trazabilidad de las operaciones sensible (registro, login, cambio de rol).
- Permitir que el frontend evolucione el contrato sin romper consumidores.
- Dejar la puerta abierta a refresh tokens, revocación y bloqueo por intentos fallidos, hoy
  ausentes (§15).

## 4. Estilo arquitectónico

**Arquitectura hexagonal (puertos y adaptadores)** con dos contextos de nivel superior dentro
del paquete base `com.ticketflow.user`:

```
com.ticketflow.user
├── common/          # transversal a todos los contextos
└── user/            # contexto acotado: usuarios
```

Reglas del estilo:

1. El **dominio** no depende de Spring, JPA, HTTP ni de ninguna biblioteca de infraestructura.
2. La **aplicación** orquesta los casos de uso usando solo los puertos declarados.
3. Los **adaptadores** implementan los puertos y son los únicos que conocen la tecnología
   concreta (JPA, Argon2, jjwt, HTTP).
4. La dependencia apunta siempre **hacia adentro**: `infrastructure → application → domain`.
5. Ningún caso de uso persiste ni hashea directamente: usa `UserRepositoryPort`,
   `PasswordEncoderPort` y `TokenProviderPort`.

> La estructura de paquetes sigue la guía del repositorio:
> `docs/guia-microservicio-clean-code.md`.

## 5. Stack tecnológico

Versiones **obligatorias y uniformes para todo el proyecto**. Este repositorio solo declara las
tecnologías que **usa**; las de los demás servicios están en sus respectivos
`contrato_api.md`.

| Tecnología | Versión estándar | Versión actual en el repo | Estado | Uso en el servicio |
|---|---|---|---|---|
| Java | **25 (LTS)** | 21 | ⚠️ Pendiente de migración | Lenguaje |
| Spring Boot | **4.1.1** | 4.1.1 | ✔ Al día | Framework, auto-configuración, actuator |
| Spring Cloud | **2025.1.3** | 2025.1.3 | ✔ Al día | Cliente de Eureka |
| Spring MVC | **6.x** (WebMVC) | 6.x (Boot 4.1.1) | ✔ Al día | API REST síncrona (`spring-boot-starter-webmvc`) |
| Spring Security | **7.x** (Boot 4.1.1) | 7.x | ✔ Al día | Cadena de filtros, `STATELESS` |
| PostgreSQL | **18.x** | 18 (`postgres:18-alpine`) | ✔ Al día | Persistencia de usuarios |
| Hibernate / JPA | BOM de Boot 4 | 6.x | ✔ Al día | Mapeo de `UserEntity` |
| jjwt | **0.13.0** | 0.13.0 | ✔ Al día | Emisión y validación del JWT (HS256) |
| Argon2 | `de.mkammerer:argon2-jvm` | en el `pom.xml` | ✔ Al día | Hash de contraseñas (Argon2id) |
| MapStruct | **1.5.5** | 1.5.5 | ✔ Al día | Mapeo entidad ↔ dominio ↔ DTO |
| Lombok | BOM de Boot 4 | en el `pom.xml` | ✔ Al día | Reducción de código boilerplate |
| springdoc-openapi | **3.0.x** | 3.x (BOM de Boot) | ✔ Al día | `/swagger-ui.html`, `/v3/api-docs` |

> Este servicio **no** usa Redis, Kafka, Flyway, Resilience4j ni WebFlux: no aparecen en su stack.
> Redis lo usan el API Gateway y el servicio de Inventario; Kafka, solo el de Inventario.

### 5.1 Efectos de la versión estándar de Java (25)

La migración a Java 25 habilita **hilos virtuales**. Es relevante en este servicio porque
**Argon2 es una operación de CPU intensiva** (`ITERATIONS=2`, `MEMORY_IN_KIB=65536`,
`PARALLELISM=1`): con hilos de plataforma, un pico de logins bloquea el pool de Tomcat. La
migración es, por tanto, una decisión de **rendimiento**, no solo de versión.

## 6. Estructura de paquetes

```text
src/main/java/com/ticketflow/user/
├── UserApplication.java                  # @SpringBootApplication
│
├── common/                               # transversal a todos los contextos
│   ├── application/dto/ApiResponse.java  # envelope de respuesta
│   ├── domain/exception/                 # TicketflowException, RequiredAttributeException
│   ├── domain/rules/DomainRule.java      # contrato de regla de dominio
│   └── infrastructure/
│       ├── config/OpenApiConfig.java
│       └── handler/GlobalExceptionHandler.java
│
└── user/                                 # contexto acotado: usuarios
    ├── application/
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── dto/                  # RegisterRequestDTO, LoginRequestDTO,
    │   │   │   │                         # LoginResponseDTO, GetUserResponseDTO,
    │   │   │   │                         # ChangeRoleRequestDTO
    │   │   │   └── service/              # RegisterUserPort, LoginUserPort,
    │   │   │                             # GetMePort, ChangeRolePort
    │   │   └── out/                      # UserRepositoryPort, PasswordEncoderPort,
    │   │                                 # TokenProviderPort
    │   └── usecase/                      # RegisterUserUseCase, LoginUserUseCase,
    │                                     # GetMeUseCase, ChangeRoleUseCase
    ├── domain/
    │   ├── exception/
    │   │   ├── business/                 # EmailAlreadyExists, InvalidCredentials,
    │   │   │                             # RoleNotAllowedForRegistration,
    │   │   │                             # UnauthorizedRoleChange, UserNotFound
    │   │   └── format/                   # UserNameInvalid, UserEmailInvalid,
    │   │                                 # UserPasswordInvalid
    │   ├── models/
    │   │   ├── UserDomain.java
    │   │   └── enums/UserRole.java
    │   └── rules/                        # UserValidatorRule, UserNameValidRule,
    │                                     # UserEmailValidRule, UserPasswordValidRule,
    │                                     # UserRoleValidRule
    └── infrastructure/
        ├── adapter/
        │   ├── in/controller/            # AuthController, UserController
        │   └── out/
        │       ├── persistence/
        │       │   ├── entity/UserEntity.java
        │       │   ├── mapper/UserEntityMapper.java
        │       │   ├── persistence/UserJpaAdapter.java
        │       │   └── repository/UserRepository.java
        │       └── security/             # Argon2PasswordEncoder, JwtTokenProvider,
        │                                 # AuthenticatedUser
        ├── config/SecurityConfig.java
        ├── handler/UserExceptionHandler.java
        └── security/JwtAuthenticationFilter.java
```

## 7. Componentes

### 7.1 Dominio

| Elemento | Responsabilidad |
|---|---|
| `UserDomain` | Entidad de aggregate root. Inmutable en identidad (`id`, `email`, `createdAt`); `name`, `role` y `updatedAt` son mutables mediante `changeRole()` / `changePassword()`. Se construye por factorías (`create`, `from`) y se valida con `UserValidatorRule`. |
| `UserRole` | Enum de roles. **Nombre Java en inglés, valor JSON en español** (ver §10.2). |
| `UserValidatorRule` | Regla compuesta: valida nombre, email, contraseña y rol. |
| `UserNameValidRule` / `UserEmailValidRule` / `UserPasswordValidRule` / `UserRoleValidRule` | Reglas unitarias de formato y coherencia. |
| Excepciones de negocio | `EmailAlreadyExistsException`, `InvalidCredentialsException`, `RoleNotAllowedForRegistrationException`, `UnauthorizedRoleChangeException`, `UserNotFoundException`. |
| Excepciones de formato | `UserNameInvalidException`, `UserEmailInvalidException`, `UserPasswordInvalidException`. |

Todas heredan de `TicketflowException` (excepción de negocio) o de `RequiredAttributeException`
(atributo obligatorio ausente). Se traducen a respuestas HTTP en `UserExceptionHandler` y
`GlobalExceptionHandler`.

### 7.2 Aplicación (casos de uso)

| Caso de uso | Puerto de entrada | Responsabilidad |
|---|---|---|
| `RegisterUserUseCase` | `RegisterUserPort` | Valida el rol permitido para registro, comprueba que el email no exista, hashea la contraseña y persiste el usuario. |
| `LoginUserUseCase` | `LoginUserPort` | Verifica credenciales contra el hash almacenado y emite el JWT. |
| `GetMeUseCase` | `GetMePort` | Devuelve el perfil del usuario autenticado. |
| `ChangeRoleUseCase` | `ChangeRolePort` | Cambia el rol de un usuario; exige que el actor sea `ADMIN` y no puede degradarse a sí mismo. |

**Puertos de salida** (contratos con la infraestructura):

| Puerto | Implementación | Responsabilidad |
|---|---|---|
| `UserRepositoryPort` | `UserJpaAdapter` | Persistencia de `UserDomain`. |
| `PasswordEncoderPort` | `Argon2PasswordEncoder` | Hash y verificación de contraseñas. |
| `TokenProviderPort` | `JwtTokenProvider` | Emisión del JWT. |

### 7.3 Infraestructura

| Componente | Responsabilidad |
|---|---|
| `AuthController` | `POST /register`, `POST /login`. |
| `UserController` | `GET /me`, `PATCH /users/{id}/rol`. Recibe el id y el rol del actor como atributos de request inyectados por el filtro JWT. |
| `JwtAuthenticationFilter` | Lee `Authorization: Bearer`, valida el token y pone `userId` y `role` como atributos de la request. |
| `SecurityConfig` | Cadena de filtros `STATELESS`, CORS y reglas de autorización (§10.1). |
| `JwtTokenProvider` | Emisión (y parseo) del JWT con jjwt. |
| `Argon2PasswordEncoder` | Hash Argon2id. |
| `UserEntity` / `UserEntityMapper` / `UserJpaAdapter` / `UserRepository` | Persistencia JPA. |
| `ApiResponse<T>` | Envelope de todas las respuestas. |
| `UserExceptionHandler` / `GlobalExceptionHandler` | Traducción de excepciones a códigos HTTP. |
| `OpenApiConfig` | Configuración de springdoc. |

## 8. Modelo de datos

### 8.1 Dominio (`UserDomain`)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `UUID` | Identificador. En la base de datos lo asigna Hibernate. |
| `name` | `String` | Obligatorio, longitud máxima 50. |
| `email` | `String` | Obligatorio, único, longitud máxima 255. |
| `password` | `String` | Obligatorio. Contiene el **hash**, nunca la contraseña en claro. |
| `role` | `UserRole` | Obligatorio. |
| `createdAt` | `LocalDateTime` | Solo lectura. |
| `updatedAt` | `LocalDateTime` | Se actualiza en `changeRole()` y `changePassword()`. |

### 8.2 Tabla `users` (PostgreSQL)

| Columna | Tipo | Restricciones |
|---|---|---|
| `id` | `uuid` | PK |
| `name` | `varchar(50)` | `NOT NULL` |
| `email` | `varchar(255)` | `NOT NULL`, **única** |
| `password` | `varchar` | `NOT NULL` (hash Argon2id) |
| `role` | `varchar(20)` | `NOT NULL`, `@Enumerated(EnumType.STRING)` |
| `created_at` | `timestamp` | `NOT NULL`, no actualizable |
| `updated_at` | `timestamp` | `NOT NULL` |

> **La base de datos se llama `user`** (palabra reservada en PostgreSQL; el entrypoint del
> contenedor la entrecomilla). El esquema **no está versionado**: se crea con
> `spring.jpa.hibernate.ddl-auto=update` (solo desarrollo). La especificación de plataforma exige
> Flyway con `V1__usuarios.sql`, `V2__refresh_tokens.sql` y `V3__outbox.sql` (ver §15).

## 9. Contrato HTTP

Detalle completo (peticiones, respuestas, errores, desviaciones) en
`docs_ia/contexto/contrato_api.md`.

### 9.1 Prefijo `/api/` (obligatorio)

> **Norma del proyecto:** todo endpoint de negocio debe vivir bajo el prefijo **`/api/`**.

| Método | Ruta objetivo | Rol | Ruta actual |
|---|---|---|---|
| POST | `/api/auth/register` | Público | `POST /register` ⚠️ |
| POST | `/api/auth/login` | Público | `POST /login` ⚠️ |
| POST | `/api/auth/refresh` | Público | ❌ No implementado |
| POST | `/api/auth/logout` | Autenticado | ❌ No implementado |
| GET | `/api/usuarios/me` | Autenticado | `GET /me` ⚠️ |
| PUT | `/api/usuarios/me` | Autenticado | ❌ No implementado |
| PUT | `/api/usuarios/me/password` | Autenticado | ❌ No implementado |
| POST | `/api/usuarios/organizador/solicitud` | COMPRADOR | ❌ No implementado |
| GET | `/api/usuarios/{id}` | ADMIN | ❌ No implementado |
| PATCH | `/api/usuarios/{id}/rol` | ADMIN | `PATCH /users/{id}/rol` ⚠️ |
| PATCH | `/api/usuarios/{id}/estado` | ADMIN | ❌ No implementado |

⚠️ = la functionality existe pero **sin el prefijo `/api/`**: es la desviación nº 1 (§15).
La migración obliga a cambiar **en el mismo despliegue** los controladores de este servicio, las
rutas y la lista de rutas públicas del `JwtAuthFilter` del gateway, y el BFF del frontend.

### 9.2 Envoltura de respuesta

Todas las respuestas usan `ApiResponse<T>`:

```json
{ "success": true, "message": "...", "data": { }, "errors": null, "timestamp": "..." }
```

La especificación de plataforma exige **RFC 9457 `application/problem+json`**; el envoltorio
actual es una desviación (§15).

### 9.3 Endpoints de infraestructura

| Ruta | Descripción |
|---|---|
| `/actuator/**` | Salud e información del servicio. |
| `/swagger-ui.html`, `/v3/api-docs/**` | Documentación OpenAPI (springdoc). |

Estos endpoints **no** llevan el prefijo `/api/`: son de infraestructura.

## 10. Autenticación y autorización

### 10.1 Cadena de seguridad

- **Sin estado:** `SessionCreationPolicy.STATELESS`; no hay sesión ni cookies de servidor.
- **CSRF** deshabilitado (no aplica a una API sin sesión con token en cabecera).
- **Filtro JWT** (`JwtAuthenticationFilter`) antes de `UsernamePasswordAuthenticationFilter`.
- **Rutas públicas:** `/register`, `/login`, `/v3/api-docs/**`, `/swagger-ui/**`,
  `/swagger-ui.html`, `/actuator/**` y cualquier `OPTIONS` (preflight).
- **Resto:** `authenticated()`. El punto de entrada de error responde `401` sin cuerpo.
- **CORS:** solo `http://localhost:8081`; methods `GET, POST, PUT, PATCH, DELETE, OPTIONS`;
  headers `Authorization, Content-Type`. El navegador nunca llama a este servicio directamente:
  entra por el BFF del frontend (server-side) o por el gateway.
- **Autorización por rol:** el controlador de cambio de rol lee el rol del actor desde los
  atributos de la request y `ChangeRoleUseCase` exige `ADMIN`.

### 10.2 Roles

| Enum Java | Valor JSON | Uso |
|---|---|---|
| `BUYER` | `comprador` | Rol por defecto al registrarse. |
| `ORGANIZER` | `organizador` | No se permite en el registro público. |
| `STAFF_CHECKIN` | `staff_checkin` | No se permite en el registro público. |
| `ADMIN` | `admin` | Único rol que puede cambiar roles. |

> El **nombre del enum es en inglés** (convención de código) y el **valor JSON es el de la
> especificación** (español). `UserRole` usa `@JsonValue` para serializar y `@JsonCreator` para
> deserializar (comparación sin distinguir mayúsculas). Cambiar uno obliga a cambiar el otro.

### 10.3 JWT

- **Algoritmo:** HS256. Clave simétrica compartida (`jwt.secret`), **≥ 32 bytes**.
- **Expiración:** `jwt.expiration-ms` = `86400000` (**24 h**).
- **Claims emitidos:** `sub` (uuid del usuario), `email`, `role` (valor JSON), `iat`, `exp`.
- **Sin `jti`**: no hay mecanismo de revocación (§15).
- **Emisión:** `JwtTokenProvider` (jjwt 0.13.0). El **gateway** valida la firma con la **misma
  clave compartida**; este servicio no comparte sesión con el gateway, solo secreto.
- El **secreto debe coincidir** con el del API Gateway: si difieren, el gateway rechaza todos los
  tokens.

## 11. Persistencia

- **Tecnología:** Spring Data JPA + Hibernate sobre PostgreSQL 18.
- **Esquema:** base de datos `user`, tabla `users` (única).
- **Migraciones:** **no hay Flyway**. El esquema se crea con `ddl-auto=update` (solo dev).
- **Mapeo:** `UserEntity` ↔ `UserDomain` mediante `UserEntityMapper` (MapStruct); la
  persistencia se aísla en `UserJpaAdapter`, que implementa `UserRepositoryPort`.
- **Consultas:** solo por id y por email (unicidad); no hay consultas de listado ni paginación.
- **Transacciones:** implícitas en los casos de uso que escriben (Spring `@Transactional` en la
  capa de aplicación).

## 12. Integraciones externas

| Sistema | Estado | Detalle |
|---|---|---|
| **PostgreSQL** | ✔ En uso | Almacén de usuarios. |
| **Eureka** | ✔ En uso | Se registra como `ticketflow-user-service`; configurable con `EUREKA_ENABLED=false` en pruebas. |
| **Redis** | ✖ No se usa | La especificación contempla `auth:fail:{email}` (bloqueo por intentos fallidos) y `auth:revoked:{jti}` (revocación). No implementado (§15). |
| **Kafka** | ✖ No se usa | La especificación contempla el topic `usuarios.registrados` (productor). No implementado: este servicio no publica eventos (§15). |
| **API Gateway** | ✔ Consume | Solo por contrato HTTP: el gateway reenvía las peticiones y valida el JWT. No hay llamadas de usuario → gateway. |

## 13. Configuración

| Clave | Valor por defecto | Descripción |
|---|---|---|
| `server.port` | `8081` (`${SERVER_PORT:8081}`) | Puerto HTTP. |
| `spring.application.name` | `ticketflow-user-service` | Nombre de registro en Eureka. |
| `spring.datasource.url` | `${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/user}` | JDBC. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Solo dev: crea/actualiza el esquema. |
| `jwt.secret` | — (variable de entorno) | Secreto HS256 compartido con el gateway. **≥ 32 bytes.** |
| `jwt.expiration-ms` | `86400000` | Validez del token (24 h). |
| `eureka.client.enabled` | `${EUREKA_ENABLED:true}` | Permite desactivar el registro en pruebas. |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Documentación. |

## 14. Observabilidad

- **Actuator** expuesto en `/actuator/**`: `health`, `info`, `metrics`.
- **OpenAPI** en `/swagger-ui.html` y `/v3/api-docs`.
- **Logs:** el acceso HTTP y los errores de negocio quedan en el log de la aplicación; no hay
  trazas distribuidas ni métricas de negocio propias (correlación `X-Request-Id` no implementada;
  la realiza el gateway en el destino).
- **Health check:** `/actuator/health` (la especificación pide `liveness` y `readiness`
  separados: no implementado).

## 15. Desviaciones respecto de la especificación de plataforma

| # | Tema | Especificación | Implementado | Impacto |
|---|---|---|---|---|
| 1 | **Prefijo de rutas** | `/api/auth/**`, `/api/usuarios/**` | `/register`, `/login`, `/me`, `/users/{id}/rol` | **Incumple la norma del proyecto** (§9.1). La migración exige cambiar a la vez este servicio, el gateway, el frontend y los `contrato_api.md`. |
| 2 | **Java** | **25 LTS** | **21** | Sin hilos virtuales: Argon2 bloquea hilos de plataforma (§5.1). |
| 3 | Spring Boot | 4.1.1 | 4.1.1 | ✔ Al día |
| 4 | Esquema de BD | `usuarios` + `refresh_tokens` + `outbox` + `eventos_procesados` | BD `user`, tabla `users` | Sin refresh tokens, outbox ni idempotencia de eventos. |
| 5 | Migraciones | Flyway | `ddl-auto=update` | El esquema no es reproducible ni versionado. |
| 6 | Redis | `auth:fail:*`, `auth:revoked:*` | No se usa | Sin bloqueo por intentos fallidos ni revocación de tokens. |
| 7 | Kafka | publica `usuarios.registrados` | No publica | Las reacciones a altas de usuario no son posibles. |
| 8 | Formato de error | RFC 9457 `problem+json` | Envoltura `ApiResponse` | Inconsistente con el resto de la plataforma; migrarlo afecta al frontend (requiere decisión). |
| 9 | Expiración y refresh | Access 15 min + refresh 7 días con rotación | 24 h, sin refresh, sin logout | Ventana de robo mayor; el logout es imposible. |
| 10 | Nombre en Eureka | `usuarios-service` | `ticketflow-user-service` | Cosmético, pero condiciona las rutas `lb://` del gateway. |
| 11 | Hash de contraseña | BCrypt coste 12 | **Argon2id** (2 iteraciones, 64 MiB, paralelismo 1) | Más resistente que BCrypt; requiere más memoria y CPU. El cambio de algoritmo invalida los hashes existentes: necesita estrategia de migración. |
| 12 | Health | `liveness` y `readiness` separados | `/actuator/health` único | El balanceador no distingue «vivo» de «no preparado». |

## 16. Reglas arquitectónicas

1. El dominio no importa nada de Spring, JPA, HTTP ni de librerías de infraestructura.
2. Los casos de uso no conocen la tecnología: solo usan puertos.
3. Toda regla de negocio vive en `domain/rules` y su excepción en `domain/exception`.
4. Todo endpoint nuevo **debe** usar el prefijo `/api/` (§9.1) y devolver `ApiResponse<T>`.
5. Ningún endpoint nuevo expone contraseñas ni hashes.
6. El rol viaja siempre como **valor JSON** (`comprador`, `organizador`, `staff_checkin`, `admin`).
7. Cambiar `jwt.secret` invalida todos los tokens emitidos: es una operación coordinada con el
   gateway.
8. Toda ruta nueva debe añadirse a la lista de rutas públicas del `SecurityConfig` **solo** si es
   realmente pública.

## 17. Estado de implementación (2026-10)

| Capacidad | Estado |
|---|---|
| Registro de usuarios | ✔ Implementado (`POST /register`) |
| Inicio de sesión y emisión de JWT | ✔ Implementado (`POST /login`) |
| Perfil del usuario autenticado | ✔ Implementado (`GET /me`) |
| Cambio de rol por administrador | ✔ Implementado (`PATCH /users/{id}/rol`) |
| Refresh token / logout / revocación | ✖ No implementado |
| Bloqueo por intentos fallidos (Redis) | ✖ No implementado |
| Publicación de `usuarios.registrados` (Kafka) | ✖ No implementado |
| Listado y consulta de usuarios por id | ✖ No implementado |
| Solicitud de rango de organizador | ✖ No implementado |
| Migración a Java 25 | ✖ Pendiente |
| Prefijo `/api/` en las rutas | ✖ Pendiente |
| Migraciones Flyway | ✖ Pendiente |

**Topología:** puerto **8081**; nombre en Eureka **`ticketflow-user-service`**; base de datos
PostgreSQL **`user`**.

## 18. Cómo mantener este documento

1. Cambiar un endpoint (ruta, body, rol, código) obliga a actualizar §9 y el
   `docs_ia/contexto/contrato_api.md` en el mismo cambio.
2. Cambiar una regla de negocio obliga a actualizar §7 y las pruebas de dominio.
3. Cambiar el stack (versión, dependencia) obliga a actualizar §5 y §15.
4. Cerrar una desviación obliga a mover la fila correspondiente de §15 a §17.
5. Toda decisión arquitectónica nueva se registra en `docs_ia/decisiones/` (`DA-xxx`).
