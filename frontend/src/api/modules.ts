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
  SchoolClass,
  Student,
  StudentProfile
} from '@/types'

export interface LoginPayload {
  role: 'STUDENT' | 'ADMIN'
  username: string
  password: string
  captcha: string
  captchaId: string
}

export interface LoginResult {
  token: string
  role: 'STUDENT' | 'ADMIN'
  account: string
  displayName: string
}

export interface CaptchaResult {
  captchaId: string
  question: string
}

export const authApi = {
  login: (data: LoginPayload) => request.post<LoginResult, LoginResult>('/auth/login', data),
  captcha: () => request.get<CaptchaResult, CaptchaResult>('/auth/captcha'),
  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    request.put('/auth/password', data)
}

export const studentApi = {
  profile: () => request.get<StudentProfile, StudentProfile>('/student/profile'),
  paymentItems: () => request.get<FeeItem[], FeeItem[]>('/student/payment/items'),
  pay: (feeItemIds: number[]) => request.post('/student/payment', { feeItemIds }),
  applyModification: (data: { fieldName: string; newValue: string; reason: string }) =>
    request.post<QualificationModification, QualificationModification>('/student/qualification/apply', data),
  confirmQualification: () => request.post<Student, Student>('/student/qualification/confirm'),
  assignDorm: () => request.post('/student/dorm/assign'),
  checkin: () => request.post<StudentProfile, StudentProfile>('/student/checkin'),
  announcements: () => request.get<Announcement[], Announcement[]>('/student/announcements'),
  announcement: (id: number) => request.get<Announcement, Announcement>(`/student/announcements/${id}`)
}

export const adminApi = {
  dashboard: () => request.get<Record<string, any>, Record<string, any>>('/admin/dashboard/stats'),
  colleges: (params?: Record<string, any>) => request.get<College[], College[]>('/admin/academics/colleges', { params }),
  saveCollege: (data: College) => (data.id
    ? request.put<College, College>(`/admin/academics/colleges/${data.id}`, data)
    : request.post<College, College>('/admin/academics/colleges', data)),
  deleteCollege: (id: number) => request.delete(`/admin/academics/colleges/${id}`),
  majors: (params?: Record<string, any>) => request.get<Major[], Major[]>('/admin/academics/majors', { params }),
  saveMajor: (data: Major) => (data.id
    ? request.put<Major, Major>(`/admin/academics/majors/${data.id}`, data)
    : request.post<Major, Major>('/admin/academics/majors', data)),
  deleteMajor: (id: number) => request.delete(`/admin/academics/majors/${id}`),
  classes: (params?: Record<string, any>) => request.get<SchoolClass[], SchoolClass[]>('/admin/academics/classes', { params }),
  saveClass: (data: SchoolClass) => (data.id
    ? request.put<SchoolClass, SchoolClass>(`/admin/academics/classes/${data.id}`, data)
    : request.post<SchoolClass, SchoolClass>('/admin/academics/classes', data)),
  deleteClass: (id: number) => request.delete(`/admin/academics/classes/${id}`),
  students: (params: Record<string, any>) => request.get<PageResult<Student>, PageResult<Student>>('/admin/students', { params }),
  saveStudent: (data: Student) => (data.id
    ? request.put<Student, Student>(`/admin/students/${data.id}`, data)
    : request.post<Student, Student>('/admin/students', data)),
  deleteStudent: (id: number) => request.delete(`/admin/students/${id}`),
  resetPassword: (id: number) => request.put(`/admin/students/${id}/reset-password`),
  toggleCheckin: (id: number) => request.put(`/admin/students/${id}/checkin`),
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
