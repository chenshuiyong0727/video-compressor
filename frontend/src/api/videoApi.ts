import { getData, postData } from "./request";
import type { VideoFileInfo } from "../types/video";

export function scanVideos(minSizeMb: number) {
  return getData<VideoFileInfo[]>("/videos/scan", { minSizeMb });
}

export function moveVideosToBackup(videoIds: string[]) {
  return postData<string[]>("/videos/backup", { videoIds });
}
