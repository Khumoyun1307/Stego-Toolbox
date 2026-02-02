import type { ApiTextTransformRequest, ApiTextTransformResponse } from "./types";
import { applyApiDecode, applyApiEncode } from "./textSteps";

type ProblemDetail = {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
};

function toProblemError(instance: string, err: unknown): Error {
  const detail = err instanceof Error ? err.message : String(err);
  const pd: ProblemDetail = {
    type: "https://errors.stego-tool.local/INVALID_REQUEST",
    title: "Invalid request",
    status: 400,
    detail,
    instance,
  };
  return new Error(JSON.stringify(pd));
}

export async function apiEncode(
  req: ApiTextTransformRequest,
  _signal?: AbortSignal,
): Promise<ApiTextTransformResponse> {
  try {
    return { result: applyApiEncode(req.text, req.pipeline) };
  } catch (e) {
    throw toProblemError("/api/v1/text/encode", e);
  }
}

export async function apiDecode(
  req: ApiTextTransformRequest,
  _signal?: AbortSignal,
): Promise<ApiTextTransformResponse> {
  try {
    return { result: applyApiDecode(req.text, req.pipeline) };
  } catch (e) {
    throw toProblemError("/api/v1/text/decode", e);
  }
}
