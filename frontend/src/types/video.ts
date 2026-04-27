export interface VideoFileInfo {
  id: string;
  fileName: string;
  absolutePath: string;
  relativePath: string;
  sizeBytes: number;
  sizeText: string;
  lastModified: number;
  durationSeconds?: number;
  durationText: string;
  width?: number;
  height?: number;
  resolutionText: string;
  codec: string;
  format: string;
  compressedExists: boolean;
}
