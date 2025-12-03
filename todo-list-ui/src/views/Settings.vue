<template>
  <div class="settings-container">
    <div class="header">
      <h2>通知设置</h2>
      <el-button @click="goHome" type="primary" plain>返回主页</el-button>
    </div>
    <el-form :model="settings" label-width="120px">
      <el-form-item label="短信提醒">
        <el-switch v-model="settings.smsEnabled" />
      </el-form-item>
      <el-form-item label="邮件提醒">
        <el-switch v-model="settings.emailEnabled" />
      </el-form-item>
      <el-form-item label="提前通知时间">
        <el-select v-model="settings.advanceTime">
          <el-option label="1小时" :value="1" />
          <el-option label="6小时" :value="6" />
          <el-option label="12小时" :value="12" />
          <el-option label="24小时" :value="24" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const settings = reactive({
  smsEnabled: true,
  emailEnabled: true,
  advanceTime: 24
})

const loadSettings = () => {
  const savedSettings = localStorage.getItem('notificationSettings')
  if (savedSettings) {
    const parsed = JSON.parse(savedSettings)
    Object.assign(settings, parsed)
  }
}

const saveSettings = () => {
  localStorage.setItem('notificationSettings', JSON.stringify(settings))
  ElMessage.success('设置已保存')
}

const goHome = () => {
  router.push('/home')
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.settings-container {
  padding: 40px;
  max-width: 600px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

h2 {
  margin: 0;
  color: #303133;
}
</style>
