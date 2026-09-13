<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCard, ElRow, ElCol, ElTable, ElTableColumn, ElTag, ElButton, ElMessage } from 'element-plus'
import { levelApi, equipmentApi, riderApi, type LevelEquipmentSummary, type ObstacleEquipment, type Rider } from '@/api'

const levelSummary = ref<LevelEquipmentSummary[]>([])
const equipmentList = ref<ObstacleEquipment[]>([])
const riderList = ref<Rider[]>([])

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

onMounted(() => {
  loadData()
})

const loadData = async () => {
  try {
    levelSummary.value = await levelApi.getEquipmentSummary() as unknown as LevelEquipmentSummary[]
    equipmentList.value = await equipmentApi.listAll() as unknown as ObstacleEquipment[]
    riderList.value = await riderApi.listAll() as unknown as Rider[]
  } catch (error: any) {
    ElMessage.error('加载数据失败')
  }
}

const getRidersByLevel = (level: number) => {
  return riderList.value.filter(r => r.currentLevel === level)
}

const handleRefreshCache = async () => {
  try {
    await levelApi.refreshCache()
    ElMessage.success('缓存刷新成功')
    loadData()
  } catch (error: any) {
    ElMessage.error('缓存刷新失败')
  }
}
</script>

<template>
  <div class="level-summary-page">
    <div style="margin-bottom: 20px; display: flex; justify-content: flex-end;">
      <ElButton type="primary" @click="handleRefreshCache">刷新缓存</ElButton>
    </div>

    <ElRow :gutter="20">
      <ElCol v-for="summary in levelSummary" :key="summary.level" :span="12">
        <ElCard :title="`${summary.levelName} - ${summary.levelDesc}`" style="margin-bottom: 20px;">
          <div style="margin-bottom: 15px;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 10px;">
              <span>设备数量：</span>
              <strong>{{ summary.equipmentCount }} 件</strong>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 10px;">
              <span>骑手数量：</span>
              <strong>{{ getRidersByLevel(summary.level).length }} 人</strong>
            </div>
          </div>
          
          <ElTable :data="summary.equipments" border size="small">
            <ElTableColumn prop="equipmentCode" label="编号" />
            <ElTableColumn prop="equipmentName" label="名称" />
            <ElTableColumn prop="obstacleHeight" label="高度(cm)" />
            <ElTableColumn prop="adaptLevel" label="适配等级">
              <template #default="{ row }">
                <ElTag :type="levelColors[row.adaptLevel]">{{ getLevelName(row.adaptLevel) }}</ElTag>
              </template>
            </ElTableColumn>
          </ElTable>

          <div v-if="getRidersByLevel(summary.level).length > 0" style="margin-top: 15px;">
            <h4>所属骑手：</h4>
            <div style="display: flex; flex-wrap: wrap; gap: 10px;">
              <ElTag 
                v-for="rider in getRidersByLevel(summary.level)" 
                :key="rider.id" 
                :type="levelColors[rider.currentLevel]"
              >
                {{ rider.riderName }}
              </ElTag>
            </div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>