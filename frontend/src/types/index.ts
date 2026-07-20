export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface Student {
  id?: number
  studentId: string
  name: string
  gender: '男' | '女' | string
  college: string
  major: string
  className: string
  phone?: string
  idCard?: string
  address?: string
  paid?: boolean
  checkedIn?: boolean
  bedId?: number
  paymentStatuses?: StudentFeeStatus[]
  requiredFeePaidCount?: number
  requiredFeeTotal?: number
  createTime?: string
}

export interface College {
  id?: number
  name: string
  sortNo: number
  enabled: boolean
}

export interface Major {
  id?: number
  collegeId: number
  name: string
  sortNo: number
  enabled: boolean
}

export interface StudentFeeStatus {
  feeItemId: number
  name: string
  amount: number
  required: boolean
  enabled: boolean
  paid: boolean
  status: string
  payTime?: string
}

export interface DormBuilding {
  id?: number
  buildingNo: string
  name: string
  gender: string
  sortNo: number
}

export interface DormRoom {
  id?: number
  buildingId: number
  roomNo: string
  capacity: number
  occupiedCount: number
  major: string
  gender: string
}

export interface DormBed {
  id?: number
  roomId: number
  bedNo: string
  occupied: boolean
  studentId?: string
}

export interface FeeItem {
  id?: number
  name: string
  amount: number
  required: boolean
  enabled: boolean
  description?: string
  paid?: boolean
}

export interface QualificationModification {
  id?: number
  studentId: string
  fieldName: string
  fieldLabel: string
  oldValue?: string
  newValue: string
  reason: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | string
  reviewComment?: string
  createTime?: string
  reviewTime?: string
}

export interface Announcement {
  id?: number
  title: string
  content: string
  published: boolean
  createTime?: string
}

export interface StudentProfile {
  student: Student
  currentStep: number
  currentStepName: string
  dorm?: {
    building: DormBuilding
    room: DormRoom
    bed: DormBed
    display: string
  } | null
  modifications: QualificationModification[]
}
