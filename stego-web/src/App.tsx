import { useEffect, useRef, useState } from "react";
import { apiDecode, apiEncode } from "./lib/api";
import { cryptoDecrypt, cryptoEncrypt } from "./lib/crypto";
import { embedBytesAsPng, estimatePayloadCapacityBytes, extractBytes, readImageInfo } from "./lib/imageStego";
import type {
  ApiPipelineStep,
  Mode,
  PipelineStep,
  StepType,
  ZeroWidthMode,
} from "./lib/types";

const MAX_STEPS = 5;

const DEFAULT_PIPELINE: PipelineStep[] = [
  { id: crypto.randomUUID(), type: "ZERO_WIDTH", mode: "RAW", coverText: "" },
];

const RELEASES_URL =
  import.meta.env.VITE_RELEASES_URL ?? "https://github.com/REPLACE_ME/REPLACE_ME/releases/latest";
const REPO_URL = RELEASES_URL.replace(/\/releases\/latest$/, "");

function stepLabel(type: StepType): string {
  switch (type) {
    case "CRYPTO":
      return "Crypto (AES)";
    case "BASE64":
      return "Base64";
    case "EMOJI":
      return "Emoji";
    case "ZERO_WIDTH":
      return "Zero-Width";
  }
}

function createPipelineStep(type: StepType, id: string): PipelineStep {
  switch (type) {
    case "CRYPTO":
      return { id, type: "CRYPTO", password: "" };
    case "ZERO_WIDTH":
      return { id, type: "ZERO_WIDTH", mode: "RAW", coverText: "" };
    case "BASE64":
      return { id, type: "BASE64" };
    case "EMOJI":
      return { id, type: "EMOJI" };
  }
}

function toApiSteps(pipeline: PipelineStep[]): ApiPipelineStep[] {
  const isNonCrypto = (s: PipelineStep): s is Exclude<PipelineStep, { type: "CRYPTO" }> =>
    s.type !== "CRYPTO";

  return pipeline.filter(isNonCrypto).map((s) => {
    if (s.type === "ZERO_WIDTH" && s.mode === "EMBED_IN_COVER") {
      return { type: "ZERO_WIDTH", zeroWidthMode: "EMBED_IN_COVER", coverText: s.coverText };
    }
    return { type: s.type };
  });
}

async function copyToClipboard(text: string): Promise<void> {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text);
    return;
  }
  const ta = document.createElement("textarea");
  ta.value = text;
  document.body.appendChild(ta);
  ta.select();
  document.execCommand("copy");
  document.body.removeChild(ta);
}

type ProblemDetailLike = {
  type?: unknown;
  title?: unknown;
  status?: unknown;
  detail?: unknown;
  instance?: unknown;
};

function parseProblemDetailMessage(message: string): string | null {
  const trimmed = message.trim();
  if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return null;

  try {
    const parsed: unknown = JSON.parse(trimmed);
    if (!parsed || typeof parsed !== "object") return null;
    const pd = parsed as ProblemDetailLike;
    if (typeof pd.detail === "string" && pd.detail.trim()) return pd.detail;
    if (typeof pd.title === "string" && pd.title.trim()) return pd.title;
    return null;
  } catch {
    return null;
  }
}

function getUserFacingErrorMessage(err: unknown): string {
  const fallback = "Something went wrong.";

  if (err && typeof err === "object") {
    const obj = err as { message?: unknown; detail?: unknown; title?: unknown };
    if (typeof obj.detail === "string" && obj.detail.trim()) return obj.detail;
    if (typeof obj.title === "string" && obj.title.trim()) return obj.title;

    if (typeof obj.message === "string") {
      const msg = obj.message.trim();
      if (!msg) return fallback;
      return parseProblemDetailMessage(msg) ?? msg;
    }
  }

  if (typeof err === "string") return err.trim() || fallback;
  return fallback;
}

export default function App() {
  const [mode, setMode] = useState<Mode>("encode");
  const [input, setInput] = useState("");
  const [output, setOutput] = useState("");
  const [pipeline, setPipeline] = useState<PipelineStep[]>(DEFAULT_PIPELINE);
  const [status, setStatus] = useState<{ kind: "idle" | "ok" | "error"; message?: string }>({
    kind: "idle",
  });
  const [running, setRunning] = useState(false);
  const [showScrollTop, setShowScrollTop] = useState(false);
  const [navOpen, setNavOpen] = useState(false);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imageInfo, setImageInfo] = useState<{ width: number; height: number } | null>(null);
  const [imagePreviewUrl, setImagePreviewUrl] = useState("");
  const [imageOutputUrl, setImageOutputUrl] = useState("");
  const [imageInput, setImageInput] = useState("");
  const [imageOutput, setImageOutput] = useState("");
  const [imageStatus, setImageStatus] = useState<{ kind: "idle" | "ok" | "error"; message?: string }>({
    kind: "idle",
  });
  const [imageMode, setImageMode] = useState<Mode>("encode");
  const [imageRunning, setImageRunning] = useState(false);
  const [imageDragActive, setImageDragActive] = useState(false);
  const imageInputRef = useRef<HTMLInputElement | null>(null);

  const stepLimitReached = pipeline.length >= MAX_STEPS;

  useEffect(() => {
    if (!imageFile) {
      setImagePreviewUrl("");
      return;
    }

    const url = URL.createObjectURL(imageFile);
    setImagePreviewUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [imageFile]);

  useEffect(() => {
    if (!imageOutputUrl) return;
    const url = imageOutputUrl;
    return () => URL.revokeObjectURL(url);
  }, [imageOutputUrl]);

  useEffect(() => {
    if (!imageFile) {
      setImageInfo(null);
      return;
    }

    let cancelled = false;
    void readImageInfo(imageFile)
      .then((info) => {
        if (cancelled) return;
        setImageInfo(info);
      })
      .catch((e) => {
        if (cancelled) return;
        setImageInfo(null);
        setImageStatus({ kind: "error", message: getUserFacingErrorMessage(e) });
      });

    return () => {
      cancelled = true;
    };
  }, [imageFile]);

  useEffect(() => {
    setImageStatus({ kind: "idle" });
    setImageOutputUrl("");
    setImageOutput("");
  }, [imageMode]);

  useEffect(() => {
    function onScroll() {
      setShowScrollTop(window.scrollY > 600);
    }

    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    if (!navOpen) return;

    function onKeyDown(e: KeyboardEvent) {
      if (e.key === "Escape") setNavOpen(false);
    }

    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [navOpen]);

  useEffect(() => {
    document.body.style.overflow = navOpen ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [navOpen]);

  useEffect(() => {
    function onResize() {
      if (window.innerWidth > 768) setNavOpen(false);
    }

    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, []);

  function selectImageFile(file: File | null) {
    setImageStatus({ kind: "idle" });
    setImageOutputUrl("");
    setImageOutput("");
    setImageFile(file);
  }

  async function runTextTransform(runMode: Mode, startText: string, pipelineSteps: PipelineStep[]): Promise<string> {
    if (pipelineSteps.length === 0) {
      throw new Error("Add at least one step.");
    }

    let text = startText;
    const steps = runMode === "encode" ? pipelineSteps : pipelineSteps.slice().reverse();
    const phase = runMode === "encode" ? "Encode" : "Decode";

    for (const step of steps) {
      try {
        if (step.type === "CRYPTO") {
          if (!step.password) {
            throw new Error("Password is required for Crypto.");
          }
          text = runMode === "encode"
            ? await cryptoEncrypt(text, step.password)
            : await cryptoDecrypt(text, step.password);
          continue;
        }

        if (
          runMode === "encode" &&
          step.type === "ZERO_WIDTH" &&
          step.mode === "EMBED_IN_COVER" &&
          !step.coverText.trim()
        ) {
          throw new Error('Cover text is required when Zero-Width is set to "Embed in cover".');
        }

        const apiStep = toApiSteps([step]);
        const res =
          runMode === "encode"
            ? await apiEncode({ text, pipeline: apiStep })
            : await apiDecode({ text, pipeline: apiStep });
        text = res.result;
      } catch (e) {
        throw new Error(`${phase} failed at ${stepLabel(step.type)}: ${getUserFacingErrorMessage(e)}`);
      }
    }

    return text;
  }

  async function onRun() {
    if (running) return;
    setStatus({ kind: "idle" });
    setOutput("");
    setRunning(true);
    try {
      const text = await runTextTransform(mode, input, pipeline);
      setOutput(text);
      setStatus({ kind: "ok", message: mode === "encode" ? "Encoded successfully." : "Decoded successfully." });
    } catch (e) {
      setStatus({ kind: "error", message: getUserFacingErrorMessage(e) });
    } finally {
      setRunning(false);
    }
  }

  async function onImageRun() {
    if (imageRunning) return;
    setImageStatus({ kind: "idle" });
    setImageOutputUrl("");
    setImageOutput("");
    setImageRunning(true);

    try {
      if (!imageFile) {
        throw new Error("Choose an image first.");
      }

      if (imageMode === "encode") {
        if (!imageInput.trim()) {
          throw new Error("Enter a message to hide.");
        }

        const transformed = await runTextTransform("encode", imageInput, pipeline);
        const payload = new TextEncoder().encode(transformed);
        const res = await embedBytesAsPng(imageFile, payload);
        setImageOutputUrl(URL.createObjectURL(res.blob));
        setImageStatus({ kind: "ok", message: "Image encoded successfully." });
        return;
      }

      const payload = await extractBytes(imageFile);
      const extracted = new TextDecoder().decode(payload);
      const decoded = await runTextTransform("decode", extracted, pipeline);
      setImageOutput(decoded);
      setImageStatus({ kind: "ok", message: "Message extracted." });
    } catch (e) {
      setImageStatus({ kind: "error", message: getUserFacingErrorMessage(e) });
    } finally {
      setImageRunning(false);
    }
  }

  function addStep(type: StepType) {
    setPipeline((prev) => {
      if (prev.length >= MAX_STEPS) return prev;
      const id = crypto.randomUUID();
      return [...prev, createPipelineStep(type, id)];
    });
  }

  function removeStep(id: string) {
    setPipeline((prev) => prev.filter((s) => s.id !== id));
  }

  function moveStep(id: string, dir: -1 | 1) {
    setPipeline((prev) => {
      if (prev.length < 2) return prev;
      const idx = prev.findIndex((s) => s.id === id);
      if (idx < 0) return prev;
      const nextIdx = idx + dir;

      const copy = prev.slice();

      if (dir === -1) {
        if (idx === 0) {
          const [item] = copy.splice(0, 1);
          copy.push(item);
        } else {
          const tmp = copy[idx - 1];
          copy[idx - 1] = copy[idx];
          copy[idx] = tmp;
        }
        return copy;
      }

      if (nextIdx >= copy.length) {
        const [item] = copy.splice(copy.length - 1, 1);
        copy.unshift(item);
        return copy;
      }

      const tmp = copy[idx + 1];
      copy[idx + 1] = copy[idx];
      copy[idx] = tmp;
      return copy;
    });
  }

  function updateZeroWidth(id: string, patch: Partial<{ mode: ZeroWidthMode; coverText: string }>) {
    setPipeline((prev) =>
      prev.map((s) => {
        if (s.id !== id || s.type !== "ZERO_WIDTH") return s;
        return {
          ...s,
          mode: patch.mode ?? s.mode,
          coverText: patch.coverText ?? s.coverText,
        };
      }),
    );
  }

  function updateCryptoPassword(id: string, password: string) {
    setPipeline((prev) =>
      prev.map((s) => (s.id === id && s.type === "CRYPTO" ? { ...s, password } : s)),
    );
  }

  function AddStepDropdown() {
    const [open, setOpen] = useState(false);
    const rootRef = useRef<HTMLDivElement | null>(null);
    const stepLimitNote = `Max ${MAX_STEPS} steps. Remove one to add another.`;

    useEffect(() => {
      if (stepLimitReached && open) setOpen(false);
    }, [open, stepLimitReached]);

    useEffect(() => {
      function onDocMouseDown(e: MouseEvent) {
        if (!open) return;
        const root = rootRef.current;
        if (!root) return;
        if (e.target instanceof Node && root.contains(e.target)) return;
        setOpen(false);
      }

      function onDocKeyDown(e: KeyboardEvent) {
        if (!open) return;
        if (e.key === "Escape") setOpen(false);
      }

      document.addEventListener("mousedown", onDocMouseDown);
      document.addEventListener("keydown", onDocKeyDown);
      return () => {
        document.removeEventListener("mousedown", onDocMouseDown);
        document.removeEventListener("keydown", onDocKeyDown);
      };
    }, [open]);

    function pick(type: StepType) {
      if (stepLimitReached) return;
      addStep(type);
      setOpen(false);
    }

    return (
      <div className="dropdown" ref={rootRef}>
        <button
          type="button"
          className="dropdown-btn"
          onClick={() => {
            if (stepLimitReached) return;
            setOpen((v) => !v);
          }}
          aria-expanded={open}
          aria-disabled={stepLimitReached}
          title={stepLimitReached ? stepLimitNote : undefined}
        >
          <span>{stepLimitReached ? `Step limit (${MAX_STEPS})` : "+ Add step"}</span>
          <span className="dropdown-chevron" aria-hidden="true">
            {"\u25BE"}
          </span>
        </button>

        {open && (
          <div className="dropdown-menu" role="menu" aria-label="Add step">
            <button type="button" className="dropdown-item" onClick={() => pick("CRYPTO")} role="menuitem">
              Crypto (client-side)
            </button>
            <button type="button" className="dropdown-item" onClick={() => pick("BASE64")} role="menuitem">
              Base64
            </button>
            <button type="button" className="dropdown-item" onClick={() => pick("EMOJI")} role="menuitem">
              Emoji
            </button>
            <button type="button" className="dropdown-item" onClick={() => pick("ZERO_WIDTH")} role="menuitem">
              Zero-Width
            </button>
          </div>
        )}
      </div>
    );
  }

  return (
    <>
      <header className="nav">
        <div className="container nav-inner">
          <a className="brand" href="#tool" aria-label="Stego Tool Home" onClick={() => setNavOpen(false)}>
            <span className="brand-mark" aria-hidden="true" />
            Stego Tool
          </a>

          <button
            type="button"
            className="nav-toggle"
            onClick={() => setNavOpen((v) => !v)}
            aria-label={navOpen ? "Close menu" : "Open menu"}
            aria-expanded={navOpen}
            aria-controls="primary-nav"
          >
            {navOpen ? "\u2715" : "\u2630"}
          </button>

          <nav className="nav-links" id="primary-nav" data-open={navOpen} aria-label="Primary">
            <a href="#tool" onClick={() => setNavOpen(false)}>
              Toolbox
            </a>
            <a href="#how" onClick={() => setNavOpen(false)}>
              How it works
            </a>
            <a href="#steps" onClick={() => setNavOpen(false)}>
              Steps
            </a>
            <a href="#desktop" onClick={() => setNavOpen(false)}>
              Desktop
            </a>
            <a href="#author" onClick={() => setNavOpen(false)}>
              Author
            </a>
          </nav>
        </div>
      </header>

      <div className="nav-overlay" data-open={navOpen} onClick={() => setNavOpen(false)} aria-hidden={!navOpen} />

      <main className="page">
        <section className="hero">
          <div className="container">
            <h1>Steganography Toolbox</h1>
              <p>
                Encode and decode hidden messages using <b>Zero-Width</b>, <b>Base64</b>, <b>Emoji</b>, and{" "}
                <b>Crypto</b>.
              </p>
            </div>
          </section>

          <section className="anchor" id="tool">
            <div className="container grid">
              <div className="card tool-card">
                <div className="row card-head">
                  <h2>Toolbox</h2>
                  <div className="seg" role="tablist" aria-label="Mode">
                    <button
                      type="button"
                      onClick={() => setMode("encode")}
                    data-active={mode === "encode"}
                    role="tab"
                    aria-selected={mode === "encode"}
                  >
                    Encode
                  </button>
                  <button
                    type="button"
                    onClick={() => setMode("decode")}
                    data-active={mode === "decode"}
                    role="tab"
                    aria-selected={mode === "decode"}
                  >
                    Decode
                  </button>
                </div>
              </div>

                <div className="field field-top">
                  <label htmlFor="input">Input</label>
                  <textarea
                    id="input"
                    className="tool-textarea"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder={mode === "encode" ? "Enter message to encode..." : "Paste text to decode..."}
                  />
                </div>

              <div className="row">
                <button className="btn btn-primary" type="button" onClick={onRun}>
                  {running ? "Running..." : mode === "encode" ? "Encode" : "Decode"}
                </button>
                <button
                  className="btn"
                  type="button"
                  onClick={() => {
                    setInput("");
                    setOutput("");
                    setStatus({ kind: "idle" });
                  }}
                >
                  Clear
                </button>
                <button
                  className="btn"
                  type="button"
                  onClick={async () => {
                    if (!output) return;
                    try {
                      await copyToClipboard(output);
                      setStatus({ kind: "ok", message: "Copied to clipboard." });
                    } catch (e) {
                      setStatus({ kind: "error", message: getUserFacingErrorMessage(e) });
                    }
                  }}
                >
                  Copy output
                </button>
              </div>

              <div className="status-block">
                {status.kind === "error" && <div className="error">{status.message}</div>}
                {status.kind === "ok" && <div className="ok">{status.message}</div>}
              </div>

                <div className="field field-top">
                  <label htmlFor="output">Output</label>
                  <textarea
                    id="output"
                    className="tool-textarea"
                    value={output}
                    readOnly
                    placeholder="Result will appear here..."
                  />
                </div>
              </div>

            <div className="card pipeline-card">
              <div className="row card-head">
                <h2>Pipeline</h2>
                <div className="row row-tight">
                  <AddStepDropdown />
                </div>
              </div>

              <p className="muted meta-line">
                Build a sequence of reversible steps. Use the arrows to change the step order.
              </p>

              <div
                className="step-list"
                role="list"
              >
                {pipeline.map((s) => (
                  <div key={s.id} className="step" role="listitem">
                    <div className="step-head">
                      <div className="step-title">{stepLabel(s.type)}</div>
                      <div className="step-actions">
                        <button
                          className="btn mini icon"
                          type="button"
                          onClick={() => moveStep(s.id, -1)}
                          aria-label="Move up"
                        >
                          {"\u2191"}
                        </button>
                        <button
                          className="btn mini icon"
                          type="button"
                          onClick={() => moveStep(s.id, 1)}
                          aria-label="Move down"
                        >
                          {"\u2193"}
                        </button>
                        <button
                          className="btn btn-danger mini"
                          type="button"
                          onClick={() => removeStep(s.id)}
                          aria-label="Remove step"
                        >
                          Remove
                        </button>
                      </div>
                    </div>

                    <div className="step-body">
                      {s.type === "CRYPTO" && (
                        <>
                          <div className="field">
                            <label>Password</label>
                            <input
                              type="password"
                              value={s.password}
                              onChange={(e) => updateCryptoPassword(s.id, e.target.value)}
                              placeholder="Enter password..."
                            />
                          </div>
                        </>
                      )}

                      {s.type === "ZERO_WIDTH" && (
                        <>
                          <div className="field">
                            <label>Mode</label>
                            <div className="row">
                              <button
                                className="btn"
                                type="button"
                                onClick={() => updateZeroWidth(s.id, { mode: "RAW" })}
                                aria-pressed={s.mode === "RAW"}
                              >
                                Raw
                              </button>
                              <button
                                className="btn"
                                type="button"
                                onClick={() => updateZeroWidth(s.id, { mode: "EMBED_IN_COVER" })}
                                aria-pressed={s.mode === "EMBED_IN_COVER"}
                              >
                                Embed in cover
                              </button>
                            </div>
                          </div>

                          {s.mode === "EMBED_IN_COVER" && (
                            <div className="field">
                              <label>Cover text</label>
                              <textarea
                                value={s.coverText}
                                onChange={(e) => updateZeroWidth(s.id, { coverText: e.target.value })}
                                placeholder="Visible (innocent) text..."
                              />
                            </div>
                          )}
                        </>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="container tool-stack">
            <div className="card image-card">
              <div className="row card-head">
                <h2>Image Stego</h2>
                <div className="seg" role="tablist" aria-label="Image Mode">
                  <button
                    type="button"
                    onClick={() => setImageMode("encode")}
                    data-active={imageMode === "encode"}
                    role="tab"
                    aria-selected={imageMode === "encode"}
                  >
                    Encode
                  </button>
                  <button
                    type="button"
                    onClick={() => setImageMode("decode")}
                    data-active={imageMode === "decode"}
                    role="tab"
                    aria-selected={imageMode === "decode"}
                  >
                    Decode
                  </button>
                </div>
              </div>

              <input
                ref={imageInputRef}
                type="file"
                accept="image/*"
                onChange={(e) => {
                  const f = e.target.files?.[0] ?? null;
                  selectImageFile(f);
                  e.currentTarget.value = "";
                }}
                style={{ display: "none" }}
              />

              <div
                className="dropzone"
                data-active={imageDragActive}
                role="button"
                tabIndex={0}
                onClick={() => imageInputRef.current?.click()}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    imageInputRef.current?.click();
                  }
                }}
                onDragOver={(e) => {
                  e.preventDefault();
                  setImageDragActive(true);
                  e.dataTransfer.dropEffect = "copy";
                }}
                onDragLeave={(e) => {
                  e.preventDefault();
                  setImageDragActive(false);
                }}
                onDrop={(e) => {
                  e.preventDefault();
                  setImageDragActive(false);
                  const f = e.dataTransfer.files?.[0] ?? null;
                  if (f) {
                    selectImageFile(f);
                  } else {
                    setImageStatus({
                      kind: "error",
                      message: "Drop an image file from your device (dragging a link won't work).",
                    });
                  }
                }}
              >
                <div className="dropzone-title">{imageFile ? imageFile.name : "Drag & drop an image here"}</div>
                <div className="dropzone-meta">
                  Lossless formats recommended (PNG/BMP). JPEG/JPG will be converted to PNG output.
                </div>
                {!imageFile && <div className="dropzone-sub">or click to browse</div>}
                {imagePreviewUrl && (
                  <img className="dropzone-preview" src={imagePreviewUrl} alt="Selected image preview" />
                )}
              </div>

              {imageFile && (
                <>
                  {imageInfo && (
                    <p className="muted meta-line">
                      {imageInfo.width}×{imageInfo.height} • capacity{" "}
                      <b>{estimatePayloadCapacityBytes(imageInfo.width, imageInfo.height)}</b> bytes
                    </p>
                  )}

                  {imageMode === "encode" && (
                    <div className="field">
                      <label htmlFor="imageInput">Message</label>
                      <textarea
                        id="imageInput"
                        className="tool-textarea"
                        value={imageInput}
                        onChange={(e) => setImageInput(e.target.value)}
                        placeholder="Enter message to hide in the image..."
                      />
                    </div>
                  )}

                  {imageMode === "decode" && (
                    <div className="field">
                      <label htmlFor="imageOutput">Image output</label>
                      <textarea
                        id="imageOutput"
                        className="tool-textarea"
                        value={imageOutput}
                        readOnly
                        placeholder="Extracted message will appear here..."
                      />
                    </div>
                  )}

                  <div className="row card-head">
                    <button className="btn btn-primary" type="button" onClick={onImageRun}>
                      {imageRunning ? "Working..." : imageMode === "encode" ? "Hide in image" : "Extract from image"}
                    </button>

                    <div className="row row-tight">
                      {imageOutputUrl && imageMode === "encode" && (
                        <a
                          className="btn"
                          href={imageOutputUrl}
                          download={(imageFile?.name?.replace(/\.[^.]+$/, "") || "stego") + "-stego.png"}
                        >
                          Download PNG
                        </a>
                      )}
                      {imageMode === "decode" && (
                        <button
                          className="btn"
                          type="button"
                          onClick={async () => {
                            if (!imageOutput) return;
                            try {
                              await copyToClipboard(imageOutput);
                              setImageStatus({ kind: "ok", message: "Copied to clipboard." });
                            } catch (e) {
                              setImageStatus({ kind: "error", message: getUserFacingErrorMessage(e) });
                            }
                          }}
                        >
                          Copy output
                        </button>
                      )}
                      <button className="btn" type="button" onClick={() => selectImageFile(null)}>
                        Clear image
                      </button>
                    </div>
                  </div>
                </>
              )}

              <div className="status-block">
                {imageStatus.kind === "error" && <div className="error">{imageStatus.message}</div>}
                {imageStatus.kind === "ok" && <div className="ok">{imageStatus.message}</div>}
              </div>
            </div>
          </div>
        </section>

        <section className="anchor" id="how">
          <div className="container">
            <div className="card section-card">
              <h2>How it works</h2>
              <p className="muted">
                Stego Tool treats text steganography as a <b>pipeline</b>: you chain reversible transformations, then
                run Encode or Decode.
              </p>
              <div className="info-grid">
                <div className="info-card">
                  <h3>What is steganography?</h3>
                  <ul>
                    <li>Steganography hides that a message exists by embedding it into other content.</li>
                    <li>Unlike encryption, it focuses on concealment rather than confidentiality.</li>
                    <li>For secrecy, layer steganography with cryptography (use the Crypto step).</li>
                  </ul>
                </div>
                <div className="info-card">
                  <h3>How the pipeline works?</h3>
                  <ul>
                    <li>Each step is reversible and can be chained for layered transformations.</li>
                    <li>Encode applies steps top → bottom.</li>
                    <li>Decode applies the same steps in reverse order.</li>
                    <li>Reordering steps changes the result, so you can experiment safely.</li>
                  </ul>
                </div>
                <div className="info-card">
                  <h3>Local-first architecture</h3>
                  <ul>
                    <li>The desktop app runs the core transformations locally (offline by default).</li>
                    <li>The web app runs all transformations locally (no backend required).</li>
                    <li>Crypto runs client-side in your browser, so passwords never leave your device.</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="anchor" id="steps">
          <div className="container">
            <div className="card section-card">
              <h2>Steps</h2>
              <p className="muted">
                Use one step, or chain multiple for layered transformations. On the web, Crypto runs in your
                browser.
              </p>

              <div className="info-grid">
                <div className="info-card">
                  <h3>Crypto (AES)</h3>
                  <ul>
                    <li>Key derivation: PBKDF2 (SHA‑256, 65,536 iterations).</li>
                    <li>Encryption: AES‑256‑CBC.</li>
                    <li>
                      Output format: <code>salt:iv:ciphertext</code> (Base64 segments).
                    </li>
                    <li>This format matches the desktop app.</li>
                  </ul>
                </div>

                <div className="info-card">
                  <h3>Zero‑Width</h3>
                  <ul>
                    <li>Encodes UTF‑8 bytes into invisible characters (U+200B / U+200C).</li>
                    <li>RAW mode outputs only zero‑width characters.</li>
                    <li>Decode filters the input to zero‑width characters, then reconstructs UTF‑8.</li>
                  </ul>
                </div>

                <div className="info-card">
                  <h3>Zero‑Width (Embed in cover)</h3>
                  <ul>
                    <li>Appends an invisible payload to your visible cover text.</li>
                    <li>Cover text is required in this mode.</li>
                    <li>Decode extracts the zero‑width payload from the full text before decoding.</li>
                  </ul>
                </div>
              </div>

              <div className="info-grid">
                <div className="info-card">
                  <h3>Emoji</h3>
                  <ul>
                    <li>Maps bytes to emoji so the output looks like ordinary emoji text.</li>
                    <li>Each byte is split into two 4‑bit nibbles → two emojis.</li>
                    <li>Reversible as long as the emoji sequence stays intact.</li>
                  </ul>
                </div>

                <div className="info-card">
                  <h3>Base64</h3>
                  <ul>
                    <li>A clean reversible encoding layer for transport and debugging.</li>
                    <li>Useful when you want an ASCII-only representation between steps.</li>
                  </ul>
                </div>

                <div className="info-card">
                  <h3>Limitations</h3>
                  <ul>
                    <li>Some platforms strip invisible characters (Zero‑Width) or normalize emojis.</li>
                    <li>Steganography is not a substitute for strong security.</li>
                    <li>Test your target channel (chat apps, editors, email) before relying on it.</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="anchor" id="desktop">
          <div className="container">
            <div className="card section-card">
              <h2>Desktop app</h2>
              <p className="muted">
                The desktop client stays offline by default and runs the same core transformations locally.
                Releases will include Windows, macOS, and Linux artifacts.
              </p>
              <div className="row">
                <a className="btn btn-primary" href={RELEASES_URL} target="_blank" rel="noreferrer">
                  Download (Latest Release)
                </a>
              </div>
            </div>
          </div>
        </section>

        <section className="anchor" id="author">
          <div className="container">
            <div className="card section-card">
              <h2>Author</h2>
              <div className="info-grid">
                <div className="info-card">
                  <h3>Your Name</h3>
                  <ul>
                    <li>Title — Place</li>
                    <li>Short one-line bio goes here.</li>
                  </ul>
                </div>

                <div className="info-card">
                  <h3>Contact</h3>
                  <div className="row contact-actions">
                    <a className="btn" href="https://example.com" target="_blank" rel="noreferrer">
                      Website
                    </a>
                    <a className="btn" href="https://github.com/yourname" target="_blank" rel="noreferrer">
                      GitHub
                    </a>
                    <a className="btn" href="https://linkedin.com/in/REPLACE_ME" target="_blank" rel="noreferrer">
                      LinkedIn
                    </a>
                    <a className="btn" href="mailto:you@example.com">
                      Email
                    </a>
                  </div>
                </div>

                <div className="info-card">
                  <h3>About this project</h3>
                  <ul>
                    <li>Stego Tool is an educational playground for reversible text transformations.</li>
                    <li>Suggestions and feedback are welcome.</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </section>

      </main>

      {showScrollTop && (
        <button
          type="button"
          className="scroll-top"
          onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}
          aria-label="Scroll to top"
        >
          {"\u2191"}
        </button>
      )}

      <footer className="site-footer" role="contentinfo">
        <div className="container footer-container">
          <div className="footer-grid">
            <section className="footer-section" aria-label="Product">
              <h3>Stego Tool</h3>
            </section>

            <section className="footer-section" aria-label="Links">
              <h3>Links</h3>
              <ul className="footer-links">
                <li className="footer-link">
                  <a href="#tool">Toolbox</a>
                </li>
                <li className="footer-link">
                  <a href="#how">How it works</a>
                </li>
                <li className="footer-link">
                  <a href="#steps">Steps</a>
                </li>
                <li className="footer-link">
                  <a href="#author">Author</a>
                </li>
              </ul>
            </section>

            <section className="footer-section" aria-label="Downloads">
              <h3>Downloads</h3>
              <ul className="footer-links">
                <li className="footer-link">
                  <a href={RELEASES_URL} target="_blank" rel="noreferrer">
                    Latest desktop release
                  </a>
                </li>
                <li className="footer-link">
                  <a href={REPO_URL} target="_blank" rel="noreferrer">
                    Source code
                  </a>
                </li>
              </ul>
            </section>
          </div>

          <div className="footer-bottom">
            <div>
              {"\u00A9"} {new Date().getFullYear()} Stego Tool
            </div>
          </div>
        </div>
      </footer>
    </>
  );
}
