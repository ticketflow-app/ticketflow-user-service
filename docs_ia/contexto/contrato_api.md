# Contrato de API — ticketflow-user-service

## 1. Propósito y estado

Define el contrato HTTP de **Usuarios / Autenticación** (registro, login, perfil y cambio de rol) tal como está implementado hoy, y lo contrasta con el contrato de la especificación de la plataforma.

- **Fuentes de verdad del contrato:**
  - Especificación de plataforma: `docs/Microservicios.docx` (sección 4 «usuarios-service») y `docs/Arquitectura.docx` (sección 8.1 «Usuarios»), copiadas en el workspace raíz.
  - Implementación real: código de este repositorio (los controladores son la fuente primaria).
- **Alcance:** endpoints propios del servicio. El enrutamiento desde el gateway se documenta en `ticketflow-api-gateway/docs_ia/contexto/contrato_api.md`.
- **Estado:** vigente. Las desviaciones respecto de la especificación están listadas en la sección 7 y son **conocidas y aceptadas** hasta que se decida lo contrario.

> Cuando cambie un endpoint, actualiza este documento en el mismo cambio. No sobrescribas decisiones: si una desviación se corrige, documenta aquí el nuevo estado.

## 2. Identidad del servicio

| Campo | Implementado | Contrato (spec) | Nota |
|---|---|---|---|
| Nombre en Eureka | `ticketflow-user-service` | `usuarios-service` | Nombre de registro ≠ nombre de la spec. El gateway lo resuelve por `lb://ticketflow-user-service`. |
| Puerto | **8081** | 8081 | Coincide. |
| Prefijo de rutas | `/` (raíz) — **pendiente de migrar** | **`/api/` (obligatorio)** | Ver sección 5. |
| Base de datos | PostgreSQL 18, BD `user` | PostgreSQL 18, esquema `usuarios` | Nombre de BD/esquema distinto. |
| Stack | Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3, MVC | Java 25 LTS, Spring Boot 4.1.1, Spring Cloud 2025.1.3 | Ver sección 3. |
| Migraciones | `spring.jpa.hibernate.ddl-auto=update` (sin Flyway) | Flyway (`V1__usuarios.sql`, `V2__refresh_tokens.sql`, `V3__outbox.sql`) | **Desviación** (solo dev). |
| Variable de entorno | `SPRING_DATASOURCE_URL`, `JWT_SECRET`, `EUREKA_ENABLED`, `SERVER_PORT` | `DB_URL`, `JWT_SECRET`, `EUREKA_URL` | Nombres de variables distintos a los de la spec. |

## 3. Estándar de versiones

Versiones **obligatorias y uniformes para todo el proyecto**. Solo se listan aquí las tecnologías
que **este servicio usa**; las de los demás repos están en sus respectivos `contrato_api.md`.

| Tecnología | Versión estándar | Versión actual en el repo | Estado |
|---|---|---|---|
| Java | **25 (LTS)** | 21 | ⚠️ Pendiente de migración |
| Spring Boot | **4.1.1** | 4.1.1 | ✔ Al día |
| Spring Cloud | **2025.1.3** | 2025.1.3 | ✔ Al día |
| PostgreSQL | **18.x** | 18 (`postgres:18-alpine`) | ✔ Al día |
| jjwt | **0.13.0** | 0.13.0 | ✔ Al día |
| springdoc-openapi | **3.0.x** | 3.x (BOM de Boot) | ✔ Al día |

> Este servicio **no** usa Redis, Kafka ni Resilience4j: no aparecen en su stack. Kafka lo usa
> `ticketflow-inventory-service`; Redis, `ticketflow-api-gateway` y `ticketflow-inventory-service`.

## 4. Convenciones de API

| Concepto | Implementado | Contrato (spec) |
|---|---|---|
| Autenticación | `Authorization: Bearer <JWT>` (HS256, secreto compartido con el gateway) | Igual, con validación de `jti` contra `auth:revoked:*` |
| **Prefijo de rutas** | **sin prefijo** — **no cumple** | **`/api/` obligatorio** (ver sección 5) |
| Rutas públicas | `/register`, `/login` (y `/actuator*`) | `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh` |
| Roles | `BUYER`→`comprador`, `ORGANIZER`→`organizador`, `STAFF_CHECKIN`→`staff_checkin`, `ADMIN`→`admin` (enum Java en inglés, **valor JSON en español**, igual que la spec) | `COMPRADOR`, `ORGANIZADOR`, `STAFF_CHECKIN`, `ADMIN` |
| Formato de error | Envoltura `{success, message, data, errors, timestamp}` (`ApiResponse<T>`) | RFC 9457 `application/problem+json` |
| Expiración del token | `jwt.expiration-ms` = 86400000 (24 h), sin refresh token | Access 15 min + refresh 7 días con rotación |
| Paginación | no implementada | `?page=0&size=20&sort=...` (máx. 100) |
| Idempotencia | no aplica (no hay escrituras idempotentes) | `Idempotency-Key` en operaciones sensibles |
| Fechas e IDs | `LocalDateTime` en las respuestas, UUID para usuarios | ISO-8601 UTC, UUID v7 |
| Versionado | no implementado | prefijos `/api/v2` para cambios incompatibles |
| Documentación | `/swagger-ui.html`, `/v3/api-docs` | Igual |
| Health | `/actuator/health` (con detalle) | `/actuator/health/liveness` y `/readiness` |

## 5. Prefijo de rutas `/api/` (obligatorio)

> **Norma del proyecto:** todo endpoint de negocio expuesto por este servicio **debe** vivir bajo
> el prefijo **`/api/`**. Solo se exceptúan los endpoints de infraestructura (`/actuator/**`,
> `/v3/api-docs`, `/swagger-ui.html`) y los que el framework provee.

### 5.1 Rutas objetivo (con prefijo `/api/`)

| Método | Ruta objetivo | Rol | Estado en el código |
|---|---|---|---|
| POST | `/api/auth/register` | Público | ⚠️ Hoy es `POST /register` |
| POST | `/api/auth/login` | Público | ⚠️ Hoy es `POST /login` |
| POST | `/api/auth/refresh` | Público | ❌ No implementado |
| POST | `/api/auth/logout` | Autenticado | ❌ No implementado |
| GET | `/api/usuarios/me` | Autenticado | ⚠️ Hoy es `GET /me` |
| PUT | `/api/usuarios/me` | Autenticado | ❌ No implementado |
| PUT | `/api/usuarios/me/password` | Autenticado | ❌ No implementado |
| POST | `/api/usuarios/organizador/solicitud` | COMPRADOR | ❌ No implementado |
| GET | `/api/usuarios/{id}` | ADMIN | ❌ No implementado |
| PATCH | `/api/usuarios/{id}/rol` | ADMIN | ⚠️ Hoy es `PATCH /users/{id}/rol` |
| PATCH | `/api/usuarios/{id}/estado` | ADMIN | ❌ No implementado |

### 5.2 Endpoints implementados (rutas actuales)

| Método | Ruta | Rol | Descripción | Respuestas |
|---|---|---|---|---|
| POST | `/register` | Público | Crea la cuenta. Body: `{name, email, password, role?}` (`role` es opcional; por defecto `comprador`). | 201 con `ApiResponse<GetUserResponseDTO>`; 400/409 por reglas de negocio |
| POST | `/login` | Público | Autentica. Body: `{email, password}`. Devuelve el JWT. | 200 con `ApiResponse<LoginResponseDTO>`; 401 |
| GET | `/me` | Autenticado | Perfil del usuario del token (id, name, email, role, createdAt). | 200 con `ApiResponse<GetUserResponseDTO>` |
| PATCH | `/users/{id}/rol` | ADMIN | Cambia el rol de un usuario. Body: `{role}`. | 200 con `ApiResponse<Void>`; 401/403/404 |

### 5.3 Impacto de la migración al prefijo `/api/`

Al migrar hay que cambiar **en el mismo despliegue**:

1. Las rutas de los controladores de este servicio (`@PostMapping`, `@GetMapping`, `@PatchMapping`).
2. Las rutas del gateway (`ticketflow-api-gateway/src/main/resources/application.yml`): los `Path`
   pasan a `/api/auth/register,/api/auth/login,/api/usuarios/**` y `/api/usuarios/{id}/rol`.
3. La lista de rutas públicas del `JwtAuthFilter` del gateway.
4. El BFF del frontend (`ticketflow-frontend/src/app/api/gateway/[...path]/route.ts`) y sus rutas.
5. Este documento, el del gateway y el del frontend.

> **No hay versión de compatibilidad**: se migra de golpe (cambio incompatible). Si fuera necesario
> mantener las rutas viejas durante una transición, deben convivir con el prefijo `/api/` y
> marcarse como obsoletas en un `DA-xxx`; nunca sustituir `/api/**` por rutas sin prefijo.

Detalle de las formas de datos:

```jsonc
// GET /me -> data
{
  "id": "uuid",
  "name": "Ada",
  "email": "ada@uni.edu.co",
  "role": "comprador",
  "createdAt": "2026-10-01T10:00:00"
}

// POST /login -> data
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": "uuid",
  "name": "Ada",
  "email": "ada@uni.edu.co",
  "role": "comprador"
}
```

## 6. Endpoints del contrato pendientes

Ordenados según la sección 4 de `docs/Microservicios.docx`. El detalle completo (rutas objetivo)
está en la sección 5.1.

| Método | Ruta de la spec | Rol | Estado |
|---|---|---|---|
| POST | `/api/auth/refresh` | Público | No implementado (no hay refresh tokens) |
| POST | `/api/auth/logout` | Autenticado | No implementado |
| GET | `/api/usuarios/me` | Autenticado | Existe como `GET /me` (sin prefijo): falta migrar la ruta |
| PUT | `/api/usuarios/me` | Autenticado | No implementado (actualizar nombre/teléfono) |
| PUT | `/api/usuarios/me/password` | Autenticado | No implementado |
| POST | `/api/usuarios/organizador/solicitud` | COMPRADOR | No implementado |
| GET | `/api/usuarios/{id}` | ADMIN | No implementado |
| PATCH | `/api/usuarios/{id}/estado` | ADMIN | No implementado (activar/suspender) |

## 7. Desviaciones respecto de la especificación

| # | Tema | Especificación | Implementado | Impacto |
|---|---|---|---|---|
| 1 | **Prefijo de rutas** | `/api/auth/**`, `/api/usuarios/**` | `/register`, `/login`, `/me`, `/users/{id}/rol` | **Incumple la norma del proyecto** (sección 5). El gateway enruta por rutas exactas: hay que migrar gateway, frontend y los `contrato_api.md` a la vez. |
| 2 | Puerto | 8081 | 8081 | — |
| 3 | **Java** | **25 LTS** | **21** | Sin hilos virtuales; el BCrypt bloquea hilos de plataforma. |
| 4 | Spring Boot | 4.1.1 | 4.1.1 | ✔ Al día |
| 5 | Esquema de BD | `usuarios` + `refresh_tokens` + `outbox` + `eventos_procesados` | BD `user`, tablas generadas por Hibernate (`users`) | Sin refresh tokens, outbox ni idempotencia de eventos. |
| 6 | Migraciones | Flyway | `ddl-auto=update` | El esquema no es reproducible ni versionado. |
| 7 | Redis | `auth:fail:{email}`, `auth:revoked:{jti}` | No se usa | Sin bloqueo por intentos fallidos ni revocación de tokens. |
| 8 | Kafka | publica `usuarios.registrados` | No publica | Notificaciones no puede reaccionar a altas. |
| 9 | Formato de error | RFC 9457 | Envoltura `ApiResponse` | Inconsistente con el resto de servicios; migrar a `problem+json` requiere decisión (afecta al frontend). |
| 10 | Expiración / refresh | 15 min + refresh 7 días | 24 h, sin refresh | Ventana de robo mayor; logout imposible. |
| 11 | Nombre en Eureka | `usuarios-service` | `ticketflow-user-service` | Cosmético, pero afecta las rutas `lb://` del gateway. |
| 12 | Contraseña | BCrypt costo 12 | Hash con `PasswordEncoder` | Verificar coste y algoritmo en la implementación de `PasswordHasherPort`. |

## 8. Cómo mantener este documento

1. Cambiar un endpoint (ruta, body, código o rol) implica actualizar las secciones 5.1 y 5.2 en el mismo commit.
2. Cerrar una desviación implica mover la fila correspondiente a «implementado» y registrar la decisión en `docs_ia/decisiones/`.
3. La especificación de la plataforma vive fuera de este repositorio (`docs/Microservicios.docx` en la raíz del workspace); aquí solo se refleja su efecto en este servicio.
4. Toda ruta nueva **debe** incluir el prefijo `/api/` (sección 5).
