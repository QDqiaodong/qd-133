<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  ElAlert,
  ElCard,
  ElRow,
  ElCol,
  ElForm,
  ElFormItem,
  ElDatePicker,
  ElInput,
  ElButton,
  ElTable,
  ElTableColumn,
  ElTag,
  ElMessage
} from 'element-plus'
import { dutyLogApi, type DutyLog } from '@/api'

const logs = ref<DutyLog[]>([])
const loading = ref(false)
const submitting = ref(false)

const today = () => {
  const now = new Date()
  const month = `${now.getMonth() + 1}`.padStart(2, '0')
  const day = `${now.getDate()}`.padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

const form = ref({
  dutyDate: today(),
  onDutyCoach: '',
  incomingCoach: '',
  tonightNotes: ''
})

const queryDate = ref<string>('')

const loadLogs = async () => {
  loading.value = true
  try {
    logs.value = (await dutyLogApi.list(queryDate.value || undefined)) as unknown as DutyLog[]
  } catch (error: any) {
    ElMessage.error(error?.message || '加载值班记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadLogs)

const handleQuery = async () => {
  await loadLogs()
}

const resetQuery = async () => {
  queryDate.value = ''
  await loadLogs()
}

const handleSubmit = async () => {
  if (!form.value.dutyDate) {
    ElMessage.warning('请选择值班日期')
    return
  }
  if (!form.value.onDutyCoach.trim()) {
    ElMessage.warning('请填写当班教练')
    return
  }
  if (!form.value.incomingCoach.trim()) {
    ElMessage.warning('请填写接班教练')
    return
  }
  if (!form.value.tonightNotes.trim()) {
    ElMessage.warning('请写下今晚要留意的事')
    return
  }

  submitting.value = true
  const dutyDate = form.value.dutyDate
  try {
    await dutyLogApi.create({
      dutyDate,
      onDutyCoach: form.value.onDutyCoach.trim(),
      incomingCoach: form.value.incomingCoach.trim(),
      tonightNotes: form.value.tonightNotes.trim()
    })
    ElMessage.success('值班记录提交成功')
    form.value = {
      dutyDate: today(),
      onDutyCoach: '',
      incomingCoach: '',
      tonightNotes: ''
    }
    queryDate.value = dutyDate
    await loadLogs()
  } catch (error: any) {
    ElMessage.error(error?.message || '值班记录提交失败')
  } finally {
    submitting.value = false
  }
}

const formatTime = (t?: string | null) => {
  if (!t) return '-'
  return t.replace('T', ' ').slice(0, 16)
}
</script>

<template>
  <div class="duty-log-page">
    <ElAlert
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px;"
      title="值班交接：每天只写一条，当班教练、接班教练、今晚要留意的事都填全才能提交。历史记录可按日期翻查，刷新或隔天再打开仍然查得到。"
    />

    <ElRow :gutter="20" style="margin-bottom: 20px;">
      <ElCol :span="24">
        <ElCard>
          <template #header>写值班记录</template>
          <ElForm label-width="110px">
            <ElRow :gutter="16">
              <ElCol :span="8">
                <ElFormItem label="值班日期" required>
                  <ElDatePicker
                    v-model="form.dutyDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="选择日期"
                    style="width: 100%;"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="8">
                <ElFormItem label="当班教练" required>
                  <ElInput
                    v-model="form.onDutyCoach"
                    placeholder="例如：陈教练"
                    maxlength="50"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="8">
                <ElFormItem label="接班教练" required>
                  <ElInput
                    v-model="form.incomingCoach"
                    placeholder="例如：林教练"
                    maxlength="50"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>
            <ElFormItem label="今晚留意事项" required>
              <ElInput
                v-model="form.tonightNotes"
                type="textarea"
                :rows="3"
                maxlength="1000"
                show-word-limit
                placeholder="写下今晚要留意的事，例如：三号训练位栏杆松动需要维修、二号障碍杆漆面开裂要更换"
              />
            </ElFormItem>
            <ElFormItem>
              <ElButton type="primary" :loading="submitting" @click="handleSubmit">
                提交值班记录
              </ElButton>
            </ElFormItem>
          </ElForm>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElCard>
      <template #header>
        <div class="list-header">
          <span>历史值班记录</span>
          <div class="query-bar">
            <ElDatePicker
              v-model="queryDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="按日期翻查"
              clearable
              style="width: 170px;"
            />
            <ElButton type="primary" :loading="loading" @click="handleQuery">查询</ElButton>
            <ElButton :disabled="!queryDate" @click="resetQuery">查看全部</ElButton>
          </div>
        </div>
      </template>

      <div class="stats-bar">
        <ElTag :type="queryDate ? 'primary' : 'info'">
          {{ queryDate ? `${queryDate} 记录` : '全部记录' }}：{{ logs.length }} 条
        </ElTag>
        <ElTag type="warning">同一天只能提交一条</ElTag>
      </div>

      <ElTable v-loading="loading" :data="logs" border>
        <ElTableColumn prop="dutyDate" label="值班日期" width="120" align="center" />
        <ElTableColumn prop="onDutyCoach" label="当班教练" width="120" align="center" />
        <ElTableColumn prop="incomingCoach" label="接班教练" width="120" align="center" />
        <ElTableColumn prop="tonightNotes" label="今晚要留意的事" min-width="360" show-overflow-tooltip />
        <ElTableColumn label="提交时间" width="160" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </ElTableColumn>
        <template #empty>
          {{ queryDate ? '这一天还没有值班记录' : '还没有值班记录，先在上方写一条' }}
        </template>
      </ElTable>
    </ElCard>
  </div>
</template>

<style scoped>
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-bar {
  display: flex;
  gap: 8px;
  align-items: center;
}

.stats-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
</style>
