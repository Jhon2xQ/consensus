# PRD - Consensus API

## 1. Overview

Consensus es una API REST para gestión de procesos electorales anónimos basados en el protocolo Semaphore (ZK proofs on-chain). Permite crear procesos electorales, inscribir votantes mediante commitments, y escuchar votos validados en blockchain para calcular resultados.

**Stack:** Spring Boot 4.0.6 + Java 25 + PostgreSQL + JPA (por ahora sin autenticación)

---

## 2. Dominio y Entidades

### 2.1 Tablas

| Entidad | Descripción | Relaciones |
|---------|-------------|------------|
| **ElectoralProcess** | Proceso electoral con fechas y estados | 1:N con Team, 1:N con Enrollment |
| **Team** | Opción/equipo candidato dentro de un proceso | N:1 con ElectoralProcess |
| **Enrollment** | Inscripción de un votante (commitment) en un proceso | N:1 con ElectoralProcess |
| **Record** | Voto validado escuchado desde Semaphore Relayer | N:1 con ElectoralProcess (vía groupId) |

### 2.2 Estados del Proceso Electoral (máquina de estados)

```
NONE (inicial) 
  → COMMITMENT (commitmentStart ≤ now ≤ commitmentEnd)
  → NONE (commitmentEnd < now < votingStart)
  → VOTING (votingStart ≤ now ≤ votingEnd)
  → NONE (votingEnd < now < results)
  → CLOSED (results ≤ now)
```

Los estados son **calculados automáticamente** según las fechas, no persistidos.

### 2.3 Campos Clave

**ElectoralProcess:**
- `id` (PK, UUID)
- `name` (unique, nombre del proceso)
- `scope` (unique, identificador Semaphore scope)
- `description` (opcional)
- `creatorId` (ID del creador, por ahora libre)
- `groupId` (smallint, ID del grupo Semaphore)
- `commitmentStart`, `commitmentEnd`
- `votingStart`, `votingEnd`
- `results` (fecha de publicación de resultados)
- Timestamps

**Team:**
- `id` (PK, UUID)
- `processId` (FK → ElectoralProcess, UUID)
- `name` (nombre del equipo/opción)
- `avatarUrl` (URL del avatar — el front la guarda en S3 y envía la URL)
- `voteCount` (calculado post-votación)
- Timestamps

**Enrollment:**
- `id` (PK, UUID)
- `processId` (FK → ElectoralProcess, UUID)
- `userId` (ID del votante, UUID)
- `commitment` (identity commitment Semaphore)
- `hasVoted` (boolean, default false)
- Timestamps
- **Unique constraint:** `(processId, userId)` — un usuario no puede inscribirse 2 veces en el mismo proceso
- **Unique constraint:** `(processId, commitment)` — un commitment no puede repetirse en el mismo proceso

**Record:**
- `id` (PK, UUID)
- `groupId` (smallint, coincide con ElectoralProcess.groupId)
- `nullifier` (previene doble voto, manejado por Semaphore)
- `message` (el voto — en este caso, el nombre del team)
- `scope` (scope del proceso)
- `transactionHash` (hash on-chain)
- `createdAt`

---

## 3. Reglas de Negocio

### 3.1 ElectoralProcess

- **Crear:** Cualquiera puede crear (sin auth por ahora). Se crea con estado NONE.
- **Modificar fechas:** Sí, se pueden modificar las fechas.
- **Eliminar:** Solo si NO tiene Teams, Enrollments ni Records asociados (no hay FK).
- **Listar:** Paginado. Filtros futuros: por estado, por creador.
- **Resultados:** `voteCount` de cada Team se calcula al finalizar la votación, contando todos los `record.message` que coincidan con cada `team.name`.

### 3.2 Team

- **Crear:** Asociado a un ElectoralProcess existente.
- **CRUD completo:** Sí.
- **Restricción:** Un proceso puede tener múltiples teams.
- **avatarUrl:** El frontend guarda la imagen directamente en S3 y envía solo la URL. El backend no maneja uploads.

### 3.3 Enrollment

- **Crear:** Un usuario envía su `commitment` para un proceso.
- **Validaciones:**
  - El proceso debe estar en estado NONE (antes de commitmentStart) o COMMITMENT (entre commitmentStart y commitmentEnd).
  - Un `(userId, processId)` no puede repetirse.
  - Un `(commitment, processId)` no puede repetirse.
- **hasVoted:** Se marca true cuando Semaphore valida un voto para ese nullifier (escuchado desde el Relayer).
- **No se puede eliminar** una inscripción (por integridad del voto anónimo).

### 3.4 Record

- **Endpoint privado:** `POST /api/private/records` — usado por el Semaphore Relayer para guardar votos validados.
- **No tiene GET/PUT/DELETE** por ahora.
- El Semaphore Relayer escucha eventos `ProofValidated` on-chain y luego consume este endpoint para persistir el voto.

---

## 4. Endpoints API

### Prefijos de Ruta

Todos los endpoints están bajo `/api/private/` (por ahora, pendiente de autenticación). En el futuro se separarán en públicos y privados cuando se implemente Logto.

### 4.1 ElectoralProcess (público)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/public/processes` | Crear proceso electoral |
| GET | `/api/public/processes` | Listar procesos (paginado) |
| GET | `/api/public/processes/{id}` | Obtener proceso por ID |
| PUT | `/api/public/processes/{id}` | Actualizar proceso/fechas |
| DELETE | `/api/public/processes/{id}` | Eliminar (solo sin dependencias) |
| GET | `/api/public/processes/{id}/state` | Estado actual calculado |
| GET | `/api/public/processes/{id}/results` | Resultados (solo si estado = CLOSED) |

### 4.2 Team (público)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/private/processes/{processId}/teams` | Crear team para un proceso |
| GET | `/api/private/processes/{processId}/teams` | Listar teams de un proceso |
| GET | `/api/private/teams/{id}` | Obtener team por ID |
| PUT | `/api/private/teams/{id}` | Actualizar team |
| DELETE | `/api/private/teams/{id}` | Eliminar team |

### 4.3 Enrollment (público)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/private/processes/{processId}/enrollments` | Inscribirse (enviar commitment) |
| GET | `/api/private/processes/{processId}/enrollments` | Listar inscripciones de un proceso |
| GET | `/api/private/enrollments/{id}` | Obtener inscripción por ID |

### 4.4 Record (privado)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/private/records` | Guardar voto validado (consumido por Semaphore Relayer) |

---

## 5. Respuesta Estándar

Todas las respuestas siguen este formato:

```json
{
  "success": true | false,
  "message": "Mensaje descriptivo",
  "data": { ... } | null,
  "timestamp": 1234567890
}
```

**Errores:**
- 400: Validación de negocio (estado inválido, duplicado, fechas incorrectas)
- 404: Recurso no encontrado
- 409: Conflicto (inscripción duplicada, proceso con dependencias)
- 500: Error interno

---

## 6. Integración Semaphore

### 6.1 Flujo de Votación

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND                                  │
│  1. Usuario se inscribe: POST /api/private/processes/{id}/enrollments
│  2. Usuario genera ZK proof con Semaphore SDK                   │
│  3. Usuario envía proof al Semaphore Relayer                    │
│     POST http://relayer:3000/api/semaphore/proofs/validate      │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼ viem.watchContractEvent
┌─────────────────────────────────────────────────────────────────┐
│                  SEMAPHORE RELAYER (Node/Bun)                    │
│  Escucha evento ProofValidated desde blockchain                  │
│  Consume endpoint de Consensus para persistir el voto:           │
│  POST /api/private/records                                       │
└─────────────────────────────────────────────────────────────────┘
```

### 6.2 Evento ProofValidado

El Semaphore Relayer envía al endpoint `/api/private/records`:

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

## 7. Estados y Validaciones

### 7.1 Transiciones de Estado

El estado se calcula en tiempo real comparando `now` con las fechas del proceso:

| Estado | Condición |
|--------|-----------|
| NONE | `now < commitmentStart` OR `commitmentEnd < now < votingStart` OR `votingEnd < now < results` |
| COMMITMENT | `commitmentStart ≤ now ≤ commitmentEnd` |
| VOTING | `votingStart ≤ now ≤ votingEnd` |
| CLOSED | `results ≤ now` |

### 7.2 Validaciones por Estado

| Operación | Estados Permitidos | Error si no |
|-----------|-------------------|-------------|
| Inscribirse (enrollment) | NONE, COMMITMENT | 400: "Enrollment not open" |
| Votar (vía Semaphore) | VOTING | Relayer valida on-chain |
| Ver resultados | CLOSED | 400: "Results not available yet" |
| Modificar fechas | Cualquiera | — |
| Eliminar proceso | Solo si sin dependencias | 409: "Process has dependencies" |

---

## 8. Cálculo de Resultados

Cuando el proceso pasa a CLOSED (`results ≤ now`):

1. Contar todos los `record` donde `groupId = electoralProcess.groupId`
2. Agrupar por `message` (nombre del team)
3. Asignar `voteCount` a cada `team` donde `team.name = record.message`
4. Endpoint `/api/public/processes/{id}/results` devuelve:

```json
{
  "success": true,
  "message": "Results retrieved",
  "data": {
    "processId": "uuid",
    "totalVotes": 150,
    "teams": [
      { "name": "Team Alpha", "voteCount": 90 },
      { "name": "Team Beta", "voteCount": 60 }
    ]
  }
}
```

---

## 9. Paginación

Solo para listado de procesos electorales:

```
GET /api/public/processes?page=0&size=20&sort=createdAt,desc
```

Respuesta:
```json
{
  "success": true,
  "message": "Processes retrieved",
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

## 10. Feature Futuras

- **Autenticación con Logto:** JWT validation, roles CREATOR/VOTER
- **Roles:** CREATOR (crea procesos + vota), VOTER (solo vota)
- **Endpoints avanzados:** `POST /api/public/processes/{id}/vote`, `GET /api/public/my-enrollments`, etc.
- **Filtros:** por estado, por creador, por fecha
- **WebSocket/SSE propio:** Para notificar a frontend de nuevos votos
- **Soft delete:** para procesos electorales
- **Rate limiting:** en endpoints de votación e inscripción

---

## 11. Arquitectura de Carpetas (Clean Architecture / Hexagonal)

Adaptación de la arquitectura HonoJS/TypeScript a Spring Boot:

```
com.carmenio.consensus
├── application/
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateElectoralProcessRequest.java
│   │   │   ├── UpdateElectoralProcessRequest.java
│   │   │   ├── CreateTeamRequest.java
│   │   │   ├── CreateEnrollmentRequest.java
│   │   │   └── CreateRecordRequest.java
│   │   └── response/
│   │       ├── ElectoralProcessResponse.java
│   │       ├── TeamResponse.java
│   │       ├── EnrollmentResponse.java
│   │       ├── RecordResponse.java
│   │       ├── ProcessStateResponse.java
│   │       ├── ProcessResultsResponse.java
│   │       └── PaginatedResponse.java
│   ├── use_case/
│   │   ├── electoral_process/
│   │   │   ├── CreateElectoralProcessUseCase.java
│   │   │   ├── DeleteElectoralProcessUseCase.java
│   │   │   ├── FindElectoralProcessByIdUseCase.java
│   │   │   ├── ListElectoralProcessesUseCase.java
│   │   │   ├── UpdateElectoralProcessUseCase.java
│   │   │   ├── GetProcessStateUseCase.java
│   │   │   └── GetProcessResultsUseCase.java
│   │   ├── team/
│   │   │   ├── CreateTeamUseCase.java
│   │   │   ├── DeleteTeamUseCase.java
│   │   │   ├── FindTeamByIdUseCase.java
│   │   │   ├── ListTeamsByProcessUseCase.java
│   │   │   └── UpdateTeamUseCase.java
│   │   ├── enrollment/
│   │   │   ├── CreateEnrollmentUseCase.java
│   │   │   ├── FindEnrollmentByIdUseCase.java
│   │   │   └── ListEnrollmentsByProcessUseCase.java
│   │   └── record/
│   │       └── CreateRecordUseCase.java
│   └── util/
│       ├── ProcessStateCalculator.java
│       └── VoteCounter.java
├── common/
│   ├── config/
│   │   ├── WebConfig.java
│   │   ├── JpaConfig.java
│   │   └── JacksonConfig.java
│   ├── constant/
│   │   └── ProcessStatus.java
│   └── library/
│       └── (librerías externas wrappeadas)
├── domain/
│   ├── entity/
│   │   ├── ElectoralProcess.java
│   │   ├── Team.java
│   │   ├── Enrollment.java
│   │   └── Record.java
│   ├── exception/
│   │   ├── DomainException.java
│   │   ├── ProcessNotFoundException.java
│   │   ├── InvalidProcessStateException.java
│   │   ├── DuplicateEnrollmentException.java
│   │   └── ProcessHasDependenciesException.java
│   └── repository/
│       ├── ElectoralProcessRepository.java
│       ├── TeamRepository.java
│       ├── EnrollmentRepository.java
│       └── RecordRepository.java
├── infrastructure/
│   ├── mapper/
│   │   ├── ElectoralProcessMapper.java
│   │   ├── TeamMapper.java
│   │   ├── EnrollmentMapper.java
│   │   └── RecordMapper.java
│   ├── repository/
│   │   ├── JpaElectoralProcessRepository.java
│   │   ├── JpaTeamRepository.java
│   │   ├── JpaEnrollmentRepository.java
│   │   └── JpaRecordRepository.java
│   └── service/
│       └── (servicios externos, ej: cliente HTTP al Relayer si fuera necesario)
├── presentation/
│   ├── controller/
│   │   ├── private_/
│   │   │   ├── ElectoralProcessController.java
│   │   │   ├── TeamController.java
│   │   │   ├── EnrollmentController.java
│   │   │   └── RecordController.java
│   ├── middleware/
│   │   ├── ExceptionHandlerMiddleware.java
│   │   └── ResponseWrapperMiddleware.java
│   └── schema/
│       └── (validaciones Jakarta Validation / JSON Schema)
└── ConsensusApplication.java
```

### Convenciones de Nombres

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Paquetes | snake_case | `com.carmenio.consensus.application.use_case` |
| Clases | PascalCase | `ElectoralProcessController` |
| Interfaces | PascalCase (sufijo descriptivo) | `ElectoralProcessRepository` |
| Métodos | camelCase | `findById` |
| Variables | camelCase | `processId` |
| Constantes | UPPER_SNAKE_CASE | `DEFAULT_PAGE_SIZE` |
| Enums | PascalCase | `ProcessStatus` |
| Archivos de configuración | kebab-case | `application.properties` |

---

## 12. Testing

### 12.1 Estrategia de Testing

| Tipo | Herramienta | Cobertura |
|------|-------------|-----------|
| **Unit tests** | JUnit 5 + Mockito | Use cases, domain services, mappers |
| **Integration tests** | Spring Boot Test + Testcontainers (PostgreSQL) | Controllers, repositories, endpoints |
| **Contract tests** | Spring MVC Test | Validación de request/response DTOs |

### 12.2 Convenciones de Tests

- Un test por use case: `{UseCaseName}Test.java`
- Tests de controller: `{ControllerName}Test.java` (WebMvcTest)
- Tests de integración: `{FeatureName}IntegrationTest.java` (SpringBootTest + Testcontainers)
- Nomenclatura de métodos: `should{ExpectedBehavior}When{Condition}`

### 12.3 Estructura de Tests

```
src/test/java/com/carmenio/consensus
├── application/
│   └── use_case/
│       ├── electoral_process/
│       │   └── CreateElectoralProcessUseCaseTest.java
│       ├── team/
│       ├── enrollment/
│       └── record/
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

### 12.4 Tests Mínimos Requeridos

Para cada entidad se requieren tests que cubran:
- Creación válida e inválida
- Validaciones de negocio (estados, fechas, constraints)
- CRUD completo en controller
- Casos de error (404, 409, 400)

---

**Autor:** Carmenio
**Fecha:** 2026-05-05
**Versión:** 1.2-Final
