# Stego Tool

Stego Tool is a pipeline-based **text steganography** playground with:
- a reusable Java core engine
- an optional Spring Boot API (for integrations / reuse)
- a JavaFX desktop app (offline)
- a modern React website (glass UI)

Crypto is designed to be **client-side** (web uses WebCrypto; desktop uses local code) so passwords never leave the client.

---

## Features

- Pipeline builder: chain reversible steps; decode reverses the chain automatically
- Steps: **Zero-Width** (raw / embed-in-cover), **Base64**, **Emoji**, **Crypto** (AES-256 + PBKDF2)
- Desktop app: offline by default, single-step + pipeline mode
- Web app: clean UI inspired by StegZero's "tool + docs + FAQ" structure
- API: OpenAPI/Swagger docs + consistent error responses (Problem Details)

---

## How It Works (Conceptually)

- Each step is a reversible transformation of text.
- **Encode** runs steps in order.
- **Decode** runs steps in reverse order.
- On the web, everything runs client-side (static site). Crypto never leaves your browser.

---

## How It Works (Under the Hood)

### Modules
- `stego-core`: pure Java pipeline model + step implementations (no UI, no Spring)
- `stego-api`: Spring Boot REST API that maps requests -> `stego-core` pipelines (**rejects CRYPTO**)
- `stego-desktop`: JavaFX client that calls `stego-core` directly (offline)
- `stego-web`: React + TypeScript client (fully static; all steps run in-browser)

### Web flow
- **Encode**: Browser runs steps (including Crypto) -> output
- **Decode**: Browser runs the same steps in reverse -> output

---

## Using the Web App

1. Build your pipeline in the right-hand panel.
2. Paste input text.
3. Click **Encode** or **Decode**.
4. Copy output.

Notes:
- If you add **Crypto**, it stays pinned as the first step (client-side only).
- If you set Zero-Width to "Embed in cover", you must provide cover text.

---

## Running Locally

Full instructions + IntelliJ run configurations: `docs/RUNNING.md`.

### API (Spring Boot)
```powershell
.\mvnw -pl stego-api -am spring-boot:run
```
- API base: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Web (Vite dev server)
```bash
cd stego-web
npm install
npm run dev
```
No backend is required for the web app.

Optional env var for the website:
- `VITE_RELEASES_URL` (used for "Download desktop" buttons)

### Desktop (JavaFX)
```powershell
.\mvnw -pl stego-desktop -am javafx:run
```

---

## Docker / Compose
```bash
docker compose up --build
```
- Web (static): `http://localhost:8081`

Optional (run the API too):
```bash
docker compose -f compose.with-api.yaml up --build
```
- API: `http://localhost:8080`
- Web (static): `http://localhost:8081`

---

## Backend vs Static-Only Website

The web app is now fully static: text transformations and image stego run in your browser.

`stego-api` is still included for optional reuse/integrations and future expansion (e.g., adding a Python image-stego service behind an API gateway).

---

## CI/CD

- `CI` builds/tests Java modules and builds the web app.
- `Release` (tags `v*`) produces:
  - API JAR + GHCR Docker image
  - Web `dist` artifact
  - Desktop artifacts for Windows/macOS/Linux (jlink zip + best-effort installers)

---

## License

MIT License. See `LICENSE`.
