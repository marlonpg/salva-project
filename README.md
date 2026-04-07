# Salva Project

Project with a Spring Boot backend (API + H2 + Liquibase) and a React frontend.

## Structure

- backend: REST API, H2 database, Liquibase migrations, Docker Compose
- frontend: React + Vite application
- frontend/pwa: previous static PWA version kept as reference

## Start backend

In the backend directory:

```bash
docker compose up --build
```

Health check:

```text
http://localhost:8080/actuator/health
```

## Start frontend

In the frontend directory:

```bash
npm install
npm run dev
```

Local access:

```text
http://localhost:5500
```

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
