import { getData, postData } from "./request";
import type { CompressRequest, CompressTask } from "../types/task";

export function createCompressTasks(request: CompressRequest) {
  return postData<CompressTask[]>("/tasks/compress", request);
}

export function listTasks() {
  return getData<CompressTask[]>("/tasks");
}

export function cancelTask(taskId: string) {
  return postData<CompressTask>(`/tasks/${taskId}/cancel`);
}
