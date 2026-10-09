# AGENTS.md — ticketflow-user-service

Usuarios y autenticación de TicketFlow: registro, login, `/me` y cambio de rol.

## Servicio

| Campo | Valor |
|---|---|
| Rol | Usuarios / auth (register, login, /me, role change) |
| Eureka name | `ticketflow-user-service` |
| Puerto | 8081 |
| Depende de | PostgreSQL (`TicketFlow`), Eureka |
| Stack | Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3 |

## Comandos

```powershell
.\mvnw.cmd test                                  # H2 en memoria; no requiere PostgreSQL
.\mvnw.cmd -Dtest=RegisterUserUseCaseTest test   # un test concreto
.\mvnw.cmd spring-boot:run
```

## Gotchas

- Los tests usan **H2 en memoria** (`src/test/resources/application.properties`) — no necesitas PostgreSQL.
- `spring.jpa.hibernate.ddl-auto=update` (solo dev; Flyway está planeado, no existe).
- **MapStruct 1.5.5 + Lombok** se configuran vía `annotationProcessorPaths` en `pom.xml`; **no lo elimines** (el codegen lo necesita).
- JWT: este servicio **emite** los tokens (el gateway valida). `JWT_SECRET` debe coincidir con el gateway y ser ≥32 bytes (HS256). jjwt `0.13.0` (el gateway usa `0.12.6`).
- El `README.md` muestra el puerto `:8080`, pero el servicio escucha en `:8081` (dato obsoleto).
- `docs/guia-microservicio-clean-code.md` es la guía de patrón **autoritativa** para servicios nuevos.

## Convenciones

- Hexagonal + Clean + DDD. Dos paquetes bajo `com.ticketflow.user`: `common/` (transversal) y `user/` (bounded context). Regla de dependencia: `infrastructure → application → domain`.
- Código, identificadores y comentarios **en inglés**; documentación en español.

## Gobernanza (docs_ia)

- `docs_ia/contexto/` es la **única fuente autorizada** de requisitos, arquitectura, calidad y pruebas.
- Decisiones arquitectónicas: proponer en `docs_ia/decisiones/DA-xxx-*.md` (estado `PROPUESTA`) y esperar aprobación humana.
- Trazabilidad: registrar interacciones relevantes en `docs_ia/prompts/P-xxx-*.md`.
- No agregar/quitar/actualizar dependencias ni modificar archivos (salvo trazabilidad/propuestas) sin autorización humana explícita.

## Grafo (codebase-memory)

- Proyecto: `C-Users-Juan-Diego-Duque-Documents-Programacion-ticketflow-ticketflow-user-service`.
- Este repo es git → `detect_changes --base-branch main` funciona.
- Arquitectura entre servicios → grafo padre `C-Users-Juan-Diego-Duque-Documents-Programacion-ticketflow`.
- **Cambiar de rama NO refresca este grafo automáticamente**: tras `git checkout`, fuerza re-index con `index_repository --repo-path . --mode full`.

## No tocar

- `.mvn/`, `.idea/`, `target/` = build/IDE output.
