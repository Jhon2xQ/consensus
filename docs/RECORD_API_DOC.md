# Record API (Votos)

Base path: `/api/private/records`

---

## Índice Rápido

| Método | Endpoint | Auth | Rol |
|--------|----------|------|-----|
| GET | `/api/private/records` | ✅ Bearer JWT | Autenticado |
| POST | `/api/private/records` | ➖ Exento | Semaphore Relayer |

Detalle completo debajo.

- [GET /api/private/records — Listar registros de voto](#get-apiprivaterecords-listar)
- [POST /api/private/records — Ingresar voto](#post-apiprivaterecords-ingresar)

---

## GET /api/private/records <a name="get-apiprivaterecords-listar"></a>

Lista los registros de voto con paginación, o filtra por scope sin paginación.

> **Auth**: ✅ Bearer JWT — Cualquier usuario autenticado

### Parámetros (Query)

| Nombre  | Tipo    | Requerido | Descripción                                                                                     |
| ------- | ------- | --------- | ----------------------------------------------------------------------------------------------- |
| `scope` | string  | No        | Filtra por scope del proceso electoral. Cuando se provee, retorna TODOS los registros coincidentes sin paginación. |
| `page`  | integer | No        | Número de página (default: 0). Solo aplica cuando NO se provee `scope`.                         |
| `size`  | integer | No        | Tamaño de página (default: 20). Solo aplica cuando NO se provee `scope`.                        |
| `sort`  | string  | No        | Campo de ordenación, ej. `createdAt,desc`. Solo aplica cuando NO se provee `scope`.             |

### Respuesta `200 OK` — Paginada (sin `scope`)

```
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": "uuid",
        "groupId": "string",
        "nullifier": "string",
        "message": "string",
        "scope": "string",
        "transactionHash": "string | null",
        "createdAt": "instant (ISO-8601)"
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

### Respuesta `200 OK` — Por scope (con `scope`)

```
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": "uuid",
      "groupId": "string",
      "nullifier": "string",
      "message": "string",
      "scope": "string",
      "transactionHash": "string | null",
      "createdAt": "instant (ISO-8601)"
    }
  ],
  "timestamp": 1234567890
}
```

### Respuesta `401 Unauthorized`

```
{
  "success": false,
  "message": "Authentication required",
  "data": null,
  "timestamp": 1234567890
}
```

---

## POST /api/private/records <a name="post-apiprivaterecords-ingresar"></a>

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

### Respuesta `200 OK`

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
