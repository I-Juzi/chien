<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>欢迎使用 Chien Admin 后台管理系统</h2>
              <p>当前登录用户: <strong>{{ userStore.username }}</strong></p>
            </div>
            <div class="welcome-time">
              <el-icon size="64" color="#409EFF"><HomeFilled /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>用户管理</span>
          </template>
          <div class="stat-item">
            <el-icon size="40" color="#409EFF"><User /></el-icon>
            <div class="stat-info">
              <p class="stat-title">用户管理</p>
              <p class="stat-desc">管理系统用户、分配角色</p>
            </div>
          </div>
          <el-button type="primary" text @click="$router.push('/system/user')">进入管理</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>角色管理</span>
          </template>
          <div class="stat-item">
            <el-icon size="40" color="#67C23A"><UserFilled /></el-icon>
            <div class="stat-info">
              <p class="stat-title">角色管理</p>
              <p class="stat-desc">管理角色、分配权限</p>
            </div>
          </div>
          <el-button type="success" text @click="$router.push('/system/role')">进入管理</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>菜单管理</span>
          </template>
          <div class="stat-item">
            <el-icon size="40" color="#E6A23C"><Menu /></el-icon>
            <div class="stat-info">
              <p class="stat-title">菜单管理</p>
              <p class="stat-desc">管理菜单结构、权限标识</p>
            </div>
          </div>
          <el-button type="warning" text @click="$router.push('/system/menu')">进入管理</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { HomeFilled, User, UserFilled, Menu } from '@element-plus/icons-vue'

const userStore = useUserStore()
</script>

<style scoped>
.welcome-card .welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 8px;
  color: #303133;
}

.welcome-text p {
  color: #606266;
  margin: 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.stat-desc {
  font-size: 13px;
  color: #909399;
  margin: 4px 0 0;
}
</style>
