const ITERATIONS = 65536;
const KEY_LENGTH = 256;
const SALT_LENGTH = 16;
const IV_LENGTH = 16;

type Bytes = Uint8Array<ArrayBuffer>;

function utf8Encode(input: string): Bytes {
  return new TextEncoder().encode(input) as Bytes;
}

function utf8Decode(bytes: Bytes): string {
  return new TextDecoder().decode(bytes);
}

function b64Encode(bytes: Bytes): string {
  let binary = "";
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

function b64Decode(b64: string): Bytes {
  const binary = atob(b64);
  const out: Bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    out[i] = binary.charCodeAt(i);
  }
  return out;
}

async function deriveAesKey(password: string, salt: Bytes): Promise<CryptoKey> {
  const keyMaterial = await crypto.subtle.importKey(
    "raw",
    utf8Encode(password),
    "PBKDF2",
    false,
    ["deriveKey"],
  );
  return crypto.subtle.deriveKey(
    {
      name: "PBKDF2",
      salt,
      iterations: ITERATIONS,
      hash: "SHA-256",
    },
    keyMaterial,
    { name: "AES-CBC", length: KEY_LENGTH },
    false,
    ["encrypt", "decrypt"],
  );
}

function randomBytes(length: number): Bytes {
  const bytes: Bytes = new Uint8Array(length);
  crypto.getRandomValues(bytes);
  return bytes;
}

export async function cryptoEncrypt(plainText: string, password: string): Promise<string> {
  if (!password) {
    throw new Error("Password is required for CRYPTO.");
  }
  const salt = randomBytes(SALT_LENGTH);
  const iv = randomBytes(IV_LENGTH);
  const key = await deriveAesKey(password, salt);

  const ciphertext: Bytes = new Uint8Array(
    await crypto.subtle.encrypt({ name: "AES-CBC", iv }, key, utf8Encode(plainText)),
  );

  return `${b64Encode(salt)}:${b64Encode(iv)}:${b64Encode(ciphertext)}`;
}

export async function cryptoDecrypt(cipherText: string, password: string): Promise<string> {
  if (!password) {
    throw new Error("Password is required for CRYPTO.");
  }
  const parts = cipherText.split(":");
  if (parts.length !== 3) {
    throw new Error("Invalid encrypted format (expected salt:iv:ciphertext).");
  }

  const salt = b64Decode(parts[0]);
  const iv = b64Decode(parts[1]);
  const data = b64Decode(parts[2]);

  const key = await deriveAesKey(password, salt);

  const plainBytes: Bytes = new Uint8Array(
    await crypto.subtle.decrypt({ name: "AES-CBC", iv }, key, data),
  );

  return utf8Decode(plainBytes);
}
