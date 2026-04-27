<template>
  <div>
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadTasks">
          刷新任务
        </el-button>
        <el-switch v-model="autoRefresh" active-text="自动刷新" />
        <span class="muted">查看批次</span>
        <el-select v-model="selectedBatchId" style="width: 220px" placeholder="选择批次">
          <el-option label="全部任务" value="ALL" />
          <el-option
            v-for="batch in batchOptions"
            :key="batch.value"
            :label="batch.label"
            :value="batch.value"
          />
        </el-select>
      </div>
      <div class="toolbar-right muted">批量任务按单线程队列执行</div>
    </div>

    <div class="panel progress-panel">
      <div class="progress-summary">
        <strong>总任务进度</strong>
        <span>
          当前批次共 {{ totalStats.total }} 个，成功 {{ totalStats.success }}，失败 {{ totalStats.failed }}，
          进行中 {{ totalStats.running }}，等待 {{ totalStats.waiting }}
        </span>
      </div>
      <el-progress :percentage="totalProgress" :status="totalProgressStatus" />
    </div>

    <el-tabs v-model="activeTab" class="table-panel">
      <el-tab-pane label="任务记录" name="tasks">
        <TaskTable :tasks="visibleTasks" @cancel="handleCancel" />
      </el-tab-pane>
      <el-tab-pane label="操作记录" name="logs">
        <el-table :data="operationLogs" height="calc(100vh - 220px)" row-key="id">
          <el-table-column prop="createTime" label="时间" width="190" />
          <el-table-column prop="operationType" label="操作" width="150">
            <template #default="{ row }">{{ operationText(row.operationType) }}</template>
          </el-table-column>
          <el-table-column prop="fileName" label="文件名" min-width="260" show-overflow-tooltip />
          <el-table-column prop="message" label="说明" min-width="320" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, onActivated, onDeactivated, onMounted, onUnmounted, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import TaskTable from "../components/TaskTable.vue";
import { cancelTask, listTasks } from "../api/taskApi";
import { listOperationLogs } from "../api/systemApi";
import type { CompressTask } from "../types/task";
import type { OperationLog } from "../types/system";

const tasks = ref<CompressTask[]>([]);
const operationLogs = ref<OperationLog[]>([]);
const loading = ref(false);
const autoRefresh = ref(true);
const activeTab = ref("tasks");
const CURRENT_BATCH_CACHE_KEY = "local-video-compressor:current-batch-id";
const selectedBatchId = ref(localStorage.getItem(CURRENT_BATCH_CACHE_KEY) || "ALL");
let timer: number | undefined;

const batchOptions = computed(() => {
  const batches = new Map<string, CompressTask[]>();
  tasks.value.forEach((task) => {
    if (!task.batchId) {
      return;
    }
    const batchTasks = batches.get(task.batchId) || [];
    batchTasks.push(task);
    batches.set(task.batchId, batchTasks);
  });
  return Array.from(batches.entries()).map(([batchId, batchTasks]) => {
    const firstTask = batchTasks[0];
    return {
      value: batchId,
      label: `${formatBatchTime(firstTask?.createTime)}（${batchTasks.length} 个）`
    };
  });
});

const visibleTasks = computed(() => {
  if (selectedBatchId.value === "ALL") {
    return tasks.value;
  }
  return tasks.value
    .filter((task) => task.batchId === selectedBatchId.value)
    .sort((a, b) => taskTime(a.createTime) - taskTime(b.createTime));
});

const totalStats = computed(() => {
  const total = visibleTasks.value.length;
  const success = visibleTasks.value.filter((task) => task.status === "SUCCESS").length;
  const failed = visibleTasks.value.filter((task) => task.status === "FAILED").length;
  const running = visibleTasks.value.filter((task) => task.status === "RUNNING").length;
  const waiting = visibleTasks.value.filter((task) => task.status === "WAITING").length;
  return { total, success, failed, running, waiting };
});

const totalProgress = computed(() => {
  if (visibleTasks.value.length === 0) {
    return 0;
  }
  const total = visibleTasks.value.reduce((sum, task) => {
    if (task.status === "SUCCESS") {
      return sum + 100;
    }
    if (task.status === "FAILED" || task.status === "CANCELLED") {
      return sum + 100;
    }
    return sum + (task.progress || 0);
  }, 0);
  return Math.round(total / visibleTasks.value.length);
});

const totalProgressStatus = computed(() => {
  if (visibleTasks.value.length === 0) {
    return undefined;
  }
  if (totalStats.value.failed > 0) {
    return "exception";
  }
  if (totalProgress.value >= 100) {
    return "success";
  }
  return undefined;
});

onMounted(() => {
  loadTasks();
  startTimer();
});

onActivated(() => {
  loadTasks();
  startTimer();
});

onDeactivated(() => {
  stopTimer();
});

onUnmounted(() => {
  stopTimer();
});

watch(autoRefresh, (enabled) => {
  if (enabled) startTimer();
  else stopTimer();
});

watch(selectedBatchId, (batchId) => {
  if (batchId === "ALL") {
    localStorage.removeItem(CURRENT_BATCH_CACHE_KEY);
  } else {
    localStorage.setItem(CURRENT_BATCH_CACHE_KEY, batchId);
  }
});

async function loadTasks() {
  loading.value = true;
  try {
    const [taskResult, logResult] = await Promise.all([listTasks(), listOperationLogs()]);
    tasks.value = taskResult;
    operationLogs.value = logResult;
    keepSelectedBatchValid();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "加载任务失败");
  } finally {
    loading.value = false;
  }
}

function keepSelectedBatchValid() {
  if (selectedBatchId.value === "ALL") {
    const cachedBatchId = localStorage.getItem(CURRENT_BATCH_CACHE_KEY);
    if (cachedBatchId && tasks.value.some((task) => task.batchId === cachedBatchId)) {
      selectedBatchId.value = cachedBatchId;
    }
    return;
  }
  if (!tasks.value.some((task) => task.batchId === selectedBatchId.value)) {
    selectedBatchId.value = tasks.value.find((task) => task.batchId)?.batchId || "ALL";
  }
}

function formatBatchTime(value?: string) {
  if (!value) {
    return "未知批次";
  }
  return new Date(value).toLocaleString();
}

function taskTime(value?: string) {
  if (!value) {
    return 0;
  }
  const time = new Date(value).getTime();
  return Number.isFinite(time) ? time : 0;
}

async function handleCancel(row: CompressTask) {
  try {
    await cancelTask(row.taskId);
    ElMessage.success("任务已取消");
    await loadTasks();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "取消失败");
  }
}

function startTimer() {
  stopTimer();
  timer = window.setInterval(loadTasks, 2000);
}

function stopTimer() {
  if (timer) {
    window.clearInterval(timer);
    timer = undefined;
  }
}

function operationText(type: string) {
  const map: Record<string, string> = {
    SCAN_VIDEO: "扫描视频",
    CREATE_TASK: "创建任务",
    TASK_RUNNING: "开始压缩",
    TASK_SUCCESS: "压缩成功",
    TASK_FAILED: "压缩失败",
    CANCEL_TASK: "取消任务",
    MOVE_TO_BACKUP: "移动备份"
  };
  return map[type] || type;
}
</script>
