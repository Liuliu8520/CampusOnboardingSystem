<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Refresh, Tickets } from '@element-plus/icons-vue'
import { adminApi } from '@/api/modules'
import type { DormBuilding, DormRoom, Major } from '@/types'

const loading = ref(false)
const buildings = ref<DormBuilding[]>([])
const rooms = ref<DormRoom[]>([])
const occupancy = ref<any[]>([])
const majors = ref<Major[]>([])
const buildingDialog = ref(false)
const roomDialog = ref(false)
const selectedBuildingId = ref<number | undefined>()

const buildingForm = reactive<DormBuilding>({
  buildingNo: '',
  name: '',
  gender: '男',
  sortNo: 1
})

const roomForm = reactive({
  buildingId: undefined as number | undefined,
  major: '',
  gender: '男',
  startNo: 101,
  count: 1,
  capacity: 4
})

const selectedBuilding = computed(() => buildings.value.find((item) => item.id === selectedBuildingId.value))
const roomMajorOptions = computed(() => majors.value)

function resetBuilding() {
  Object.assign(buildingForm, { id: undefined, buildingNo: '', name: '', gender: '男', sortNo: 1 })
}

function resetRoom() {
  Object.assign(roomForm, {
    buildingId: selectedBuildingId.value || buildings.value[0]?.id,
    major: '',
    gender: selectedBuilding.value?.gender || buildings.value[0]?.gender || '男',
    startNo: 101,
    count: 1,
    capacity: 4
  })
}

async function loadData() {
  loading.value = true
  try {
    const [buildingData, occupancyData, majorData] = await Promise.all([
      adminApi.buildings(),
      adminApi.occupancy(),
      adminApi.majors({ enabled: true })
    ])
    buildings.value = buildingData
    occupancy.value = occupancyData
    majors.value = majorData
    if (!selectedBuildingId.value && buildingData.length) {
      selectedBuildingId.value = buildingData[0].id
    }
    await loadRooms()
  } finally {
    loading.value = false
  }
}

async function loadRooms() {
  rooms.value = await adminApi.rooms({ buildingId: selectedBuildingId.value })
}

function openBuilding(row?: DormBuilding) {
  resetBuilding()
  if (row) {
    Object.assign(buildingForm, row)
  }
  buildingDialog.value = true
}

async function saveBuilding() {
  if (!buildingForm.buildingNo || !buildingForm.name || !buildingForm.gender) {
    ElMessage.warning('请填写楼栋编号、名称和性别')
    return
  }
  await adminApi.saveBuilding(buildingForm)
  ElMessage.success('楼栋保存成功')
  buildingDialog.value = false
  await loadData()
}

async function removeBuilding(row: DormBuilding) {
  await ElMessageBox.confirm(`确认删除 ${row.name}？`, '删除楼栋', { type: 'warning' })
  await adminApi.deleteBuilding(row.id!)
  ElMessage.success('删除成功')
  await loadData()
}

function openBatchRoom() {
  resetRoom()
  roomDialog.value = true
}

async function saveRooms() {
  if (!roomForm.buildingId || !roomForm.major) {
    ElMessage.warning('请选择楼栋并填写专业')
    return
  }
  await adminApi.batchRooms(roomForm)
  ElMessage.success('房间生成成功')
  roomDialog.value = false
  await loadData()
}

async function removeRoom(row: DormRoom) {
  await ElMessageBox.confirm(`确认删除 ${row.roomNo} 室？`, '删除房间', { type: 'warning' })
  await adminApi.deleteRoom(row.id!)
  ElMessage.success('删除成功')
  await loadData()
}

function onBuildingChange() {
  const building = selectedBuilding.value
  if (building) {
    roomForm.gender = building.gender
  }
  loadRooms()
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">宿舍管理</h2>
      </div>
      <div class="toolbar-actions">
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openBuilding()">新增楼栋</el-button>
        <el-button type="success" :icon="Tickets" @click="openBatchRoom">批量生成房间</el-button>
      </div>
    </div>

    <section class="dorm-layout">
      <div class="panel">
        <div class="panel-title">楼栋</div>
        <el-table :data="buildings" border v-loading="loading">
          <el-table-column prop="buildingNo" label="编号" width="90" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="gender" label="性别" width="80" />
          <el-table-column prop="sortNo" label="排序" width="70" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button :icon="Edit" circle @click="openBuilding(row)" />
              <el-button :icon="Delete" circle type="danger" @click="removeBuilding(row)" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel">
        <div class="room-header">
          <div class="panel-title">房间</div>
          <el-select v-model="selectedBuildingId" style="width: 180px" @change="onBuildingChange">
            <el-option v-for="item in buildings" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </div>
        <el-table :data="rooms" border v-loading="loading">
          <el-table-column prop="roomNo" label="房间号" width="100" />
          <el-table-column prop="major" label="专业" min-width="140" />
          <el-table-column prop="gender" label="性别" width="80" />
          <el-table-column label="占用" width="120">
            <template #default="{ row }">{{ row.occupiedCount }} / {{ row.capacity }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button :icon="Delete" circle type="danger" :disabled="row.occupiedCount > 0" @click="removeRoom(row)" />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <section class="panel occupancy-panel">
      <div class="panel-title">床位占用图</div>
      <el-collapse>
        <el-collapse-item v-for="building in occupancy" :key="building.building.id" :title="`${building.building.name}（${building.building.gender}）`">
          <div class="room-cards">
            <div v-for="room in building.rooms" :key="room.room.id" class="room-card">
              <div class="room-card-title">
                <strong>{{ room.room.roomNo }} 室</strong>
                <span>{{ room.room.major }}</span>
              </div>
              <div class="bed-grid">
                <div v-for="bed in room.beds" :key="bed.bed.id" class="bed-cell" :class="{ occupied: bed.bed.occupied }">
                  <span>{{ bed.bed.bedNo }}床</span>
                  <strong>{{ bed.student?.name || '空床' }}</strong>
                  <small v-if="bed.student">{{ bed.student.studentId }}</small>
                </div>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </section>

    <el-dialog v-model="buildingDialog" :title="buildingForm.id ? '编辑楼栋' : '新增楼栋'" width="520px">
      <el-form :model="buildingForm" label-position="top">
        <el-form-item label="楼栋编号"><el-input v-model="buildingForm.buildingNo" /></el-form-item>
        <el-form-item label="楼栋名称"><el-input v-model="buildingForm.name" /></el-form-item>
        <el-form-item label="宿舍性别">
          <el-select v-model="buildingForm.gender" class="wide-input">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="buildingForm.sortNo" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buildingDialog = false">取消</el-button>
        <el-button type="primary" @click="saveBuilding">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roomDialog" title="批量生成房间" width="560px">
      <el-form :model="roomForm" label-position="top" class="form-grid">
        <el-form-item label="楼栋">
          <el-select v-model="roomForm.buildingId" class="wide-input" @change="onBuildingChange">
            <el-option v-for="item in buildings" :key="item.id" :label="`${item.name}（${item.gender}）`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="roomForm.major" class="wide-input" filterable>
            <el-option v-for="item in roomMajorOptions" :key="item.id" :label="item.name" :value="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="起始房号"><el-input-number v-model="roomForm.startNo" :min="1" /></el-form-item>
        <el-form-item label="房间数量"><el-input-number v-model="roomForm.count" :min="1" :max="50" /></el-form-item>
        <el-form-item label="床位容量"><el-input-number v-model="roomForm.capacity" :min="1" :max="8" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRooms">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar-actions {
  display: flex;
  gap: 10px;
}

.dorm-layout {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 14px;
}

.panel-title {
  margin-bottom: 12px;
  font-weight: 800;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.occupancy-panel {
  margin-top: 14px;
}

.room-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.room-card {
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 12px;
  background: #fff;
}

.room-card-title {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.room-card-title span {
  color: var(--app-muted);
}

.bed-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.bed-cell {
  min-height: 62px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  padding: 8px;
  border-radius: 8px;
  background: #f5f7fa;
  border: 1px solid var(--app-border);
}

.bed-cell.occupied {
  background: var(--app-primary-soft);
  border-color: rgba(37, 111, 115, 0.28);
}

.bed-cell span {
  color: var(--app-muted);
  font-size: 12px;
}

.bed-cell strong {
  font-size: 14px;
}

.bed-cell small {
  color: var(--app-muted);
  font-size: 12px;
}

.wide-input {
  width: 100%;
}

@media (max-width: 1100px) {
  .dorm-layout,
  .room-cards {
    grid-template-columns: 1fr;
  }
}
</style>
