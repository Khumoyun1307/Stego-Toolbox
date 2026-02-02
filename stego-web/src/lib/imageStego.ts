type Bytes = Uint8Array<ArrayBuffer>;

const MAGIC = new Uint8Array([0x53, 0x54, 0x47, 0x30]) as Bytes; // "STG0"
const VERSION = 1;
const HEADER_SIZE = 16;

function writeU32BE(buf: Uint8Array, offset: number, value: number): void {
  buf[offset] = (value >>> 24) & 0xff;
  buf[offset + 1] = (value >>> 16) & 0xff;
  buf[offset + 2] = (value >>> 8) & 0xff;
  buf[offset + 3] = value & 0xff;
}

function readU32BE(buf: Uint8Array, offset: number): number {
  return (
    ((buf[offset] << 24) | (buf[offset + 1] << 16) | (buf[offset + 2] << 8) | buf[offset + 3]) >>>
    0
  );
}

const CRC32_TABLE = (() => {
  const table = new Uint32Array(256);
  for (let i = 0; i < 256; i++) {
    let c = i;
    for (let j = 0; j < 8; j++) {
      c = (c & 1) !== 0 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    }
    table[i] = c >>> 0;
  }
  return table;
})();

function crc32(bytes: Uint8Array): number {
  let c = 0xffffffff;
  for (let i = 0; i < bytes.length; i++) {
    c = CRC32_TABLE[(c ^ bytes[i]) & 0xff] ^ (c >>> 8);
  }
  return (c ^ 0xffffffff) >>> 0;
}

function buildHeader(payload: Uint8Array): Uint8Array {
  const header = new Uint8Array(HEADER_SIZE);
  header.set(MAGIC, 0);
  header[4] = VERSION;
  header[5] = 0;
  header[6] = 0;
  header[7] = 0;
  writeU32BE(header, 8, payload.length >>> 0);
  writeU32BE(header, 12, crc32(payload));
  return header;
}

class RgbLsbBitWriter {
  private idx = 0;

  constructor(private readonly rgba: Uint8ClampedArray) {}

  writeBit(bit: number): void {
    while (this.idx < this.rgba.length && (this.idx & 3) === 3) this.idx++;
    if (this.idx >= this.rgba.length) {
      throw new Error("Image is too small to hold the payload.");
    }
    const b = bit & 1;
    this.rgba[this.idx] = (this.rgba[this.idx] & 0xfe) | b;
    this.idx++;
  }

  writeByte(byte: number): void {
    for (let bit = 7; bit >= 0; bit--) {
      this.writeBit((byte >> bit) & 1);
    }
  }

  writeBytes(bytes: Uint8Array): void {
    for (let i = 0; i < bytes.length; i++) this.writeByte(bytes[i]);
  }
}

class RgbLsbBitReader {
  private idx = 0;

  constructor(private readonly rgba: Uint8ClampedArray) {}

  readBit(): number {
    while (this.idx < this.rgba.length && (this.idx & 3) === 3) this.idx++;
    if (this.idx >= this.rgba.length) {
      throw new Error("Image does not contain a complete payload.");
    }
    return this.rgba[this.idx++] & 1;
  }

  readByte(): number {
    let b = 0;
    for (let i = 0; i < 8; i++) {
      b = (b << 1) | this.readBit();
    }
    return b & 0xff;
  }

  readBytes(count: number): Uint8Array {
    const out = new Uint8Array(count);
    for (let i = 0; i < count; i++) out[i] = this.readByte();
    return out;
  }
}

export function estimatePayloadCapacityBytes(width: number, height: number): number {
  const pixels = width * height;
  const totalBits = pixels * 3;
  const totalBytes = Math.floor(totalBits / 8);
  return Math.max(0, totalBytes - HEADER_SIZE);
}

export async function readImageInfo(file: File): Promise<{ width: number; height: number }> {
  const bmp = await createImageBitmap(file);
  try {
    return { width: bmp.width, height: bmp.height };
  } finally {
    bmp.close();
  }
}

async function loadImageData(file: File): Promise<{
  canvas: HTMLCanvasElement;
  ctx: CanvasRenderingContext2D;
  imageData: ImageData;
}> {
  const bmp = await createImageBitmap(file);
  const canvas = document.createElement("canvas");
  canvas.width = bmp.width;
  canvas.height = bmp.height;

  const ctx = canvas.getContext("2d", { willReadFrequently: true });
  if (!ctx) throw new Error("Canvas is not available in this browser.");

  ctx.drawImage(bmp, 0, 0);
  bmp.close();

  const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
  return { canvas, ctx, imageData };
}

export async function embedBytesAsPng(file: File, payload: Uint8Array): Promise<{
  blob: Blob;
  width: number;
  height: number;
  payloadCapacityBytes: number;
}> {
  const { canvas, ctx, imageData } = await loadImageData(file);
  const payloadCapacityBytes = estimatePayloadCapacityBytes(imageData.width, imageData.height);

  const header = buildHeader(payload);
  const combined = new Uint8Array(header.length + payload.length);
  combined.set(header, 0);
  combined.set(payload, header.length);

  if (payload.length > payloadCapacityBytes) {
    throw new Error(
      `Payload is too large for this image (max ${payloadCapacityBytes} bytes, got ${payload.length}).`,
    );
  }

  const out = new Uint8ClampedArray(imageData.data);
  const writer = new RgbLsbBitWriter(out);
  writer.writeBytes(combined);

  ctx.putImageData(new ImageData(out, imageData.width, imageData.height), 0, 0);

  const blob = await new Promise<Blob>((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error("Failed to export PNG."))), "image/png");
  });

  return { blob, width: imageData.width, height: imageData.height, payloadCapacityBytes };
}

export async function extractBytes(file: File): Promise<Uint8Array> {
  const { imageData } = await loadImageData(file);
  const reader = new RgbLsbBitReader(imageData.data);

  const header = reader.readBytes(HEADER_SIZE);
  for (let i = 0; i < MAGIC.length; i++) {
    if (header[i] !== MAGIC[i]) {
      throw new Error("No embedded payload found in this image.");
    }
  }
  const version = header[4];
  if (version !== VERSION) {
    throw new Error(`Unsupported payload version (${version}).`);
  }

  const payloadLen = readU32BE(header, 8);
  const expectedCrc = readU32BE(header, 12);

  const payloadCapacityBytes = estimatePayloadCapacityBytes(imageData.width, imageData.height);
  if (payloadLen > payloadCapacityBytes) {
    throw new Error("Embedded payload length is invalid for this image.");
  }

  const payload = reader.readBytes(payloadLen);
  const actualCrc = crc32(payload);
  if (actualCrc !== expectedCrc) {
    throw new Error("Embedded payload checksum mismatch.");
  }

  return payload;
}

