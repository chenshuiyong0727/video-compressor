<template>
  <div class="settings-grid">
    <SystemCheckPanel :result="checkResult" :loading="checking" @refresh="runCheck" />

    <div class="panel">
      <div class="toolbar-left" style="justify-content: space-between; margin-bottom: 14px">
        <strong>本地目录</strong>
        <el-button :icon="Refresh" :loading="dirLoading" @click="loadDirs">刷新</el-button>
      </div>
      <el-descriptions v-if="dirs" :column="1" border>
        <el-descriptions-item label="input"><span class="path-value">{{ dirs.inputDir }}</span></el-descriptions-item>
        <el-descriptions-item label="output"><span class="path-value">{{ dirs.outputDir }}</span></el-descriptions-item>
        <el-descriptions-item label="backup"><span class="path-value">{{ dirs.backupDir }}</span></el-descriptions-item>
        <el-descriptions-item label="temp"><span class="path-value">{{ dirs.tempDir }}</span></el-descriptions-item>
        <el-descriptions-item label="logs"><span class="path-value">{{ dirs.logDir }}</span></el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无目录信息" />
    </div>

    <div class="panel" style="grid-column: 1 / -1">
      <strong>压缩模式</strong>
      <el-table :data="profiles" style="margin-top: 14px">
        <el-table-column prop="name" label="模式" width="160" />
        <el-table-column prop="command" label="参数" min-width="360" />
        <el-table-column prop="scene" label="适用场景" min-width="220" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import SystemCheckPanel from "../components/SystemCheckPanel.vue";
import { checkSystem, getSystemDirs } from "../api/systemApi";
import type { SystemCheckResult, SystemDirs } from "../types/system";

const checkResult = ref<SystemCheckResult>();
const dirs = ref<SystemDirs>();
const checking = ref(false);
const dirLoading = ref(false);

const profiles = [
  { name: "高清优先", command: "H.264 / libx264 / preset slow / crf 22 / aac 128k", scene: "重要视频，清晰度保留更好" },
  { name: "平衡模式", command: "H.264 / libx264 / preset medium / crf 24 / aac 128k", scene: "默认推荐，适合大部分手机视频" },
  { name: "小体积优先", command: "H.264 / libx264 / preset medium / crf 27 / aac 96k", scene: "不重要的视频，体积更小" }
];

onMounted(async () => {
  await Promise.all([runCheck(), loadDirs()]);
});

async function runCheck() {
  checking.value = true;
  try {
    checkResult.value = await checkSystem();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "检测失败");
  } finally {
    checking.value = false;
  }
}

async function loadDirs() {
  dirLoading.value = true;
  try {
    dirs.value = await getSystemDirs();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "读取目录失败");
  } finally {
    dirLoading.value = false;
  }
}
</script>
