<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  ElTable,
  ElTableColumn,
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElTag,
  ElRadioGroup,
  ElRadioButton,
  ElAlert
} from 'element-plus'
import { recheckApi, type ObstacleEquipment } from '@/api'

// 与后端 HeightRecheckRules.RECHECK_TOLERANCE_CM 保持一致的约定厘米数
const TOLERANCE_CM = 5

const equipmentList = ref<ObstacleEquipment[]>([])
const loading = ref(false)
// 筛选：''=全部，'1'=仅已复核，'0'=仅未复核
const filter = ref<'' | '1' | '0'>('')

const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(0)
const form = ref({
  equipmentName: '',
  nominalHeight: 0,
  measuredHeight: 0,
  reviewer: ''
})

const loadList = async () => {
  loading.value = true
  try {
    const rechecked = filter.value === '' ? undefined : filter.value === '1'
    equipmentList.value = (await recheckApi.list(rechecked)) as unknown as ObstacleEquipment[]
  } catch (error: any) {
    ElMessage.error(error?.message || '加载杆高复核列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadList)

const stats = computed(() => {
  const total = equipmentList.value.length
  const recheckedCount = equipmentList.value.filter(e => e.rechecked).length
  const mismatchCount = equipmentList.value.filter(e => e.recheckResult === 'MISMATCH').length
  const bindableCount = equipmentList.value.filter(e => e.bindable).length
  return { total, recheckedCount, mismatchCount, bindableCount }
})

const openRecheckDialog = (row: ObstacleEquipment) => {
  editingId.value = row.id
  form.value = {
    equipmentName: `${row.equipmentCode} - ${row.equipmentName}`,
    nominalHeight: row.obstacleHeight,
    measuredHeight: row.measuredHeight ?? row.obstacleHeight,
    reviewer: row.recheckReviewer ?? ''
  }
  dialogVisible.value = true
}

// 表单实时预览结论，提交仍以后端按同一约定计算的结论为准
const previewDiff = computed(() => {
  const v = Number(form.value.measuredHeight)
  return Number.isFinite(v) ? v - form.value.nominalHeight : 0
})
const previewMismatch = computed(() => Math.abs(previewDiff.value) > TOLERANCE_CM)

const handleSubmit = async () => {
  const measured = Number(form.value.measuredHeight)
  if (!Number.isFinite(measured) || measured <= 0 || measured > 300) {
    ElMessage.warning('请填写 0~300cm 之间的实测高度')
    return
  }
  if (!form.value.reviewer.trim()) {
    ElMessage.warning('请填写复测人')
    return
  }
  submitting.value = true
  try {
    await recheckApi.submit(editingId.value, {
      measuredHeight: measured,
      reviewer: form.value.reviewer.trim()
    })
    ElMessage.success(previewMismatch.value ? '已提交复核：高度不符，该杆已被拦住绑定' : '已提交复核：高度相符，可以绑上训练位')
    dialogVisible.value = false
    await loadList()
  } catch (error: any) {
    ElMessage.error(error?.message || '复核提交失败')
  } finally {
    submitting.value = false
  }
}

const formatDiff = (diff?: number | null) => {
  if (diff === null || diff === undefined) return '-'
  const n = Number(diff)
  return `${n > 0 ? '+' : ''}${n.toFixed(1)} cm`
}

const formatHeight = (v?: number | null) =>
  v === null || v === undefined ? '-' : `${Number(v).toFixed(1)} cm`

const formatTime = (t?: string | null) => {
  if (!t) return '-'
  return t.replace('T', ' ').slice(0, 16)
}
</script>

<template>
  <div class="recheck-page">
    <ElAlert
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px;"
      :title="`杆高复核规则：实测高度与档案标称高度相差超过约定 ${TOLERANCE_CM}cm 判定为「高度不符」；未复核或高度不符的杆不能绑上训练位，已绑定的不符杆会被立即拆下。`"
    />

    <div class="toolbar">
      <ElRadioGroup v-model="filter" @change="loadList">
        <ElRadioButton value="">全部</ElRadioButton>
        <ElRadioButton value="1">已复核</ElRadioButton>
        <ElRadioButton value="0">未复核</ElRadioButton>
      </ElRadioGroup>
      <ElButton :loading="loading" @click="loadList">刷新</ElButton>
    </div>

    <div class="stats">
      <ElTag type="info">当前 {{ stats.total }} 根</ElTag>
      <ElTag type="success">已复核 {{ stats.recheckedCount }}</ElTag>
      <ElTag type="danger">高度不符 {{ stats.mismatchCount }}</ElTag>
      <ElTag type="primary">可绑定 {{ stats.bindableCount }}</ElTag>
    </div>

    <ElTable v-loading="loading" :data="equipmentList" border>
      <ElTableColumn prop="equipmentCode" label="器材编号" width="100" />
      <ElTableColumn prop="equipmentName" label="障碍杆" min-width="150" />
      <ElTableColumn label="标称高度" width="100" align="center">
        <template #default="{ row }">{{ formatHeight(row.obstacleHeight) }}</template>
      </ElTableColumn>
      <ElTableColumn label="实测高度" width="100" align="center">
        <template #default="{ row }">{{ formatHeight(row.measuredHeight) }}</template>
      </ElTableColumn>
      <ElTableColumn label="高度差" width="100" align="center">
        <template #default="{ row }">
          <span :style="{ color: row.recheckResult === 'MISMATCH' ? '#f56c6c' : '#67c23a', fontWeight: 600 }">
            {{ formatDiff(row.recheckHeightDiff) }}
          </span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="复测人" width="100" align="center">
        <template #default="{ row }">{{ row.recheckReviewer || '-' }}</template>
      </ElTableColumn>
      <ElTableColumn label="复核时间" width="150" align="center">
        <template #default="{ row }">{{ formatTime(row.recheckTime) }}</template>
      </ElTableColumn>
      <ElTableColumn label="复核结论" width="110" align="center">
        <template #default="{ row }">
          <ElTag v-if="row.recheckResult === 'MATCH'" type="success">高度相符</ElTag>
          <ElTag v-else-if="row.recheckResult === 'MISMATCH'" type="danger">高度不符</ElTag>
          <ElTag v-else type="info">未复核</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn label="能否绑定" width="110" align="center">
        <template #default="{ row }">
          <ElTag v-if="row.bindable" type="success">可绑定</ElTag>
          <ElTag v-else type="danger">禁止绑定</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <ElButton type="primary" size="small" @click="openRecheckDialog(row as ObstacleEquipment)">
            {{ row.rechecked ? '重新复核' : '杆高复核' }}
          </ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElDialog v-model="dialogVisible" title="杆高复核" width="460px">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="障碍杆">
          <ElInput v-model="form.equipmentName" disabled />
        </ElFormItem>
        <ElFormItem label="标称高度">
          <ElInput :model-value="`${form.nominalHeight} cm`" disabled />
        </ElFormItem>
        <ElFormItem label="实测高度">
          <ElInputNumber v-model="form.measuredHeight" :min="1" :max="300" :precision="1" :step="1" />
          <span class="unit-text">cm（教练填写）</span>
        </ElFormItem>
        <ElFormItem label="复测人">
          <ElInput v-model="form.reviewer" placeholder="请输入复测人姓名" maxlength="50" />
        </ElFormItem>
        <ElFormItem label="实时校验">
          <ElTag :type="previewMismatch ? 'danger' : 'success'">
            差值 {{ previewDiff > 0 ? '+' : '' }}{{ previewDiff.toFixed(1) }}cm：
            {{ previewMismatch ? `超过约定${TOLERANCE_CM}cm，高度不符，将拦住绑定` : '高度相符，可绑定' }}
          </ElTag>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交复核</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.stats {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.unit-text {
  margin-left: 10px;
  color: #909399;
  font-size: 13px;
}
</style>
