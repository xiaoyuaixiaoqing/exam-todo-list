<template>
  <el-dialog :model-value="visible" :title="task.id ? '编辑任务' : '新建任务'" width="500px" @close="$emit('close')">
    <el-form :model="task" label-width="80px">
      <el-form-item label="标题">
        <el-input v-model="task.title" placeholder="请输入任务标题" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="task.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="task.category" style="width: 100%">
          <el-option label="工作" value="工作" />
          <el-option label="学习" value="学习" />
          <el-option label="生活" value="生活" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" v-if="task.id">
        <el-select v-model="task.status" style="width: 100%">
          <el-option label="待办" :value="0" />
          <el-option label="未完成" :value="1" />
          <el-option label="已完成" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级">
        <el-select v-model="task.priority" style="width: 100%">
          <el-option label="低" :value="1" />
          <el-option label="中" :value="2" />
          <el-option label="高" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="截止日期">
        <el-date-picker v-model="task.dueDate" type="datetime" style="width: 100%" :disabledDate="disabledDate" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div style="display: flex; justify-content: space-between; width: 100%;">
        <el-button @click="$emit('aiClassify')" :icon="MagicStick">AI智能分类</el-button>
        <div>
          <el-button @click="$emit('close')">取消</el-button>
          <el-button type="primary" @click="$emit('save', task)">保存</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { MagicStick } from '@element-plus/icons-vue'

defineProps<{
  visible: boolean
  task: any
}>()

defineEmits<{
  close: []
  save: [task: any]
  aiClassify: []
}>()

const disabledDate = (time: Date) => {
  return time.getTime() < new Date().getTime()
}
</script>
