<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElMessage, ElTag, ElMessageBox } from 'element-plus'
import { stationApi, equipmentApi, riderApi, type TrainingStation, type ObstacleEquipment, type Rider } from '@/api'

const stationList = ref<TrainingStation[]>([])
const equipmentList = ref<ObstacleEquipment[]>([])
const riderList = ref<Rider[]>([])
const dialogVisible = ref(false)
const bindDialogVisible = ref(false)
const form = ref({
  stationCode: '',
  stationName: '',
  riderId: undefined as number | undefined,
  equipmentId: undefined as number | undefined
})
const bindForm = ref({
  riderId: undefined as number | undefined,
  equipmentId: undefined as number | undefined
})
const editMode = ref(false)
const editingId = ref(0)
const bindingStationId = ref(0)

const levelColors: Record<number, 'success' | 'primary' | 'warning' | 'danger' | 'info'> = {
  1: 'success',
  2: 'primary',
  3: 'warning',
  4: 'danger',
  5: 'info'
}

const getLevelName = (level: number) => {
  const levelNames: Record<number, string> = {
    1: '初级',
    2: '中级',
    3: '高级',
    4: '专业级',
    5: '大师级'
  }
  return levelNames[level] || '未知'
}

const getRiderOptions = computed(() => {
  return riderList.value.map(r => ({
    label: `${r.riderCode} - ${r.riderName} (${getLevelName(r.currentLevel)})`,
    value: r.id
  }))
})

const equipmentOptionLabel = (e: ObstacleEquipment) => {
  let recheckTag = ''
  if (!e.rechecked) {
    recheckTag = '【未复核·禁绑】'
  } else if (e.recheckResult === 'MISMATCH') {
    recheckTag = '【高度不符·禁绑】'
  }
  return `${e.equipmentCode} - ${e.equipmentName} (${getLevelName(e.adaptLevel)})${recheckTag}`
}

const equipmentBindBlockReason = (equipment: ObstacleEquipment | undefined) => {
  if (!equipment) return ''
  if (!equipment.rechecked) {
    return `杆[${equipment.equipmentName}]未做杆高复核，不能绑上训练位`
  }
  if (equipment.recheckResult === 'MISMATCH') {
    return `杆[${equipment.equipmentName}]复核结论为高度不符（标称 ${equipment.obstacleHeight}cm / 实测 ${equipment.measuredHeight}cm），已拦住绑定`
  }
  return ''
}

const getEquipmentOptions = computed(() => {
  return equipmentList.value.map(e => ({
    label: equipmentOptionLabel(e),
    value: e.id,
    // 未做杆高复核或复核高度不符的杆不能绑上训练位
    disabled: !e.bindable
  }))
})

const getAvailableEquipmentOptions = computed(() => {
  const selectedRiderId = bindForm.value.riderId
  if (!selectedRiderId) return getEquipmentOptions.value

  const rider = riderList.value.find(r => r.id === selectedRiderId)
  if (!rider) return getEquipmentOptions.value

  return equipmentList.value
    .filter(e => e.adaptLevel <= rider.currentLevel)
    .map(e => ({
      label: equipmentOptionLabel(e),
      value: e.id,
      // 未做杆高复核或复核高度不符的杆不能绑上训练位
      disabled: !e.bindable
    }))
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  try {
    stationList.value = await stationApi.listAll() as unknown as TrainingStation[]
    equipmentList.value = await equipmentApi.listAll() as unknown as ObstacleEquipment[]
    riderList.value = await riderApi.listAll() as unknown as Rider[]
  } catch (error: any) {
    ElMessage.error('加载数据失败')
  }
}

const openAddDialog = () => {
  editMode.value = false
  editingId.value = 0
  form.value = {
    stationCode: '',
    stationName: '',
    riderId: undefined,
    equipmentId: undefined
  }
  dialogVisible.value = true
}

const openEditDialog = (item: TrainingStation) => {
  editMode.value = true
  editingId.value = item.id
  form.value = {
    stationCode: item.stationCode,
    stationName: item.stationName,
    riderId: item.rider?.id,
    equipmentId: item.equipment?.id
  }
  dialogVisible.value = true
}

const openBindDialog = (item: TrainingStation) => {
  bindingStationId.value = item.id
  bindForm.value = {
    riderId: item.rider?.id,
    equipmentId: item.equipment?.id
  }
  bindDialogVisible.value = true
}

const handleSubmit = async () => {
  if (form.value.equipmentId) {
    const blockReason = equipmentBindBlockReason(
      equipmentList.value.find(e => e.id === form.value.equipmentId)
    )
    if (blockReason) {
      ElMessageBox.alert(blockReason, '禁止绑定', { type: 'error' })
      return
    }
  }
  try {
    if (editMode.value) {
      await stationApi.update(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await stationApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleBind = async () => {
  if (!bindForm.value.riderId || !bindForm.value.equipmentId) {
    ElMessage.warning('请选择骑手和设备')
    return
  }

  const rider = riderList.value.find(r => r.id === bindForm.value.riderId)
  const equipment = equipmentList.value.find(e => e.id === bindForm.value.equipmentId)

  // 杆高复核拦截：未复核 / 高度不符的杆不能绑上训练位（以后端校验为准）
  const blockReason = equipmentBindBlockReason(equipment)
  if (blockReason) {
    ElMessageBox.alert(blockReason, '禁止绑定', { type: 'error' })
    return
  }

  if (rider && equipment && equipment.adaptLevel > rider.currentLevel) {
    ElMessageBox.alert(
      `等级匹配失败：骑手[${rider.riderName}]当前等级[${getLevelName(rider.currentLevel)}]无法使用障碍设备[${equipment.equipmentName}]，该设备适配等级[${getLevelName(equipment.adaptLevel)}]`,
      '等级不匹配',
      { type: 'error' }
    )
    return
  }

  try {
    await stationApi.bind(bindingStationId.value, bindForm.value.riderId, bindForm.value.equipmentId)
    ElMessage.success('绑定成功')
    bindDialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessageBox.alert(error.message || '绑定失败', '绑定失败', { type: 'error' })
  }
}

const handleDelete = async (id: number) => {
  try {
    await stationApi.delete(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}
</script>

<template>
  <div class="station-page">
    <div style="margin-bottom: 20px; display: flex; justify-content: flex-end;">
      <ElButton type="primary" @click="openAddDialog">添加训练位</ElButton>
    </div>
    <ElTable :data="stationList" border>
      <ElTableColumn prop="stationCode" label="训练位编号" />
      <ElTableColumn prop="stationName" label="训练位名称" />
      <ElTableColumn label="骑手">
        <template #default="{ row }">
          <span v-if="row.rider">{{ row.rider.riderName }}</span>
          <span v-else style="color: #909399;">未绑定</span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="骑手等级">
        <template #default="{ row }">
          <ElTag v-if="row.rider" :type="levelColors[row.rider.currentLevel]">{{ getLevelName(row.rider.currentLevel) }}</ElTag>
          <span v-else style="color: #909399;">-</span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="设备">
        <template #default="{ row }">
          <span v-if="row.equipment">{{ row.equipment.equipmentName }}</span>
          <span v-else style="color: #909399;">未绑定</span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="设备适配等级">
        <template #default="{ row }">
          <ElTag v-if="row.equipment" :type="levelColors[row.equipment.adaptLevel]">{{ getLevelName(row.equipment.adaptLevel) }}</ElTag>
          <span v-else style="color: #909399;">-</span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="杆高复核" width="100" align="center">
        <template #default="{ row }">
          <ElTag v-if="row.equipment && row.equipment.recheckResult === 'MATCH'" type="success">高度相符</ElTag>
          <ElTag v-else-if="row.equipment && row.equipment.recheckResult === 'MISMATCH'" type="danger">高度不符</ElTag>
          <span v-if="!row.equipment" style="color: #909399;">-</span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="状态">
        <template #default="{ row }">
          <span v-if="row.rider && row.equipment" style="color: #67c23a;">已绑定</span>
          <span v-else style="color: #e6a23c;">待绑定</span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="操作">
        <template #default="{ row }">
          <ElButton type="primary" size="small" @click="openEditDialog(row as unknown as TrainingStation)">编辑</ElButton>
          <ElButton type="success" size="small" @click="openBindDialog(row as unknown as TrainingStation)">绑定</ElButton>
          <ElButton type="danger" size="small" @click="handleDelete(row.id)">删除</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElDialog :visible="dialogVisible" :title="editMode ? '编辑训练位' : '添加训练位'" @close="dialogVisible = false">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="训练位编号" prop="stationCode">
          <ElInput v-model="form.stationCode" placeholder="请输入训练位编号" />
        </ElFormItem>
        <ElFormItem label="训练位名称" prop="stationName">
          <ElInput v-model="form.stationName" placeholder="请输入训练位名称" />
        </ElFormItem>
        <ElFormItem label="骑手" prop="riderId">
          <ElSelect v-model="form.riderId" placeholder="请选择骑手">
            <ElOption v-for="option in getRiderOptions" :key="option.value" :label="option.label" :value="option.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="设备" prop="equipmentId">
          <ElSelect v-model="form.equipmentId" placeholder="请选择设备">
            <ElOption
              v-for="option in getEquipmentOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
              :disabled="option.disabled"
            />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>

    <ElDialog :visible="bindDialogVisible" title="绑定骑手与设备" @close="bindDialogVisible = false">
      <ElForm :model="bindForm" label-width="100px">
        <ElFormItem label="选择骑手" prop="riderId">
          <ElSelect v-model="bindForm.riderId" placeholder="请选择骑手" @change="bindForm.equipmentId = undefined">
            <ElOption v-for="option in getRiderOptions" :key="option.value" :label="option.label" :value="option.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="选择设备" prop="equipmentId">
          <ElSelect v-model="bindForm.equipmentId" placeholder="请选择设备">
            <ElOption
              v-for="option in getAvailableEquipmentOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
              :disabled="option.disabled"
            />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <div style="margin-top: 10px; padding: 10px; background: #f5f5f5; border-radius: 4px;">
        <p style="color: #606266; font-size: 14px; margin-bottom: 4px;">
          <strong>等级匹配规则：</strong>骑手等级必须大于或等于设备适配等级才能绑定。
        </p>
        <p style="color: #f56c6c; font-size: 14px;">
          <strong>杆高复核规则：</strong>未做杆高复核、或实测与标称相差超过约定 5cm 判定为高度不符的杆，一律禁止绑上训练位（下拉中已置灰）。
        </p>
      </div>
      <template #footer>
        <ElButton @click="bindDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleBind">确定绑定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>