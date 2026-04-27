export type CompressProfile = "HIGH_QUALITY" | "BALANCED" | "SMALL_SIZE";
export type TaskStatus = "WAITING" | "RUNNING" | "SUCCESS" | "FAILED" | "CANCELLED";

export interface CompressTask {
  taskId: string;
  videoId: string;
  sourcePath: string;
  outputPath: string;
  fileName: string;
  sourceSizeBytes: number;
  outputSizeBytes?: number;
  sourceSizeText: string;
  outputSizeText?: string;
  progress: number;
  status: TaskStatus;
  message?: string;
  profile: CompressProfile;
  createTime: string;
  startTime?: string;
  endTime?: string;
  savePercent?: number;
}

export interface CompressRequest {
  videoIds: string[];
  profile: CompressProfile;
  overwriteOutput: boolean;
}
