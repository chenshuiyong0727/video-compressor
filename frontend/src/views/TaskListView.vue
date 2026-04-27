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

    <div class="table-panel">
      <TaskTable :tasks="tasks" @cancel="handleCancel" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import TaskTable from "../components/TaskTable.vue";
import { cancelTask, listTasks } from "../api/taskApi";
import type { CompressTask } from "../types/task";

const tasks = ref<CompressTask[]>([]);
const loading = ref(false);
const autoRefresh = ref(true);
let timer: number | undefined;

onMounted(() => {
  loadTasks();
  startTimer();
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
    tasks.value = await listTasks();
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
</script>
