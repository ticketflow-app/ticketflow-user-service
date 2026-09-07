# ticketflow-user-service

Microservicio de **Usuarios** de TicketFlow. Base de todo el sistema — sin esto, ni Catálogo ni Inventario saben quién está pidiendo qué.

## Stack

- **Java 21** + **Spring Boot 4.1.1** (Spring Web MVC, Security, Data JPA, Actuator)
- **PostgreSQL 18** (base de datos)
- **Argon2id** (hashing de contraseñas)
- **JWT (jjwt / HMAC-SHA)** — autenticación estateless
- **OpenAPI / Swagger UI** — documentación de API
- Arquitectura **Hexagonal + Clean Architecture + DDD** (mismo patrón que `trackinvest-account-service`)

## Estructura

```
src/main/java/com/ticketflow/user/
├── common/        # Cross-cutting: ApiResponse, excepción base, DomainRule, OpenAPI, handler global
└── user/          # Bounded Context "Usuario"
    ├── domain/          # Java puro: UserDomain, roles, reglas de validación, excepciones
    ├── application/     # Puertos (in/out) + casos de uso
    └── infrastructure/  # Controllers, JPA, JWT, Argon2, SecurityConfig
```

**Regla de dependencia:** `infrastructure → application → domain → (Java puro)`.

## Modelo de datos

```
User(id UUID, name, email, password[hash Argon2], rol, created_in, updated_at)
```

Rol: `comprador` | `organizador` | `staff_checkin` | `admin` (solo asignable por un admin).

## Endpoints

| Método | Ruta             | Descripción                                 | Auth |
|--------|------------------|---------------------------------------------|------|
| POST   | `/register`      | Crea usuario, hashea la contraseña (Argon2) | —    |
| POST   | `/login`         | Valida credenciales, emite un JWT           | —    |
| GET    | `/me`            | Datos del usuario autenticado (desde JWT)   | Bearer |
| PATCH  | `/users/{id}/rol`| Cambia rol — **solo admin**                 | Bearer + rol admin |

## Decisiones de diseño clave

- **JWT en vez de sesiones**: el token lleva `userId`, `email` y `rol` embebidos y firmados con un secreto compartido (`jwt.secret`). Los demás microservicios lo validan localmente, sin llamar a Usuarios en cada request. Así Usuarios no se convierte en el cuello de botella / punto único de falla del sistema.
- **Argon2id** (no BCrypt) para resistir ataques GPU por fuerza bruta.
- **Reglas de negocio en el dominio** (no en anotaciones de Jakarta): validación de formato, roles permitidos en registro, "solo admin cambia roles".

## Cómo correr

### Local (requiere PostgreSQL)

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

### Todo en Docker

```bash
docker compose up -d --build
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator health: http://localhost:8080/actuator/health

### Tests

```bash
./mvnw test
```

> Usan H2 en memoria; no requieren PostgreSQL instalado.

## Documentación

- **Guía paso a paso** (crear un microservicio desde cero, clase por clase): [`docs/guia-microservicio-clean-code.md`](docs/guia-microservicio-clean-code.md)

## Próximos pasos

- Eventos de dominio (`UserCreated` → Kafka) con Outbox pattern, replicando `trackinvest-account-service`.
- Migraciones con Flyway (`ddl-auto=update` es solo para desarrollo).
- Kubernetes manifests + CI (GitHub Actions + SonarCloud) como en trackinvest.