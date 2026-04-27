<template>
  <el-table
    :data="videos"
    height="calc(100vh - 250px)"
    row-key="id"
    @selection-change="emitSelection"
  >
    <el-table-column type="selection" width="46" />
    <el-table-column prop="fileName" label="文件名" min-width="260" show-overflow-tooltip />
    <el-table-column prop="sizeBytes" label="大小" sortable width="120">
      <template #default="{ row }">{{ row.sizeText }}</template>
    </el-table-column>
    <el-table-column prop="durationSeconds" label="时长" sortable width="110">
      <template #default="{ row }">{{ row.durationText }}</template>
    </el-table-column>
    <el-table-column prop="resolutionText" label="分辨率" sortable width="130" />
    <el-table-column prop="codec" label="编码" width="100" />
    <el-table-column prop="lastModified" label="修改时间" sortable width="178">
      <template #default="{ row }">{{ formatTime(row.lastModified) }}</template>
    </el-table-column>
    <el-table-column prop="compressedExists" label="已有压缩" width="110">
      <template #default="{ row }">
        <el-tag :type="row.compressedExists ? 'warning' : 'info'" effect="plain">
          {{ row.compressedExists ? "是" : "否" }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="lastTaskStatus" label="历史状态" width="118">
      <template #default="{ row }">
        <el-tag v-if="row.lastTaskStatus" :type="statusType(row.lastTaskStatus)" effect="plain">
          {{ statusText(row.lastTaskStatus) }}
        </el-tag>
        <span v-else class="muted">无</span>
      </template>
    </el-table-column>
    <el-table-column prop="compressedCount" label="成功/失败" width="112">
      <template #default="{ row }">
        {{ row.compressedCount || 0 }}/{{ row.failedCount || 0 }}
      </template>
    </el-table-column>
    <el-table-column prop="duplicateCompressionRisk" label="重复风险" width="110">
      <template #default="{ row }">
        <el-tag v-if="row.duplicateCompressionRisk" type="danger" effect="plain">已成功</el-tag>
        <span v-else class="muted">-</span>
      </template>
    </el-table-column>
    <el-table-column label="操作" fixed="right" width="190">
      <template #default="{ row }">
        <el-button size="small" type="primary" :icon="VideoPlay" @click="$emit('compress-one', row)">
          压缩
        </el-button>
        <el-button size="small" :icon="FolderChecked" @click="$emit('backup-one', row)">
          备份
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { FolderChecked, VideoPlay } from "@element-plus/icons-vue";
import type { VideoFileInfo } from "../types/video";

defineProps<{
  videos: VideoFileInfo[];
}>();

const emit = defineEmits<{
  selection: [rows: VideoFileInfo[]];
  "compress-one": [row: VideoFileInfo];
  "backup-one": [row: VideoFileInfo];
}>();

function emitSelection(rows: VideoFileInfo[]) {
  emit("selection", rows);
}

function formatTime(value: number) {
  if (!value) return "-";
  return new Date(value).toLocaleString();
}

function statusText(status: string) {
  const map: Record<string, string> = {
    WAITING: "等待中",
    RUNNING: "压缩中",
    SUCCESS: "成功",
    FAILED: "失败",
    CANCELLED: "已取消"
  };
  return map[status] || status;
}

function statusType(status: string) {
  if (status === "SUCCESS") return "success";
  if (status === "FAILED") return "danger";
  if (status === "RUNNING") return "primary";
  if (status === "WAITING") return "warning";
  return "info";
}
</script>
