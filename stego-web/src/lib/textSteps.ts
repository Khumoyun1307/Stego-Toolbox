import type { ApiPipelineStep, ZeroWidthMode } from "./types";

type Bytes = Uint8Array<ArrayBuffer>;

const EMOJI_MAP = [
  "😀",
  "😁",
  "😂",
  "😃",
  "😄",
  "😅",
  "😆",
  "😉",
  "😊",
  "😋",
  "😎",
  "😍",
  "😘",
  "😗",
  "😙",
  "😚",
];

const EMOJI_REVERSE = new Map<string, number>(EMOJI_MAP.map((e, i) => [e, i]));

const ZW_SPACE = "\u200B";
const ZW_NON_JOINER = "\u200C";

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

export function base64Encode(input: string): string {
  return b64Encode(utf8Encode(input));
}

export function base64Decode(input: string): string {
  return utf8Decode(b64Decode(input));
}

export function emojiEncode(input: string): string {
  const data = utf8Encode(input);
  let out = "";
  for (let i = 0; i < data.length; i++) {
    const b = data[i];
    const high = (b >> 4) & 0x0f;
    const low = b & 0x0f;
    out += EMOJI_MAP[high] + EMOJI_MAP[low];
  }
  return out;
}

export function emojiDecode(input: string): string {
  const emojis = Array.from(input);
  if (emojis.length % 2 !== 0) {
    throw new Error("Invalid emoji sequence.");
  }

  const result: Bytes = new Uint8Array(emojis.length / 2);
  for (let i = 0; i < emojis.length; i += 2) {
    const high = EMOJI_REVERSE.get(emojis[i]);
    const low = EMOJI_REVERSE.get(emojis[i + 1]);
    if (high === undefined || low === undefined) {
      throw new Error("Invalid emoji sequence.");
    }
    result[i / 2] = ((high << 4) | low) & 0xff;
  }

  return utf8Decode(result);
}

export function zeroWidthEncode(input: string): string {
  const data = utf8Encode(input);
  const out: string[] = [];
  for (let byteIdx = 0; byteIdx < data.length; byteIdx++) {
    const b = data[byteIdx];
    for (let bit = 7; bit >= 0; bit--) {
      out.push(((b >> bit) & 1) === 1 ? ZW_NON_JOINER : ZW_SPACE);
    }
  }
  return out.join("");
}

function filterZeroWidth(input: string): string {
  const out: string[] = [];
  for (const ch of input) {
    if (ch === ZW_SPACE || ch === ZW_NON_JOINER) out.push(ch);
  }
  return out.join("");
}

export function zeroWidthDecode(input: string): string {
  const filtered = filterZeroWidth(input);

  if (filtered.length % 8 !== 0) {
    throw new Error(
      `Encoded string length must be a multiple of 8, but was ${filtered.length}`,
    );
  }

  const bytes: Bytes = new Uint8Array(filtered.length / 8);
  for (let byteIdx = 0; byteIdx < bytes.length; byteIdx++) {
    let b = 0;
    for (let bit = 0; bit < 8; bit++) {
      const c = filtered.charAt(byteIdx * 8 + bit);
      b <<= 1;
      if (c === ZW_NON_JOINER) b |= 1;
    }
    bytes[byteIdx] = b & 0xff;
  }

  return utf8Decode(bytes);
}

function encodeZeroWidthWithMode(input: string, mode?: ZeroWidthMode, coverText?: string): string {
  if (mode === "EMBED_IN_COVER") {
    return `${coverText ?? ""}${zeroWidthEncode(input)}`;
  }
  return zeroWidthEncode(input);
}

export function applyApiEncode(input: string, pipeline: ApiPipelineStep[]): string {
  let text = input;
  for (const step of pipeline) {
    switch (step.type) {
      case "BASE64":
        text = base64Encode(text);
        break;
      case "EMOJI":
        text = emojiEncode(text);
        break;
      case "ZERO_WIDTH":
        text = encodeZeroWidthWithMode(text, step.zeroWidthMode, step.coverText);
        break;
    }
  }
  return text;
}

export function applyApiDecode(input: string, pipeline: ApiPipelineStep[]): string {
  let text = input;
  for (let i = pipeline.length - 1; i >= 0; i--) {
    const step = pipeline[i];
    switch (step.type) {
      case "BASE64":
        text = base64Decode(text);
        break;
      case "EMOJI":
        text = emojiDecode(text);
        break;
      case "ZERO_WIDTH":
        text = zeroWidthDecode(text);
        break;
    }
  }
  return text;
}

