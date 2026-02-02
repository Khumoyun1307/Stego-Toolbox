# Architecture

## Modules

### `stego-core`
Pure Java domain + engine:
- Pipeline model (`Pipeline`, `StepConfig`, typed options)
- Step implementations (Zero-Width, Base64, Emoji, Crypto)
- Validation + stable error codes

No Spring and no JavaFX.

### `stego-api`
Spring Boot REST API:
- Exposes text encode/decode endpoints
- Maps request DTOs -> `stego-core` pipeline
- Returns consistent Problem Details errors
- Rejects `CRYPTO` (client-side only)

### `stego-desktop`
JavaFX desktop client:
- Offline by default
- Single-step mode + pipeline mode
- UI calls `stego-core` engine; no stego rules in controllers

### `stego-web`
React + TypeScript website:
- Fully static: runs all steps locally (including Crypto via WebCrypto)
- Includes a client-side image stego tool

## Data flow

### Web
- **Encode**: Browser runs steps -> output
- **Decode**: Browser runs steps in reverse -> output

No backend is required, so secrets never leave the client.

## Future: image steganography

If you later want server-side image processing (Python):
- Add a new `image-stego-service/` (Python) for image ops
- Use `stego-api` as a gateway (routes text -> `stego-core`, image -> Python service)
