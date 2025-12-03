<template>
  <el-popover placement="bottom" :width="400" trigger="click">
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
        <el-button :icon="Bell" circle />
      </el-badge>
    </template>

    <div class="notification-center">
      <div class="header">
        <h3>通知中心</h3>
        <el-button link size="small" @click="markAllAsRead" v-if="unreadCount > 0">
          全部已读
        </el-button>
      </div>

      <el-divider style="margin: 10px 0" />

      <div class="notification-list">
        <el-empty v-if="notifications.length === 0" description="暂无通知" :image-size="80" />

        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item"
          :class="{ unread: notification.status === 'unread' }"
        >
          <div class="notification-content">
            <p class="content-text">{{ notification.content }}</p>
            <span class="time">{{ formatTime(notification.sendTime) }}</span>
          </div>
          <div class="notification-actions">
            <el-button
              v-if="notification.status === 'unread'"
              link
              size="small"
              @click="markAsRead(notification.id)"
            >
              标记已读
            </el-button>
            <el-button
              link
              size="small"
              type="danger"
              @click="deleteNotification(notification.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getNotifications,
  getUnreadCount,
  markAsRead as markAsReadApi,
  deleteNotification as deleteNotificationApi,
  type Notification
} from '../api/notification'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const notifications = ref<Notification[]>([])
const unreadCount = ref(0)

const loadNotifications = async () => {
  try {
    const response = await getNotifications()
    notifications.value = response.data || []
  } catch (error) {
    console.error('加载通知失败:', error)
  }
}

const loadUnreadCount = async () => {
  try {
    const response = await getUnreadCount()
    unreadCount.value = response.data || 0
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

const markAsRead = async (id: number) => {
  try {
    await markAsReadApi(id)
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.status = 'read'
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const markAllAsRead = async () => {
  const unreadNotifications = notifications.value.filter(n => n.status === 'unread')
  for (const notification of unreadNotifications) {
    await markAsRead(notification.id)
  }
  ElMessage.success('已全部标记为已读')
}

const deleteNotification = async (id: number) => {
  try {
    await deleteNotificationApi(id)
    const index = notifications.value.findIndex(n => n.id === id)
    if (index !== -1) {
      const notification = notifications.value[index]
      if (notification.status === 'unread') {
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
      notifications.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

const refresh = () => {
  loadNotifications()
  loadUnreadCount()
}

onMounted(() => {
  loadNotifications()
  loadUnreadCount()

  // 每30秒刷新一次
  setInterval(refresh, 30000)
})

defineExpose({ refresh })
</script>

<style scoped>
.notification-badge {
  margin-right: 10px;
}

.notification-center {
  max-height: 500px;
  overflow-y: auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-content {
  margin-bottom: 8px;
}

.content-text {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.time {
  font-size: 12px;
  color: #909399;
}

.notification-actions {
  display: flex;
  gap: 10px;
}
</style>
