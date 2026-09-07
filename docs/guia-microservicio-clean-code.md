# Cómo construir un microservicio desde cero con Clean Architecture

Guía paso a paso — clase por clase — para crear un microservicio con **Arquitectura Hexagonal (Ports & Adapters) + Clean Architecture + DDD**, siguiendo la misma estructura y patrones que `trackinvest-account-service`.

Este documento usa el microservicio **`ticketflow-user-service`** como ejemplo vivo: cada clase que se explica ya existe en este repositorio, así que puedes leerla mientras lees la guía.

---

## Índice

1. [Conceptos base (leer primero)](#1-conceptos-base-leer-primero)
2. [Paso 0: Crear el proyecto con Spring Initializr](#2-paso-0-crear-el-proyecto-con-spring-initializr)
3. [Paso 1: `pom.xml` — las dependencias correctas](#3-paso-1-pomxml--las-dependencias-correctas)
4. [Paso 2: `application.properties` — la configuración](#4-paso-2-applicationproperties--la-configuracion)
5. [Paso 3: La estructura de paquetes](#5-paso-3-la-estructura-de-paquetes)
6. [Paso 4: El módulo `common` (columna vertebral)](#6-paso-4-el-modulo-common-columna-vertebral)
7. [Paso 5: El módulo del dominio — `user/domain`](#7-paso-5-el-modulo-del-dominio--userdomain)
8. [Paso 6: La capa de aplicación — `user/application`](#8-paso-6-la-capa-de-aplicacion--userapplication)
9. [Paso 7: La capa de infraestructura — `user/infrastructure`](#9-paso-7-la-capa-de-infraestructura--userinfrastructure)
10. [Paso 8: Docker](#10-paso-8-docker)
11. [Paso 9: Tests y cobertura](#11-paso-9-tests-y-cobertura)
12. [Próximos pasos (Kafka, Outbox, etc.)](#12-proximos-pasos-kafka-outbox-etc)
13. [Checklist rápido de Clean Code](#13-checklist-rapido-de-clean-code)

---

## 1. Conceptos base (leer primero)

Antes de escribir una sola línea, necesitas tener claras **tres reglas de oro** que gobiernan todo el proyecto:

### Regla 1: La regla de dependencia (la más importante)

> Las dependencias apuntan **hacia adentro**. La capa de dominio no conoce nada de Spring, ni de HTTP, ni de la base de datos. La infraestructura es la única capa que conoce frameworks.

```
infrastructure → application → domain → (Java puro)
```

- `domain/`: 100% Java puro. **Cero imports de Spring.** Aquí viven las reglas de negocio.
- `application/`: orquesta casos de uso contra puertos (interfaces). Conoce `domain`, pero no conoce Spring HTTP ni JPA.
- `infrastructure/`: la única capa que usa `@RestController`, `@Entity`, JWT, etc.

**¿Por qué?** Porque si mañana cambias de Spring a Quarkus, o de PostgreSQL a MongoDB, o de REST a gRPC, el negocio (domain + application) NO se toca. Solo se reemplazan los adaptadores de infraestructura.

### Regla 2: Los puertos son interfaces; los adaptadores son implementaciones

- **Ports (puertos)**: las interfaces que definen "qué necesita el negocio". Viven en `application/ports/`.
- **Adapters (adaptadores)**: las implementaciones concretas. Viven en `infrastructure/adapter/`.

El caso de uso solo conoce el **puerto** (`UserRepositoryPort`), nunca el adaptador (`UserJpaAdapter`). Así puedes cambiar PostgreSQL por otra cosa sin tocar el caso de uso.

### Regla 3: El modelo de dominio es sagrado

`UserDomain` es el corazón del negocio. **No se expone nunca al cliente** (se convierte a DTO) y **no se persiste directamente** (se convierte a `UserEntity`). Los mappers traducen entre estas tres representaciones:

| Representación | Capa | Propósito | Contiene password en claro |
|---|---|---|---|
| `UserDomain` | domain | Reglas de negocio | No (hash) |
| `UserEntity` | infrastr. | Persistencia JPA | No (hash) |
| DTOs (`GetUserResponseDTO`, etc.) | application | Límite con HTTP | No |

---

## 2. Paso 0: Crear el proyecto con Spring Initializr

1. Ve a [start.spring.io](https://start.spring.io).
2. Selecciona:
   - **Project**: Maven
   - **Language**: Java
   - **Spring Boot**: 4.x.x (la más reciente estable)
   - **Group**: `com.ticketflow`
   - **Artifact**: `user`
   - **Java**: 21
3. **Dependencias iniciales**: `Spring Web`, `Spring Security`, `Lombok`.

En esta guía el proyecto fue generado como `ticketflow-user-service` con las dependencias de arranque. ¿Por qué *solo* esas tres al inicio? Porque el resto (JPA, PostgreSQL, JWT, Argon2, OpenAPI) se agregan manualmente en el `pom.xml` con versiones explícitas que **tú controlas** (como hace trackinvest), en lugar de aceptar lo que Spring Initializr decide.

---

## 3. Paso 1: `pom.xml` — las dependencias correctas

**Archivo**: `pom.xml`

El `pom.xml` es lo primero que se toca. No es "un archivo más": es el contrato de qué librerías usa el microservicio. Después de editar, ejecuta `./mvnw clean compile` para validar que todo resuelve.

### Qué dependencias agregar y por qué

| Dependencia | ¿Por qué? |
|---|---|
| `spring-boot-starter-data-jpa` | ORM (Hibernate) para PostgreSQL. |
| `spring-boot-starter-webmvc` | REST controllers. (En Boot 4 `web` se renombró a `webmvc`). |
| `spring-boot-starter-security` | Seguridad de endpoints (proteger `/me`, etc.). |
| `spring-boot-starter-validation` | Validaciones (aunque aquí validamos con reglas de dominio). |
| `spring-boot-starter-actuator` | Health checks para Kubernetes/Docker. |
| `org.postgresql:postgresql` (runtime) | Driver de la base de datos. |
| `org.projectlombok:lombok` (provided) | Reducir boilerplate (getters, constructores). |
| `org.mapstruct:mapstruct` + `mapstruct-processor` | Traducir `UserDomain ↔ UserEntity` en el adapter de persistencia. |
| `io.jsonwebtoken:jjwt-api` + `jjwt-impl` + `jjwt-jackson` | Generar y validar JWT (estadonada, ver regla de la sección 9.5). |
| `de.mkammerer:argon2-jvm` | Hashing de contraseñas con Argon2. |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui` | Swagger UI / OpenAPI 3. |
| `com.h2database:h2` (test) | Base en memoria para los tests de integración. |

### Cosas de Clean Code a cuidar en el `pom.xml`

1. **Group/artifact claros**: `com.ticketflow` / `user`. El `name` debe ser el nombre real del servicio (`ticketflow-user-service`).
2. **Versiones explícitas** para librerías de terceros (MapStruct, jjwt, argon2, springdoc). No dejes que Maven "decida" por ti.
3. **Scopes correctos**: `runtime` para drivers (postgresql) y `impl` de jjwt; `provided` para Lombok y MapStruct processor.
4. **Jacoco con exclusión de infraestructura**: igual que trackinvest, la cobertura de código mide el negocio (domain + application), no los adaptadores:
   ```xml
   <sonar.coverage.exclusions>**/infrastructure/**/*</sonar.coverage.exclusions>
   ```
   y en el plugin jacoco: `<exclude>**/infrastructure/**/*</exclude>`.
5. **Annotation processors en el `maven-compiler-plugin`**: para que Lombok y MapStruct trabajen juntos se declaran en `annotationProcessorPaths`:
   - `lombok`
   - `lombok-mapstruct-binding` (para que MapStruct vea los getters/setters generados por Lombok)
   - `mapstruct-processor`

---

## 4. Paso 2: `application.properties` — la configuración

**Archivo**: `src/main/resources/application.properties`

```properties
spring.application.name=ticketflow-user-service

spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/TicketFlow}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:admin}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

jwt.secret=${JWT_SECRET:...}
jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}

springdoc.swagger-ui.path=/swagger-ui.html
```

### Qués configurar y por qué

- **`spring.datasource.*`**: conexión a PostgreSQL. Se usa sintaxis `${ENV_VAR:valor_por_defecto}` — así el mismo `application.properties` funciona local y en Docker/K8s (las variables de entorno sobreescriben sin tocar el archivo).
- **`spring.jpa.hibernate.ddl-auto=update`**: Hibernate crea/actualiza las tablas automáticamente. Útil en desarrollo; en producción usarás flyway/liquibase.
- **`spring.jpa.open-in-view=false`**: obligatorio para arquitectura limpia. Evita que Hibernate mantenga la sesión abierta durante el renderizado de la respuesta (evita LazyInitializationException y N+1).
- **`jwt.*`**: el secreto compartido entre TODOS los microservicios de TicketFlow y el tiempo de expiración del token. Este es el contrato de confianza entre servicios (ver sección 9.5).
- **`management.*`**: Actuator para health checks.

### Test properties

La configuración de **test** va en `src/test/resources/application.properties` y usa H2 en memoria para que los tests no dependan de PostgreSQL:

```properties
spring.datasource.url=jdbc:h2:mem:ticketflow;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
jwt.secret=ticketflow-test-secret-key-1234567890-abcdef
```

---

## 5. Paso 3: La estructura de paquetes

Crea esta estructura bajo `src/main/java/com/ticketflow/user/`:

```
src/main/java/com/ticketflow/user/
├── UserApplication.java                  # Punto de entrada (Spring Boot)
├── common/                               # Cross-cutting: se comparte entre módulos
│   ├── application/
│   │   └── dto/ApiResponse.java
│   ├── domain/
│   │   ├── exception/{TicketflowException, RequiredAttributeException}
│   │   └── rules/DomainRule.java
│   └── infrastructure/
│       ├── config/OpenApiConfig.java
│       └── handler/GlobalExceptionHandler.java
└── user/                                 # Bounded Context "Usuario"
    ├── application/
    │   ├── ports/in/{dto/, RegisterUserPort, LoginUserPort, GetMePort, ChangeRolePort}
    │   ├── ports/out/{UserRepositoryPort, PasswordEncoderPort, TokenProviderPort}
    │   └── usecase/{RegisterUserUseCase, LoginUserUseCase, GetMeUseCase, ChangeRoleUseCase}
    ├── domain/
    │   ├── enums/UserRole.java
    │   ├── exception/business/ + exception/format/
    │   ├── models/UserDomain.java
    │   └── rules/{UserValidatorRule, UserNameValidRule, UserEmailValidRule, UserPasswordValidRule, UserRoleValidRule}
    └── infrastructure/
        ├── adapter/in/controller/{AuthController, UserController}
        ├── adapter/out/persistence/{entity, repository, mapper, persistence}
        ├── adapter/out/security/{JwtTokenProvider, Argon2PasswordEncoder, AuthenticatedUser}
        ├── config/SecurityConfig.java
        ├── handler/UserExceptionHandler.java
        └── security/JwtAuthenticationFilter.java
```

### ¿Por qué dos paquetes top-level (`common` y `user`)?

Siguiendo DDD, el proyecto se divide en **bounded contexts** (módulos de negocio): `user`, `wallet`, `catalog`, etc. Cada módulo replica internamente las 3 capas. Hoy solo existe `user`.

El paquete `common` NO es negocio: es lo que todos los módulos reutilizan (respuestas HTTP estándar, excepción base, regla base, handlers genéricos, OpenAPI). Es el equivalente al `common` de trackinvest.

> **Regla de oro:** si una clase solo sirve a `user`, vive DENTRO de `user`. Si sirve a varios módulos, vive en `common`. Nada importa `user` desde `common` (evitar dependencias circulares).

---

## 6. Paso 4: El módulo `common` (columna vertebral)

Empezamos por `common` porque todo el resto depende de estas clases base. **Orden de creación: excepciones → regla → DTO → handler → OpenAPI.**

### 6.1 `common/domain/exception/TicketflowException.java`

```java
public class TicketflowException extends RuntimeException {
    protected TicketflowException(String message) {
        super(message);
    }
}
```

**Qué hace**: es la excepción base de todo el sistema de errores.

**Por qué Clean Code**:
- **Jerarquía clara**: todas las excepciones de negocio y de formato extienden de esta, así los handlers pueden capturar la base o cada hija.
- **Constructor `protected`**: fuerza a que nadie instancie la clase genérica directamente; siempre se usa una subclase con mensaje descriptivo.
- Vive en `domain` (no en infrastructure) porque las reglas de negocio (que lanzan excepciones) están en `domain` y no deben depender de Spring.

### 6.2 `common/domain/exception/RequiredAttributeException.java`

```java
public class RequiredAttributeException extends TicketflowException {
    public RequiredAttributeException(String attributeName) {
        super(String.format("The field '%s' is required.", attributeName));
    }
}
```

**Qué hace**: se lanza cuando falta un atributo obligatorio al construir un agregado.

**Por qué Clean Code**: recibe el *nombre del atributo* y construye un mensaje consistente. Así cada llamada no repite texto; solo pasa el dato que cambia.

---

### 6.3 `common/domain/rules/DomainRule.java`

```java
public interface DomainRule<T> {
    T validate(T data);
}
```

**Qué hace**: la interfaz que todas las "reglas compuestas" implementan. Recibe un modelo y devuelve el mismo modelo validado (o lanza excepción).

**Por qué Clean Code**:
- **Una interfaz, muchas implementaciones**: cada regla compleja (como `UserValidatorRule`) implementa `DomainRule`. Permite componer: puedes construir redes de validación con el mismo contrato.
- **Devuelve `T`**: permite encadenar (`new UserValidatorRule().validate(user)` devuelve el user validado).

---

### 6.4 `common/application/dto/ApiResponse.java`

```java
public record ApiResponse<T>(
        boolean success, String message, T data, Object errors, String timestamp) {
    public static <T> ApiResponse<T> success(T data, String message) { ... }
    public static <T> ApiResponse<T> error(String message, Object errors) { ... }
}
```

**Qué hace**: el **cuerpo de respuesta uniforme** de TODA la API. Todos los controllers devuelven `ApiResponse`, todos los handlers de error devuelven `ApiResponse.error(...)`.

**Por qué Clean Code**:
- **Record inmutable**: un DTO no debe mutar.
- **Métodos fábrica estáticos `success()`/`error()`**: los controllers no construyen `new ApiResponse(...)` con 5 argumentos; llaman métodos con nombre que expresan intención.
- **Genérico `<T>`**: sirve para `ApiResponse<GetUserResponseDTO>`, `ApiResponse<Void>`, etc.
- Vive en `common/application` porque es un contrato de la capa de aplicación (no del dominio).

---

### 6.5 `common/infrastructure/config/OpenApiConfig.java`

```java
@Configuration
@OpenAPIDefinition(info = @Info(title = "TicketFlow User API", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {}
```

**Qué hace**: configura Swagger UI y declara que la API usa JWT en el header `Authorization`.

**Por qué Clean Code**:
- Vive en `infrastructure` porque Swagger es un accesorio externo (aunque el resto del código no lo sepa).
- Un solo lugar para documentar el esquema de seguridad global.

---

### 6.6 `common/infrastructure/handler/GlobalExceptionHandler.java`

```java
@RestControllerAdvice   // Sin @Order() → prioridad LOWER (genérico)
public class GlobalExceptionHandler {
    @ExceptionHandler(RequiredAttributeException.class) → 400
    @ExceptionHandler(HttpMessageNotReadableException.class) → 400  // JSON malformado / enum inválido
    @ExceptionHandler(Exception.class) → 500 (log + mensaje genérico)
}
```

**Qué hace**: el **plan B** de manejo de errores. Captura lo que los handlers específicos no capturan.

**Por qué Clean Code**:
- **Nunca filtra detalles internos**: el 500 devuelve "An unexpected error occurred" y loguea el stacktrace real (con `log.error`). Evitar informar al atacante sobre el internals.
- **Respuestas estandarizadas**: siempre `ApiResponse.error(...)`.
- Vive en `common/infrastructure` porque cruza todos los módulos.

---

## 7. Paso 5: El módulo del dominio — `user/domain`

Esta es la capa MÁS importante del proyecto. **Nada que esté aquí puede importar Spring.** El orden de creación dentro de un módulo de dominio es: **enums → reglas simples → excepciones → regla compuesta → modelo de dominio**.

### 7.1 `user/domain/enums/UserRole.java`

```java
public enum UserRole {
    BUYER("comprador"),
    ORGANIZER("organizador"),
    STAFF_CHECKIN("staff_checkin"),
    ADMIN("admin");

    @JsonValue public String getValue() { return value; }
    @JsonCreator public static UserRole from(String value) { ... lanza IllegalArgumentException si no existe ... }
}
```

**Qué hace**: los roles del sistema. `ADMIN` existe porque el cambio de rol requiere "solo admin" (es un rol de sistema, no asignable en registro).

**Por qué Clean Code**:
- **Value Objects antes de entidades**: el rol es un "tipo" con significado de negocio, así que se modela como enum con `value`.
- **`@JsonValue` / `@JsonCreator`**: el cliente manda `"comprador"` (español) y la API responde `"comprador"`, pero internamente el código usa `BUYER`. Así el wire format de la API queda desacoplado de los identificadores de Java.
- Vive en `domain` porque expresa una regla del negocio: "los roles posibles son estos".

### 7.2 Reglas simples — `rules/UserNameValidRule`, `UserEmailValidRule`, `UserPasswordValidRule`

```java
public class UserNameValidRule {
    private static final String NAME_REGEX = "^[a-zA-Z0-9 ]+$";
    private UserNameValidRule() {}   // ← constructor privado: clase de utilidad pura
    public static void validate(String name) { ... }
}
```

**Qué hace**: validan **formato** de un atributo: nombre (3–50 caracteres, sin especiales), email (regex), password (8–72 caracteres).

**Por qué Clean Code**:
- **Una responsabilidad por clase**: cada regla valida UNA sola cosa. Fácil de testear (mira `UserRulesTest`).
- **Método estático `validate` + constructor privado**: no hay estado; es lógica pura.
- **Lanzan excepciones de formato** (`User*InvalidException`) que ya viven cerca. La "excepción format" comunica *dato mal formado*; la "business" comunica *regla de negocio rota*.

### 7.3 Excepciones de dominio

```java
// exception/format/  → el dato NO cumple el formato
public class UserEmailInvalidException extends TicketflowException {
    public UserEmailInvalidException() { super("The email is invalid..."); }
}

// exception/business/ → una regla de negocio se rompió
public class UserNotFoundException extends TicketflowException { ... }
public class InvalidCredentialsException extends TicketflowException { ... }
public class EmailAlreadyExistsException extends TicketflowException { ... }
public class UnauthorizedRoleChangeException extends TicketflowException { ... }
public class RoleNotAllowedForRegistrationException extends TicketflowException { ... }
```

**Qué hace**: un mensaje descriptivo por cada situación de error del dominio.

**Por qué Clean Code**:
- **Clasificación en dos sub-carpetas**: `format` (dato mal escrito) vs `business` (regla de negocio). El handler de excepciones puede mapear cada familia al status HTTP correcto (400 vs 409 vs 403).
- **Constructor sin parámetros**: el mensaje está embebido en la clase, con redacción precisa sobre el dominio. Cero duplicación de strings.
- **Una situación = una excepción**: aunque parezca verbose, permite testear y manejar cada caso finamente.

### 7.4 `rules/UserRoleValidRule.java`

```java
public class UserRoleValidRule {
    public static final UserRole[] REGISTRATION_ALLOWED_ROLES = {UserRole.BUYER, UserRole.ORGANIZER};
    public static void validate(UserRole role) { ... }
    public static boolean isAllowedForRegistration(UserRole role) { ... }
}
```

**Qué hace**: encapsula una **regla de negocio pura**: al registrarse un usuario solo puede autoseleccionar `comprador` u `organizador`; `staff_checkin` y `admin` solo los asigna un admin después.

**Por qué Clean Code**: la regla "quién puede tener qué rol al registrarse" es lógica de negocio, no del controller. Está en el lugar correcto y es testeable en aislamiento. Si mañana un organizador puede registrar staff, se cambia aquí, no en los controllers.

### 7.5 `rules/UserValidatorRule.java` (regla compuesta)

```java
public class UserValidatorRule implements DomainRule<UserDomain> {
    @Override
    public UserDomain validate(UserDomain user) {
        if (user.getId() == null) throw new RequiredAttributeException("id");
        if (user.getName() == null || trim blank) throw new RequiredAttributeException("name");
        ... email, password, rol, createdAt, updatedAt ...
        return user;
    }
}
```

**Qué hace**: la **regla compuesta**: valida que TODOS los atributos obligatorios existan al crear el agregado.

**Por qué Clean Code**:
- Implementa `DomainRule<UserDomain>` → comparte el contrato de validación.
- Lanza `RequiredAttributeException` (de `common`) con el nombre exacto del atributo.
- Se ejecuta automáticamente en `UserDomain.create(...)`: **el agregado se valida a sí mismo al nacer** (invariante de negocio).

### 7.6 `models/UserDomain.java` (el agregado)

```java
public class UserDomain {
    private final UUID id;
    private String name;
    private final String email;
    private String password;      // ← siempre HASH, nunca en claro
    private UserRole role;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserDomain(...) { Objects.requireNonNull(...); }   // constructor privado

    public static UserDomain create(UUID id, String name, String email, String password, UserRole role) {
        UserDomain user = new UserDomain(id, name, email, password, role, LocalDateTime.now(), LocalDateTime.now());
        return new UserValidatorRule().validate(user);   // auto-validación
    }

    public static UserDomain from(...) { ... }            // reconstruir desde la BD

    public void changeRole(UserRole newRole) {
        UserRoleValidRule.validate(newRole);
        this.role = newRole; this.updatedAt = LocalDateTime.now();
    }
}
```

**Qué hace**: el modelo de dominio del usuario. Contiene los datos + los métodos de comportamiento (`changeRole`, `changePassword`) que **siempre dejan al agregado en un estado válido**.

**Por qué Clean Code — los 5 patrones clave**:

1. **Constructores privados + métodos fábrica estáticos** (`create`, `from`): el objeto se crea por intención, no a ciegas. `create` = nuevo agregado (valida invariantes); `from` = reconstruir desde BD (no revalida todo, solo usa `requireNonNull`).
2. **`Objects.requireNonNull` en el constructor**: invariantes field-level: ningún campo nulo en un agregado persistido.
3. **Auto-validación**: `create()` termina con `new UserValidatorRule().validate(user)`. El agregado garantiza su propia integridad.
4. **Comportamiento, no setters**: no hay `setRol()`. Hay `changeRole()` que valida y actualiza `updatedAt`. **Los métodos de comportamiento son la única forma de mutar.**
5. **`password` es el hash**: el dominio recibe la contraseña YA hasheada (el caso de uso la hashea con `PasswordEncoderPort`). El dominio jamás ve la contraseña en claro.

> **Nota de diseño**: el constructor usa `requireNonNull` (falla rápido con NPE) ANTES de la regla compuesta. Esto es igual que en trackinvest: si el caso de uso ya validó el formato, el NPE del constructor es solo una red de seguridad para bugs internos.

---

## 8. Paso 6: La capa de aplicación — `user/application`

La capa de aplicación es **el cerebro operativo**: recibe un DTO, valida, coordina puertos, y responde con otro DTO. **No conoce ni HTTP ni la BD.** Orden de creación: **DTOs → puertos de entrada → puertos de salida → casos de uso**.

### 8.1 DTOs — `application/ports/in/dto/`

```java
// Entrada (request)
public record RegisterRequestDTO(String name, String email, String password, UserRole role) {}
public record LoginRequestDTO(String email, String password) {}
public record ChangeRoleRequestDTO(UserRole role) {}

// Salida (response)
public record GetUserResponseDTO(UUID id, String name, String email, UserRole role, LocalDateTime createdAt) {
    public static GetUserResponseDTO fromDomain(UserDomain user) { ... }   // mapper estático domain→DTO
}
public record LoginResponseDTO(String token, UUID id, String name, String email, UserRole role) {
    public static LoginResponseDTO from(String token, UserDomain user) { ... }
}
```

**Qué hace**: los contratos de entrada/salida del API. `GetUserResponseDTO` **expone exactamente lo que el cliente debe ver** — nótese que NO incluye `password`.

**Por qué Clean Code**:
- **Records (inmutables)**: DTO puro, sin lógica.
- **PUT no existe**: la validación no está en anotaciones de Jakarta (`@NotBlank`) sobre el DTO; está en las reglas de dominio. El DTO es un contenedor neutro. (Esto es una decisión arquitectónica: el negocio valida, no el framework.)
- **`fromDomain()` en los DTOs de salida**: el mapeo domain→DTO vive dentro del DTO, con un método de nombre descriptivo. Así el caso de uso hace `.map(GetUserResponseDTO::fromDomain)` y queda limpio.
- **La contraseña nunca sale**: `GetUserResponseDTO` y `LoginResponseDTO` no tienen el campo. **Clean Code = no exponer secretos.**

### 8.2 Puertos de entrada — `application/ports/in/`

```java
public interface RegisterUserPort { GetUserResponseDTO register(RegisterRequestDTO request); }
public interface LoginUserPort    { LoginResponseDTO login(LoginRequestDTO request); }
public interface GetMePort        { GetUserResponseDTO execute(UUID userId); }
public interface ChangeRolePort   { void execute(UUID actorUserId, UserRole actorRole, UUID targetUserId, UserRole newRole); }
```

**Qué hace**: **qué** puede hacer el sistema, sin decir **cómo**. Son los "casos de uso vistos desde fuera" (los usa el controller).

**Por qué Clean Code**:
- **Dependes de la abstracción**: `AuthController` depende de `RegisterUserPort`, NO de `RegisterUserUseCase`. Si mañana hay una implementación distinta, el controller no cambia.
- **Nombres de intención**: `RegisterUser`, `LoginUser`, `GetMe`, `ChangeRole` son los casos de uso del sistema (lenguaje ubicuo).

### 8.3 Puertos de salida — `application/ports/out/`

```java
public interface UserRepositoryPort {
    Optional<UserDomain> findById(UUID id);
    Optional<UserDomain> findByEmail(String email);
    UserDomain save(UserDomain user);
    boolean existsByEmail(String email);
}

public interface PasswordEncoderPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}

public interface TokenProviderPort {
    String generateToken(UUID userId, String email, UserRole role);
}
```

**Qué hace**: las **dependencias invertidas**. Definen lo que el negocio NECESITA del mundo exterior, sin importar cómo se hace.

**Por qué Clean Code — este es el corazón de la Arquitectura Hexagonal**:
- Trabajan con `UserDomain`, es decir, **el mundo exterior acepta y entrega objetos de dominio**, no entidades JPA.
- `PasswordEncoderPort`: el caso de uso no sabe si usa BCrypt, Argon2 o lo que sea. Hoy es Argon2, mañana scrypt → solo cambias el adapter.
- `TokenProviderPort`: el caso de uso no importa jjwt. Emite el token a través del puerto. (La VALIDACIÓN de tokens sí la hace el filtro de infraestructura directamente, no es tema del negocio.)

### 8.4 Casos de uso — `application/usecase/`

```java
@Service
@RequiredArgsConstructor
public class RegisterUserUseCase implements RegisterUserPort {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    @Transactional
    public GetUserResponseDTO register(RegisterRequestDTO request) {
        UserNameValidRule.validate(request.name());
        UserEmailValidRule.validate(request.email());
        UserPasswordValidRule.validate(request.password());

        UserRole role = request.role() == null ? UserRole.BUYER : request.role();
        if (!UserRoleValidRule.isAllowedForRegistration(role)) throw new RoleNotAllowedForRegistrationException();

        if (userRepository.existsByEmail(request.email())) throw new EmailAlreadyExistsException();

        String encodedPassword = passwordEncoder.encode(request.password());
        UserDomain user = UserDomain.create(UUID.randomUUID(), request.name(), request.email(), encodedPassword, role);
        userRepository.save(user);

        return GetUserResponseDTO.fromDomain(user);
    }
}
```

**Qué hace**: orquesta la operación paso a paso: valida formatos → valida regla de rol → chequea duplicado → hashea → construye el agregado → persiste → devuelve DTO.

**Por qué Clean Code — los 6 roles de un caso de uso**:
1. **Es un `@Service`** pero solo porque Spring necesita instanciar beans; la lógica interna no usa ninguna API de Spring.
2. **`@Transactional`**: el commit/rollback es un preocupación de orquestación que vive aquí. `save` + registro atómico.
3. **Secuencia legible**: lee como una historia de negocio, línea por línea.
4. **Depende de puertos** (inyectados por constructor con `@RequiredArgsConstructor`): construible y testeable con Mockito sin levantar nada.
5. **No hace trabajo de bajo nivel**: no abre transacciones manuales, no usa JPA directamente, no hashea él mismo — delega en puertos.
6. **Traduce a DTO** al final: el controller recibe algo listo para responder.

**LoginUserUseCase** (otro ejemplo de intención):

```java
@Override
public LoginResponseDTO login(LoginRequestDTO request) {
    UserDomain user = userRepository.findByEmail(request.email())
            .orElseThrow(InvalidCredentialsException::new);
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
        throw new InvalidCredentialsException();
    }
    String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
    return LoginResponseDTO.from(token, user);
}
```

Nota de seguridad: para email inexistente **también** se lanza `InvalidCredentialsException` (no `UserNotFoundException`), para no revelar qué emails están registrados (evitar enumeración de usuarios).

**ChangeRoleUseCase** (regla de autorización en el negocio):

```java
public void execute(UUID actorUserId, UserRole actorRole, UUID targetUserId, UserRole newRole) {
    if (actorRole != UserRole.ADMIN) throw new UnauthorizedRoleChangeException();
    UserDomain user = userRepository.findById(targetUserId).orElseThrow(UserNotFoundException::new);
    user.changeRole(newRole);
    userRepository.save(user);
}
```

La regla "solo admin cambia roles" es una **regla de negocio**, así que vive aquí, no `@PreAuthorize` sobre el controller.

---

## 9. Paso 7: La capa de infraestructura — `user/infrastructure`

Esta es la única capa consciente de Spring, JPA y HTTP. **Orden de creación: persistencia (entity→repo→mapper→adapter) → seguridad (JWT, hashing) → configuración (SecurityConfig) → controllers → exception handler.**

### 9.1 `persistence/entity/UserEntity.java`

```java
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity {
    @Id private UUID id;
    @Column(nullable = false, length = 50) private String name;
    @Column(nullable = false, unique = true, length = 255) private String email;
    @Column(nullable = false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserRole role;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
```

**Qué hace**: la representación JPA del usuario. **Es un detalle de infraestructura, no el dominio.** Notas:
- `@Column(nullable = false, unique = true)` en `email`: la base de datos también garantiza unicidad (defensa en profundidad).
- `password` almacena el hash Argon2.
- `@Enumerated(EnumType.STRING)`: guarda `BUYER` (nombre) en lugar del ordinal (`0,1,2...`). Los `EnumType.ORDINAL` cambian si reordenas el enum y rompen la BD.

**Por qué Clean Code**: separación clara entre el modelo de dominio (que puede tener validación y reglas) y la entidad (estructura de tabla sin reglas). Si la BD cambia de esquema, solo cambia `UserEntity`.

### 9.2 `persistence/repository/UserRepository.java`

```java
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

**Qué hace**: el repositorio **de Spring Data** (crudo, trabaja con `UserEntity`). Nota la diferencia de nombre: este es `UserRepository`; el del dominio/application es `UserRepositoryPort`. **Son dos cosas distintas.**

### 9.3 `persistence/mapper/UserEntityMapper.java`

```java
@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity toEntity(UserDomain domain);                    // ← MapStruct genera el código

    default UserDomain toDomain(UserEntity entity) {           // ← manual (dominio no tiene setters)
        if (entity == null) return null;
        return UserDomain.from(
            entity.getId(), entity.getName(), entity.getEmail(),
            entity.getPassword(), entity.getRole(),
            entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
```

**Qué hace**: traduce entre las dos representaciones.

**Por qué Clean Code**:
- `toEntity` se **genera automáticamente** con MapStruct (los nombres coinciden).
- `toDomain` es un `default` method **porque `UserDomain` no tiene setters** y MapStruct no puede construir un objeto con constructor privado. La mejor intervención manual es: generar lo automático, sobreescribir lo que el dominio protege.
- **El password viaja** entre domain y entity (el dominio lo necesita para verificar en login), PERO **nunca** a los DTOs de salida.

### 9.4 `persistence/persistence/UserJpaAdapter.java`

```java
@Component
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepositoryPort {
    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public Optional<UserDomain> findById(UUID id) {
        return userRepository.findById(id).map(userEntityMapper::toDomain);
    }
    @Override
    public UserDomain save(UserDomain user) {
        return userEntityMapper.toDomain(userRepository.save(userEntityMapper.toEntity(user)));
    }
    ...
}
```

**Qué hace**: el **adapter** que implementa el puerto `UserRepositoryPort`. Translate: `UserDomain → UserEntity → (JPA) → UserEntity → UserDomain`.

**Por qué Clean Code — aquí se cumple el patrón completo**:
- El `UserRepositoryPort` (application) lo usa el caso de uso.
- El `UserJpaAdapter` (infrastructure) lo implementa.
- El caso de uso nunca ve `UserEntity`; el adapter traduce y entrega un `UserDomain`.
- **Si mañana cambias de PostgreSQL a MySQL o a un API externa**, solo cambias `UserJpaAdapter`; `RegisterUserUseCase` permanece intacto.

### 9.5 `security/JwtTokenProvider.java`

```java
@Component
public class JwtTokenProvider implements TokenProviderPort {

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(UUID userId, String email, UserRole role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role.getValue())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {  // lo usa el JwtAuthenticationFilter
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
```

**Qué hace**: genera y valida el JWT con la **firma HMAC-SHA** usando un secreto compartido.

**La decisión de diseño clave (léelo dos veces):** en TicketFlow, **cada request NO llama a Usuarios para validar el token**. El token lleva embebido y **firmado** `userId`, `email` y `role`. Catalog, Inventory y los demás servicios validan la **firma localmente** con la misma clave compartida (`jwt.secret`), sin llamar a este servicio. Eso es lo que hace que Usuarios no se convierta en el cuello de botella de todo el sistema.

**Por qué Clean Code**:
- Implementa `TokenProviderPort` → el negocio no conoce jjwt.
- `parse` (validación) queda en la clase concreta, usada SOLO por infraestructura (el filtro). No contamina el puerto del dominio.
- Si el secreto en producción es distinto al local, no importa: se inyecta por `@Value` desde env var.

### 9.6 `security/Argon2PasswordEncoder.java`

```java
@Component
public class Argon2PasswordEncoder implements PasswordEncoderPort {

    private static final int ITERATIONS = 2;      // parámetros OWASP-recomendados para Argon2id
    private static final int MEMORY_IN_KIB = 65536;
    private static final int PARALLELISM = 1;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    @Override public String encode(String rawPassword) { ... }
    @Override public boolean matches(String rawPassword, String encodedPassword) { ... }
}
```

**Qué hace**: hashea con **Argon2id** (ganador del Password Hashing Competition).

**Por qué Clean Code**:
- Implementa `PasswordEncoderPort` → el negocio pide `passwordEncoder.encode(...)` sin saber qué algoritmo es.
- **Nunca loguea ni almacena la contraseña en claro**; los arrays `char[]` son locales al método.
- El secreto NO se guarda en texto plano en la BD: se guarda el hash Argon2 (selfdescribing, incluye salt + parámetros).

### 9.7 `security/JwtAuthenticationFilter.java`

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // constantes: REQUEST_ATTRIBUTE_USER_ID, REQUEST_ATTRIBUTE_USER_ROLE

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtTokenProvider.parse(header.substring(7));
                // extrae userId, role, email; crea AuthenticatedUser;
                // SecurityContextHolder.getContext().setAuthentication(...);
                // request.setAttribute(USER_ID, userId);  request.setAttribute(USER_ROLE, role);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

**Qué hace**: intercepta cada request HTTP, lee el `Authorization: Bearer <token>`, valida la firma y **expone el usuario autenticado** de dos formas:
1. En el **SecurityContext** de Spring (para `anyRequest().authenticated()`).
2. Como **request attributes** `USER_ID` (`UUID`) y `USER_ROLE` (`UserRole`) que los controllers leen con `@RequestAttribute`.

**Por qué Clean Code**:
- **Sin llamadas a BD**: el userId ya está en el token. No hay N+1 de "verificar cada request contra la BD" (esa es la promo del JWT).
- **Atributos tipados en el request**: `@RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID)` — el controller no parsea strings.
- **Token no parsea como excepción fatal**: si el token es inválido, se limpia el contexto y Spring devuelve 401. No truena la request.

### 9.8 `config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
          .csrf(disable)                                   // API stateless sin cookies
          .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/register", "/login",
                  "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**").permitAll()
              .requestMatchers(HttpMethod.OPTIONS).permitAll()
              .anyRequest().authenticated())
          .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
          .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }
}
```

**Qué hace**: define **qué rutas son públicas** (`/register`, `/login`, Swagger, Actuator) y **qué rutas exigen JWT válido** (todo lo demás, como `/me` y `/users/{id}/rol`).

**Por qué Clean Code**:
- **Confi-guración declarativa**: las reglas se leen de un vistazo.
- **STATELESS + CSRF off**: correcto para una API JWT sin cookies (nada que un atacante pueda "inyectar" como credencial ambiental).
- `addFilterBefore(jwtAuthenticationFilter, ...)`: garantiza que el contexto de seguridad se arma ANTES de que Spring Security evalúe `authenticated()`.

### 9.9 Controllers — `adapter/in/controller/`

```java
@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "...") // ← documentación Swagger por controller
public class AuthController {
    private final RegisterUserPort registerUserPort;   // ← puertos, no implementaciones
    private final LoginUserPort loginUserPort;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<GetUserResponseDTO>> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(registerUserPort.register(request), "User registered successfully"));
    }
}

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "...")
public class UserController {
    private final GetMePort getMePort;
    private final ChangeRolePort changeRolePort;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetUserResponseDTO>> getMe(
        @RequestAttribute(JwtAuthenticationFilter.REQUEST_ATTRIBUTE_USER_ID) UUID userId) { ... }

    @PatchMapping("/users/{id}/rol")
    public ResponseEntity<ApiResponse<Void>> changeRole(
        @PathVariable UUID id,
        @RequestBody ChangeRoleRequestDTO request,
        @RequestAttribute(...USER_ID...) UUID actorUserId,
        @RequestAttribute(...USER_ROLE...) UserRole actorRole) { ... }
}
```

**Qué hace**: la **puerta de entrada HTTP**. Traducen `HTTP → puerto` y `puerto → HTTP`.

**Por qué Clean Code — los controllers son DELGADOS**:
- **Solo 4 cosas**: reciben el request, llaman al puerto, envuelven en `ApiResponse`, devuelven `ResponseEntity`.
- **NO validan, NO hashean, NO consultan la BD, NO tienen lógica de negocio**: todo eso está en casos de uso / dominio.
- Dependen de los **puertos de entrada** (interfaces), no de implementaciones concretas.
- `/me` no recibe el userId por URL (como `/users/{id}`) — lo lee del request attribute del token: **el usuario autenticado se identifica solo**.

### 9.10 `handler/UserExceptionHandler.java`

```java
@RestControllerAdvice(basePackages = "com.ticketflow.user.user")
@Order(Ordered.HIGHEST_PRECEDENCE)    // ← se evalúa ANTES que el GlobalExceptionHandler
public class UserExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class) → 404
    @ExceptionHandler(InvalidCredentialsException.class) → 401
    @ExceptionHandler(UnauthorizedRoleChangeException.class) → 403
    @ExceptionHandler(EmailAlreadyExistsException.class) → 409
    @ExceptionHandler(UserNameInvalidException / UserEmailInvalidException / UserPasswordInvalidException)
        → 400
    @ExceptionHandler(RoleNotAllowedForRegistrationException.class) → 400
}
```

**Qué hace**: mapea **cada excepción de dominio del módulo user** a su status HTTP.

**Por qué Clean Code**:
- **`@Order(HIGHEST_PRECEDENCE)` + `basePackages`**: este handler gana sobre el global para las excepciones del módulo `user`. El global es la red de seguridad final.
- **Un mapping por excepción**: el código HTTP que corresponde a cada situación queda explícito (404/401/403/409/400).
- **Respuesta siempre con `ApiResponse.error(...)`**: mismo contrato de respuesta que los éxitos.

---

## 10. Paso 8: Docker

### `docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:18-alpine
    environment:
      POSTGRES_DB: TicketFlow
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: admin
    healthcheck: { test: ["CMD-SHELL", "pg_isready -U postgres"], ... }
  app:
    build: { context: ., dockerfile: dockerfile }
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/TicketFlow
      JWT_SECRET: ${JWT_SECRET:...}
    depends_on:
      postgres: { condition: service_healthy }
```

**Qué hace**: levanta PostgreSQL + la app. Nótese cómo las variables de entorno de la app coinciden con los placeholders de `application.properties` (`SPRING_DATASOURCE_URL`, `JWT_SECRET`): **la misma config funciona local y en contenedor**.
`condition: service_healthy` evita que la app arranque antes de que la BD esté lista.

### `dockerfile` (multi-stage)

```dockerfile
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /build
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Por qué multi-stage**: la imagen final pesa MUCHO menos (solo JRE + jar, sin JDK ni Maven). La etapa builder compila y se descarta.

---

## 11. Paso 9: Tests y cobertura

Igual que trackinvest, los tests cubren lo **más valioso**: `domain` (reglas + modelo) y `application` (casos de uso con Mockito). La infraestructura se excluye de la cobertura (Jacoco) porque son adaptadores.

### Qué testear y cómo

| Archivo de test | Qué valida | Técnica |
|---|---|---|
| `UserRulesTest` | Reglas de formato (nombre/email/password) | JUnit puro |
| `UserDomainTest` | Creación, invariantes, `changeRole`/`changePassword` | JUnit puro |
| `RegisterUserUseCaseTest` | Happy path + email duplicado + rol no permitido + formatos inválidos | Mockito (mockear `UserRepositoryPort` y `PasswordEncoderPort`) |
| `LoginUserUseCaseTest` | Token correcto + password errónea + email inexistente | Mockito |
| `GetMeUseCaseTest` | Usuario encontrado / no encontrado | Mockito |
| `ChangeRoleUseCaseTest` | Admin + no-admin + usuario inexistente | Mockito |
| `UserApplicationTests` | El contexto de Spring arranca (usa H2, no PostgreSQL) | `@SpringBootTest` |

### Por qué Mockito sin levantar Spring

Los casos de uso dependen **solo de puertos (interfaces)**. El constructor es público (gracias a `@RequiredArgsConstructor`), así que el test puede inyectar mocks directamente:

```java
userRepository = mock(UserRepositoryPort.class);
passwordEncoder = mock(PasswordEncoderPort.class);
useCase = new RegisterUserUseCase(userRepository, passwordEncoder);
```

No hace falta contexto Spring: es testeo de **unidad pura**, rápido y sin BD.

Para el test de contexto (`UserApplicationTests`) usamos **H2 en memoria** via `src/test/resources/application.properties`, así `./mvnw test` pasa en cualquier máquina sin PostgreSQL instalado.

---

## 12. Próximos pasos (Kafka, Outbox, etc.)

El microservicio actual es **REST + JWT** sin mensajería. Cuando Catalog e Inventory existan, se agrega la capa de eventos replicando trackinvest:

1. **`common/domain/event/DomainEvent.java`** — interfaz base de eventos (`eventId`, `aggregateId`, `eventType`, `occurredOn`).
2. **`common/domain/event/BaseDomainEvent.java`** — implementación abstracta con `version` (schema version) y `correlationId`.
3. **`user/domain/event/UserCreatedEvent.java`** — el evento del agregado (`user.created`).
4. **`common/application/ports/out/EventPublisherPort.java`** — puerto de salida `publish(BaseDomainEvent)`.
5. **Outbox pattern**: guardar el evento en la tabla `outbox_events` **en la misma transacción** que el registro del user (transaccionalidad atómica).
6. **Un relay job** (`@Scheduled`) que lee la outbox y publica a **Kafka** el topic `ticketflow.user.events`.

**¿Por qué Kafka para TicketFlow y no RabbitMQ?** Porque un sistema de ticketing tiene picos de eventos (ventas, inventario) donde Kafka escala horizontalmente con particiones, y los microservicios de Catalog/Inventory pueden tener sus propias consumer groups re-procesables. El patrón es idéntico; solo cambia el adaptador de mensajería.

---

## 13. Checklist rápido de Clean Code

**Por clase / capa:**

- [ ] `domain` → 100% Java puro. **Cero imports de Spring/JPA.** Reglas de negocio visiblemente testeadas.
- [ ] `application` → depende solo de **interfaces** (puertos). Orquesta, valida y traduce a DTO. Nunca importa JPA/HTTP.
- [ ] `infrastructure` → la única capa con `@Entity`, `@RestController`, jjwt, etc. Implementa los puertos como adaptadores.
- [ ] Modelo de dominio con constructor privado + fábricas `create`/`from` y auto-validación.
- [ ] Sin setters públicos: solo métodos de comportamiento que validan (`changeRole`).
- [ ] Una excepción por situación, en `format/` o `business/`; cada una con mensaje de negocio.
- [ ] Controllers **delgados** (4 líneas), dependen de puertos, respuesta siempre `ApiResponse`.
- [ ] Handlers de excepción por módulo con `@Order(HIGHEST_PRECEDENCE)` + global como fallback.
- [ ] Secrets vía `env` placeholders con default solo local (`${JWT_SECRET:...}`).
- [ ] Token JWT sin verificación por request contra la BD: `userId`/`role` viajan firmados en el token.
- [ ] La contraseña solo existe como hash Argon2 en el dominio/entidad; nunca en DTOs ni logs.
- [ ] Cada caso de uso tiene su test con Mockito; `./mvnw test` pasa sin infraestructura externa.