import { createRouter, createWebHashHistory } from "vue-router";
import VideoListView from "../views/VideoListView.vue";
import TaskListView from "../views/TaskListView.vue";
import SettingsView from "../views/SettingsView.vue";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/", redirect: "/videos" },
    { path: "/videos", component: VideoListView },
    { path: "/tasks", component: TaskListView },
    { path: "/settings", component: SettingsView }
  ]
});

export default router;
