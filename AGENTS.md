# AGENTS.md — Consensus API

> Contexto y reglas para agentes de IA trabajando en este proyecto.

---

## 1. Contexto del Proyecto

**Consensus** es una API REST para gestión de procesos electorales anónimos basados en el protocolo **Semaphore** (ZK proofs on-chain).

- **Propósito**: Crear procesos electorales, inscribir votantes mediante commitments, y calcular resultados a partir de votos validados en blockchain.
- **Stack**: Spring Boot 4.0.6 + Java 25 + PostgreSQL + JPA (Hibernate)
- **Arquitectura**: Clean Architecture / Hexagonal
- **Autenticación**: Sin auth por ahora (feature futura con Logto)
- **Estado**: En desarrollo activo

---

## 2. Stack Tecnológico

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Framework | Spring Boot | 4.0.6 |
| Lenguaje | Java | 25 |
| JVM Target | — | 25 |
| Base de datos | PostgreSQL | Latest |
| ORM | Spring Data JPA (Hibernate) | — |
| Testing | JUnit 5 + Mockito | — |
| Build | Maven | — |
| Virtual Threads | Activadas (`spring.threads.virtual.enabled=true`) | — |

---

## 3. Arquitectura de Carpetas (Clean Architecture)

```
com.carmenio.consensus
├── application/          # Casos de uso, DTOs, utilidades
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── use_case/         # Un use case = una operación de negocio
│   └── util/
├── common/               # Configs, constantes, librerías
│   ├── config/
│   ├── constant/
│   └── library/
├── domain/               # Entidades, excepciones, interfaces de repositorio
│   ├── entity/
│   ├── exception/
│   └── repository/       # Ports (interfaces)
├── infrastructure/       # Implementaciones JPA, mappers, servicios externos
│   ├── mapper/
│   ├── repository/       # Adapters (implementaciones JPA)
│   └── service/
├── presentation/         # Controllers, middlewares (filters), validaciones
│   ├── controller/
│   │   └── private_/     # Todos los endpoints son /api/private/* por ahora
│   ├── middleware/
│   └── schema/
└── ConsensusApplication.java
```

**Regla de oro**: Las dependencias apuntan SIEMPRE hacia adentro. `presentation` → `application` → `domain`. `infrastructure` implementa interfaces de `domain`.

---

## 4. Convenciones de Nombres

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Paquetes | `snake_case` | `com.carmenio.consensus.application.use_case` |
| Clases | `PascalCase` | `ElectoralProcessController` |
| Interfaces | `PascalCase` | `ElectoralProcessRepository` |
| Métodos | `camelCase` | `findById` |
| Variables | `camelCase` | `processId` |
| Constantes | `UPPER_SNAKE_CASE` | `DEFAULT_PAGE_SIZE` |
| Enums | `PascalCase` | `ProcessStatus` |
| Archivos de config | `kebab-case` | `application.properties` |
| Use cases | `PascalCase` + sufijo | `CreateElectoralProcessUseCase` |
| DTOs request | `PascalCase` + sufijo | `CreateElectoralProcessRequest` |
| DTOs response | `PascalCase` + sufijo | `ElectoralProcessResponse` |
| Mappers | `PascalCase` + sufijo | `ElectoralProcessMapper` |

---

## 5. Reglas de Negocio Críticas

### 5.1 Estados del Proceso Electoral

Los estados se **calculan en tiempo real**, NO se persisten:

```
NONE → COMMITMENT → NONE → VOTING → NONE → CLOSED
```

| Estado | Condición |
|--------|-----------|
| NONE | `now < commitmentStart` OR `commitmentEnd < now < votingStart` OR `votingEnd < now < results` |
| COMMITMENT | `commitmentStart ≤ now ≤ commitmentEnd` |
| VOTING | `votingStart ≤ now ≤ votingEnd` |
| CLOSED | `results ≤ now` |

### 5.2 Validaciones por Estado

- **Inscripción (enrollment)**: Solo en estado NONE o COMMITMENT
- **Resultados**: Solo visible cuando estado = CLOSED
- **Eliminar proceso**: Solo si NO tiene Teams, Enrollments ni Records

### 5.3 Constraints de BD

- `ElectoralProcess.name` → UNIQUE
- `ElectoralProcess.scope` → UNIQUE
- `Enrollment` → UNIQUE(`processId`, `userId`)
- `Enrollment` → UNIQUE(`processId`, `commitment`)

### 5.4 IDs

- **TODOS** los IDs son UUID (`java.util.UUID`)
- Generados automáticamente por la base de datos o la aplicación

---

## 6. Patrones a Seguir

### 6.1 Un Use Case = Una Clase

```java
// ✅ Correcto
@Component
@RequiredArgsConstructor
public class CreateElectoralProcessUseCase {
    private final ElectoralProcessRepository repository;
    private final ElectoralProcessMapper mapper;
    
    public ElectoralProcessResponse execute(CreateElectoralProcessRequest request) {
        // validaciones
        // lógica de negocio
        // persistencia
        // retorno
    }
}
```

### 6.2 Controller delega 100% al Use Case

```java
// ✅ Correcto
@RestController
@RequiredArgsConstructor
public class ElectoralProcessController {
    private final CreateElectoralProcessUseCase createUseCase;
    
    @PostMapping("/api/private/processes")
    public ResponseEntity<ApiResponse> create(@RequestBody CreateElectoralProcessRequest request) {
        var response = createUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

### 6.3 Respuesta Estándar

TODAS las respuestas usan este wrapper:

```json
{
  "success": true | false,
  "message": "Mensaje descriptivo",
  "data": { ... } | null,
  "timestamp": 1234567890
}
```

### 6.4 Manejo de Errores

- Usar excepciones de dominio (`DomainException`, `ProcessNotFoundException`, etc.)
- El `ExceptionHandlerMiddleware` (filter) las captura y las convierte en respuesta estándar
- NUNCA lanzar `RuntimeException` genérico

### 6.5 Repositorios

- Interfaz en `domain.repository` (port)
- Implementación en `infrastructure.repository` (adapter) con prefijo `Jpa`
- Usar Spring Data JPA, NO JDBC nativo

---

## 7. Anti-Patrones (NUNCA HACER)

### ❌ Lógica de negocio en el controller
```java
// MAL
@PostMapping("/processes")
public ResponseEntity<?> create(@RequestBody ProcessDTO dto) {
    if (dto.getName() == null) { ... }  // Lógica de validación en controller
    repository.save(dto);               // Acceso directo a repo desde controller
}
```

### ❌ Entidad JPA expuesta en la API
```java
// MAL
@GetMapping("/processes/{id}")
public ElectoralProcess get(@PathVariable UUID id) {  // Retorna entidad JPA directamente
    return repository.findById(id);
}
```

### ❌ IDs autoincrementales
```java
// MAL
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)  // ❌ NO. Usar UUID.
private Long id;
```

### ❌ `null` sin manejar
```java
// MAL
public Process findById(UUID id) {
    return repository.findById(id).get();  // ❌ .get() sin Optional
}
```

### ❌ Lógica de negocio en entidades
```java
// MAL (anemic domain model)
@Entity
public class ElectoralProcess {
    // Solo getters/setters, sin comportamiento
}
```

### ❌ Repositorio accediendo a otro repositorio
```java
// MAL
public class TeamRepository {
    @Autowired
    private ElectoralProcessRepository processRepo;  // ❌ Repositorio no debe conocer otro repositorio
}
```

---

## 8. Testing

### 8.1 Estrategia

| Tipo | Cobertura | Herramienta |
|------|-----------|-------------|
| Unit tests | Use cases, domain services, mappers | JUnit 5 + Mockito |
| Integration tests | Controllers, repositories, endpoints | Spring Boot Test + Testcontainers |
| Contract tests | Request/response DTOs | Spring MVC Test |

### 8.2 Convenciones de Nombres de Tests

```java
@Test
void shouldCreateProcessWhenRequestIsValid() { }

@Test
void shouldThrowExceptionWhenNameIsDuplicate() { }

@Test
void shouldReturn404WhenProcessNotFound() { }
```

### 8.3 Estructura de Tests

```
src/test/java/com/carmenio/consensus
├── application/
│   └── use_case/
│       └── electoral_process/
│           └── CreateElectoralProcessUseCaseTest.java
├── domain/
│   └── entity/
│       └── ElectoralProcessTest.java
├── infrastructure/
│   └── repository/
│       └── JpaElectoralProcessRepositoryTest.java
└── presentation/
    └── controller/
        └── ElectoralProcessControllerTest.java
```

### 8.4 Reglas de Testing

- Un test por caso de uso
- Usar `@Mock` para dependencias, `@InjectMocks` para el SUT
- Tests de integración usan `@Testcontainers` con PostgreSQL
- Tests de controller usan `@WebMvcTest` + `MockMvc`
- NUNCA testear getters/setters
- Siempre testear el happy path + al menos 2 casos de error

---

## 9. Endpoints

**Prefijo base**: `/api/private/`

### ElectoralProcess
- `POST /api/private/processes` — Crear
- `GET /api/private/processes` — Listar (paginado)
- `GET /api/private/processes/{id}` — Obtener
- `PUT /api/private/processes/{id}` — Actualizar
- `DELETE /api/private/processes/{id}` — Eliminar
- `GET /api/private/processes/{id}/state` — Estado actual
- `GET /api/private/processes/{id}/results` — Resultados

### Team
- `POST /api/private/processes/{processId}/teams`
- `GET /api/private/processes/{processId}/teams`
- `GET /api/private/teams/{id}`
- `PUT /api/private/teams/{id}`
- `DELETE /api/private/teams/{id}`

### Enrollment
- `POST /api/private/processes/{processId}/enrollments`
- `GET /api/private/processes/{processId}/enrollments`
- `GET /api/private/enrollments/{id}`

### Record
- `POST /api/private/records` — Consumido por Semaphore Relayer

---

## 10. Integración Semaphore

El Semaphore Relayer (microservicio Node/Bun) escucha eventos `ProofValidated` on-chain y consume `POST /api/private/records` para persistir votos.

**Evento esperado**:
```json
{
  "groupId": "1",
  "nullifier": "11111111111111111111111111111111",
  "message": "Team Alpha",
  "scope": "33333333333333333333333333333333",
  "transactionHash": "0xabc123...",
  "createdAt": "2026-05-05T12:00:00Z"
}
```

---

## 11. Código de Estado HTTP

| Código | Uso |
|--------|-----|
| 200 | Operación exitosa |
| 201 | Recurso creado |
| 400 | Error de validación de negocio |
| 404 | Recurso no encontrado |
| 409 | Conflicto (duplicado, dependencias) |
| 500 | Error interno del servidor |

---

## 12. Reglas para el Agente de IA

Cuando generes código para este proyecto:

1. **Seguí la arquitectura Clean/Hexagonal** — Nunca mezcles capas
2. **Usá UUID para todos los IDs** — Sin excepciones
3. **Un use case por operación** — No combines lógica en servicios genéricos
4. **Usá DTOs** — Nunca expongas entidades JPA en la API
5. **Validá en el use case** — No en el controller ni en la entidad
6. **Usá Optional** — Nunca retornes `null` desde repositorios
7. **Lanzá excepciones de dominio** — No `RuntimeException` genérico
8. **Escribí tests** — Al menos unit tests para cada use case
9. **Seguí las convenciones de nombres** — Exactamente como están definidas
10. **Documentá con Javadoc** — Al menos en interfaces públicas y use cases
11. **Usá `@RequiredArgsConstructor`** — No `@Autowired` en campos
12. **Mantené inmutabilidad donde sea posible** — `final` en campos, records para DTOs

---

**Última actualización**: 2026-05-05
**Versión**: 1.0
**Autor**: Carmenio
