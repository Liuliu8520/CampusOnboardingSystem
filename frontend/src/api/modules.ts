import request from './request'
import type {
  Announcement,
  College,
  DormBuilding,
  DormRoom,
  FeeItem,
  Major,
  PageResult,
  QualificationModification,
  Student,
  StudentProfile
} from '@/types'

export interface LoginPayload {
  role: 'STUDENT' | 'ADMIN'
  username: string
  password: string
  captcha: string
}

export interface LoginResult {
  token: string
  role: 'STUDENT' | 'ADMIN'
  account: string
  displayName: string
}

export const authApi = {
  login: (data: LoginPayload) => request.post<LoginResult, LoginResult>('/auth/login', data)
}

export const studentApi = {
  profile: () => request.get<StudentProfile, StudentProfile>('/student/profile'),
  paymentItems: () => request.get<FeeItem[], FeeItem[]>('/student/payment/items'),
  pay: (feeItemIds: number[]) => request.post('/student/payment', { feeItemIds }),
  applyModification: (data: { fieldName: string; newValue: string; reason: string }) =>
    request.post<QualificationModification, QualificationModification>('/student/qualification/apply', data),
  assignDorm: () => request.post('/student/dorm/assign'),
  checkin: () => request.post<StudentProfile, StudentProfile>('/student/checkin'),
  announcements: () => request.get<Announcement[], Announcement[]>('/student/announcements')
}

export const adminApi = {
  dashboard: () => request.get<Record<string, any>, Record<string, any>>('/admin/dashboard/stats'),
  colleges: () => request.get<College[], College[]>('/admin/academics/colleges'),
  majors: (params?: Record<string, any>) => request.get<Major[], Major[]>('/admin/academics/majors', { params }),
  students: (params: Record<string, any>) => request.get<PageResult<Student>, PageResult<Student>>('/admin/students', { params }),
  saveStudent: (data: Student) => (data.id
    ? request.put<Student, Student>(`/admin/students/${data.id}`, data)
    : request.post<Student, Student>('/admin/students', data)),
  deleteStudent: (id: number) => request.delete(`/admin/students/${id}`),
  resetPassword: (id: number) => request.put(`/admin/students/${id}/reset-password`),
  adminCheckin: (id: number) => request.put(`/admin/students/${id}/checkin`),
  updateStudentPayments: (id: number, paidFeeItemIds: number[]) =>
    request.put<Student, Student>(`/admin/students/${id}/payments`, { paidFeeItemIds }),
  buildings: () => request.get<DormBuilding[], DormBuilding[]>('/admin/dorm/buildings'),
  saveBuilding: (data: DormBuilding) => (data.id
    ? request.put<DormBuilding, DormBuilding>(`/admin/dorm/buildings/${data.id}`, data)
    : request.post<DormBuilding, DormBuilding>('/admin/dorm/buildings', data)),
  deleteBuilding: (id: number) => request.delete(`/admin/dorm/buildings/${id}`),
  rooms: (params: Record<string, any>) => request.get<DormRoom[], DormRoom[]>('/admin/dorm/rooms', { params }),
  batchRooms: (data: Record<string, any>) => request.post<DormRoom[], DormRoom[]>('/admin/dorm/rooms/batch', data),
  deleteRoom: (id: number) => request.delete(`/admin/dorm/rooms/${id}`),
  occupancy: () => request.get<any[], any[]>('/admin/dorm/occupancy'),
  fees: (params: Record<string, any>) => request.get<PageResult<FeeItem>, PageResult<FeeItem>>('/admin/fees', { params }),
  saveFee: (data: FeeItem) => (data.id
    ? request.put<FeeItem, FeeItem>(`/admin/fees/${data.id}`, data)
    : request.post<FeeItem, FeeItem>('/admin/fees', data)),
  deleteFee: (id: number) => request.delete(`/admin/fees/${id}`),
  modifications: (params: Record<string, any>) =>
    request.get<PageResult<QualificationModification>, PageResult<QualificationModification>>('/admin/modifications/list', { params }),
  reviewModification: (id: number, data: { approved: boolean; comment?: string }) =>
    request.put(`/admin/modifications/approve/${id}`, data),
  announcements: (params: Record<string, any>) =>
    request.get<PageResult<Announcement>, PageResult<Announcement>>('/admin/announcements', { params }),
  saveAnnouncement: (data: Announcement) => (data.id
    ? request.put<Announcement, Announcement>(`/admin/announcements/${data.id}`, data)
    : request.post<Announcement, Announcement>('/admin/announcements', data)),
  deleteAnnouncement: (id: number) => request.delete(`/admin/announcements/${id}`)
}
