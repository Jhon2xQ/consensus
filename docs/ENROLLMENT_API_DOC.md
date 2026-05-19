# Enrollment API

Base path: `/api/private`

---

## Índice Rápido

| Método | Endpoint | Auth | Rol |
|--------|----------|------|-----|
| GET | `/api/private/processes/{processId}/enrollments` | ✅ Bearer JWT | `user` |
| GET | `/api/private/enrollments/{id}` | ✅ Bearer JWT | `user` |
| POST | `/api/private/processes/{processId}/enrollments` | ✅ Bearer JWT | `user` |

Detalle completo debajo.

- [GET /api/private/processes/{processId}/enrollments — Listar inscripciones](#get-apiprivateprocessesprocessidenrollments-listar)
- [GET /api/private/enrollments/{id} — Obtener inscripción](#get-apiprivateenrollmentsid-obtener)
- [POST /api/private/processes/{processId}/enrollments — Crear inscripción](#post-apiprivateprocessesprocessidenrollments-crear)

---

## GET /api/private/processes/{processId}/enrollments <a name="get-apiprivateprocessesprocessidenrollments-listar"></a>

Lista todas las inscripciones de un proceso electoral.

> **Auth**: ✅ Bearer JWT — Requiere rol `user`

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
      "userId": "string",
      "commitment": "string",
      "hasVoted": true
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

> **Auth**: ✅ Bearer JWT — Requiere rol `user`

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
    "userId": "string",
    "commitment": "string",
    "hasVoted": true
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

Inscribe un votante en un proceso electoral mediante su commitment de Semaphore.

Solo se permite inscribir cuando el proceso está en estado `NONE` o `COMMITMENT`.

> **Auth**: ✅ Bearer JWT — Requiere rol `user`

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `processId` | UUID | Sí |

### Request Body

```
{
  "userId": "string (requerido)",
  "commitment": "string (requerido)"
}
```

> Nota: `electoralProcessId` se setea automáticamente desde el path parameter.

### Respuesta `201 Created`

```
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": "uuid",
    "electoralProcessId": "uuid",
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
  "message": "Validation error description",
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
  "message": "Enrollment already exists for this user in this process",
  "data": null,
  "timestamp": 1234567890
}
```
