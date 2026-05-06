# Record API (Votos & Resultados)

Base path: `/api/private`

---

## Índice Rápido

- [GET /api/private/processes/{id}/results — Resultados del proceso](#get-apiprivateprocessesidresults-resultados)
- [POST /api/private/records — Ingresar voto](#post-apiprivaterecords-ingresar)

---

## GET /api/private/processes/{id}/results <a name="get-apiprivateprocessesidresults-resultados"></a>

Obtiene los resultados de un proceso electoral. Solo disponible cuando el proceso está en estado `CLOSED`.

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
    "processName": "string",
    "teamResults": [
      {
        "teamName": "string",
        "voteCount": 0
      }
    ],
    "totalVotes": 0,
    "status": "CLOSED"
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

### Respuesta `400 Bad Request`

```
{
  "success": false,
  "message": "Results are not available yet. Process is in NONE state",
  "data": null,
  "timestamp": 1234567890
}
```

---

## POST /api/private/records <a name="post-apiprivaterecords-ingresar"></a>

Ingresa un voto validado desde el Semaphore Relayer. Endpoint **idempotente** — si ya existe un registro con el mismo `nullifier`, retorna el existente sin duplicar.

### Request Body

```
{
  "groupId": "string (requerido)",
  "nullifier": "string (requerido)",
  "message": "string (requerido) — nombre del equipo votado",
  "scope": "string (requerido)",
  "transactionHash": "string | null (opcional)"
}
```

### Respuesta `201 Created`

```
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": "uuid",
    "groupId": "string",
    "nullifier": "string",
    "message": "string",
    "scope": "string",
    "transactionHash": "string | null",
    "createdAt": "instant (ISO-8601)"
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
  "message": "Process not found for the given scope",
  "data": null,
  "timestamp": 1234567890
}
```
