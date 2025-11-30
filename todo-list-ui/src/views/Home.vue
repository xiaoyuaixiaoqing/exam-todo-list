<template>
  <div class="home-container">
    <el-container>
      <el-header>
        <h2>TODO List</h2>
        <el-button @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <el-button type="primary" @click="showDialog = true">新建任务</el-button>
        <el-table :data="tasks" style="margin-top: 20px">
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              {{ ['待办', '进行中', '已完成'][row.status] }}
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级">
            <template #default="{ row }">
              {{ ['低', '中', '高'][row.priority - 1] }}
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button size="small" @click="editTask(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-main>
    </el-container>

    <el-dialog v-model="showDialog" :title="currentTask.id ? '编辑任务' : '新建任务'">
      <el-form :model="currentTask">
        <el-form-item label="标题">
          <el-input v-model="currentTask.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="currentTask.description" type="textarea" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="currentTask.status">
            <el-option label="待办" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="currentTask.priority">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTasks, createTask, updateTask, deleteTask } from '../api/task'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const tasks = ref([])
const showDialog = ref(false)
const currentTask = reactive({ id: null, title: '', description: '', status: 0, priority: 1, userId: 1 })

const loadTasks = async () => {
  const res: any = await getTasks({ userId: 1 })
  tasks.value = res.data.records
}

const editTask = (task: any) => {
  Object.assign(currentTask, task)
  showDialog.value = true
}

const handleSave = async () => {
  if (currentTask.id) {
    await updateTask(currentTask.id, currentTask)
    ElMessage.success('更新成功')
  } else {
    await createTask(currentTask)
    ElMessage.success('创建成功')
  }
  showDialog.value = false
  Object.assign(currentTask, { id: null, title: '', description: '', status: 0, priority: 1, userId: 1 })
  loadTasks()
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除？')
  await deleteTask(id)
  ElMessage.success('删除成功')
  loadTasks()
}

const logout = () => {
  userStore.clearToken()
  router.push('/login')
}

onMounted(loadTasks)
</script>

<style scoped>
.home-container {
  height: 100vh;
}
.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
