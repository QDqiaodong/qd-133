<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  ElAlert,
  ElCard,
  ElRow,
  ElCol,
  ElForm,
  ElFormItem,
  ElSelect,
  ElOption,
  ElInput,
  ElRate,
  ElButton,
  ElTable,
  ElTableColumn,
  ElTag,
  ElMessage
} from 'element-plus'
import { reviewApi, riderApi, type Rider, type SessionReview, type SessionReviewSummary } from '@/api'

const riderList = ref<Rider[]>([])
const reviews = ref<SessionReview[]>([])
const summary = ref<SessionReviewSummary>({ total: 0, averageStars: 0, starCounts: [] })
const loading = ref(false)
const submitting = ref(false)

const form = ref({
  riderId: null as number | null,
  sessionFocus: '',
  starRating: 0
})

const starTexts = ['一星', '二星', '三星', '四星', '五星']

const loadRiders = async () => {
  try {
    riderList.value = (await riderApi.listAll()) as unknown as Rider[]
  } catch (error: any) {
    ElMessage.error(error?.message || '加载骑手列表失败')
  }
}

// 明细与汇总都重新拉一遍：两处同表同源，条数始终一致
const loadReviews = async () => {
  loading.value = true
  try {
    const [list, sum] = await Promise.all([reviewApi.listAll(), reviewApi.summary()])
    reviews.value = list as unknown as SessionReview[]
    summary.value = sum as unknown as SessionReviewSummary
  } catch (error: any) {
    ElMessage.error(error?.message || '加载点评数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRiders()
  loadReviews()
})

// 汇总条数与明细条数必须保持一致
const consistent = computed(() => summary.value.total === reviews.value.length)

// 汇总条按五星到一星展示，没评过的星级补 0
const summaryBars = computed(() => {
  const counts = summary.value.starCounts ?? []
  const bars = [5, 4, 3, 2, 1].map((stars) => {
    const bucket = counts.find((c) => c.stars === stars)
    return { stars, count: bucket ? Number(bucket.count) : 0 }
  })
  const maxCount = Math.max(...bars.map((b) => b.count), 0)
  return bars.map((b) => ({
    ...b,
    width: maxCount > 0 ? (b.count / maxCount) * 100 : 0
  }))
})

const handleSubmit = async () => {
  if (!form.value.riderId) {
    ElMessage.warning('请先选好骑手')
    return
  }
  if (!form.value.sessionFocus.trim()) {
    ElMessage.warning('请写下本节重点')
    return
  }
  if (form.value.starRating < 1 || form.value.starRating > 5) {
    ElMessage.warning('请选择一到五星的星级')
    return
  }
  submitting.value = true
  try {
    await reviewApi.create({
      riderId: form.value.riderId,
      sessionFocus: form.value.sessionFocus.trim(),
      starRating: form.value.starRating
    })
    ElMessage.success('点评提交成功')
    form.value = { riderId: null, sessionFocus: '', starRating: 0 }
    await loadReviews()
  } catch (error: any) {
    ElMessage.error(error?.message || '点评提交失败')
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
  <div class="review-page">
    <ElAlert
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px;"
      title="课后点评：选好骑手，写下本节重点并打一到五星后提交。点评明细与星级汇总按同一份数据统计，条数保持一致，刷新页面两处都还在。"
    />

    <ElRow :gutter="20" style="margin-bottom: 20px;">
      <ElCol :span="9">
        <ElCard>
          <template #header>写点评</template>
          <ElForm label-width="80px">
            <ElFormItem label="骑手" required>
              <ElSelect v-model="form.riderId" placeholder="选好骑手" style="width: 100%;">
                <ElOption
                  v-for="r in riderList"
                  :key="r.id"
                  :value="r.id"
                  :label="`${r.riderCode} ${r.riderName}（${r.currentLevelName}）`"
                />
              </ElSelect>
            </ElFormItem>
            <ElFormItem label="本节重点" required>
              <ElInput
                v-model="form.sessionFocus"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="写下本节课的训练重点"
              />
            </ElFormItem>
            <ElFormItem label="星级" required>
              <ElRate v-model="form.starRating" :max="5" :texts="starTexts" show-text />
            </ElFormItem>
            <ElFormItem>
              <ElButton type="primary" :loading="submitting" style="width: 100%;" @click="handleSubmit">
                提交点评
              </ElButton>
            </ElFormItem>
          </ElForm>
        </ElCard>
      </ElCol>
      <ElCol :span="15">
        <ElCard>
          <template #header>
            <div class="summary-header">
              <span>星级汇总</span>
              <span>
                <ElTag type="primary" style="margin-right: 8px;">共 {{ summary.total }} 条</ElTag>
                <ElTag type="warning">平均 {{ summary.averageStars.toFixed(1) }} 星</ElTag>
              </span>
            </div>
          </template>
          <div v-for="bar in summaryBars" :key="bar.stars" class="summary-bar-row">
            <span class="summary-bar-label">{{ bar.stars }} 星</span>
            <div class="summary-bar-track">
              <div class="summary-bar-fill" :style="{ width: bar.width + '%' }"></div>
            </div>
            <span class="summary-bar-count">{{ bar.count }} 条</span>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElCard>
      <template #header>
        <div class="summary-header">
          <span>点评明细</span>
          <span>
            <ElTag type="info" style="margin-right: 8px;">明细 {{ reviews.length }} 条</ElTag>
            <ElTag type="info" style="margin-right: 8px;">汇总 {{ summary.total }} 条</ElTag>
            <ElTag :type="consistent ? 'success' : 'danger'" style="margin-right: 8px;">
              {{ consistent ? '条数一致' : '条数不一致' }}
            </ElTag>
            <ElButton size="small" :loading="loading" @click="loadReviews">刷新</ElButton>
          </span>
        </div>
      </template>
      <ElTable v-loading="loading" :data="reviews" border>
        <ElTableColumn label="骑手" width="180">
          <template #default="{ row }">
            {{ row.rider.riderCode }} {{ row.rider.riderName }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="sessionFocus" label="本节重点" min-width="260" show-overflow-tooltip />
        <ElTableColumn label="星级" width="180" align="center">
          <template #default="{ row }">
            <ElRate :model-value="row.starRating" :max="5" disabled />
          </template>
        </ElTableColumn>
        <ElTableColumn label="点评时间" width="160" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </ElTableColumn>
        <template #empty>还没有点评，先在左上角写一条</template>
      </ElTable>
    </ElCard>
  </div>
</template>

<style scoped>
.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.summary-bar-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.summary-bar-row:last-child {
  margin-bottom: 0;
}
.summary-bar-label {
  width: 48px;
  color: #606266;
  font-size: 14px;
}
.summary-bar-track {
  flex: 1;
  height: 14px;
  background: #f0f2f5;
  border-radius: 7px;
  overflow: hidden;
  margin: 0 12px;
}
.summary-bar-fill {
  height: 100%;
  background: #f7ba2a;
  border-radius: 7px;
  transition: width 0.3s;
}
.summary-bar-count {
  width: 56px;
  text-align: right;
  color: #606266;
  font-size: 14px;
}
</style>
