<template>
  <div class="login-container">
    <div class="login-box">
      <h2>登录</h2>
      <p class="subtitle">欢迎回来</p>
      <el-form :model="form" @submit.prevent="handleLogin" class="login-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" size="large" style="width: 100%">登录</el-button>
        </el-form-item>
        <div class="register-link">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ username: '', password: '' })

const handleLogin = async () => {
  try {
    const res: any = await login(form)
    userStore.setToken(res.data)
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (error) {
    ElMessage.error('登录失败')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f7f7f8;
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 48px 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.login-box h2 {
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

.login-form {
  margin-top: 24px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
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

.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #6e6e80;
}

.register-link a {
  color: #10a37f;
  text-decoration: none;
  margin-left: 4px;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
