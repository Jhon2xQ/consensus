# Electoral Process API

Base path: `/api/private/processes`

---

## Índice Rápido

- [GET /api/private/processes — Listar procesos](#get-apiprivateprocesses-listar)
- [GET /api/private/processes/{id} — Obtener proceso](#get-apiprivateprocessesid-obtener)
- [GET /api/private/processes/{id}/state — Estado actual](#get-apiprivateprocessesidstate-estado)
- [POST /api/private/processes — Crear proceso](#post-apiprivateprocesses-crear)
- [PUT /api/private/processes/{id} — Actualizar proceso](#put-apiprivateprocessesid-actualizar)
- [DELETE /api/private/processes/{id} — Eliminar proceso](#delete-apiprivateprocessesid-eliminar)

---

## GET /api/private/processes <a name="get-apiprivateprocesses-listar"></a>

Lista todos los procesos electorales con paginación.

### Parámetros (Query)

| Nombre | Tipo | Requerido | Descripción |
|--------|------|-----------|-------------|
| `page` | integer | No | Número de página (default: 0) |
| `size` | integer | No | Tamaño de página (default: 20) |
| `sort` | string | No | Campo de ordenación, ej. `name,asc` |

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "string",
        "scope": "string",
        "commitmentStart": "instant (ISO-8601)",
        "commitmentEnd": "instant (ISO-8601)",
        "votingStart": "instant (ISO-8601)",
        "votingEnd": "instant (ISO-8601)",
        "results": "instant (ISO-8601)"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  },
  "timestamp": 1234567890
}
```

---

## GET /api/private/processes/{id} <a name="get-apiprivateprocessesid-obtener"></a>

Obtiene un proceso electoral por su ID.

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
    "name": "string",
    "scope": "string",
    "commitmentStart": "instant (ISO-8601)",
    "commitmentEnd": "instant (ISO-8601)",
    "votingStart": "instant (ISO-8601)",
    "votingEnd": "instant (ISO-8601)",
    "results": "instant (ISO-8601)"
  },
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Process not found",
  "data": null,
  "timestamp": 1234567890
}
```

---

## GET /api/private/processes/{id}/state <a name="get-apiprivateprocessesidstate-estado"></a>

Obtiene el estado en tiempo real del proceso electoral basado en sus fechas.

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
    "processId": "uuid",
    "state": "NONE | COMMITMENT | VOTING | CLOSED"
  },
  "timestamp": 1234567890
}
```

### Posibles estados

| Estado | Condición |
|--------|-----------|
| `NONE` | Antes de `commitmentStart`, entre `commitmentEnd` y `votingStart`, o entre `votingEnd` y `results` |
| `COMMITMENT` | `commitmentStart ≤ now ≤ commitmentEnd` |
| `VOTING` | `votingStart ≤ now ≤ votingEnd` |
| `CLOSED` | `results ≤ now` |

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Process not found",
  "data": null,
  "timestamp": 1234567890
}
```

---

## POST /api/private/processes <a name="post-apiprivateprocesses-crear"></a>

Crea un nuevo proceso electoral.

### Request Body

```
{
  "name": "string (requerido)",
  "scope": "string (requerido)",
  "commitmentStart": "instant (ISO-8601, requerido)",
  "commitmentEnd": "instant (ISO-8601, requerido)",
  "votingStart": "instant (ISO-8601, requerido)",
  "votingEnd": "instant (ISO-8601, requerido)",
  "results": "instant (ISO-8601, requerido)"
}
```

### Respuesta `201 Created`

```
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": "uuid",
    "name": "string",
    "scope": "string",
    "commitmentStart": "instant (ISO-8601)",
    "commitmentEnd": "instant (ISO-8601)",
    "votingStart": "instant (ISO-8601)",
    "votingEnd": "instant (ISO-8601)",
    "results": "instant (ISO-8601)"
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

### Respuesta `409 Conflict`

```
{
  "success": false,
  "message": "A process with this name already exists",
  "data": null,
  "timestamp": 1234567890
}
```

---

## PUT /api/private/processes/{id} <a name="put-apiprivateprocessesid-actualizar"></a>

Actualiza un proceso electoral existente. Todos los campos son opcionales.

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `id` | UUID | Sí |

### Request Body (todos opcionales)

```
{
  "name": "string",
  "commitmentStart": "instant (ISO-8601)",
  "commitmentEnd": "instant (ISO-8601)",
  "votingStart": "instant (ISO-8601)",
  "votingEnd": "instant (ISO-8601)",
  "results": "instant (ISO-8601)"
}
```

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": "uuid",
    "name": "string",
    "scope": "string",
    "commitmentStart": "instant (ISO-8601)",
    "commitmentEnd": "instant (ISO-8601)",
    "votingStart": "instant (ISO-8601)",
    "votingEnd": "instant (ISO-8601)",
    "results": "instant (ISO-8601)"
  },
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Process not found",
  "data": null,
  "timestamp": 1234567890
}
```

---

## DELETE /api/private/processes/{id} <a name="delete-apiprivateprocessesid-eliminar"></a>

Elimina un proceso electoral. Solo se puede eliminar si no tiene equipos, inscripciones ni registros de voto asociados.

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `id` | UUID | Sí |

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "Process deleted successfully",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Process not found",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `409 Conflict`

```
{
  "success": false,
  "message": "Cannot delete process with existing dependencies",
  "data": null,
  "timestamp": 1234567890
}
```
