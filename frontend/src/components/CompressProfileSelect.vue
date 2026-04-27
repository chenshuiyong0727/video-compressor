<template>
  <el-radio-group :model-value="modelValue" @update:model-value="emitValue">
    <el-radio-button label="HIGH_QUALITY">高清优先</el-radio-button>
    <el-radio-button label="BALANCED">平衡模式</el-radio-button>
    <el-radio-button label="SMALL_SIZE">小体积优先</el-radio-button>
  </el-radio-group>
</template>

<script setup lang="ts">
import type { CompressProfile } from "../types/task";

defineProps<{
  modelValue: CompressProfile;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: CompressProfile];
}>();

function emitValue(value: string | number | boolean | undefined) {
  emit("update:modelValue", value as CompressProfile);
}
</script>
