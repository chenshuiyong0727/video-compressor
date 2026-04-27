<template>
  <el-container class="app-shell">
    <el-aside class="sidebar" width="236px">
      <div class="brand">
        <div class="brand-mark">VC</div>
        <div>
          <div class="brand-title">本地视频批量压缩工具</div>
          <div class="brand-subtitle">local-video-compressor</div>
        </div>
      </div>
      <el-menu router :default-active="$route.path" class="nav-menu">
        <el-menu-item index="/videos">
          <el-icon><VideoCamera /></el-icon>
          <span>视频扫描</span>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <span>压缩任务</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          <strong>{{ title }}</strong>
          <span>{{ subtitle }}</span>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import { List, Setting, VideoCamera } from "@element-plus/icons-vue";

const route = useRoute();

const title = computed(() => {
  if (route.path === "/tasks") return "压缩任务";
  if (route.path === "/settings") return "系统设置";
  return "视频扫描";
});

const subtitle = computed(() => {
  if (route.path === "/tasks") return "查看队列、进度、结果和失败原因";
  if (route.path === "/settings") return "检查 FFmpeg、ffprobe 和本地目录";
  return "扫描 data/input，选择视频并提交压缩";
});
</script>
