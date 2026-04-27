export interface SystemCheckResult {
  ffmpegExists: boolean;
  ffprobeExists: boolean;
  ffmpegPath: string;
  ffprobePath: string;
  ffmpegVersion: string;
  ffprobeVersion: string;
  inputDirExists: boolean;
  outputDirExists: boolean;
  backupDirExists: boolean;
  message: string;
}

export interface SystemDirs {
  inputDir: string;
  outputDir: string;
  backupDir: string;
  tempDir: string;
  logDir: string;
}

export interface OperationLog {
  id: number;
  operationType: string;
  videoId?: string;
  taskId?: string;
  fileName?: string;
  message?: string;
  createTime: string;
}
