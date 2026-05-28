<template>
  <!-- Iconify 图标 (如 mdi:home, fa6-solid:user) -->
  <el-icon v-if="icon && icon.includes(':')" :size="size">
    <iconify-icon :icon="icon" :width="size" :height="size" />
  </el-icon>
  <!-- Element Plus 图标 (如 Setting, User) -->
  <el-icon v-else-if="icon" :size="size"><component :is="icon" /></el-icon>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '' },
  size: { type: [Number, String], default: 18 }
})
</script>
