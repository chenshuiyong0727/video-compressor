<template>
  <div class="settings-grid">
    <SystemCheckPanel :result="checkResult" :loading="checking" @refresh="runCheck" />

    <div class="panel">
      <div class="toolbar-left" style="justify-content: space-between; margin-bottom: 14px">
        <strong>本地目录</strong>
        <div class="toolbar-left">
          <el-button :icon="Refresh" :loading="dirLoading" @click="loadDirs">刷新</el-button>
          <el-button type="primary" :icon="Check" :loading="saving" @click="saveDirs">保存</el-button>
        </div>
      </div>

      <el-form v-if="dirForm" label-width="96px" label-position="left">
        <el-form-item label="input">
          <el-input v-model="dirForm.inputDir" placeholder="例如：E:/照片" clearable />
        </el-form-item>
        <el-form-item label="output">
          <el-input v-model="dirForm.outputDir" placeholder="例如：E:/压缩后视频 或 ./data/output" clearable />
        </el-form-item>
        <el-form-item label="backup">
          <el-input v-model="dirForm.backupDir" placeholder="例如：E:/视频备份 或 ./data/backup" clearable />
        </el-form-item>
        <el-form-item label="temp">
          <el-input v-model="dirForm.tempDir" placeholder="例如：./data/temp" clearable />
        </el-form-item>
        <el-form-item label="logs">
          <el-input v-model="dirForm.logDir" placeholder="例如：./data/logs" clearable />
        </el-form-item>
      </el-form>
      <el-empty v-else description="暂无目录信息" />

      <div class="muted">
        支持绝对路径，例如 E:/照片。保存后后端会立即使用新目录，并自动创建不存在的目录。
      </div>
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
import { Check, Refresh } from "@element-plus/icons-vue";
import SystemCheckPanel from "../components/SystemCheckPanel.vue";
import { checkSystem, getSystemDirs, saveSystemDirs } from "../api/systemApi";
import type { SystemCheckResult, SystemDirs } from "../types/system";

const checkResult = ref<SystemCheckResult>();
const dirForm = ref<SystemDirs>();
const checking = ref(false);
const dirLoading = ref(false);
const saving = ref(false);

const profiles = [
  { name: "高清优先", command: "MP4 / H.264 / yuv420p / crf 22 / aac 128k", scene: "重要视频，清晰度保留更好" },
  { name: "平衡模式", command: "MP4 / H.264 / yuv420p / crf 24 / aac 128k", scene: "默认推荐，适合大部分手机视频" },
  { name: "小体积优先", command: "MP4 / H.264 / yuv420p / crf 27 / aac 96k", scene: "不重要的视频，体积更小" }
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
    dirForm.value = await getSystemDirs();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "读取目录失败");
  } finally {
    dirLoading.value = false;
  }
}

async function saveDirs() {
  if (!dirForm.value) {
    return;
  }
  saving.value = true;
  try {
    dirForm.value = await saveSystemDirs(dirForm.value);
    ElMessage.success("目录配置已保存，请重新扫描视频");
    await runCheck();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "保存目录失败");
  } finally {
    saving.value = false;
  }
}
</script>
