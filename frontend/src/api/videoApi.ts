import { getData, postData } from "./request";
import type { VideoFileInfo } from "../types/video";

export interface VideoScanParams {
  minSizeMb?: number;
  maxSizeMb?: number;
  compressStatus?: string;
}

export function scanVideos(params: VideoScanParams) {
  return getData<VideoFileInfo[]>("/videos/scan", params);
}

export function moveVideosToBackup(videoIds: string[]) {
  return postData<string[]>("/videos/backup", { videoIds });
}
