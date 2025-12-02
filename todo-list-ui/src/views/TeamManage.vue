<template>
  <div class="team-manage">
    <el-card>
      <template #header>
        <div class="header">
          <div class="title-section">
            <el-button text @click="goHome" class="back-btn">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span>团队管理</span>
          </div>
          <el-button type="primary" @click="showCreateDialog">创建团队</el-button>
        </div>
      </template>
      <el-table :data="teams" style="width: 100%">
        <el-table-column prop="name" label="团队名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createTime" label="创建时间" :formatter="formatCreateTime" />
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEnterTeam(row.id)">进入团队</el-button>
            <el-button size="small" @click="showMembers(row)">成员</el-button>
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑团队' : '创建团队'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="团队名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="memberDialogVisible" title="团队成员" width="600px">
      <div style="margin-bottom: 16px">
        <el-button type="primary" size="small" @click="showAddMemberDialog">添加成员</el-button>
      </div>
      <el-table :data="members" style="width: 100%">
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            {{ row.role === 1 ? '所有者' : '成员' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleRemoveMember(row.userId)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="addMemberDialogVisible" title="添加成员" width="400px">
      <el-form :model="memberForm" label-width="80px">
        <el-form-item label="用户ID">
          <el-input v-model.number="memberForm.userId" type="number" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="memberForm.role">
            <el-option label="成员" :value="2" />
            <el-option label="所有者" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMember">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getMyTeams, createTeam, updateTeam, deleteTeam, getMembers, addMember, removeMember, type Team } from '@/api/team'

const router = useRouter()
const teams = ref<Team[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<Team>({ name: '', description: '' })
const memberDialogVisible = ref(false)
const members = ref<any[]>([])
const currentTeamId = ref<string | number>(0)
const addMemberDialogVisible = ref(false)
const memberForm = ref({ userId: 0, role: 2 })

const loadTeams = async () => {
  const res = await getMyTeams()
  teams.value = res.data
}

const showCreateDialog = () => {
  isEdit.value = false
  form.value = { name: '', description: '' }
  dialogVisible.value = true
}

const showEditDialog = (team: Team) => {
  isEdit.value = true
  form.value = { ...team }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (isEdit.value && form.value.id) {
    await updateTeam(form.value.id, form.value)
    ElMessage.success('更新成功')
  } else {
    await createTeam(form.value)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  loadTeams()
}

const handleDelete = async (id: string | number) => {
  await ElMessageBox.confirm('确定删除该团队吗？', '提示', { type: 'warning' })
  await deleteTeam(id)
  ElMessage.success('删除成功')
  loadTeams()
}

const showMembers = async (team: Team) => {
  currentTeamId.value = team.id!
  const res = await getMembers(team.id!)
  members.value = res.data
  memberDialogVisible.value = true
}

const showAddMemberDialog = () => {
  memberForm.value = { userId: 0, role: 2 }
  addMemberDialogVisible.value = true
}

const handleAddMember = async () => {
  await addMember(currentTeamId.value, memberForm.value.userId, memberForm.value.role)
  ElMessage.success('添加成功')
  addMemberDialogVisible.value = false
  showMembers({ id: currentTeamId.value } as Team)
}

const handleRemoveMember = async (userId: string | number) => {
  await ElMessageBox.confirm('确定移除该成员吗？', '提示', { type: 'warning' })
  await removeMember(currentTeamId.value, userId)
  ElMessage.success('移除成功')
  showMembers({ id: currentTeamId.value } as Team)
}

onMounted(() => {
  loadTeams()
})

const goHome = () => {
  router.push('/home')
}

const handleEnterTeam = (teamId: string | number) => {
  router.push(`/teams/${teamId}/tasks`)
}

// 格式化创建时间
const formatCreateTime = (row: any) => {
  if (!row.createTime) return '-'
  const date = new Date(row.createTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}
</script>

<style scoped>
.team-manage {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  color: #6e6e80;
  padding: 8px 12px;
}

.back-btn:hover {
  background: #f5f7fa;
}

.title-section span {
  font-size: 18px;
  font-weight: 600;
  color: #202123;
}
</style>
