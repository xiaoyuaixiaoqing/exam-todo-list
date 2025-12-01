<template>
  <div class="log-container">
    <div class="log-header">
      <h1>操作日志</h1>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <el-timeline>
      <el-timeline-item v-for="log in logs" :key="log.id" :timestamp="formatTime(log.createTime)">
        <div class="log-item">
          <div class="log-header">
            <div class="log-action">{{ getActionText(log.action) }}</div>
            <el-button v-if="log.action !== 'DELETE'" size="small" type="primary" link @click="handleRollback(log)">
              回滚到此状态
            </el-button>
          </div>
          <div v-if="log.action === 'UPDATE' && log.oldValue && log.newValue" class="log-changes">
            <div v-for="change in getChanges(log.oldValue, log.newValue)" :key="change.field" class="change-item">
              <span class="field-name">{{ change.field }}:</span>
              <span class="old-value">{{ change.oldValue }}</span>
              <span class="arrow">→</span>
              <span class="new-value">{{ change.newValue }}</span>
            </div>
          </div>
          <div v-else-if="log.action === 'CREATE' && log.newValue" class="log-detail">
            创建了任务: {{ parseTask(log.newValue).title }}
          </div>
          <div v-else-if="log.action === 'DELETE' && log.oldValue" class="log-detail">
            删除了任务: {{ parseTask(log.oldValue).title }}
          </div>
          <div v-else-if="log.action === 'RESTORE' && log.newValue" class="log-detail">
            从回收站恢复了任务: {{ parseTask(log.newValue).title }}
          </div>
          <div v-else-if="log.action === 'ROLLBACK' && log.oldValue && log.newValue" class="log-changes">
            <div v-for="change in getChanges(log.oldValue, log.newValue)" :key="change.field" class="change-item">
              <span class="field-name">{{ change.field }}:</span>
              <span class="old-value">{{ change.oldValue }}</span>
              <span class="arrow">→</span>
              <span class="new-value">{{ change.newValue }}</span>
            </div>
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-if="logs.length === 0" description="暂无日志" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTaskLogs, rollbackToLog } from '../api/log'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const logs = ref([])

const loadLogs = async () => {
  const taskId = route.params.id as string
  const res: any = await getTaskLogs(taskId)
  logs.value = res.data
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getActionText = (action: string) => {
  const map: any = {
    CREATE: '创建任务',
    UPDATE: '更新任务',
    DELETE: '删除任务',
    RESTORE: '恢复任务',
    ROLLBACK: '历史回放'
  }
  return map[action] || action
}

const parseTask = (jsonStr: string) => {
  try {
    return JSON.parse(jsonStr)
  } catch {
    return {}
  }
}

const fieldNames: any = {
  title: '标题',
  description: '描述',
  status: '状态',
  priority: '优先级',
  category: '分类',
  dueDate: '截止日期'
}

const statusNames: any = { 0: '待办', 1: '未完成', 2: '已完成', 4: '已超期' }
const priorityNames: any = { 1: '低', 2: '中', 3: '高' }

const getChanges = (oldJson: string, newJson: string) => {
  const oldTask = parseTask(oldJson)
  const newTask = parseTask(newJson)
  const changes: any[] = []

  const fields = ['title', 'description', 'status', 'priority', 'category', 'dueDate']
  fields.forEach(field => {
    if (oldTask[field] !== newTask[field]) {
      let oldValue = oldTask[field]
      let newValue = newTask[field]

      if (field === 'status') {
        oldValue = statusNames[oldValue] || oldValue
        newValue = statusNames[newValue] || newValue
      } else if (field === 'priority') {
        oldValue = priorityNames[oldValue] || oldValue
        newValue = priorityNames[newValue] || newValue
      } else if (field === 'dueDate') {
        oldValue = oldValue ? dayjs(oldValue).format('YYYY-MM-DD HH:mm') : '无'
        newValue = newValue ? dayjs(newValue).format('YYYY-MM-DD HH:mm') : '无'
      }

      changes.push({
        field: fieldNames[field] || field,
        oldValue: oldValue || '无',
        newValue: newValue || '无'
      })
    }
  })

  return changes
}

const handleRollback = async (log: any) => {
  try {
    await ElMessageBox.confirm(`确认回滚到此状态？`, '历史回放', {
      confirmButtonText: '确认回滚',
      cancelButtonText: '取消'
    })
    const taskId = route.params.id as string
    await rollbackToLog(taskId, log.id, 1)
    ElMessage.success('回滚成功')
    router.back()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('回滚失败')
    }
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.log-container {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.log-header h1 {
  margin: 0;
  font-size: 20px;
}

.log-item {
  padding: 8px 0;
}

.log-item .log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.log-action {
  font-weight: 600;
}

.log-detail {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.log-changes {
  margin-top: 8px;
}

.change-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  font-size: 14px;
}

.field-name {
  font-weight: 500;
  color: #333;
  min-width: 70px;
}

.old-value {
  color: #f56c6c;
  text-decoration: line-through;
}

.arrow {
  color: #909399;
}

.new-value {
  color: #67c23a;
  font-weight: 500;
}
</style>
