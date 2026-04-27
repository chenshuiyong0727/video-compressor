import { getData } from "./request";
import type { SystemCheckResult, SystemDirs } from "../types/system";

export function checkSystem() {
  return getData<SystemCheckResult>("/system/check");
}

export function getSystemDirs() {
  return getData<SystemDirs>("/system/dirs");
}
