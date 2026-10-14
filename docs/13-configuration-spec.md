# Experiment 13 — Configuration Specification (target node)

Everything a fresh Ubuntu 22.04/24.04 server needs to run the portal, and the Ansible task that provides it (`ansible/provision.yml`, variables in `ansible/group_vars/appservers.yml`).

| Category | Item | Value | Ansible task |
|---|---|---|---|
| Packages | Docker engine | `docker.io` | Install required packages |
| | Docker SDK for Python (used by Ansible modules) | `python3-docker` | 〃 |
| | HTTP client for health checks | `curl` | 〃 |
| | CA certificates | `ca-certificates` | 〃 |
| Services | Docker daemon | running + enabled at boot | Ensure Docker service… |
| | Application container `parking-appeal` | restart policy `unless-stopped` | deploy.yml |
| Users / groups | Service account | `parkingapp` (system, no login shell, in `docker` group) | Create application user |
| | Container user | uid `1001` (`appuser` inside image) | — |
| Folders | App root | `/opt/parking-appeal` (parkingapp, 0750) | Create application folders |
| | Release history | `/opt/parking-appeal/releases` (`current_tag`, `previous_tag`) | 〃 |
| | Logs | `/opt/parking-appeal/logs` | 〃 |
| | Data (H2 DB + uploads) | `/opt/parking-appeal/data` (owner 1001, 0770) → mounted at `/app/data` | Create data folder… |
| Files | Environment file | `/opt/parking-appeal/app.env` (0640): `APP_ENV`, `REVIEWER_KEY`, `APP_DATA_DIR` | Write environment file |
| | Health helper | `/usr/local/bin/parking-appeal-health` | Install health-check helper |
| Ports | Application | host `8088` → container `8080`/tcp | deploy.yml `published_ports` |
| | Firewall (optional) | `ufw allow 8088/tcp` when `manage_firewall=true` | Open application port… |
| Health | Endpoint | `http://localhost:8088/actuator/health` → HTTP 200 `{"status":"UP"}` | Health check task |
