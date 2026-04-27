<template>
  <div class="panel">
    <div class="toolbar-left" style="justify-content: space-between; margin-bottom: 14px">
      <strong>系统检测</strong>
      <el-button type="primary" :icon="Refresh" :loading="loading" @click="$emit('refresh')">
        检测环境
      </el-button>
    </div>
    <el-descriptions v-if="result" :column="1" border>
      <el-descriptions-item label="FFmpeg">
        <el-tag :type="result.ffmpegExists ? 'success' : 'danger'" effect="plain">
          {{ result.ffmpegExists ? "存在" : "缺失" }}
        </el-tag>
        <span class="path-value"> {{ result.ffmpegPath }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="ffprobe">
        <el-tag :type="result.ffprobeExists ? 'success' : 'danger'" effect="plain">
          {{ result.ffprobeExists ? "存在" : "缺失" }}
        </el-tag>
        <span class="path-value"> {{ result.ffprobePath }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="input 目录">
        <el-tag :type="result.inputDirExists ? 'success' : 'danger'" effect="plain">
          {{ result.inputDirExists ? "存在" : "缺失" }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="output 目录">
        <el-tag :type="result.outputDirExists ? 'success' : 'danger'" effect="plain">
          {{ result.outputDirExists ? "存在" : "缺失" }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="backup 目录">
        <el-tag :type="result.backupDirExists ? 'success' : 'danger'" effect="plain">
          {{ result.backupDirExists ? "存在" : "缺失" }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="检测结果">{{ result.message }}</el-descriptions-item>
    </el-descriptions>
    <el-empty v-else description="暂无检测结果" />
  </div>
</template>

<script setup lang="ts">
import { Refresh } from "@element-plus/icons-vue";
import type { SystemCheckResult } from "../types/system";

defineProps<{
  result?: SystemCheckResult;
  loading?: boolean;
}>();

defineEmits<{
  refresh: [];
}>();
</script>
