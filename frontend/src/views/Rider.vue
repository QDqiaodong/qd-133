<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElInputNumber, ElMessage, ElTag } from 'element-plus'
import { riderApi, type Rider } from '@/api'

const riderList = ref<Rider[]>([])
const dialogVisible = ref(false)
const levelDialogVisible = ref(false)
const form = ref({
  riderCode: '',
  riderName: '',
  age: 0,
  currentLevel: 1,
  phone: '',
  email: ''
})
const levelForm = ref({
  newLevel: 1,
  changeReason: '',
  operator: ''
})
const editMode = ref(false)
const editingId = ref(0)
const currentRider = ref<Rider | null>(null)

const levelOptions = [
  { label: '初级 (1)', value: 1 },
  { label: '中级 (2)', value: 2 },
  { label: '高级 (3)', value: 3 },
  { label: '专业级 (4)', value: 4 },
  { label: '大师级 (5)', value: 5 }
]

const levelColors: Record<number, 'success' | 'primary' | 'warning' | 'danger' | 'info'> = {
  1: 'success',
  2: 'primary',
  3: 'warning',
  4: 'danger',
  5: 'info'
}

const getLevelName = (level: number) => {
  return levelOptions.find(o => o.value === level)?.label || '未知'
}

onMounted(() => {
  loadRiders()
})

const loadRiders = async () => {
  try {
    riderList.value = await riderApi.listAll() as unknown as Rider[]
  } catch (error: any) {
    ElMessage.error('加载骑手列表失败')
  }
}

const openAddDialog = () => {
  editMode.value = false
  editingId.value = 0
  form.value = {
    riderCode: '',
    riderName: '',
    age: 0,
    currentLevel: 1,
    phone: '',
    email: ''
  }
  dialogVisible.value = true
}

const openEditDialog = (item: Rider) => {
  editMode.value = true
  editingId.value = item.id
  form.value = {
    riderCode: item.riderCode,
    riderName: item.riderName,
    age: item.age || 0,
    currentLevel: item.currentLevel,
    phone: item.phone || '',
    email: item.email || ''
  }
  dialogVisible.value = true
}

const openLevelDialog = (item: Rider) => {
  currentRider.value = item
  levelForm.value = {
    newLevel: item.currentLevel,
    changeReason: '',
    operator: ''
  }
  levelDialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (editMode.value) {
      await riderApi.update(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await riderApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRiders()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleLevelUpdate = async () => {
  if (!currentRider.value) return
  
  try {
    await riderApi.updateLevel({
      riderId: currentRider.value.id,
      newLevel: levelForm.value.newLevel,
      changeReason: levelForm.value.changeReason,
      operator: levelForm.value.operator
    })
    ElMessage.success('等级更新成功')
    levelDialogVisible.value = false
    loadRiders()
  } catch (error: any) {
    ElMessage.error(error.message || '等级更新失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await riderApi.delete(id)
    ElMessage.success('删除成功')
    loadRiders()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}
</script>

<template>
  <div class="rider-page">
    <div style="margin-bottom: 20px; display: flex; justify-content: flex-end;">
      <ElButton type="primary" @click="openAddDialog">添加骑手</ElButton>
    </div>
    <ElTable :data="riderList" border>
      <ElTableColumn prop="riderCode" label="骑手编号" />
      <ElTableColumn prop="riderName" label="骑手姓名" />
      <ElTableColumn prop="age" label="年龄" />
      <ElTableColumn prop="currentLevel" label="当前等级">
        <template #default="{ row }">
          <ElTag :type="levelColors[row.currentLevel]">{{ getLevelName(row.currentLevel) }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="currentLevelDesc" label="等级描述" show-overflow-tooltip />
      <ElTableColumn prop="phone" label="联系电话" />
      <ElTableColumn prop="email" label="邮箱" />
      <ElTableColumn label="操作">
        <template #default="{ row }">
          <ElButton type="primary" size="small" @click="openEditDialog(row as unknown as Rider)">编辑</ElButton>
          <ElButton type="success" size="small" @click="openLevelDialog(row as unknown as Rider)">升级</ElButton>
          <ElButton type="danger" size="small" @click="handleDelete(row.id)">删除</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElDialog :visible="dialogVisible" :title="editMode ? '编辑骑手' : '添加骑手'" @close="dialogVisible = false">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="骑手编号" prop="riderCode">
          <ElInput v-model="form.riderCode" placeholder="请输入骑手编号" />
        </ElFormItem>
        <ElFormItem label="骑手姓名" prop="riderName">
          <ElInput v-model="form.riderName" placeholder="请输入骑手姓名" />
        </ElFormItem>
        <ElFormItem label="年龄" prop="age">
          <ElInputNumber v-model="form.age" :min="1" :max="100" />
        </ElFormItem>
        <ElFormItem label="当前等级" prop="currentLevel">
          <ElSelect v-model="form.currentLevel">
            <ElOption v-for="option in levelOptions" :key="option.value" :label="option.label" :value="option.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="联系电话" prop="phone">
          <ElInput v-model="form.phone" placeholder="请输入联系电话" />
        </ElFormItem>
        <ElFormItem label="邮箱" prop="email">
          <ElInput v-model="form.email" placeholder="请输入邮箱" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>

    <ElDialog :visible="levelDialogVisible" title="骑手等级升级" @close="levelDialogVisible = false">
      <div v-if="currentRider" style="margin-bottom: 20px;">
        <p>骑手：{{ currentRider.riderName }}</p>
        <p>当前等级：<ElTag :type="levelColors[currentRider.currentLevel]">{{ getLevelName(currentRider.currentLevel) }}</ElTag></p>
      </div>
      <ElForm :model="levelForm" label-width="100px">
        <ElFormItem label="新等级" prop="newLevel">
          <ElSelect v-model="levelForm.newLevel">
            <ElOption 
              v-for="option in levelOptions" 
              :key="option.value" 
              :label="option.label" 
              :value="option.value"
              :disabled="option.value <= (currentRider?.currentLevel || 1)"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="升级原因" prop="changeReason">
          <ElInput v-model="levelForm.changeReason" type="textarea" placeholder="请输入升级原因" />
        </ElFormItem>
        <ElFormItem label="操作人" prop="operator">
          <ElInput v-model="levelForm.operator" placeholder="请输入操作人姓名" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="levelDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleLevelUpdate">确定升级</ElButton>
      </template>
    </ElDialog>
  </div>
</template>