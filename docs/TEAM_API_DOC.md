# Team API

Base path (public): `/api/public`
Base path (private): `/api/private`

---

## Índice Rápido

| Método | Endpoint | Auth | Rol |
|--------|----------|------|-----|
| GET | `/api/public/processes/{processId}/teams` | ❌ Público | — |
| GET | `/api/public/teams/{id}` | ❌ Público | — |
| POST | `/api/private/processes/{processId}/teams` | ✅ Bearer JWT | `consensus-creator` |
| PUT | `/api/private/teams/{id}` | ✅ Bearer JWT | `consensus-creator` |
| DELETE | `/api/private/teams/{id}` | ✅ Bearer JWT | `consensus-creator` |

Detalle completo debajo.

- [GET /api/public/processes/{processId}/teams — Listar equipos](#get-apipublicprocessesprocessidteams-listar)
- [GET /api/public/teams/{id} — Obtener equipo](#get-apipublicteamsid-obtener)
- [POST /api/private/processes/{processId}/teams — Crear equipo](#post-apiprivateprocessesprocessidteams-crear)
- [PUT /api/private/teams/{id} — Actualizar equipo](#put-apiprivateteamsid-actualizar)
- [DELETE /api/private/teams/{id} — Eliminar equipo](#delete-apiprivateteamsid-eliminar)

---

## GET /api/public/processes/{processId}/teams <a name="get-apipublicprocessesprocessidteams-listar"></a>

Lista todos los equipos de un proceso electoral.

> **Auth**: ❌ Público — No requiere autenticación

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
      "name": "string",
      "avatarUrl": "string | null",
      "electoralProcessId": "uuid"
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

## GET /api/public/teams/{id} <a name="get-apipublicteamsid-obtener"></a>

Obtiene un equipo por su ID.

> **Auth**: ❌ Público — No requiere autenticación

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
    "avatarUrl": "string | null",
    "electoralProcessId": "uuid"
  },
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Team not found",
  "data": null,
  "timestamp": 1234567890
}
```

---

## POST /api/private/processes/{processId}/teams <a name="post-apiprivateprocessesprocessidteams-crear"></a>

Crea equipos dentro de un proceso electoral (operación batch).
Acepta un array JSON de equipos. Todos se crean atómicamente —
cualquier fallo de validación revierte el batch completo.

> **Auth**: ✅ Bearer JWT — Requiere rol `consensus-creator`

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `processId` | UUID | Sí |

### Request Body

```
[
  {
    "name": "string (requerido)",
    "avatarUrl": "string | null (opcional)"
  }
]
```

> Nota: `electoralProcessId` se setea automáticamente desde el path parameter.

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "N teams created",
  "data": [
    {
      "id": "uuid",
      "name": "string",
      "avatarUrl": "string | null",
      "electoralProcessId": "uuid"
    }
  ],
  "timestamp": 1234567890
}
```

### Respuesta `400 Bad Request`

```
{
  "success": false,
  "message": "At least one team is required",
  "data": null,
  "timestamp": 1234567890
}
```

```
{
  "success": false,
  "message": "Team name is required",
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
  "message": "Duplicate team name in request: \"Alpha\"",
  "data": null,
  "timestamp": 1234567890
}
```

```
{
  "success": false,
  "message": "Team with name \"X\" already exists in this process",
  "data": null,
  "timestamp": 1234567890
}
```

---

## PUT /api/private/teams/{id} <a name="put-apiprivateteamsid-actualizar"></a>

Actualiza el nombre y/o avatar de un equipo. Todos los campos son opcionales.

> **Auth**: ✅ Bearer JWT — Requiere rol `consensus-creator`

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `id` | UUID | Sí |

### Request Body (todos opcionales)

```
{
  "name": "string",
  "avatarUrl": "string"
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
    "avatarUrl": "string | null",
    "electoralProcessId": "uuid"
  },
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Team not found",
  "data": null,
  "timestamp": 1234567890
}
```

---

## DELETE /api/private/teams/{id} <a name="delete-apiprivateteamsid-eliminar"></a>

Elimina un equipo por su ID.

> **Auth**: ✅ Bearer JWT — Requiere rol `consensus-creator`

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `id` | UUID | Sí |

### Respuesta `200 OK`

```
{
  "success": true,
  "message": "Team deleted successfully",
  "data": null,
  "timestamp": 1234567890
}
```

### Respuesta `404 Not Found`

```
{
  "success": false,
  "message": "Team not found",
  "data": null,
  "timestamp": 1234567890
}
```
