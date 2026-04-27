<template>
  <div>
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="muted">最小大小</span>
        <el-input-number v-model="minSizeMb" :min="0" :step="50" controls-position="right" style="width: 150px" />
        <span class="muted">MB</span>
        <el-button type="primary" :icon="Search" :loading="loading" @click="scan">
          扫描视频
        </el-button>
      </div>
      <div class="toolbar-right">
        <CompressProfileSelect v-model="profile" />
        <el-button type="success" :icon="VideoPlay" :disabled="selected.length === 0" @click="compressSelected">
          批量压缩
        </el-button>
        <el-button :icon="FolderChecked" :disabled="selected.length === 0" @click="backupSelected">
          移动备份
        </el-button>
      </div>
    </div>

    <div class="dir-line">input：<span class="path-value">{{ dirs?.inputDir || "./data/input" }}</span></div>

    <div class="table-panel">
      <VideoTable
        :videos="videos"
        @selection="selected = $event"
        @compress-one="compressOne"
        @backup-one="backupOne"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { FolderChecked, Search, VideoPlay } from "@element-plus/icons-vue";
import CompressProfileSelect from "../components/CompressProfileSelect.vue";
import VideoTable from "../components/VideoTable.vue";
import { createCompressTasks } from "../api/taskApi";
import { moveVideosToBackup, scanVideos } from "../api/videoApi";
import { getSystemDirs } from "../api/systemApi";
import type { CompressProfile } from "../types/task";
import type { SystemDirs } from "../types/system";
import type { VideoFileInfo } from "../types/video";

const router = useRouter();
const minSizeMb = ref(100);
const loading = ref(false);
const videos = ref<VideoFileInfo[]>([]);
const selected = ref<VideoFileInfo[]>([]);
const profile = ref<CompressProfile>("BALANCED");
const dirs = ref<SystemDirs>();

onMounted(async () => {
  dirs.value = await getSystemDirs();
});

async function scan() {
  loading.value = true;
  try {
    videos.value = await scanVideos(minSizeMb.value);
    selected.value = [];
    ElMessage.success(`扫描完成，共 ${videos.value.length} 个视频`);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "扫描失败");
  } finally {
    loading.value = false;
  }
}

async function compressOne(row: VideoFileInfo) {
  await submitCompress([row.id]);
}

async function compressSelected() {
  await submitCompress(selected.value.map((item) => item.id));
}

async function submitCompress(videoIds: string[]) {
  if (videoIds.length === 0) return;
  try {
    const tasks = await createCompressTasks({
      videoIds,
      profile: profile.value,
      overwriteOutput: false
    });
    ElMessage.success(`已创建 ${tasks.length} 个压缩任务`);
    await router.push("/tasks");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "创建任务失败");
  }
}

async function backupOne(row: VideoFileInfo) {
  await backup([row.id]);
}

async function backupSelected() {
  await backup(selected.value.map((item) => item.id));
}

async function backup(videoIds: string[]) {
  if (videoIds.length === 0) return;
  try {
    await ElMessageBox.confirm("源文件将从 input 移动到 backup，不会删除。", "移动到备份", {
      type: "warning",
      confirmButtonText: "移动",
      cancelButtonText: "取消"
    });
    const files = await moveVideosToBackup(videoIds);
    ElMessage.success(`已移动 ${files.length} 个视频到备份目录`);
    await scan();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error instanceof Error ? error.message : "移动备份失败");
    }
  }
}
</script>
