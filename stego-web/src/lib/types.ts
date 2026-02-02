export type Mode = "encode" | "decode";

export type StepType = "CRYPTO" | "BASE64" | "EMOJI" | "ZERO_WIDTH";
export type ZeroWidthMode = "RAW" | "EMBED_IN_COVER";

export type PipelineStep =
  | {
      id: string;
      type: "CRYPTO";
      password: string;
    }
  | {
      id: string;
      type: "BASE64";
    }
  | {
      id: string;
      type: "EMOJI";
    }
  | {
      id: string;
      type: "ZERO_WIDTH";
      mode: ZeroWidthMode;
      coverText: string;
    };

export type ApiPipelineStep = {
  type: Exclude<StepType, "CRYPTO">;
  zeroWidthMode?: ZeroWidthMode;
  coverText?: string;
};

export type ApiTextTransformRequest = {
  text: string;
  pipeline: ApiPipelineStep[];
};

export type ApiTextTransformResponse = {
  result: string;
};

