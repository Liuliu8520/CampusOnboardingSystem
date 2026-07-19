package com.campus.onboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.onboarding.common.BizException;
import com.campus.onboarding.dto.DormBatchCreateRequest;
import com.campus.onboarding.dto.ModificationReviewRequest;
import com.campus.onboarding.dto.StudentSaveRequest;
import com.campus.onboarding.entity.CheckinRecord;
import com.campus.onboarding.entity.DormBed;
import com.campus.onboarding.entity.DormBuilding;
import com.campus.onboarding.entity.DormRoom;
import com.campus.onboarding.entity.PaymentRecord;
import com.campus.onboarding.entity.QualificationModification;
import com.campus.onboarding.entity.Student;
import com.campus.onboarding.mapper.CheckinRecordMapper;
import com.campus.onboarding.mapper.DormBedMapper;
import com.campus.onboarding.mapper.DormBuildingMapper;
import com.campus.onboarding.mapper.DormRoomMapper;
import com.campus.onboarding.mapper.PaymentRecordMapper;
import com.campus.onboarding.mapper.QualificationModificationMapper;
import com.campus.onboarding.mapper.StudentMapper;
import com.campus.onboarding.security.AuthContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminWorkflowService {
    private final StudentMapper studentMapper;
    private final QualificationModificationMapper modificationMapper;
    private final DormBuildingMapper buildingMapper;
    private final DormRoomMapper roomMapper;
    private final DormBedMapper bedMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminWorkflowService(StudentMapper studentMapper,
                                QualificationModificationMapper modificationMapper,
                                DormBuildingMapper buildingMapper,
                                DormRoomMapper roomMapper,
                                DormBedMapper bedMapper,
                                PaymentRecordMapper paymentRecordMapper,
                                CheckinRecordMapper checkinRecordMapper,
                                BCryptPasswordEncoder passwordEncoder) {
        this.studentMapper = studentMapper;
        this.modificationMapper = modificationMapper;
        this.buildingMapper = buildingMapper;
        this.roomMapper = roomMapper;
        this.bedMapper = bedMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.checkinRecordMapper = checkinRecordMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<Student> studentPage(long page, long size, String keyword, String college, String major, Boolean checkedIn) {
        AuthContext.requireAdmin();
        QueryWrapper<Student> wrapper = new QueryWrapper<Student>().orderByDesc("create_time").orderByAsc("student_id");
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("student_id", keyword).or().like("name", keyword).or().like("phone", keyword));
        }
        if (StringUtils.hasText(college)) {
            wrapper.eq("college", college);
        }
        if (StringUtils.hasText(major)) {
            wrapper.eq("major", major);
        }
        if (checkedIn != null) {
            wrapper.eq("is_checked_in", checkedIn);
        }
        return studentMapper.selectPage(Page.of(page, size), wrapper);
    }

    @Transactional
    public Student saveStudent(StudentSaveRequest request) {
        AuthContext.requireAdmin();
        Student student = request.id() == null ? new Student() : studentMapper.selectById(request.id());
        if (student == null) {
            throw new BizException("学生不存在");
        }
        student.setStudentId(request.studentId());
        student.setName(request.name());
        student.setGender(request.gender());
        student.setCollege(request.college());
        student.setMajor(request.major());
        student.setClassName(request.className());
        student.setPhone(request.phone());
        student.setIdCard(request.idCard());
        student.setAddress(request.address());
        student.setPaid(Boolean.TRUE.equals(request.paid()));
        student.setCheckedIn(Boolean.TRUE.equals(request.checkedIn()));
        if (request.id() == null) {
            student.setPassword(passwordEncoder.encode("123456"));
            studentMapper.insert(student);
        } else {
            studentMapper.updateById(student);
        }
        return student;
    }

    @Transactional
    public void resetStudentPassword(Long id) {
        AuthContext.requireAdmin();
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BizException("学生不存在");
        }
        student.setPassword(passwordEncoder.encode("123456"));
        studentMapper.updateById(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        AuthContext.requireAdmin();
        Student student = studentMapper.selectById(id);
        if (student == null) {
            return;
        }
        if (student.getBedId() != null) {
            DormBed bed = bedMapper.selectById(student.getBedId());
            if (bed != null) {
                bed.setOccupied(false);
                bed.setStudentId(null);
                bedMapper.updateById(bed);
                DormRoom room = roomMapper.selectById(bed.getRoomId());
                if (room != null && room.getOccupiedCount() > 0) {
                    room.setOccupiedCount(room.getOccupiedCount() - 1);
                    roomMapper.updateById(room);
                }
            }
        }
        studentMapper.deleteById(id);
    }

    @Transactional
    public List<DormRoom> batchCreateRooms(DormBatchCreateRequest request) {
        AuthContext.requireAdmin();
        DormBuilding building = buildingMapper.selectById(request.buildingId());
        if (building == null) {
            throw new BizException("楼栋不存在");
        }
        if (!building.getGender().equals(request.gender())) {
            throw new BizException("房间性别必须与楼栋性别一致");
        }
        List<DormRoom> rooms = new ArrayList<>();
        for (int i = 0; i < request.count(); i++) {
            DormRoom room = new DormRoom();
            room.setBuildingId(request.buildingId());
            room.setRoomNo(String.valueOf(request.startNo() + i));
            room.setCapacity(request.capacity());
            room.setOccupiedCount(0);
            room.setMajor(request.major());
            room.setGender(request.gender());
            roomMapper.insert(room);
            for (int bedIndex = 1; bedIndex <= request.capacity(); bedIndex++) {
                DormBed bed = new DormBed();
                bed.setRoomId(room.getId());
                bed.setBedNo(String.valueOf(bedIndex));
                bed.setOccupied(false);
                bedMapper.insert(bed);
            }
            rooms.add(room);
        }
        return rooms;
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        AuthContext.requireAdmin();
        DormRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            return;
        }
        if (room.getOccupiedCount() != null && room.getOccupiedCount() > 0) {
            throw new BizException("房间已有学生入住，不能删除");
        }
        bedMapper.delete(new QueryWrapper<DormBed>().eq("room_id", roomId));
        roomMapper.deleteById(roomId);
    }

    public List<Map<String, Object>> occupancy() {
        AuthContext.requireAdmin();
        List<DormBuilding> buildings = buildingMapper.selectList(new QueryWrapper<DormBuilding>().orderByAsc("sort_no").orderByAsc("id"));
        List<DormRoom> rooms = roomMapper.selectList(new QueryWrapper<DormRoom>().orderByAsc("building_id").orderByAsc("room_no"));
        List<DormBed> beds = bedMapper.selectList(new QueryWrapper<DormBed>().orderByAsc("room_id").orderByAsc("bed_no"));
        Map<String, Student> students = studentMapper.selectList(new QueryWrapper<Student>()).stream()
                .collect(Collectors.toMap(Student::getStudentId, Function.identity(), (left, right) -> left));
        Map<Long, List<DormBed>> bedsByRoom = beds.stream().collect(Collectors.groupingBy(DormBed::getRoomId));
        Map<Long, List<DormRoom>> roomsByBuilding = rooms.stream().collect(Collectors.groupingBy(DormRoom::getBuildingId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (DormBuilding building : buildings) {
            Map<String, Object> buildingNode = new LinkedHashMap<>();
            buildingNode.put("building", building);
            List<Map<String, Object>> roomNodes = new ArrayList<>();
            for (DormRoom room : roomsByBuilding.getOrDefault(building.getId(), List.of())) {
                Map<String, Object> roomNode = new LinkedHashMap<>();
                roomNode.put("room", room);
                List<Map<String, Object>> bedNodes = new ArrayList<>();
                for (DormBed bed : bedsByRoom.getOrDefault(room.getId(), List.of())) {
                    Map<String, Object> bedNode = new LinkedHashMap<>();
                    bedNode.put("bed", bed);
                    bedNode.put("student", bed.getStudentId() == null ? null : students.get(bed.getStudentId()));
                    bedNodes.add(bedNode);
                }
                roomNode.put("beds", bedNodes);
                roomNodes.add(roomNode);
            }
            buildingNode.put("rooms", roomNodes);
            result.add(buildingNode);
        }
        return result;
    }

    public Page<QualificationModification> modificationPage(long page, long size, String status) {
        AuthContext.requireAdmin();
        QueryWrapper<QualificationModification> wrapper = new QueryWrapper<QualificationModification>().orderByDesc("create_time");
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        return modificationMapper.selectPage(Page.of(page, size), wrapper);
    }

    @Transactional
    public QualificationModification reviewModification(Long id, ModificationReviewRequest request) {
        AuthContext.requireAdmin();
        QualificationModification modification = modificationMapper.selectById(id);
        if (modification == null) {
            throw new BizException("申请不存在");
        }
        if (!"PENDING".equals(modification.getStatus())) {
            throw new BizException("该申请已审核");
        }
        if (Boolean.TRUE.equals(request.approved())) {
            Student student = studentMapper.selectOne(new QueryWrapper<Student>().eq("student_id", modification.getStudentId()));
            if (student == null) {
                throw new BizException("学生不存在");
            }
            applyStudentField(student, modification.getFieldName(), modification.getNewValue());
            studentMapper.updateById(student);
            modification.setStatus("APPROVED");
        } else {
            modification.setStatus("REJECTED");
        }
        modification.setReviewComment(request.comment());
        modification.setReviewer(AuthContext.get().account());
        modification.setReviewTime(LocalDateTime.now());
        modificationMapper.updateById(modification);
        return modification;
    }

    public Map<String, Object> dashboard() {
        AuthContext.requireAdmin();
        long totalStudents = studentMapper.selectCount(new QueryWrapper<>());
        long paidStudents = studentMapper.selectCount(new QueryWrapper<Student>().eq("is_paid", true));
        long checkedInStudents = studentMapper.selectCount(new QueryWrapper<Student>().eq("is_checked_in", true));
        long assignedStudents = studentMapper.selectCount(new QueryWrapper<Student>().isNotNull("bed_id"));

        List<Map<String, Object>> collegeRows = studentMapper.selectMaps(new QueryWrapper<Student>()
                .select("college", "COUNT(*) AS total", "SUM(CASE WHEN is_checked_in = 1 THEN 1 ELSE 0 END) AS checked_in")
                .groupBy("college")
                .orderByAsc("college"));

        LocalDate start = LocalDate.now().minusDays(6);
        List<Map<String, Object>> paymentRows = paymentRecordMapper.selectMaps(new QueryWrapper<PaymentRecord>()
                .select("DATE(pay_time) AS day", "COUNT(*) AS count")
                .eq("status", "PAID")
                .ge("pay_time", start.atStartOfDay())
                .groupBy("DATE(pay_time)")
                .orderByAsc("day"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalStudents", totalStudents);
        result.put("paidStudents", paidStudents);
        result.put("checkedInStudents", checkedInStudents);
        result.put("assignedStudents", assignedStudents);
        result.put("paidRate", rate(paidStudents, totalStudents));
        result.put("checkinRate", rate(checkedInStudents, totalStudents));
        result.put("collegeCheckin", collegeRows);
        result.put("paymentTrend", paymentRows);
        return result;
    }

    @Transactional
    public Student adminCheckin(Long studentId) {
        AuthContext.requireAdmin();
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BizException("学生不存在");
        }
        if (student.getBedId() == null) {
            throw new BizException("学生尚未分配宿舍");
        }
        student.setCheckedIn(true);
        studentMapper.updateById(student);

        CheckinRecord record = new CheckinRecord();
        record.setStudentId(student.getStudentId());
        record.setOperator(AuthContext.get().account());
        record.setRemark("管理员确认报到");
        record.setCheckinTime(LocalDateTime.now());
        checkinRecordMapper.insert(record);
        return student;
    }

    private double rate(long part, long total) {
        if (total == 0) {
            return 0;
        }
        return Math.round(part * 10000.0 / total) / 100.0;
    }

    private void applyStudentField(Student student, String fieldName, String value) {
        switch (fieldName) {
            case "name" -> student.setName(value);
            case "college" -> student.setCollege(value);
            case "major" -> student.setMajor(value);
            case "className" -> student.setClassName(value);
            case "phone" -> student.setPhone(value);
            case "idCard" -> student.setIdCard(value);
            case "address" -> student.setAddress(value);
            default -> throw new BizException("该字段不允许修改");
        }
    }
}
