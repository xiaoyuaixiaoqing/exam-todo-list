<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册</h2>
      <p class="subtitle">创建您的账户</p>
      <el-form :model="form" @submit.prevent="handleRegister" class="register-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.email" placeholder="邮箱（可选）" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.phone" placeholder="手机号（可选）" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" size="large" style="width: 100%">注册</el-button>
        </el-form-item>
        <div class="login-link">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'

const router = useRouter()
const form = reactive({ username: '', password: '', email: '', phone: '' })

const handleRegister = async () => {
  try {
    await register(form)
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    ElMessage.error('注册失败')
  }
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
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
