# Running & IntelliJ Setup

## Prerequisites
- JDK 17+
- Node.js 20+ (for `stego-web`)
- Optional: Docker Desktop (for `docker compose`)

## Run (terminal)

### API (Spring Boot)
From repo root:
```powershell
.\mvnw -pl stego-api -am spring-boot:run
```
- API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

If you prefer running the built JAR:
```powershell
.\mvnw -pl stego-api -am -DskipTests package
java -jar .\stego-api\target\stego-api-1.0.0-SNAPSHOT.jar
```

### Web (Vite dev server)
```powershell
cd stego-web
npm install
npm run dev
```
No backend is required for the web app.

Optional env var used by the site:
- `VITE_RELEASES_URL` (link for the desktop download buttons)

### Desktop (JavaFX)
```powershell
.\mvnw -pl stego-desktop -am javafx:run
```
If JavaFX classifier resolution is wrong for your OS, force a profile:
- Windows: `-P windows`
- macOS: `-P mac`
- Linux: `-P linux`

## Run (Docker)
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

## IntelliJ IDEA run configurations

### Import
1) **File → Open** the repo root folder.
2) Use **JDK 17** for the Maven project (Project Structure → Project SDK).
3) Let IntelliJ import all Maven modules (`stego-core`, `stego-api`, `stego-desktop`).

### API (recommended: Spring Boot config)
Run/Debug Configurations → **Add New…** → **Spring Boot**
- **Main class**: `com.yourorg.stegoapp.api.StegoApiApplication`
- **Use classpath of module**: `stego-api`
- Optional env var (CORS): `STEGO_CORS_ALLOWED_ORIGINS=http://localhost:5173`

### Web (npm config)
Run/Debug Configurations → **Add New…** → **npm**
- **package.json**: `stego-web/package.json`
- **Command**: `run`
- **Scripts**: `dev`

### Desktop (recommended: Maven config)
Run/Debug Configurations → **Add New…** → **Maven**
- **Working directory**: repo root
- **Command line**: `-pl stego-desktop -am javafx:run`

### Run everything together
Run/Debug Configurations → **Add New…** → **Compound**
- Add: `Web (npm)` (+ optionally `API (Spring Boot)` / `Desktop`)

## About the root `src/`, `resources/`, `test/` folders
Those are legacy from the pre-Maven layout and are not used by the multi-module build.
For a clean project/IDE experience, delete them (or mark them **Excluded** in IntelliJ).

## Tests & coverage

### Java (unit + integration)
Run core + API unit tests and integration tests:
```powershell
.\mvnw -pl stego-core,stego-api verify
```

Run desktop tests (resource sanity checks; does not launch JavaFX):
```powershell
.\mvnw -pl stego-desktop test
```

### JaCoCo reports
After running `verify`/`test`, open the HTML reports:
- `stego-core/target/site/jacoco/index.html`
- `stego-api/target/site/jacoco/index.html`
- `stego-desktop/target/site/jacoco/index.html`

To generate a combined aggregate report, run the full reactor:
```powershell
.\mvnw verify
```
Then open:
- `target/site/jacoco-aggregate/index.html`
