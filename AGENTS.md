# AGENTS.md — ticketflow-user-service

Usuarios y autenticación de TicketFlow (registro, login, `/me`, cambio de rol). Es el que **emite el JWT** que valida el gateway.

> Este archivo orienta; **la fuente de verdad del proyecto es `docs_ia/contexto`**. El diseño, los requisitos y la arquitectura no se describen aquí.

## Estado

| Campo | Valor |
|---|---|
| Rol | Usuarios / autenticación |
| Eureka name | `ticketflow-user-service` (en la especificación de plataforma: `usuarios-service`) |
| Puerto | 8081 (`server.port=${SERVER_PORT:8081}`) — coincide con la especificación |
| Depende de | PostgreSQL (`user`), Eureka |
| Stack (estándar del proyecto) | **Java 25 LTS**, **Spring Boot 4.1.1**, **Spring Cloud 2025.1.3**, **jjwt 0.13.0**, **PostgreSQL 18.x** — hoy en **Java 21** ⚠️ |
| Prefijo de rutas | **`/api/` obligatorio** (norma del proyecto); hoy las rutas están en la raíz ⚠️ |
| Contrato HTTP | `docs_ia/contexto/contrato_api.md` |

## Contrato y endpoints

Detalle completo (peticiones, respuestas, desviaciones): `docs_ia/contexto/contrato_api.md`.

| Método | Ruta | Rol | Respuesta |
|---|---|---|---|
| POST | `/register` | Público | 201 · `ApiResponse<GetUserResponseDTO>` |
| POST | `/login` | Público | 200 · `ApiResponse<LoginResponseDTO>` (JWT HS256) |
| GET | `/me` | Autenticado | 200 · `ApiResponse<GetUserResponseDTO>` |
| PATCH | `/users/{id}/rol` | ADMIN | 200 · `ApiResponse<Void>` |

Desviaciones más relevantes frente a la especificación de plataforma (`docs/Microservicios.docx`,
sección 4):

- **Sin prefijo `/api`**: la norma del proyecto exige `/api/auth/**` y `/api/usuarios/**`; hoy las rutas están en la raíz. Tabla de rutas objetivo y correspondencia: `docs_ia/contexto/contrato_api.md` §5.
- **Sin Redis ni Kafka**: no hay `auth:fail:*` / `auth:revoked:*` ni publicación de `usuarios.registrados`.
- **Sin Flyway**: el esquema se crea con `ddl-auto=update`; la spec exige `V1__usuarios.sql`, `V2__refresh_tokens.sql`, `V3__outbox.sql`.
- **Sin refresh token ni logout**: el token expira a las 24 h (`jwt.expiration-ms=86400000`), la spec pide 15 min + refresh de 7 días.
- **Error con envoltorio `ApiResponse`** en lugar de RFC 9457 `problem+json`.
- **Java 21** (estándar del proyecto: **25 LTS**); Spring Boot 4.1.1 y jjwt 0.13.0 ya están al día.
- Los **valores JSON de los roles sí coinciden** con la spec (`comprador`, `organizador`, `staff_checkin`, `admin`); el enum Java usa nombres en inglés (`BUYER`, `ORGANIZER`, `STAFF_CHECKIN`, `ADMIN`).
- El **hash es Argon2id**, no BCrypt coste 12 como dice la spec (decisión del código, no documentada en una `DA`).

## Docs

- `docs_ia/contexto/contexto_negocio.md` — contexto de negocio.
- `docs_ia/contexto/contrato_api.md` — **contrato HTTP vigente**: endpoints, auth, versiones, desviaciones frente a la especificación de plataforma.
- `docs_ia/contexto/arquitectura/` — `arquitectura.md` (arquitectura **de este servicio**: dominio, aplicación, persistencia, JWT, endpoints y desviaciones), `desiciones_arquitectonicas.md`.
- `docs_ia/contexto/requisitos/` — `casos_uso.md`, `epicas_feactures.md`, `historias_usuarios.md`.
- `docs_ia/contexto/calidad/` — `atributos_calidad.md`, `escenarios_calidad.md`.
- `docs_ia/contexto/pruebas/` — `estrategia_pruebas.md`, `pruebas_cargas.md`.
- `docs_ia/decisiones/` — decisiones arquitectónicas (`DA-xxx`).
- `docs_ia/prompts/` — trazabilidad de uso de IA (`P-xxx`).
- `docs/guia-microservicio-clean-code.md` — guía paso a paso del patrón de microservicio (autoritativa para estructura de paquetes).

## Comandos

```powershell
.\mvnw.cmd test                              # usa H2 en memoria; no requiere PostgreSQL
.\mvnw.cmd -Dtest=RegisterUserUseCaseTest test
.\mvnw.cmd spring-boot:run
```

## Gobernanza (para OpenCode)

> Adaptación para **OpenCode** de las reglas de `.clinerules` (ver el archivo completo en los repos que lo tienen: `ticketflow-inventory-service`, `ticketflow-frontend`). Aplica a todo el trabajo en este repo; su fuente de verdad es `docs_ia/contexto`.

### 1. Fuente de contexto

- `docs_ia/contexto` es la **única fuente autorizada** para: requisitos, atributos de calidad, arquitectura, diseño, estrategia de pruebas y restricciones del proyecto.
- No asumir información que no esté definida en `docs_ia/contexto`.
- Antes de una tarea relevante, revisar los archivos de `docs_ia/contexto` necesarios para esa tarea.
- Si falta información, distinguir entre el hecho conocido, la inferencia y la propuesta; si la tarea entra en conflicto con el contexto, detener el cambio afectado y pedir aclaración.

### 2. Decisiones arquitectónicas

- No tomar decisiones arquitectónicas importantes de forma autónoma.
- Cuando haga falta decidir, analizar alternativas y proponer una recomendación.
- Crear la propuesta en `docs_ia/decisiones/` en Markdown, usando el siguiente identificador `DA-xxx` disponible.
- Esperar aprobación humana antes de implementar la decisión propuesta.
- Una decisión aprobada queda documentada en `docs_ia/decisiones/`.
- No sobrescribir decisiones anteriores; si una decisión cambia, crear una nueva que explique el cambio.
- Cada decisión debe incluir: contexto, problema, alternativas consideradas, recomendación, ventajas, desventajas, impacto y estado.
- Estado `PROPUESTA` hasta recibir aprobación humana explícita; solo entonces `APROBADA` y aplicable.

### 3. Código

- Puede leer y modificar el código cuando la tarea haya sido aprobada.
- No hacer cambios importantes basados en suposiciones.
- Antes de cambios arquitectónicos, explicar brevemente qué se modificará y por qué.
- Mantener coherencia con las decisiones aprobadas.
- Nomenclatura **en inglés**: variables, métodos, clases, interfaces, paquetes, endpoints, DTOs, entidades, tablas, columnas y archivos de código.
- Comentarios y documentación técnica dentro del código, **en inglés**.
- No usar nombres en español en el código, salvo nombres externos obligados por una API, librería o requisito existente.
- Nomenclatura consistente y descriptiva según el lenguaje y framework.

### 4. Documentación

- Toda decisión arquitectónica o técnica importante se documenta en Markdown.
- Identificadores consecutivos: `DA-001`, `DA-002`, `DA-003`, ...
- No sobrescribir decisiones anteriores; si una decisión cambia, crear una nueva que explique el cambio.
- Cada decisión debe incluir: contexto, problema, alternativas consideradas, recomendación, ventajas, desventajas, impacto y estado.
- Estado `PROPUESTA` hasta recibir aprobación humana explícita; solo entonces `APROBADA` y aplicable.
- Documentación de proyecto en **español**; código y comentarios técnicos en **inglés**.

### 5. Pruebas

- No modificar la estrategia de pruebas definida en `docs_ia/contexto` sin proponerlo antes.
- Cuando corresponda, crear o modificar pruebas junto con el código.
- No introducir nuevas herramientas o frameworks de pruebas sin analizar antes su necesidad y compatibilidad.
- Las pruebas deben verificar comportamientos concretos, no limitarse a confirmar que las clases existen.

### 6. Gestión de dependencias

- Antes de agregar, eliminar, actualizar o reemplazar una dependencia, revisar los archivos relevantes de `docs_ia/contexto`.
- Revisar siempre el manifiesto de dependencias actual (`pom.xml`) antes de proponer cambios.
- Clasificar las dependencias como: indispensables, recomendadas, opcionales o no justificadas actualmente.
- Para cada dependencia propuesta indicar: propósito, funcionalidad, relación con la arquitectura, requisito que satisface, alternativas, impacto técnico y compatibilidad con las versiones actuales.
- Verificar compatibilidad con: versión de Java, versión de Spring Boot, versión de Maven y arquitectura definida.
- No agregar dependencias por conveniencia, costumbre o sugerencia automática de una herramienta.
- Principio de mínima dependencia: solo las justificadas por requisitos y arquitectura aprobada.
- **No agregar, eliminar, actualizar ni modificar dependencias sin autorización humana explícita.**
- Antes de modificar el manifiesto, presentar análisis y propuesta; tras la autorización, aplicar solo lo aprobado.
- Toda modificación importante de dependencias se documenta en `docs_ia/decisiones`.

### 7. Uso de herramientas

- Puede leer archivos; crear y modificar cuando la tarea esté aprobada; ejecutar comandos necesarios para la tarea aprobada.
- No ejecutar comandos que modifiquen el proyecto sin autorización cuando los cambios no estén aprobados.
- Pedir aprobación antes de comandos destructivos o peligrosos y antes de instalar paquetes, dependencias, herramientas o software.
- No eliminar archivos, dependencias, configuraciones o código sin autorización explícita.
- Si una herramienta propone cambios automáticos, analizar primero y pedir autorización antes de aplicarlos.

### 8. Autorización de cambios

- Toda tarea que modifique código, configuración, arquitectura, dependencias o infraestructura requiere autorización humana si no estaba previamente aprobada.
- Analizar primero, modificar después.
- Una autorización específica no autoriza cambios adicionales no relacionados.
- No interpretar expresiones ambiguas como autorización para cambios importantes.
- Si la tarea requiere una decisión no definida en `docs_ia/contexto`, detener la implementación y presentar la decisión.
- El permiso de lectura no implica permiso de escritura: cada cambio debe caer dentro del alcance autorizado.

### 9. Comunicación

- Explicar las decisiones de forma breve y técnica.
- Si falta información en `docs_ia/contexto`, indicarlo explícitamente.
- Diferenciar siempre entre: información definida, suposición, propuesta y decisión aprobada.
- Presentar las alternativas técnicamente válidas antes de elegir una.
- No presentar una propuesta como si fuera una decisión aprobada.
- Informar qué archivos se modificarán antes de cambios importantes.
- Tras cambios autorizados, informar: archivos modificados, cambios realizados, comandos ejecutados, pruebas realizadas, resultado y pendientes.

### 10. Trazabilidad del uso de IA

- Toda interacción relevante debe quedar registrada, incluso si es solo análisis sin cambios de código.
- Se registra desde que se ejecuta el prompt y no requiere autorización.
- Ubicación: `docs_ia/prompts`, un archivo Markdown independiente por interacción, con identificadores consecutivos `P-001`, `P-002`, ...
- Contenido mínimo: identificador, fecha, objetivo, prompt completo, archivos de contexto consultados, archivos analizados, resumen de la respuesta, propuestas, decisiones pendientes y estado.
- Estados permitidos: `ANALISIS` | `PROPUESTA` | `APROBADO` | `RECHAZADO` | `IMPLEMENTADO`.
- Si hay propuesta arquitectónica o técnica, crear también el documento correspondiente en `docs_ia/decisiones`.
- El registro de trazabilidad y la propuesta documental **no** autorizan a modificar código, configuración, dependencias o infraestructura.
- Ningún archivo (salvo trazabilidad y propuestas) se modifica sin autorización humana explícita.
- Si solo se pide análisis, ejecutarlo y documentarlo sin tocar código.
- Si luego se autoriza, actualizar el registro con autorización, cambios, archivos, comandos, pruebas, resultado y estado final.
- Nunca sobrescribir registros históricos; si algo cambia, crear un nuevo registro relacionado.
- La trazabilidad debe permitir reconstruir: Contexto → Prompt → Respuesta IA → Propuesta → Autorización humana → Implementación → Pruebas → Resultado.

## Grafo (codebase-memory)

- Proyecto de grafo: `C-Users-Juan-Diego-Duque-Documents-Programacion-ticketflow-ticketflow-user-service`.
- Este repo es git → `detect_changes --base-branch main` funciona contra este proyecto.
- Arquitectura entre servicios → grafo padre `C-Users-Juan-Diego-Duque-Documents-Programacion-ticketflow` (foto del estado actual de los 5 checkouts).
- **Cambiar de rama NO refresca este grafo automáticamente** (el watcher sigue la raíz, que no es git). Tras `git checkout` o al editar, fuerza re-index:
  ```powershell
  powershell -File ..\sync-graph.ps1 -Service ticketflow-user-service   # desde la raíz
  ```
  o, desde esta carpeta: `index_repository --repo-path . --mode full`.

## Gotchas

- **Estructura de paquetes** (guía en `docs/guia-microservicio-clean-code.md`): `com.ticketflow.user` con dos contextos de nivel superior, `common/` (transversal: `application/dto`, `domain/{exception,rules}`, `infrastructure/{config,handler}`) y `user/` (contexto acotado: `application/{ports/{in/{dto,service},out},usecase}`, `domain/{exception/{business,format},models/enums,rules}`, `infrastructure/{adapter/{in/controller,out/persistence/{entity,mapper,persistence,repository}},config,handler,security}`).
- **No hay Flyway**: el esquema se crea con `spring.jpa.hibernate.ddl-auto=update` (solo dev). No copies el patrón de Flyway de `inventory-service` sin una decisión aprobada.
- **La BD se llama `user`** (no `TicketFlow`): `spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/user}`. El hostname debe coincidir con el nombre del servicio postgres; si no, `UnknownHostException` y Hibernate aborta con *"Unable to determine Dialect without JDBC metadata"*.
- `user` es palabra reservada en PostgreSQL: funciona porque el entrypoint del contenedor entrecomilla el nombre.
- **Divergencia de compose**: el `docker-compose.yml` de este repo aún usa el servicio `postgres` con BD `TicketFlow`, mientras el compose del gateway usa `postgres-user` con BD `user`. Están desalineados: no los tomes como fuente de verdad del otro.
- **Tests con H2 en memoria** (`src/test/resources/application.properties`): no requieren PostgreSQL. Cuidado con dialectos y tipos que no existan en H2.
- **MapStruct 1.5.5 + Lombok** se procesan vía `annotationProcessorPaths` en `maven-compiler-plugin`: al editar el `pom.xml` **preserva ese bloque**, si no se rompe la generación de código.
- **Emite el JWT**: `jwt.secret` debe coincidir con el del gateway y tener ≥32 bytes (HS256); expiración por `jwt.expiration-ms` (86400000 = 24 h). jjwt `0.13.0` aquí vs `0.12.6` en el gateway.
- **El hash de contraseñas es Argon2id** (`de.mkammerer.argon2`: 2 iteraciones, 64 MiB, paralelismo 1), no BCrypt: es más resistente pero consume CPU y memoria (relevante al migrar a Java 25 con hilos virtuales). Cambiar de algoritmo invalida los hashes existentes.
- `spring.jpa.show-sql=true` y `format_sql=true`: los logs de SQL son ruidosos en dev.
- Swagger en `/swagger-ui.html` (`springdoc`), actuator en `/actuator`.
- El `README.md` dice puerto `8080`: está obsoleto, el servicio usa `8081`.
- Dockerfile en minúscula (`dockerfile`).
- **Contrato**: las rutas **no llevan prefijo `/api`**, pero la norma del proyecto lo exige. Adoptarlo obliga a cambiar a la vez gateway, frontend y los `contrato_api.md`. Tabla de rutas objetivo: `docs_ia/contexto/contrato_api.md` §5.
- **Roles**: el enum Java es en inglés (`BUYER`, `ORGANIZER`, `STAFF_CHECKIN`, `ADMIN`) pero el **valor JSON es el de la spec** (`comprador`, `organizador`, `staff_checkin`, `admin`). No cambies uno sin cambiar el otro.
- **El JWT expira a las 24 h** (`jwt.expiration-ms=86400000`) y **no hay refresh token ni logout**: no hay mecanismo de revocación.
- **La BD se llama `user`** (no `TicketFlow`) y el **esquema lo crea Hibernate** (`ddl-auto=update`): no hay Flyway, no hay outbox y no hay tabla de refresh tokens.

## No tocar

- `.mvn/`, `.idea/`, `target/` = build/IDE output.
