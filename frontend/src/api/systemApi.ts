import { getData, postData } from "./request";
import type { OperationLog, SystemCheckResult, SystemDirs } from "../types/system";

export function checkSystem() {
  return getData<SystemCheckResult>("/system/check");
}

export function getSystemDirs() {
  return getData<SystemDirs>("/system/dirs");
}

export function saveSystemDirs(dirs: SystemDirs) {
  return postData<SystemDirs>("/system/dirs", dirs);
}

export function listOperationLogs(limit = 200) {
  return getData<OperationLog[]>("/system/operations", { limit });
}
