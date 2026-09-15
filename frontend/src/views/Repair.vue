<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElMessage, ElTag } from 'element-plus'
import { equipmentApi, equipmentRepairApi, type EquipmentRepairOrder, type ObstacleEquipment } from '@/api'

const repairOrders = ref<EquipmentRepairOrder[]>([])
const activeEquipment = ref<ObstacleEquipment[]>([])
const sendDialogVisible = ref(false)
const returnDialogVisible = ref(false)
const filterStatus = ref<'IN_REPAIR' | 'RETURNED' | ''>('IN_REPAIR')
const sendingEquipment = ref<ObstacleEquipment | null>(null)
const returningOrder = ref<EquipmentRepairOrder | null>(null)
const sendForm = ref({
  faultDescription: '',
  handlerCoach: ''
})
const returnForm = ref({
  repairConclusion: ''
})

const loadOrders = async () => {
  try {
    repairOrders.value = await equipmentRepairApi.list(filterStatus.value || undefined) as unknown as EquipmentRepairOrder[]
  } catch (error: any) {
    ElMessage.error(error.message || '加载维修单失败')
  }
}

const loadActiveEquipment = async () => {
  try {
    // 在修器材状态为“在修中”，不会出现在在用器材名单里
    activeEquipment.value = await equipmentApi.listAll() as unknown as ObstacleEquipment[]
  } catch (error: any) {
    ElMessage.error(error.message || '加载器材名单失败')
  }
}

const loadData = async () => {
  await Promise.all([loadOrders(), loadActiveEquipment()])
}

onMounted(() => {
  loadData()
})

const openSendDialog = (equipment?: ObstacleEquipment) => {
  sendingEquipment.value = equipment || null
  sendForm.value = {
    faultDescription: '',
    handlerCoach: ''
  }
  sendDialogVisible.value = true
}

const openReturnDialog = (order: EquipmentRepairOrder) => {
  returningOrder.value = order
  returnForm.value = {
    repairConclusion: ''
  }
  returnDialogVisible.value = true
}

const handleSend = async () => {
  if (!sendingEquipment.value) {
    ElMessage.warning('请选择要送修的杆')
    return
  }
  if (!sendForm.value.faultDescription.trim()) {
    ElMessage.warning('请填写故障说明')
    return
  }
  if (!sendForm.value.handlerCoach.trim()) {
    ElMessage.warning('请填写经办教练')
    return
  }

  try {
    await equipmentRepairApi.send(sendingEquipment.value.id, {
      faultDescription: sendForm.value.faultDescription.trim(),
      handlerCoach: sendForm.value.handlerCoach.trim()
    })
    ElMessage.success('已从训练位拆下并送修')
    sendDialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '送修失败')
  }
}

const handleReturn = async () => {
  if (!returningOrder.value) return
  if (!returnForm.value.repairConclusion.trim()) {
    ElMessage.warning('请填写修复结论')
    return
  }

  try {
    await equipmentRepairApi.returnOrder(returningOrder.value.id, {
      repairConclusion: returnForm.value.repairConclusion.trim()
    })
    ElMessage.success('已修好归还，器材恢复为在用')
    returnDialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '归还失败')
  }
}

const formatTime = (time?: string | null) => {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 16)
}

const handleFilterChange = () => {
  loadOrders()
}
</script>

<template>
  <div class="repair-page">
    <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
      <ElSelect v-model="filterStatus" style="width: 160px;" @change="handleFilterChange">
        <ElOption label="在修中" value="IN_REPAIR" />
        <ElOption label="已归还" value="RETURNED" />
        <ElOption label="全部维修单" value="" />
      </ElSelect>
      <ElButton type="primary" @click="openSendDialog()">开单送修</ElButton>
    </div>

    <ElTable :data="repairOrders" border>
      <ElTableColumn prop="equipmentCode" label="器材编号" width="110" />
      <ElTableColumn prop="equipmentName" label="器材名称" width="160" />
      <ElTableColumn label="维修状态" width="100" align="center">
        <template #default="{ row }">
          <ElTag :type="row.status === 'IN_REPAIR' ? 'warning' : 'success'">{{ row.statusName }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="faultDescription" label="故障说明" show-overflow-tooltip />
      <ElTableColumn prop="handlerCoach" label="经办教练" width="110" />
      <ElTableColumn label="送修时间" width="180">
        <template #default="{ row }">{{ formatTime(row.sentTime) }}</template>
      </ElTableColumn>
      <ElTableColumn prop="repairConclusion" label="修复结论" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.repairConclusion">{{ row.repairConclusion }}</span>
          <span v-else style="color: #909399;">未归还</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="returnedTime" label="归还时间" width="180">
        <template #default="{ row }">
          <span v-if="row.returnedTime">{{ formatTime(row.returnedTime) }}</span>
          <span v-else style="color: #909399;">-</span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <ElButton
            v-if="row.status === 'IN_REPAIR'"
            type="success"
            size="small"
            @click="openReturnDialog(row as EquipmentRepairOrder)"
          >
            修好归还
          </ElButton>
          <span v-else style="color: #909399;">已完成</span>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElDialog :visible="sendDialogVisible" title="开单送修" @close="sendDialogVisible = false">
      <ElForm :model="sendForm" label-width="100px">
        <ElFormItem label="障碍杆" required>
          <ElSelect
            :model-value="sendingEquipment?.id"
            placeholder="请选择要送修的杆"
            style="width: 100%;"
            @update:model-value="(id: number) => sendingEquipment = activeEquipment.find(e => e.id === id) || null"
          >
            <ElOption
              v-for="equipment in activeEquipment"
              :key="equipment.id"
              :label="`${equipment.equipmentCode} - ${equipment.equipmentName}`"
              :value="equipment.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="故障说明" required>
          <ElInput
            v-model="sendForm.faultDescription"
            type="textarea"
            :rows="3"
            placeholder="请写明故障现象；提交时会先把杆从训练位拆下"
          />
        </ElFormItem>
        <ElFormItem label="经办教练" required>
          <ElInput v-model="sendForm.handlerCoach" placeholder="请输入经办教练" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="sendDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSend">拆下并送修</ElButton>
      </template>
    </ElDialog>

    <ElDialog :visible="returnDialogVisible" title="修好归还" @close="returnDialogVisible = false">
      <ElForm :model="returnForm" label-width="100px">
        <ElFormItem v-if="returningOrder" label="障碍杆">
          <span>{{ returningOrder.equipmentCode }} - {{ returningOrder.equipmentName }}</span>
        </ElFormItem>
        <ElFormItem v-if="returningOrder" label="故障说明">
          <span>{{ returningOrder.faultDescription }}</span>
        </ElFormItem>
        <ElFormItem label="修复结论" required>
          <ElInput
            v-model="returnForm.repairConclusion"
            type="textarea"
            :rows="3"
            placeholder="必须填写修复结论，未填写不能恢复为在用"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="returnDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleReturn">确认归还</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
