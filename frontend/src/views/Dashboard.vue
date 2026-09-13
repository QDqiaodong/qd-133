<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCard, ElStatistic, ElRow, ElCol, ElTable, ElTableColumn, ElTag } from 'element-plus'
import { equipmentApi, riderApi, stationApi, levelApi, type ObstacleEquipment, type Rider, type TrainingStation, type LevelEquipmentSummary } from '@/api'

const equipmentList = ref<ObstacleEquipment[]>([])
const riderList = ref<Rider[]>([])
const stationList = ref<TrainingStation[]>([])
const levelSummary = ref<LevelEquipmentSummary[]>([])

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

const recentEquipments = ref<ObstacleEquipment[]>([])

onMounted(() => {
  loadData()
})

const loadData = async () => {
  try {
    equipmentList.value = await equipmentApi.listAll() as unknown as ObstacleEquipment[]
    riderList.value = await riderApi.listAll() as unknown as Rider[]
    stationList.value = await stationApi.listAll() as unknown as TrainingStation[]
    levelSummary.value = await levelApi.getEquipmentSummary() as unknown as LevelEquipmentSummary[]
    recentEquipments.value = equipmentList.value.slice(0, 5)
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const getBarWidth = (count: number): number => {
  const maxCount = Math.max(...levelSummary.value.map(s => s.equipmentCount))
  return maxCount > 0 ? (count / maxCount) * 100 : 0
}

const getLevelColor = (level: number): string => {
  const colors: Record<number, string> = {
    1: '#67c23a',
    2: '#409eff',
    3: '#e6a23c',
    4: '#f56c6c',
    5: '#909399'
  }
  return colors[level] || '#909399'
}
</script>

<template>
  <div class="dashboard">
    <ElRow :gutter="20" style="margin-bottom: 20px;">
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="障碍器材总数" :value="equipmentList.length" suffix="件" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="骑手总数" :value="riderList.length" suffix="人" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="训练位总数" :value="stationList.length" suffix="个" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="已绑定训练位" :value="stationList.filter(s => s.rider && s.equipment).length" suffix="个" />
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="20">
      <ElCol :span="12">
        <ElCard title="等级设备分布" style="height: 100%;">
          <div v-for="summary in levelSummary" :key="summary.level" class="level-bar-container">
            <div class="level-bar-header">
              <ElTag :type="levelColors[summary.level]">{{ summary.levelName }}</ElTag>
              <span class="level-count">{{ summary.equipmentCount }}件</span>
            </div>
            <div class="level-bar">
              <div 
                class="level-bar-fill" 
                :style="{ width: getBarWidth(summary.equipmentCount) + '%', backgroundColor: getLevelColor(summary.level) }"
              ></div>
            </div>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :span="12">
        <ElCard title="最近添加的器材">
          <ElTable :data="recentEquipments" border size="small">
            <ElTableColumn prop="equipmentCode" label="编号" />
            <ElTableColumn prop="equipmentName" label="名称" />
            <ElTableColumn prop="obstacleHeight" label="高度(cm)" />
            <ElTableColumn prop="adaptLevel" label="适配等级">
              <template #default="{ row }">
                <ElTag :type="levelColors[row.adaptLevel]">{{ getLevelName(row.adaptLevel) }}</ElTag>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped>
.level-bar-container {
  margin-bottom: 15px;
}

.level-bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.level-count {
  font-size: 14px;
  color: #606266;
}

.level-bar {
  height: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.level-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}
</style>