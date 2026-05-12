# Stack Manager

A Tailscale-only Docker Compose stack manager: a Kotlin/http4k backend API + Kobweb frontend bundled into one server/container image, designed to run on a Raspberry Pi (or any server) and be accessed securely over your Tailscale network.

## Architecture

- **backend/** — Kotlin/JVM HTTP API (http4k + Undertow) that also serves the frontend SPA. Manages Docker Compose stacks under `/srv/compose/<stack-name>/compose.yml`.
- **site/** — Kobweb (Kotlin/JS + Compose HTML) frontend SPA, bundled into the Docker image and served by the backend at `/`.

File layout on the Pi:
```
/srv/compose/<stack-name>/compose.yml   ← desired state / config
/srv/containers/<container-name>/           ← runtime data / volumes (for stateful services)
```

---

## Setup

### 1. Prerequisites

- Server running linux/arm64
- [Tailscale](https://tailscale.com/download/linux) installed and connected
- Docker + Docker Compose plugin installed

Install Docker (if not already):
```bash
curl -fsSL https://get.docker.com | sh
```

Install Tailscale (if not already):
```bash
curl -fsSL https://tailscale.com/install.sh | sh
sudo tailscale up
```

### 2. Add your user to the docker group

This lets you run `docker` commands without `sudo`:
```bash
sudo usermod -aG docker $USER
# Log out and back in (or reboot) for the group change to take effect
sudo reboot
```

Verify after reboot:
```bash
docker ps   # should work without sudo
```

### 3. Create the compose directory structure

```bash
sudo mkdir -p /srv/compose
sudo chown $USER:$USER /srv/compose
```

For services with persistent data you will also create:
```bash
sudo mkdir -p /srv/containers
sudo chown $USER:$USER /srv/containers
```

### 4. Find your Tailscale IP

```bash
tailscale ip -4
# e.g. 100.125.223.81
```

---

## Running Stack Manager

### Option A: Docker Compose (recommended)

```bash
# 0. Download the Stack Manager repository
cd ~ && git clone https://github.com/omydagreat/stackmanager.git
cd stackmanager

# 1. Create config directory for StackManager itself
sudo mkdir -p /etc/stackmanager

# 2. Copy deploy files
sudo cp deploy/compose.yml /etc/stackmanager/compose.yml
sudo cp deploy/.env.example /etc/stackmanager/stackmanager.env

# 3. Edit the env file with a strong random token
sudo nano /etc/stackmanager/stackmanager.env
#   STACKMGR_TOKEN=<replace-with-a-long-random-secret>
#   STACKMGR_BIND_HOST=0.0.0.0
#   STACKMGR_PUBLISH_HOST=<your-tailscale-ip>
#   STACKMGR_IMAGE=omydagreat/stackmanager:vX.Y.Z
#   STACKMGR_DOCKER_BIN=/usr/bin/docker
#   STACKMGR_DOCKER_HOST=unix:///var/run/docker.sock

# 4. Lock down env file permissions (contains secrets)
sudo chown root:root /etc/stackmanager/stackmanager.env
sudo chmod 600 /etc/stackmanager/stackmanager.env

# 5. Start
docker compose -f /etc/stackmanager/compose.yml --env-file /etc/stackmanager/stackmanager.env up -d
```

> **Optional:** After copying the deployment files to `/etc/stackmanager/`, you can delete the cloned repository (`rm -rf ~/stackmanager`) since all runtime files are now in place and the application itself runs from the Docker image, not the repo. Also, instead of copying the `compose.yml` and `.env.example` files, you could create them directly in `/etc/stackmanager/` if you prefer. The key point is to keep Stack Manager's own deployment files separate from the managed stack definitions under `/srv/compose/<stack-name>/compose.yml`.

> Keep StackManager's own deployment files in `/etc/stackmanager` so they stay separate from managed stack definitions under `/srv/compose/<stack-name>/compose.yml`.

### Option B: Build from source

Requires Java 21+.

```bash
./gradlew build

# Prepare frontend assets for the backend server
mkdir -p backend/build/web
cp site/build/kotlin-webpack/js/productionExecutable/stackmanager.js backend/build/web/
# Optional: copy the source map if Kobweb generated one
cp site/build/kotlin-webpack/js/productionExecutable/stackmanager.js.map backend/build/web/ 2>/dev/null || true
cp -r site/build/processedResources/js/main/public/. backend/build/web/

# Start (replace token and IP)
STACKMGR_TOKEN=my-secret \
STACKMGR_BIND_HOST=100.125.223.81 \
STACKMGR_PORT=8080 \
STACKMGR_WEB_ROOT=$(pwd)/backend/build/web \
./backend/build/install/backend/bin/backend
```

### Environment Variables

| Variable                | Default       | Description                                                    |
|-------------------------|---------------|----------------------------------------------------------------|
| `STACKMGR_TOKEN`        | **required**  | Bearer token for API authentication                            |
| `STACKMGR_BIND_HOST`    | `127.0.0.1`   | App listen address; Docker uses `0.0.0.0` inside the container |
| `STACKMGR_PUBLISH_HOST` | `127.0.0.1`   | Docker host bind address; set to your Tailscale IP             |
| `STACKMGR_PORT`         | `8080`        | Port to listen on                                              |
| `STACKMGR_WEB_ROOT`     | `/app/public` | Directory containing bundled frontend assets                   |
| `STACKMGR_IMAGE`        | **required**  | Docker image tag to run (pin to a `vX.Y.Z` release)            |
| `STACKMGR_DOCKER_BIN`   | `/usr/bin/docker` | Docker CLI path used by stack actions                      |
| `STACKMGR_DOCKER_HOST`  | `unix:///var/run/docker.sock` | Docker daemon endpoint passed to CLI             |

> **Security note:** when running from source, `STACKMGR_BIND_HOST` defaults to `127.0.0.1`.  
> In Docker, leave `STACKMGR_BIND_HOST=0.0.0.0` so the app can bind inside the container, and control external exposure with `STACKMGR_PUBLISH_HOST`.

---

## Accessing via Tailscale

Once Stack Manager is running, from any device on your Tailscale network open:

- `http://<tailscale-ip>:8080`
- or `http://<magicdns-hostname>:8080`

The frontend and API are served from the same address. In `/login`, configure:

- **Bearer Token** — must match the `STACKMGR_TOKEN` you set on the Pi
- **Backend Base URL** — optional override. By default, the app uses the current site origin automatically.

You can also use the Pi's Tailscale MagicDNS hostname:
- `http://maleficpi.your-tailnet.ts.net:8080`

Check the hostname with:
```bash
tailscale status | head -3
```

The API is only reachable from devices connected to your Tailscale network — it is never exposed to the public internet.

---

## Frontend Setup

For production, the frontend is bundled into the same Docker image and served by the backend process. You do not need GitHub Pages or a separate static host.

**Quick local test with Kobweb dev server:**
```bash
cd site
kobweb run    # opens http://localhost:8080
```

---

## Docker Hub Deployment (GitHub Actions)

This repo includes a workflow at `.github/workflows/docker-publish.yml` that first builds the Gradle artifacts and then packages them into the backend image defined by `Dockerfile` before publishing to Docker Hub.

Required GitHub repository secrets:

- `DOCKERHUB_USERNAME` — your Docker Hub username (or org bot username)
- `DOCKERHUB_TOKEN` — Docker Hub access token with permission to push images

Publishing behavior:

- Pull requests to `main`: build only (no push)
- Manual run via `workflow_dispatch`: choose a bump type (`patch`, `minor`, or `major`)
- The workflow reads the latest `vX.Y.Z` git tag, computes the next semantic version, builds the Gradle artifacts, packages and pushes the image, then pushes the new git tag
- After the tag push, the workflow creates a matching GitHub Release with generated release notes
- Published image tags include only the release tag:
  - `vX.Y.Z`

To release a new image, open the GitHub Actions workflow and dispatch it with the desired bump type. For example, if the latest git tag is `v1.4.2`:

- `patch` → `v1.4.3`
- `minor` → `v1.5.0`
- `major` → `v2.0.0`

For deployments, set `STACKMGR_IMAGE` to the exact release you want to run, for example `omydagreat/stackmanager:v1.4.3`.

---

## API Reference

All endpoints except `/api/health` require `Authorization: Bearer <token>` header.

| Method | Path                        | Description                              |
|--------|-----------------------------|------------------------------------------|
| GET    | `/api/health`               | Health check (no auth)                   |
| GET    | `/api/stacks`               | List all stacks                          |
| GET    | `/api/stacks/{name}`        | Get stack compose YAML                   |
| PUT    | `/api/stacks/{name}`        | Create/update stack (JSON body)          |
| POST   | `/api/stacks/{name}/deploy` | `docker compose up -d`                   |
| POST   | `/api/stacks/{name}/stop`   | `docker compose down`                    |
| POST   | `/api/stacks/{name}/pull`   | `docker compose pull`                    |
| GET    | `/api/stacks/{name}/logs`   | Get recent logs (`?tail=N`, default 100) |

Stack names must match `^[a-z0-9-]+$`. Compose files are stored at `/srv/compose/<name>/compose.yml`.

**PUT body:**
```json
{ "composeYaml": "services:\n  ..." }
```

---

## Development

```bash
# Backend only
./gradlew :backend:build

# Frontend (requires kobweb CLI)
cd site && kobweb run
```
