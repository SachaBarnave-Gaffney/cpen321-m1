# CPEN 321 - M1 (Sacha Barnave-Gaffney)

Android app + Node/TypeScript backend for Milestone 1.

- **Deployed backend:** https://8-229-164-95.sslip.io (public IP `8.229.164.95`)
  The app in the submitted APK talks to this server; it is running and does not need to be redeployed to grade the APK.
- **Screens:** (1) Google sign-in + server/client info, (2) live 16x16 pixel art over WebSocket, (3) countdown timer that reveals a random fact with confetti.

_Keep this README up to date with the steps required to build and run the frontend and backend (including any scripts, config files, and environment variables). TAs ill follow these instructions._

## Requirements

Install the following before the frontend or backend setup steps:

- [git](https://git-scm.com/install/)


--- 

## Frontend Setup

### Requirements

- [Android Studio](https://developer.android.com/studio) (latest version)
- [Java 17](https://adoptium.net/temurin/releases/?version=17)
- [Android SDK](https://developer.android.com/studio#command-tools) with API level 36+ (Android 16)

### Setup

1. **Open project**: Open the `frontend/` directory in Android Studio
2. **Sync Gradle**: Android Studio will automatically prompt you to sync the project. Click "Sync Now". You can also manually run `cd frontend && ./gradlew build` to trigger the sync and download the necessary dependencies.
3. **Configure Android SDK**: Ensure you have Android SDK 36 installed.
4. **Set up emulator/device**:
   - Create a new AVD (Android Virtual Device) by selecting Pixel 9 as the device and Android Baklava (API level 36) as the system image.
   - Alternatively, connect a physical Android device running Android 16 (API level 36).
5. **Setup app config**: Copy the example file, then fill in local values:
   ```bash
   cp frontend/local.properties.example frontend/local.properties
   ```
   Set at least:
   - `sdk.dir`: path to your Android SDK. Android Studio usually writes this the first time you open `frontend/`. On Mac it is often `sdk.dir=/Users/<username>/Library/Android/sdk`.
   - `API_BASE_URL`: backend URL baked into the APK.
     - To use the **deployed** server (recommended): `API_BASE_URL=https://8-229-164-95.sslip.io`
     - To use a **local** backend: `http://10.0.2.2:3000` from the emulator (`10.0.2.2` is the host machine), or `http://<your-lan-ip>:3000` from a physical device.
   - `GOOGLE_CLIENT_ID`: the **Web** OAuth client ID used by Credential Manager for Google sign-in:
     `602415782819-gtid9l5fb5g099elbn8lf20u2at247c4.apps.googleusercontent.com`

### Google sign-in notes

- The emulator/device must use a **Google Play** system image and have a Google account added
  (Settings > Passwords & accounts > Add account > Google), otherwise sign-in reports
  "No credentials available".
- The OAuth consent screen is in **Testing** mode, so only registered test accounts can sign in.
  A test account for graders is listed in `M1_Doc.pdf`.
- `frontend/app/debug.keystore` is committed on purpose so every debug build shares one SHA-1,
  which is registered with the OAuth client. Do not replace it, or Google sign-in will fail.
  Its password/alias are the Android defaults (`android` / `androiddebugkey`).


### Build and Run

- **Debug build**: Click the green play button in the toolbar, to compile the code, package a debug APK, and install it on the connected device or running emulator. Alternatively, from the project root, run `./scripts/run-frontend.sh`.
- **Release build**: Go to Build -> Generate Signed App Bundle or APK -> APK. Follow the on-screen instructions to create a key, and select the "release" build variant. You will then have to manually install the generated APK on your device or the running emulator.


### Backend Configuration

Ensure the backend server is running and update the base URL in the app configuration if needed.

---
## Backend Setup

You can run the backend in one of two ways:
* Locally via Node.js 
* Via Docker Compose

Both ways use the same `backend/.env` file (see below).

### Environment configuration

From the project root:

```bash
cp backend/.env.example backend/.env
```

Set at least:
- `MY_FIRST_NAME` / `MY_LAST_NAME`: returned by `GET /api/name`.
- `SERVER_PUBLIC_IP`: the public IP of the machine running the backend, returned by `GET /api/ip`
  (the deployed server uses `8.229.164.95`).
- `PIXEL_UPSTREAM_URL` (optional): the course pixel WebSocket server. Defaults to `wss://8.229.22.124`.
- `PORT` (optional): defaults to `3000` if unset.
- `JWT_SECRET` / `MONGODB_URI`: unused in M1 (no database or tokens yet); leave the example values.

No other secrets are required.

### Backend endpoints (M1)

| Endpoint | Returns |
| --- | --- |
| `GET /health` | `{"status":"ok"}` |
| `GET /api/ip` | `{"serverIp","clientIp"}` - server public IP and the caller's IP |
| `GET /api/time` | `{"serverTime"}` - server local time as `hh:mm:ss GMT+hh:mm` |
| `GET /api/name` | `{"firstName","lastName"}` |
| `WS /pixels` | Relays each pixel update from the course server unchanged |

The server's time zone determines the GMT offset shown by `/api/time`; the deployed VM is set to
`America/Vancouver` (`sudo timedatectl set-timezone America/Vancouver`).


### Option 1: Run locally

**Requirements:** 
- [Node.js](https://nodejs.org/en/download/) 22+
- [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm) 10+

**Setup:** 
1. Install dependencies:

   ```bash
   cd backend
   npm install
   ```

2. **Development** (TypeScript with auto-reload):

   ```bash
   npm run dev
   ```

3. **Production build** (optional):

   ```bash
   npm run build
   npm start
   ```

### Option 2: Run with Docker Compose

**Requirements:** 
- [Docker](https://docs.docker.com/desktop/setup/install) and [Docker Compose](https://docs.docker.com/desktop/setup/install) v2.24+
- [curl](https://curl.se/download.html)

**Setup**
1. **Start** (from the project root):

   ```bash
   ./scripts/run-backend.sh
   ```

   Or run Compose directly:

   ```bash
   docker compose up --build -d
   ```

2. **Stop**:

   ```bash
   docker compose down
   ```

## Additional Setup

### How the deployed backend runs (for reference)

Debian 13 VM on Google Compute Engine, `us-west1`, static IP `8.229.164.95`:

1. `sudo apt install -y git caddy` and Node.js 22, plus `sudo npm install -g pm2`
2. `git clone` this repo, then `cd backend && npm install && npm run build`
3. Create `backend/.env` as described above
4. `pm2 start dist/index.js --name m1 && pm2 save && pm2 startup`
5. Caddy terminates TLS and proxies to the backend, which also upgrades the `/pixels` WebSocket:

   ```
   8-229-164-95.sslip.io {
       reverse_proxy localhost:3000
   }
   ```

`*.sslip.io` resolves to the IP embedded in the hostname, which lets Let's Encrypt issue a real
certificate for the server without owning a domain, so the Android app needs no custom trust
configuration.