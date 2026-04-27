<template>
  <div>
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="muted">文件大小</span>
        <el-input-number v-model="minSizeMb" :min="0" :step="50" controls-position="right" style="width: 150px" />
        <span class="muted">至</span>
        <el-input-number v-model="maxSizeMb" :min="0" :step="50" controls-position="right" style="width: 150px" />
        <span class="muted">MB</span>
        <span class="muted">状态</span>
        <el-select v-model="compressStatus" style="width: 138px">
          <el-option label="全部" value="ALL" />
          <el-option label="未压缩" value="NOT_COMPRESSED" />
          <el-option label="压缩成功" value="COMPRESSED" />
          <el-option label="压缩失败" value="FAILED" />
          <el-option label="等待中" value="WAITING" />
          <el-option label="压缩中" value="RUNNING" />
          <el-option label="有历史记录" value="HAS_HISTORY" />
        </el-select>
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
import { onMounted, ref, watch } from "vue";
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
const MIN_SIZE_CACHE_KEY = "local-video-compressor:min-size-mb";
const MAX_SIZE_CACHE_KEY = "local-video-compressor:max-size-mb";
const CURRENT_BATCH_CACHE_KEY = "local-video-compressor:current-batch-id";
const minSizeMb = ref(readCachedMinSizeMb());
const maxSizeMb = ref<number | undefined>(readCachedMaxSizeMb());
const loading = ref(false);
const videos = ref<VideoFileInfo[]>([]);
const selected = ref<VideoFileInfo[]>([]);
const compressStatus = ref("ALL");
const profile = ref<CompressProfile>("BALANCED");
const dirs = ref<SystemDirs>();

onMounted(async () => {
  dirs.value = await getSystemDirs();
});

watch(minSizeMb, (value) => {
  localStorage.setItem(MIN_SIZE_CACHE_KEY, String(value));
});

watch(maxSizeMb, (value) => {
  if (value == null || value <= 0) {
    localStorage.removeItem(MAX_SIZE_CACHE_KEY);
    return;
  }
  localStorage.setItem(MAX_SIZE_CACHE_KEY, String(value));
});

watch(compressStatus, () => {
  selected.value = [];
});

function readCachedMinSizeMb() {
  const cached = localStorage.getItem(MIN_SIZE_CACHE_KEY);
  if (cached == null) {
    return 200;
  }
  const value = Number(cached);
  return Number.isFinite(value) && value >= 0 ? value : 200;
}

function readCachedMaxSizeMb() {
  const cached = localStorage.getItem(MAX_SIZE_CACHE_KEY);
  if (cached == null) {
    return undefined;
  }
  const value = Number(cached);
  return Number.isFinite(value) && value > 0 ? value : undefined;
}

async function scan() {
  if (maxSizeMb.value && maxSizeMb.value > 0 && minSizeMb.value > maxSizeMb.value) {
    ElMessage.warning("最大大小不能小于最小大小");
    return;
  }
  loading.value = true;
  try {
    videos.value = await scanVideos({
      minSizeMb: minSizeMb.value,
      maxSizeMb: maxSizeMb.value,
      compressStatus: compressStatus.value
    });
    selected.value = [];
    ElMessage.success(`扫描完成，共 ${videos.value.length} 个视频`);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "扫描失败");
  } finally {
    loading.value = false;
  }
}

async function compressOne(row: VideoFileInfo) {
  await submitCompress([row]);
}

async function compressSelected() {
  await submitCompress(selected.value);
}

async function submitCompress(videosToCompress: VideoFileInfo[]) {
  const videoIds = videosToCompress.map((item) => item.id);
  if (videoIds.length === 0) return;
  const duplicateCount = videosToCompress.filter((item) => item.duplicateCompressionRisk).length;
  if (duplicateCount > 0) {
    try {
      await ElMessageBox.confirm(
        `有 ${duplicateCount} 个视频已有成功压缩记录，继续会产生重复压缩结果。`,
        "重复压缩提醒",
        {
          type: "warning",
          confirmButtonText: "继续压缩",
          cancelButtonText: "取消"
        }
      );
    } catch (error) {
      return;
    }
  }
  try {
    const tasks = await createCompressTasks({
      videoIds,
      profile: profile.value,
      overwriteOutput: false
    });
    if (tasks[0]?.batchId) {
      localStorage.setItem(CURRENT_BATCH_CACHE_KEY, tasks[0].batchId);
    }
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
