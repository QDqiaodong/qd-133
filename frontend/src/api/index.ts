import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  (error) => {
    return Promise.reject(error)
  }
)

export interface ObstacleEquipment {
  id: number
  equipmentCode: string
  equipmentName: string
  obstacleHeight: number
  adaptLevel: number
  adaptLevelName: string
  adaptLevelDesc: string
  description: string
  status: number
  measuredHeight?: number | null
  recheckReviewer?: string | null
  recheckResult?: 'MATCH' | 'MISMATCH' | null
  recheckResultName?: string | null
  recheckHeightDiff?: number | null
  recheckTime?: string | null
  rechecked?: boolean
  bindable?: boolean
}

export interface Rider {
  id: number
  riderCode: string
  riderName: string
  age: number
  currentLevel: number
  currentLevelName: string
  currentLevelDesc: string
  phone: string
  email: string
  status: number
}

export interface TrainingStation {
  id: number
  stationCode: string
  stationName: string
  rider: Rider | null
  equipment: ObstacleEquipment | null
  status: number
  occupied: boolean
  occupancyStatus: 'OCCUPIED' | 'FREE'
  occupancyStatusName: string
}

export interface TrainingStationSummary {
  total: number
  occupied: number
  free: number
}

export interface LevelChangeLog {
  id: number
  rider: Rider
  previousLevel: number
  previousLevelName: string
  newLevel: number
  newLevelName: string
  changeReason: string
  operator: string
  createTime: string
}

export interface LevelEquipmentSummary {
  level: number
  levelName: string
  levelDesc: string
  equipmentCount: number
  equipments: ObstacleEquipment[]
}

export interface LevelCacheTemplate {
  level: number
  levelName: string
  levelDesc: string
  minHeight: number
  maxHeight: number
  equipmentCodes: string[]
}

export const equipmentApi = {
  create(data: Omit<ObstacleEquipment, 'id' | 'adaptLevelName' | 'adaptLevelDesc' | 'status'>) {
    return request.post('/equipment', data)
  },
  update(id: number, data: Omit<ObstacleEquipment, 'id' | 'adaptLevelName' | 'adaptLevelDesc' | 'status'>) {
    return request.put(`/equipment/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/equipment/${id}`)
  },
  getById(id: number) {
    return request.get(`/equipment/${id}`)
  },
  listAll() {
    return request.get('/equipment')
  },
  listByLevel(level: number) {
    return request.get(`/equipment/level/${level}`)
  }
}

export const riderApi = {
  create(data: Omit<Rider, 'id' | 'currentLevelName' | 'currentLevelDesc' | 'status'>) {
    return request.post('/rider', data)
  },
  update(id: number, data: Omit<Rider, 'id' | 'currentLevelName' | 'currentLevelDesc' | 'status'>) {
    return request.put(`/rider/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/rider/${id}`)
  },
  getById(id: number) {
    return request.get(`/rider/${id}`)
  },
  listAll() {
    return request.get('/rider')
  },
  listByLevel(level: number) {
    return request.get(`/rider/level/${level}`)
  },
  updateLevel(data: { riderId: number; newLevel: number; changeReason: string; operator: string }) {
    return request.post('/rider/level/update', data)
  },
  getLevelLogs(id: number) {
    return request.get(`/rider/${id}/logs`)
  }
}

export const stationApi = {
  create(data: Omit<TrainingStation, 'id' | 'rider' | 'equipment' | 'status' | 'occupied' | 'occupancyStatus' | 'occupancyStatusName'> & { riderId?: number; equipmentId?: number }) {
    return request.post('/station', data)
  },
  update(id: number, data: Omit<TrainingStation, 'id' | 'rider' | 'equipment' | 'status' | 'occupied' | 'occupancyStatus' | 'occupancyStatusName'> & { riderId?: number; equipmentId?: number }) {
    return request.put(`/station/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/station/${id}`)
  },
  getById(id: number) {
    return request.get(`/station/${id}`)
  },
  listAll() {
    return request.get('/station')
  },
  summary() {
    return request.get('/station/summary')
  },
  unbindRider(id: number) {
    return request.post(`/station/${id}/unbind-rider`)
  },
  listByRider(riderId: number) {
    return request.get(`/station/rider/${riderId}`)
  },
  listByEquipment(equipmentId: number) {
    return request.get(`/station/equipment/${equipmentId}`)
  },
  bind(stationId: number, riderId: number, equipmentId: number) {
    return request.post(`/station/${stationId}/bind`, null, {
      params: { riderId, equipmentId }
    })
  }
}

export const recheckApi = {
  list(rechecked?: boolean) {
    return request.get('/recheck', {
      params: rechecked === undefined ? {} : { rechecked }
    })
  },
  submit(equipmentId: number, data: { measuredHeight: number; reviewer: string }) {
    return request.post(`/recheck/${equipmentId}`, data)
  }
}

export const levelApi = {
  getCacheTemplates() {
    return request.get('/level/cache')
  },
  getEquipmentSummary() {
    return request.get('/level/equipment/summary')
  },
  refreshCache() {
    return request.post('/level/cache/refresh')
  }
}

export const healthApi = {
  check() {
    return request.get('/health')
  }
}

export default request