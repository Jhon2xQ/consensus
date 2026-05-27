# Enrollment API

## Base paths

| Grupo | Base path |
|-------|-----------|
| Privado | `/api/private` |
| Público | `/api/public` |

---

## Índice Rápido

### Privado

| Método | Endpoint | Auth | Rol |
|--------|----------|------|-----|
| GET | `/api/private/processes/{processId}/enrollments` | ✅ Bearer JWT | Authenticated |
| GET | `/api/private/enrollments/{id}` | ✅ Bearer JWT | Authenticated |
| POST | `/api/private/processes/{processId}/enrollments` | ✅ Bearer JWT | `consensus-creator` |
| PUT | `/api/private/enrollments/{id}/commitment` | ✅ Bearer JWT | `consensus-user` |
| DELETE | `/api/private/enrollments/{id}` | ✅ Bearer JWT | `consensus-creator` |

### Público

| Método | Endpoint | Auth |
|--------|----------|------|
| GET | `/api/public/processes/{processId}/enrollments` | ❌ Sin auth |

Detalle completo debajo.

- [GET /api/private/processes/{processId}/enrollments — Listar inscripciones](#get-apiprivateprocessesprocessidenrollments-listar)
- [GET /api/private/enrollments/{id} — Obtener inscripción](#get-apiprivateenrollmentsid-obtener)
- [POST /api/private/processes/{processId}/enrollments — Crear inscripción (Fase 1)](#post-apiprivateprocessesprocessidenrollments-crear)
- [PUT /api/private/enrollments/{id}/commitment — Reclamar inscripción (Fase 2)](#put-apiprivateenrollmentsidcommitment-reclamar)
- [DELETE /api/private/enrollments/{id} — Eliminar inscripción](#delete-apiprivateenrollmentsid-eliminar)
- [GET /api/public/processes/{processId}/enrollments — Estadísticas públicas](#get-apipublicprocessesprocessidenrollments-estadísticas)

---

## Flujo de dos fases

1. **Fase 1 — Creator registra emails**: Un usuario con rol `consensus-creator` crea una inscripción proporcionando solo el email del votante. La inscripción queda con `userId: null` y `commitment: null`.
2. **Fase 2 — Usuario reclama su slot**: Un usuario con rol `consensus-user` autenticado vía JWT reclama la inscripción. El email del JWT debe coincidir con el email registrado. El `sub` del JWT se guarda como `userId` y el commitment de Semaphore se guarda desde el body.

---

## GET /api/private/processes/{processId}/enrollments <a name="get-apiprivateprocessesprocessidenrollments-listar"></a>

Lista todas las inscripciones de un proceso electoral.

> **Auth**: ✅ Bearer JWT — Requiere autenticación (cualquier rol)

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `processId` | UUID | Sí |

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": "uuid",
      "electoralProcessId": "uuid",
      "email": "string",
      "userId": "string | null",
      "commitment": "string | null",
      "hasVoted": boolean
    }
  ],
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Electoral process not found",
  "data": null,
  "timestamp": 1234567890
}
```

---

## GET /api/private/enrollments/{id} <a name="get-apiprivateenrollmentsid-obtener"></a>

Obtiene una inscripción por su ID.

> **Auth**: ✅ Bearer JWT — Requiere autenticación (cualquier rol)

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `id` | UUID | Sí |

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": "uuid",
    "electoralProcessId": "uuid",
    "email": "string",
    "userId": "string | null",
    "commitment": "string | null",
    "hasVoted": boolean
  },
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Enrollment not found",
  "data": null,
  "timestamp": 1234567890
}
```

---

## POST /api/private/processes/{processId}/enrollments <a name="post-apiprivateprocessesprocessidenrollments-crear"></a>

**Fase 1 — Creator**: Registra votantes en el proceso electoral mediante sus emails (operación batch).
Acepta un array JSON de inscripciones. Todas se crean atómicamente —
cualquier fallo de validación revierte el batch completo.

Solo se permite crear inscripciones cuando el proceso está en estado `NONE` o `COMMITMENT`.

> **Auth**: ✅ Bearer JWT — Requiere rol `consensus-creator`

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `processId` | UUID | Sí |

### Request Body

```
[
  {
    "email": "string (requerido, formato email válido)"
  }
]
```

> Nota: `electoralProcessId` se setea automáticamente desde el path parameter. `userId` y `commitment` no se envían en esta fase — se setean cuando el usuario reclama la inscripción en la fase 2.

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "N enrollments created",
  "data": [
    {
      "id": "uuid",
      "electoralProcessId": "uuid",
      "email": "string",
      "userId": null,
      "commitment": null,
      "hasVoted": false
    }
  ],
  "timestamp": 1234567890
}
```

### Respuesta `400 Bad Request`

```
{
  "success": false,
  "message": "At least one enrollment is required",
  "data": null,
  "timestamp": 1234567890
}
```

```
{
  "success": false,
  "message": "Enrollment not open for this process",
  "data": null,
  "timestamp": 1234567890
}
```

```
{
  "success": false,
  "message": "Email is required",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `403 Forbidden`

```
{
  "success": false,
  "message": "Access denied: insufficient permissions",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Electoral process not found",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `409 Conflict`

```
{
  "success": false,
  "message": "Duplicate email in request: dup@example.com",
  "data": null,
  "timestamp": 1234567890
}
```

```
{
  "success": false,
  "message": "Email already registered in this process",
  "data": null,
  "timestamp": 1234567890
}
```

---

## PUT /api/private/enrollments/{id}/commitment <a name="put-apiprivateenrollmentsidcommitment-reclamar"></a>

**Fase 2 — Usuario**: Reclama una inscripción existente. El email del JWT debe coincidir con el email registrado en la inscripción. El `sub` del JWT se guarda como `userId` y el commitment de Semaphore se guarda desde el body.

Solo se permite reclamar inscripciones cuando el proceso está en estado `NONE` o `COMMITMENT`.

> **Auth**: ✅ Bearer JWT — Requiere rol `consensus-user` y claim `email` en el JWT

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `id` | UUID | Sí |

### Request Body

```
{
  "electoralProcessId": "uuid (requerido)",
  "commitment": "string (requerido)"
}
```

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": "uuid",
    "electoralProcessId": "uuid",
    "email": "string",
    "userId": "string",
    "commitment": "string",
    "hasVoted": false
  },
  "timestamp": 1234567890
}
```

### Respuesta `400 Bad Request`

```
{
  "success": false,
  "message": "Enrollment not open for this process",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `401 Unauthorized`

```
{
  "success": false,
  "message": "Missing email claim in JWT",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `403 Forbidden`

```
{
  "success": false,
  "message": "Access denied: insufficient permissions",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Enrollment not found",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `409 Conflict`

```
{
  "success": false,
  "message": "A commitment with this value already exists in the process",
  "data": null,
  "timestamp": 1234567890
}
```
```
{
  "success": false,
  "message": "Enrollment with userId already exists",
  "data": null,
  "timestamp": 1234567890
}
```

---

## DELETE /api/private/enrollments/{id} <a name="delete-apiprivateenrollmentsid-eliminar"></a>

Elimina una inscripción por su ID.

> **Auth**: ✅ Bearer JWT — Requiere rol `consensus-creator`

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `id` | UUID | Sí |

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "Operation successful",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `403 Forbidden`

```
{
  "success": false,
  "message": "Access denied: insufficient permissions",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Enrollment not found",
  "data": null,
  "timestamp": 1234567890
}
```

---

## GET /api/public/processes/{processId}/enrollments <a name="get-apipublicprocessesprocessidenrollments-estadísticas"></a>

Devuelve estadísticas agregadas de inscripciones de un proceso electoral. No expone datos individuales de votantes.

> **Auth**: ❌ Sin autenticación — acceso público

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `processId` | UUID | Sí |

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "totalParticipants": integer,
    "totalCommitments": integer,
    "totalVoted": integer
  },
  "timestamp": 1234567890
}
```

### Campos del response

| Campo | Descripción |
|-------|-------------|
| `totalParticipants` | Total de inscripciones registradas en el proceso |
| `totalCommitments` | Inscripciones que ya enviaron su commitment de Semaphore |
| `totalVoted` | Inscripciones que emitieron su voto |

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Electoral process not found",
  "data": null,
  "timestamp": 1234567890
}
```
