# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a BungeeCord Minecraft proxy network running on Windows. BungeeCord acts as a reverse proxy (port 26000) that accepts player connections and routes them to two backend game servers. All three servers run as separate JVM processes launched via `.bat` files.

## Starting Servers

Each server has its own `start.bat`. They must be started in this order:
1. Backend servers first (`2 - Paper\start.bat` and/or `3 - Spigot\start.bat`)
2. Proxy last (`1 - Proxy\start.bat`)

All scripts use `java -Xms512M -Xmx1024M -jar <name>.jar nogui`.

## Network Architecture

```
Players → localhost:26000 (BungeeCord Proxy)
              ├── paper  → localhost:26001 (PaperMC)
              └── spigot → localhost:26002 (Spigot)
```

- Proxy runs in **online mode** (authenticates with Mojang)
- Backend servers run in **offline mode** (`online-mode=false` in `server.properties`) — this is required and correct for BungeeCord networks; authentication is handled at the proxy
- IP forwarding is enabled on both proxy (`config.yml`: `ip_forward: true`) and backends (`spigot.yml`: `bungeecord: true`) — these must stay in sync

## Key Configuration Files

| File | Purpose |
|------|---------|
| `1 - Proxy/config.yml` | Network topology, server list, listener port, permissions groups |
| `1 - Proxy/modules.yml` | Enabled BungeeCord modules (cmd_alert, cmd_find, etc.) |
| `2 - Paper/server.properties` | Paper server port (26001), game settings |
| `2 - Paper/config/paper-global.yml` | Paper-specific global overrides |
| `3 - Spigot/server.properties` | Spigot server port (26002), game settings |
| `2 - Paper/spigot.yml` / `3 - Spigot/spigot.yml` | BungeeCord mode, spawn rates, optimizations |

## Adding/Configuring Backend Servers

To add a new backend server, edit `1 - Proxy/config.yml` under the `servers:` section:
```yaml
servers:
  paper:
    motd: '&1Paper'
    address: localhost:26001
    restricted: false
  new_server:
    motd: '&1New'
    address: localhost:26003
    restricted: false
```
Also update `priorities:` under the listener to set fallback order.

## Plugins

- **Proxy plugins** go in `1 - Proxy/plugins/` (currently none installed)
- **Paper plugins** go in `2 - Paper/plugins/`
- **Spigot plugins** go in `3 - Spigot/plugins/`

Installed: `spark` (performance profiler) and `bStats` (metrics) on Paper; `PluginMetrics` on Spigot.

## Operator / Admin Setup

The player `beanbeanjuice` (UUID: `a9946507-3a31-4184-b2bd-8d7e4ee0108d`) is op on the Paper server (`2 - Paper/ops.json`, level 4). Proxy-level admin permissions are defined in `1 - Proxy/config.yml` under `groups.admin`.

## Important Constraints

- Backend `server.properties` must keep `online-mode=false` — changing it will break BungeeCord authentication
- `spigot.yml` `bungeecord: true` must remain set on both backends or player IPs/UUIDs will be wrong
- The proxy compression threshold (256 bytes in `config.yml`) and backend thresholds should stay consistent to avoid double-compression
