# Salva Project

claude code --dangerously-skip-permissions
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
https://localhost:5443
http://localhost:8080/actuator/health
```

Notes:

- `http://localhost:5500` redirects to the HTTPS frontend on port `5443`
- the frontend proxies `/api/*` to the backend container, so the browser talks to the API through the same HTTPS origin

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
https://localhost:5443
```

The React app is also the PWA now. Using an iframe for the PWA was intentionally avoided, because it would keep two app shells, complicate install/offline behavior, and still duplicate responsibility across code paths.

## HTTPS for PWA installability

To get the app to behave like an installable PWA on a phone, use HTTPS with a locally trusted certificate.

1. Install `mkcert` on Windows.

```powershell
winget install FiloSottile.mkcert
```

2. Open a new PowerShell window and install the local CA:

```powershell
mkcert -install
```

3. Generate certificates for your machine IP and localhost from the project root. Example for Wi-Fi IP `192.168.1.100`:

```powershell
mkdir certs -Force
Set-Location certs
mkcert 192.168.1.100 localhost 127.0.0.1
Rename-Item (Get-ChildItem "*.pem" | Where-Object { $_.Name -notmatch "key" }).Name "cert.pem"
Rename-Item (Get-ChildItem "*-key.pem").Name "key.pem"
Set-Location ..
```

4. Start the stack again:

```bash
docker compose up --build
```

The frontend container mounts `./certs` and serves HTTPS from those files.

## Access from phone on the same network

1. Find the Wi-Fi IPv4 on Windows (example: `192.168.1.100`):

```powershell
ipconfig
```

2. Make sure the stack is running and the `certs/` folder contains `cert.pem` and `key.pem`.

3. Allow firewall rules on Windows (PowerShell as Administrator):

```powershell
New-NetFirewallRule -DisplayName "salva-backend-8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow -Profile Private
New-NetFirewallRule -DisplayName "salva-frontend-5500" -Direction Inbound -Protocol TCP -LocalPort 5500 -Action Allow -Profile Private
New-NetFirewallRule -DisplayName "salva-frontend-5443" -Direction Inbound -Protocol TCP -LocalPort 5443 -Action Allow -Profile Private
```

4. Install the mkcert root certificate on the phone.

Windows can show the CA folder with:

```powershell
mkcert -CAROOT
```

Copy `rootCA.pem` from that folder to the phone and install it as a trusted certificate.

For iPhone, also enable trust in `Settings > General > About > Certificate Trust Settings`.

5. Test on your phone:

```text
http://192.168.1.23:8080/actuator/health
http://192.168.1.23:5500
https://192.168.1.23:5443
```

Expected behavior:

- `http://<ip>:5500` redirects to `https://<ip>:5443`
- `https://<ip>:5443` serves the React app and uses the proxied HTTPS API path

If the phone still cannot open the app, the usual causes are:

- Windows firewall blocking `5443`
- phone and PC are on different networks or guest isolation is enabled
- the mkcert root CA was installed but not fully trusted on the phone
