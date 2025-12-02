<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册</h2>
      <p class="subtitle">创建您的账户</p>
      <el-form :model="form" @submit.prevent="handleRegister" class="register-form">
        <el-form-item>
          <el-input 
            v-model="form.username" 
            placeholder="用户名" 
            size="large"
            :disabled="isLoading"
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            size="large"
            :disabled="isLoading"
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="form.email" 
            placeholder="邮箱（可选）" 
            size="large"
            :disabled="isLoading"
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="form.phone" 
            placeholder="手机号（可选）" 
            size="large"
            :disabled="isLoading"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            native-type="submit" 
            size="large" 
            style="width: 100%"
            :loading="isLoading"
            :disabled="isLoading || !form.username || !form.password"
          >
            {{ isLoading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>
        <div class="login-link">
          <span>已有账号？</span>
          <router-link to="/login" :class="{ disabled: isLoading }">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'

const router = useRouter()
const form = reactive({ username: '', password: '', email: '', phone: '' })
const isLoading = ref(false)

const handleRegister = async () => {
  // 输入验证
  if (!form.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!form.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }
  
  // 邮箱格式检查
  if (form.email && !isValidEmail(form.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }
  
  // 手机号格式检查
  if (form.phone && !isValidPhone(form.phone)) {
    ElMessage.warning('手机号格式不正确')
    return
  }
  
  isLoading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，正在跳转到登录...')
    
    // 使用setTimeout模拟实际的数据保存延迟
    setTimeout(() => {
      router.push('/login')
    }, 500)
  } catch (error: any) {
    // 显示具体的错误信息
    const errorMsg = error.response?.data?.message || '注册失败，请检查输入'
    ElMessage.error(errorMsg)
  } finally {
    isLoading.value = false
  }
}

const isValidEmail = (email: string) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const isValidPhone = (phone: string) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f7f7f8;
  padding: 20px;
}

.register-box {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 48px 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.register-box h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #202123;
  text-align: center;
}

.subtitle {
  margin: 0 0 32px 0;
  font-size: 14px;
  color: #6e6e80;
  text-align: center;
}

.register-form {
  margin-top: 24px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e5e5e5;
}

:deep(.el-button--primary) {
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  height: 48px;
}

.login-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #6e6e80;
}

.login-link a {
  color: #10a37f;
  text-decoration: none;
  margin-left: 4px;
  transition: all 0.3s ease;
}

.login-link a:hover {
  text-decoration: underline;
}

.login-link a.disabled {
  color: #c0c4cc;
  cursor: not-allowed;
  pointer-events: none;
}
</style>
