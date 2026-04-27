<template>
  <el-table :data="tasks" height="calc(100vh - 180px)" row-key="taskId">
    <el-table-column prop="fileName" label="文件名" min-width="260" show-overflow-tooltip />
    <el-table-column prop="status" label="状态" width="110">
      <template #default="{ row }">
        <el-tag :type="statusType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="progress" label="进度" min-width="190">
      <template #default="{ row }">
        <el-progress :percentage="row.progress || 0" :status="progressStatus(row.status)" />
      </template>
    </el-table-column>
    <el-table-column prop="sourceSizeText" label="原大小" width="120" />
    <el-table-column prop="outputSizeText" label="新大小" width="120">
      <template #default="{ row }">{{ row.outputSizeText || "-" }}</template>
    </el-table-column>
    <el-table-column prop="savePercent" label="节省" width="110">
      <template #default="{ row }">
        {{ typeof row.savePercent === "number" ? `${row.savePercent.toFixed(1)}%` : "-" }}
      </template>
    </el-table-column>
    <el-table-column prop="profile" label="模式" width="110">
      <template #default="{ row }">{{ profileText(row.profile) }}</template>
    </el-table-column>
    <el-table-column prop="message" label="消息" min-width="220" show-overflow-tooltip />
    <el-table-column label="操作" fixed="right" width="92">
      <template #default="{ row }">
        <el-button
          size="small"
          :icon="Close"
          :disabled="row.status !== 'WAITING'"
          @click="$emit('cancel', row)"
        >
          取消
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { Close } from "@element-plus/icons-vue";
import type { CompressProfile, CompressTask, TaskStatus } from "../types/task";

defineProps<{
  tasks: CompressTask[];
}>();

defineEmits<{
  cancel: [row: CompressTask];
}>();

function statusText(status: TaskStatus) {
  const map: Record<TaskStatus, string> = {
    WAITING: "等待中",
    RUNNING: "压缩中",
    SUCCESS: "成功",
    FAILED: "失败",
    CANCELLED: "已取消"
  };
  return map[status] || status;
}

function statusType(status: TaskStatus) {
  if (status === "SUCCESS") return "success";
  if (status === "FAILED") return "danger";
  if (status === "RUNNING") return "primary";
  if (status === "CANCELLED") return "info";
  return "warning";
}

function progressStatus(status: TaskStatus) {
  if (status === "SUCCESS") return "success";
  if (status === "FAILED") return "exception";
  return undefined;
}

function profileText(profile: CompressProfile) {
  const map: Record<CompressProfile, string> = {
    HIGH_QUALITY: "高清优先",
    BALANCED: "平衡模式",
    SMALL_SIZE: "小体积优先"
  };
  return map[profile] || profile;
}
</script>
