<template>
  <div class="page-container">
    <el-row :gutter="20">
      <!-- 左侧：头像和基本信息 -->
      <el-col :span="8">
        <el-card>
          <template #header>个人信息</template>
          <div style="text-align: center; margin-bottom: 20px;">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <el-avatar :size="80" :src="profile.avatar || undefined" class="avatar-clickable">
                {{ profile.nickname ? profile.nickname.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
              <div class="avatar-tip">点击更换头像</div>
            </el-upload>
            <h3 style="margin: 10px 0 5px;">{{ profile.nickname || profile.username }}</h3>
            <p style="color: #909399; margin: 0;">{{ profile.username }}</p>
          </div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ profile.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ profile.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ profile.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ profile.createTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 右侧：编辑 -->
      <el-col :span="16">
        <el-card>
          <template #header>编辑资料</template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" style="max-width: 500px;">
            <el-form-item label="用户名">
              <el-input :model-value="profile.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveProfile" :loading="saving">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>修改密码</template>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px" style="max-width: 500px;">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePwd" :loading="changingPwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getProfileApi, updateProfileApi, changePasswordApi } from '@/api/profile'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

const formRef = ref(null)
const pwdFormRef = ref(null)
const saving = ref(false)
const changingPwd = ref(false)

const profile = ref({})

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const form = reactive({
  nickname: '',
  email: '',
  phone: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

onMounted(() => {
  fetchProfile()
})

async function fetchProfile() {
  try {
    const res = await getProfileApi()
    profile.value = res.data
    form.nickname = res.data.nickname || ''
    form.email = res.data.email || ''
    form.phone = res.data.phone || ''
  } catch (e) {
    // handled
  }
}

async function handleSaveProfile() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updateProfileApi(form)
    ElMessage.success('保存成功')
    fetchProfile()
  } catch (e) {
    // handled
  } finally {
    saving.value = false
  }
}

async function handleChangePwd() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  changingPwd.value = true
  try {
    await changePasswordApi({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdFormRef.value.resetFields()
  } catch (e) {
    // handled
  } finally {
    changingPwd.value = false
  }
}

function beforeAvatarUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isImage) {
    ElMessage.error('头像只能是 JPG/PNG/GIF/WebP 格式')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

async function handleAvatarSuccess(response) {
  if (response.code === 200) {
    const avatarUrl = response.data.url
    // 保存头像URL到用户信息
    await updateProfileApi({
      nickname: profile.value.nickname,
      email: profile.value.email,
      phone: profile.value.phone,
      avatar: avatarUrl
    })
    profile.value.avatar = avatarUrl
    ElMessage.success('头像更新成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}
</script>

<style scoped>
.avatar-uploader {
  display: inline-block;
  cursor: pointer;
  text-align: center;
}

.avatar-clickable {
  transition: opacity 0.3s;
}

.avatar-clickable:hover {
  opacity: 0.7;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
</style>
