<template>
  <el-dialog v-model="visible" title="语音输入" width="500px">
    <div class="voice-input">
      <div class="recording-status">
        <el-icon v-if="isRecording" class="recording-icon" :size="60" color="#f56c6c">
          <Microphone />
        </el-icon>
        <el-icon v-else class="mic-icon" :size="60" color="#909399">
          <Microphone />
        </el-icon>
        <p class="status-text">{{ statusText }}</p>
      </div>

      <div class="controls">
        <el-button
          v-if="!isRecording"
          type="primary"
          size="large"
          @click="startRecording"
          :loading="isProcessing"
        >
          开始录音
        </el-button>
        <el-button
          v-else
          type="danger"
          size="large"
          @click="stopRecording"
        >
          停止录音
        </el-button>
      </div>

      <div v-if="recognizedText" class="result">
        <el-divider />
        <h4>识别结果：</h4>
        <p>{{ recognizedText }}</p>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Microphone } from '@element-plus/icons-vue'
import { voiceToTask } from '../api/ai'

const visible = ref(false)
const isRecording = ref(false)
const isProcessing = ref(false)
const recognizedText = ref('')
let mediaRecorder: MediaRecorder | null = null
let audioChunks: Blob[] = []

const statusText = computed(() => {
  if (isProcessing.value) return '正在识别...'
  if (isRecording.value) return '录音中...'
  return '点击开始录音'
})

const emit = defineEmits<{
  (e: 'taskCreated', taskInfo: any): void
}>()

const open = () => {
  visible.value = true
  recognizedText.value = ''
}

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (event) => {
      audioChunks.push(event.data)
    }

    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/wav' })
      await processAudio(audioBlob)
      stream.getTracks().forEach(track => track.stop())
    }

    mediaRecorder.start()
    isRecording.value = true
  } catch (error) {
    ElMessage.error('无法访问麦克风，请检查权限设置')
  }
}

const stopRecording = () => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
    isRecording.value = false
  }
}

const processAudio = async (audioBlob: Blob) => {
  isProcessing.value = true
  try {
    const file = new File([audioBlob], 'recording.wav', { type: 'audio/wav' })
    const response = await voiceToTask(file)

    if (response.data) {
      recognizedText.value = response.data.title
      ElMessage.success('语音识别成功')
      emit('taskCreated', response.data)
      setTimeout(() => {
        visible.value = false
      }, 1500)
    }
  } catch (error) {
    ElMessage.error('语音识别失败，请重试')
  } finally {
    isProcessing.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.voice-input {
  text-align: center;
  padding: 20px;
}

.recording-status {
  margin-bottom: 30px;
}

.recording-icon {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.mic-icon {
  opacity: 0.6;
}

.status-text {
  margin-top: 15px;
  font-size: 16px;
  color: #606266;
}

.controls {
  margin: 20px 0;
}

.result {
  text-align: left;
  margin-top: 20px;
}

.result h4 {
  margin-bottom: 10px;
  color: #303133;
}

.result p {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #606266;
}
</style>
