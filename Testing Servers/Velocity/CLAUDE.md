# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a Velocity proxy network setup with three independently launched Minecraft server instances. Each runs as a separate process started via its own `start.bat`.

## Starting Servers

Each server must be started in its own terminal window by running the `start.bat` in its directory. Start the proxy last (or after the backends are ready).

| Directory | Role | Port |
|-----------|------|------|
| `1 - Proxy\` | Velocity proxy (public entry point) | 25000 |
| `2 - Paper\` | Paper (Bukkit/Spigot-compatible) backend | 25001 |
| `3 - Fabric\` | Fabric backend | 25002 |

All start scripts use: `java -Xms512M -Xmx1024M -jar <file>.jar nogui`

> **Port mismatch to be aware of:** `velocity.toml` maps `fabric = "localhost:25003"` and `spigot = "localhost:25002"`, but the Fabric server's `server.properties` sets `server-port=25002`. The Fabric server needs its `server-port` changed to `25003` (or the `velocity.toml` entry updated to `25002`) before it will be reachable through the proxy. There is no Spigot server directory.

## Architecture: Player Forwarding

Velocity uses **modern forwarding** — the proxy authenticates players with Mojang (`online-mode=true`), then passes player identity to backends using a shared secret. Backends run `online-mode=false` and trust the proxy entirely.

The forwarding secret is stored in `1 - Proxy\forwarding.secret` and must match the secret configured in each backend:

- **Paper** (`2 - Paper\config\paper-global.yml`): `proxies.velocity.enabled: true` with `secret: R3QOttBsQf7K`
- **Fabric** (`3 - Fabric\config\FabricProxy-Lite.toml`): `hackOnlineMode = true` with `secret = "R3QOttBsQf7K"`

If you rotate the forwarding secret, update all three locations: `forwarding.secret`, `paper-global.yml`, and `FabricProxy-Lite.toml`.

## Key Configuration Files

- **`1 - Proxy\velocity.toml`** — proxy bind address, registered backend servers, try order, ping passthrough, rate limits
- **`2 - Paper\server.properties`** — Paper server port, gamemode, difficulty, view distance
- **`2 - Paper\config\paper-global.yml`** — Paper-specific tuning, velocity forwarding config
- **`3 - Fabric\server.properties`** — Fabric server port and base settings
- **`3 - Fabric\config\FabricProxy-Lite.toml`** — Velocity forwarding config for Fabric

## Server Network Behavior

- Players logging in are sent to `paper` first (configured in `velocity.toml` → `try = ["paper"]`)
- `ping-passthrough = "all"` — the proxy forwards the backend's server list ping response as-is
- The `spigot` server entry in `velocity.toml` exists as a placeholder but has no backing server directory

## Installed Plugins / Mods

**Paper plugins** (in `2 - Paper\plugins\`):
- `bStats` — anonymous usage metrics
- `spark` — performance profiling

**Fabric mods** (in `3 - Fabric\mods\`):
- `FabricProxy-Lite` — Velocity modern forwarding support
- `fabric-api` — base Fabric API
