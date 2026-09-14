<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElInputNumber, ElMessage, ElTag } from 'element-plus'
import { equipmentApi, type ObstacleEquipment } from '@/api'

const equipmentList = ref<ObstacleEquipment[]>([])
const dialogVisible = ref(false)
const form = ref({
  equipmentCode: '',
  equipmentName: '',
  obstacleHeight: 0,
  adaptLevel: 1,
  description: ''
})
const editMode = ref(false)
const editingId = ref(0)

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
  loadEquipment()
})

const loadEquipment = async () => {
  try {
    equipmentList.value = await equipmentApi.listAll() as unknown as ObstacleEquipment[]
  } catch (error: any) {
    ElMessage.error('加载器材列表失败')
  }
}

const openAddDialog = () => {
  editMode.value = false
  editingId.value = 0
  form.value = {
    equipmentCode: '',
    equipmentName: '',
    obstacleHeight: 0,
    adaptLevel: 1,
    description: ''
  }
  dialogVisible.value = true
}

const openEditDialog = (item: ObstacleEquipment) => {
  editMode.value = true
  editingId.value = item.id
  form.value = {
    equipmentCode: item.equipmentCode,
    equipmentName: item.equipmentName,
    obstacleHeight: item.obstacleHeight,
    adaptLevel: item.adaptLevel,
    description: item.description || ''
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (editMode.value) {
      await equipmentApi.update(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await equipmentApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadEquipment()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await equipmentApi.delete(id)
    ElMessage.success('删除成功')
    loadEquipment()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}
</script>

<template>
  <div class="equipment-page">
    <div style="margin-bottom: 20px; display: flex; justify-content: flex-end;">
      <ElButton type="primary" @click="openAddDialog">添加器材</ElButton>
    </div>
    <ElTable :data="equipmentList" border>
      <ElTableColumn prop="equipmentCode" label="器材编号" />
      <ElTableColumn prop="equipmentName" label="器材名称" />
      <ElTableColumn prop="obstacleHeight" label="障碍高度(cm)" />
      <ElTableColumn prop="adaptLevel" label="适配等级">
        <template #default="{ row }">
          <ElTag :type="levelColors[row.adaptLevel]">{{ getLevelName(row.adaptLevel) }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="adaptLevelDesc" label="等级描述" show-overflow-tooltip />
      <ElTableColumn label="杆高复核" width="100" align="center">
        <template #default="{ row }">
          <ElTag v-if="row.recheckResult === 'MATCH'" type="success">高度相符</ElTag>
          <ElTag v-else-if="row.recheckResult === 'MISMATCH'" type="danger">高度不符</ElTag>
          <ElTag v-else type="info">未复核</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn label="能否绑定" width="100" align="center">
        <template #default="{ row }">
          <ElTag :type="row.bindable ? 'success' : 'danger'">{{ row.bindable ? '可绑定' : '禁绑' }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="description" label="备注" show-overflow-tooltip />
      <ElTableColumn label="操作">
        <template #default="{ row }">
          <ElButton type="primary" size="small" @click="openEditDialog(row as unknown as ObstacleEquipment)">编辑</ElButton>
          <ElButton type="danger" size="small" @click="handleDelete(row.id)">删除</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElDialog :visible="dialogVisible" :title="editMode ? '编辑器材' : '添加器材'" @close="dialogVisible = false">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="器材编号" prop="equipmentCode">
          <ElInput v-model="form.equipmentCode" placeholder="请输入器材编号" />
        </ElFormItem>
        <ElFormItem label="器材名称" prop="equipmentName">
          <ElInput v-model="form.equipmentName" placeholder="请输入器材名称" />
        </ElFormItem>
        <ElFormItem label="障碍高度" prop="obstacleHeight">
          <ElInputNumber v-model="form.obstacleHeight" :min="0" :max="300" placeholder="单位：cm" />
        </ElFormItem>
        <ElFormItem label="适配等级" prop="adaptLevel">
          <ElSelect v-model="form.adaptLevel">
            <ElOption v-for="option in levelOptions" :key="option.value" :label="option.label" :value="option.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="备注" prop="description">
          <ElInput v-model="form.description" type="textarea" placeholder="请输入备注信息" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>