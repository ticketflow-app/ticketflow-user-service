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
.\mvnw.cmd test                                  # (actual) H2 en memoria; objetivo: Testcontainers + PostgreSQL
.\mvnw.cmd -Dtest=RegisterUserUseCaseTest test   # un test concreto
.\mvnw.cmd spring-boot:run
```

## Gotchas

- **Tests (actual → objetivo)**: hoy usan **H2 en memoria** (`src/test/resources/application.properties`), así que no necesitas PostgreSQL; el objetivo de **integración** es **Testcontainers + PostgreSQL real** (H2 diverge en dialecto/constraints y da falsos verdes). Las **unitarias** de dominio van sin Spring ni BD.
- `spring.jpa.hibernate.ddl-auto=update` (solo dev; Flyway está planeado, no existe).
- **MapStruct 1.5.5 + Lombok** se configuran vía `annotationProcessorPaths` en `pom.xml`; **no lo elimines** (el codegen lo necesita).
- JWT: este servicio **emite** los tokens (el gateway valida). `JWT_SECRET` debe coincidir con el gateway y ser ≥32 bytes (HS256). jjwt `0.13.0` (el gateway usa `0.12.6`).
- El `README.md` muestra el puerto `:8080`, pero el servicio escucha en `:8081` (dato obsoleto).
- `docs/guia-microservicio-clean-code.md` es la guía de patrón **autoritativa** para servicios nuevos.

## Convenciones

- Hexagonal + Clean + DDD. Dos paquetes bajo `com.ticketflow.user`: `common/` (transversal) y `user/` (bounded context). Regla de dependencia: `infrastructure → application → domain`.
- Código, identificadores y comentarios **en inglés**; documentación en español.

## Gobernanza (para OpenCode)

> Versión para **OpenCode** de la gobernanza de TicketFlow. Aplica a todo el trabajo en este repo; su fuente de verdad es `/docs_ia/contexto`.

### 1. Fuente de contexto

- `/docs_ia/contexto` es la **única fuente autorizada** para: requisitos, atributos de calidad, arquitectura, diseño, estrategia de pruebas y restricciones del proyecto.
- No asumir información que no esté definida en `/docs_ia/contexto`.
- Antes de una tarea relevante, revisar los archivos de `/docs_ia/contexto` necesarios para esa tarea.

### 2. Decisiones arquitectónicas

- No tomar decisiones arquitectónicas importantes de forma autónoma.
- Cuando haga falta decidir, analizar alternativas y proponer una recomendación.
- Crear la propuesta en `/docs_ia/decisiones` en Markdown.
- Esperar aprobación humana antes de implementar una decisión propuesta.
- Una decisión aprobada queda documentada en `/docs_ia/decisiones`.

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

### 5. Pruebas

- No modificar la estrategia de pruebas definida en `/docs_ia/contexto` sin proponerlo antes.
- Cuando corresponda, crear o modificar pruebas junto con el código.
- No introducir nuevas herramientas o frameworks de pruebas sin analizar antes su necesidad y compatibilidad.

### 6. Gestión de dependencias

- Antes de agregar, eliminar, actualizar o reemplazar una dependencia, revisar los archivos relevantes de `/docs_ia/contexto`.
- Revisar siempre el manifiesto de dependencias actual (`pom.xml`) antes de proponer cambios.
- Clasificar las dependencias como: indispensables, recomendadas, opcionales o no justificadas actualmente.
- Para cada dependencia propuesta indicar: propósito, funcionalidad, relación con la arquitectura, requisito que satisface, alternativas, impacto técnico y compatibilidad con las versiones actuales.
- Verificar compatibilidad con: versión de Java, versión de Spring Boot, versión de Maven y arquitectura definida.
- No agregar dependencias por conveniencia, costumbre o sugerencia automática de una herramienta.
- Principio de mínima dependencia: solo las justificadas por requisitos y arquitectura aprobada.
- **No agregar, eliminar, actualizar ni modificar dependencias sin autorización humana explícita.**
- Antes de modificar el manifiesto, presentar análisis y propuesta; tras la autorización, aplicar solo lo aprobado.
- Toda modificación importante de dependencias se documenta en `/docs_ia/decisiones`.

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
- Si la tarea requiere una decisión no definida en `/docs_ia/contexto`, detener la implementación y presentar la decisión.

### 9. Comunicación

- Explicar las decisiones de forma breve y técnica.
- Si falta información en `/docs_ia/contexto`, indicarlo explícitamente.
- Diferenciar siempre entre: información definida, suposición, propuesta y decisión aprobada.
- Presentar las alternativas técnicamente válidas antes de elegir una.
- No presentar una propuesta como si fuera una decisión aprobada.
- Informar qué archivos se modificarán antes de cambios importantes.
- Tras cambios autorizados, informar: archivos modificados, cambios realizados, comandos ejecutados, pruebas realizadas, resultado y pendientes.

### 10. Trazabilidad del uso de IA

- Toda interacción relevante debe quedar registrada, incluso si es solo análisis sin cambios de código.
- Se registra desde que se ejecuta el prompt y no requiere autorización.
- Ubicación: `/docs_ia/prompts`, un archivo Markdown independiente por interacción, con identificadores consecutivos `P-001`, `P-002`, ...
- Contenido mínimo: identificador, fecha, objetivo, prompt completo, archivos de contexto consultados, archivos analizados, resumen de la respuesta, propuestas, decisiones pendientes y estado.
- Estados permitidos: `ANALISIS` | `PROPUESTA` | `APROBADO` | `RECHAZADO` | `IMPLEMENTADO`.
- Si hay propuesta arquitectónica o técnica, crear también el documento correspondiente en `/docs_ia/decisiones`.
- El registro de trazabilidad y la propuesta documental **no** autorizan a modificar código, configuración, dependencias o infraestructura.
- Ningún archivo (salvo trazabilidad y propuestas) se modifica sin autorización humana explícita.
- Si solo se pide análisis, ejecutarlo y documentarlo sin tocar código.
- Si luego se autoriza, actualizar el registro con autorización, cambios, archivos, comandos, pruebas, resultado y estado final.
- Nunca sobrescribir registros históricos; si algo cambia, crear un nuevo registro relacionado.
- La trazabilidad debe permitir reconstruir: Contexto → Prompt → Respuesta IA → Propuesta → Autorización humana → Implementación → Pruebas → Resultado.

## Grafo (codebase-memory)

- Proyecto de grafo: `user-service`.
- Este repo es git → `detect_changes --base-branch main` funciona contra este proyecto.
- Arquitectura entre servicios → consulta cada proyecto por separado y correlaciona tú; **no hay proyecto padre**.
- **Cambiar de rama NO refresca el grafo automáticamente** (el watcher sigue la raíz, que no es git). Tras `git checkout` o editar, fuerza re-index: `index_repository(repo_path="ticketflow-user-service", mode="full", name="user-service")`. Reindexar descarta las aristas `CROSS_*` y reinicia su estado a `never_run`.
- Sincronización global: `pwsh -File .\sync-graph.ps1` desde la raíz re-indexa los 4 servicios y re-ejecuta el enlazado cross-repo.

## No tocar

- `.mvn/`, `.idea/`, `target/` = build/IDE output.
