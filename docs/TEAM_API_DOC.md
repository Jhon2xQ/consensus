# Team API

Base path: `/api/private`

---

## Índice Rápido

- [GET /api/private/processes/{processId}/teams — Listar equipos](#get-apiprivateprocessesprocessidteams-listar)
- [GET /api/private/teams/{id} — Obtener equipo](#get-apiprivateteamsid-obtener)
- [POST /api/private/processes/{processId}/teams — Crear equipo](#post-apiprivateprocessesprocessidteams-crear)
- [PUT /api/private/teams/{id} — Actualizar equipo](#put-apiprivateteamsid-actualizar)
- [DELETE /api/private/teams/{id} — Eliminar equipo](#delete-apiprivateteamsid-eliminar)

---

## GET /api/private/processes/{processId}/teams <a name="get-apiprivateprocessesprocessidteams-listar"></a>

Lista todos los equipos de un proceso electoral.

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

## GET /api/private/teams/{id} <a name="get-apiprivateteamsid-obtener"></a>

Obtiene un equipo por su ID.

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

Crea un nuevo equipo dentro de un proceso electoral.

### Parámetros (Path)

| Nombre | Tipo | Requerido |
|--------|------|-----------|
| `processId` | UUID | Sí |

### Request Body

```
{
  "name": "string (requerido)",
  "avatarUrl": "string | null (opcional)"
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
    "name": "string",
    "avatarUrl": "string | null",
    "electoralProcessId": "uuid"
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

---

## PUT /api/private/teams/{id} <a name="put-apiprivateteamsid-actualizar"></a>

Actualiza el nombre y/o avatar de un equipo. Todos los campos son opcionales.

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
