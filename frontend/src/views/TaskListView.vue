<template>
  <div>
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadTasks">
          刷新任务
        </el-button>
        <el-switch v-model="autoRefresh" active-text="自动刷新" />
      </div>
      <div class="toolbar-right muted">批量任务按单线程队列执行</div>
    </div>

    <el-tabs v-model="activeTab" class="table-panel">
      <el-tab-pane label="任务记录" name="tasks">
        <TaskTable :tasks="tasks" @cancel="handleCancel" />
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
import { onActivated, onDeactivated, onMounted, onUnmounted, ref, watch } from "vue";
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
let timer: number | undefined;

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

async function loadTasks() {
  loading.value = true;
  try {
    const [taskResult, logResult] = await Promise.all([listTasks(), listOperationLogs()]);
    tasks.value = taskResult;
    operationLogs.value = logResult;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "加载任务失败");
  } finally {
    loading.value = false;
  }
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
