# Salva Project

Project with a Spring Boot backend (API + H2 + Liquibase) and a React frontend that is also the PWA.

## Structure

- backend: REST API, embedded H2 database, Liquibase migrations
- frontend: React + Vite application, now also the single PWA source
- frontend/pwa: legacy static PWA kept only as reference during migration

## Start full stack

In the project root:

```bash
docker compose up --build
```

Application URLs:

```text
http://localhost:5500
http://localhost:8080/actuator/health
```

H2 remains embedded in the backend container and is persisted with the `h2-data` Docker volume.

## Start frontend in development mode

In the frontend directory:

```bash
npm install
npm run dev
```

Local access:

```text
http://localhost:5500
```

The React app is also the PWA now. Using an iframe for the PWA was intentionally avoided, because it would keep two app shells, complicate install/offline behavior, and still duplicate responsibility across code paths.

## Access from phone on the same network

1. Find the Wi-Fi IPv4 on Windows (example: 192.168.1.23):

```powershell
ipconfig
```

2. Make sure backend and frontend are running on ports 8080 and 5500.

3. Allow firewall rules on Windows (PowerShell as Administrator):

```powershell
New-NetFirewallRule -DisplayName "salva-backend-8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow -Profile Private
New-NetFirewallRule -DisplayName "salva-frontend-5500" -Direction Inbound -Protocol TCP -LocalPort 5500 -Action Allow -Profile Private
```

4. Test on your phone:

```text
http://192.168.1.23:8080/actuator/health
http://192.168.1.23:5500
```

If 5500 opens but 8080 fails, it is usually a firewall issue or guest network/AP isolation.
