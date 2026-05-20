# Record API (Votos & Resultados)

Base path: `/api/public`

---

## Índice Rápido

| Método | Endpoint | Auth | Rol |
|--------|----------|------|-----|
| GET | `/api/public/processes/{id}/results` | ❌ Público | — |
| POST | `/api/public/records` | ➖ Exento | Semaphore Relayer |

Detalle completo debajo.

- [GET /api/public/processes/{id}/results — Resultados del proceso](#get-apipublicprocessesidresults-resultados)
- [POST /api/public/records — Ingresar voto](#post-apipublicrecords-ingresar)

---

## GET /api/public/processes/{id}/results <a name="get-apipublicprocessesidresults-resultados"></a>

Obtiene los resultados de un proceso electoral. Solo disponible cuando el proceso está en estado `CLOSED`.

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

## POST /api/public/records <a name="post-apipublicrecords-ingresar"></a>

Ingresa un voto validado desde el Semaphore Relayer. Endpoint **idempotente** — si ya existe un registro con el mismo `nullifier`, retorna el existente sin duplicar.

> **Auth**: ➖ Exento — Acceso permitido sin autenticación (Semaphore Relayer, M2M futura)

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
