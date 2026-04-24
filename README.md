# Stack Manager

A Tailscale-only Docker Compose stack manager: a Kotlin/http4k backend API + Kobweb frontend, designed to run on a Raspberry Pi (or any server) and be accessed securely over your Tailscale network.

## Architecture

- **backend/** — Kotlin/JVM HTTP API (http4k + Undertow). Manages Docker Compose stacks under `/srv/compose/<stack-name>/compose.yml`.
- **site/** — Kobweb (Kotlin/JS + Compose HTML) frontend SPA. Stores the bearer token and backend URL in `localStorage`.

## Backend Setup

### Prerequisites

- Docker + Docker Compose plugin on the host
- Java 21+
- The server accessible on your Tailscale IP

### Environment Variables

| Variable             | Default       | Description                            |
|----------------------|---------------|----------------------------------------|
| `STACKMGR_TOKEN`     | **required**  | Bearer token for API authentication    |
| `STACKMGR_BIND_HOST` | `127.0.0.1`   | Host/IP to bind (use your Tailscale IP)|
| `STACKMGR_PORT`      | `8080`        | Port to listen on                      |

### Run with Docker Compose (recommended)

1. Copy `deploy/compose.yml` to your server.
2. Copy `deploy/.env.example` to `deploy/.env` and set a strong `STACKMGR_TOKEN`.
3. Edit `deploy/compose.yml` to replace `100.x.y.z` with your actual Tailscale IP.

```bash
cd deploy
cp .env.example .env
# edit .env and compose.yml with your Tailscale IP and token
docker compose up -d
```

### Build from source

```bash
./gradlew :backend:installDist
# Binary at backend/build/install/backend/bin/backend
STACKMGR_TOKEN=mysecret STACKMGR_BIND_HOST=100.x.y.z ./backend/build/install/backend/bin/backend
```

## Frontend Setup

The frontend is a Kobweb static site. Build and export with the Kobweb CLI:

```bash
cd site
kobweb export --layout static
```

Serve the exported `site/.kobweb/site/` directory from any static host (GitHub Pages, Nginx, Caddy, etc.).

On first visit, go to `/login` to set:
- **Bearer Token** — must match `STACKMGR_TOKEN` on the backend
- **Backend Base URL** — your Tailscale URL, e.g. `http://100.x.y.z:8080`

## API Endpoints

All endpoints require `Authorization: Bearer <token>` header.

| Method | Path                          | Description                  |
|--------|-------------------------------|------------------------------|
| GET    | `/api/health`                 | Health check                 |
| GET    | `/api/stacks`                 | List all stacks               |
| GET    | `/api/stacks/{name}`          | Get stack compose YAML        |
| PUT    | `/api/stacks/{name}`          | Create/update stack           |
| POST   | `/api/stacks/{name}/deploy`   | `docker compose up -d`        |
| POST   | `/api/stacks/{name}/stop`     | `docker compose down`         |
| POST   | `/api/stacks/{name}/pull`     | `docker compose pull`         |
| GET    | `/api/stacks/{name}/logs`     | Get logs (`?tail=N`)          |

Stack names must match `^[a-z0-9-]+$`. Compose files are stored at `/srv/compose/<name>/compose.yml`.

## Development

```bash
# Backend
./gradlew :backend:build

# Frontend (requires kobweb CLI)
cd site && kobweb run
```
