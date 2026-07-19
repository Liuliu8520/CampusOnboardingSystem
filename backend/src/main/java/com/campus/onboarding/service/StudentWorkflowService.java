package com.campus.onboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.onboarding.common.BizException;
import com.campus.onboarding.dto.ModificationApplyRequest;
import com.campus.onboarding.dto.PaymentRequest;
import com.campus.onboarding.dto.StudentProfileResponse;
import com.campus.onboarding.entity.Announcement;
import com.campus.onboarding.entity.CheckinRecord;
import com.campus.onboarding.entity.DormBed;
import com.campus.onboarding.entity.DormBuilding;
import com.campus.onboarding.entity.DormRoom;
import com.campus.onboarding.entity.FeeItem;
import com.campus.onboarding.entity.PaymentRecord;
import com.campus.onboarding.entity.QualificationModification;
import com.campus.onboarding.entity.Student;
import com.campus.onboarding.mapper.AnnouncementMapper;
import com.campus.onboarding.mapper.CheckinRecordMapper;
import com.campus.onboarding.mapper.DormBedMapper;
import com.campus.onboarding.mapper.DormBuildingMapper;
import com.campus.onboarding.mapper.DormRoomMapper;
import com.campus.onboarding.mapper.FeeItemMapper;
import com.campus.onboarding.mapper.PaymentRecordMapper;
import com.campus.onboarding.mapper.QualificationModificationMapper;
import com.campus.onboarding.mapper.StudentMapper;
import com.campus.onboarding.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentWorkflowService {
    private static final Map<String, String> MODIFIABLE_FIELDS = Map.of(
            "name", "姓名",
            "college", "学院",
            "major", "专业",
            "className", "班级",
            "phone", "手机号",
            "idCard", "身份证号",
            "address", "家庭地址"
    );

    private final StudentMapper studentMapper;
    private final QualificationModificationMapper modificationMapper;
    private final FeeItemMapper feeItemMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final DormBuildingMapper buildingMapper;
    private final DormRoomMapper roomMapper;
    private final DormBedMapper bedMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final AnnouncementMapper announcementMapper;

    public StudentWorkflowService(StudentMapper studentMapper,
                                  QualificationModificationMapper modificationMapper,
                                  FeeItemMapper feeItemMapper,
                                  PaymentRecordMapper paymentRecordMapper,
                                  DormBuildingMapper buildingMapper,
                                  DormRoomMapper roomMapper,
                                  DormBedMapper bedMapper,
                                  CheckinRecordMapper checkinRecordMapper,
                                  AnnouncementMapper announcementMapper) {
        this.studentMapper = studentMapper;
        this.modificationMapper = modificationMapper;
        this.feeItemMapper = feeItemMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.buildingMapper = buildingMapper;
        this.roomMapper = roomMapper;
        this.bedMapper = bedMapper;
        this.checkinRecordMapper = checkinRecordMapper;
        this.announcementMapper = announcementMapper;
    }

    public StudentProfileResponse profile() {
        Student student = currentStudent();
        List<QualificationModification> modifications = modificationMapper.selectList(
                new QueryWrapper<QualificationModification>()
                        .eq("student_id", student.getStudentId())
                        .orderByDesc("create_time")
        );
        int step = currentStep(student, modifications);
        return new StudentProfileResponse(student, step, stepName(step), dormDetail(student), modifications);
    }

    public Student currentStudent() {
        String studentId = AuthContext.requireStudent().account();
        Student student = studentMapper.selectOne(new QueryWrapper<Student>().eq("student_id", studentId));
        if (student == null) {
            throw new BizException(401, "学生信息不存在，请重新登录");
        }
        return student;
    }

    @Transactional
    public QualificationModification applyModification(ModificationApplyRequest request) {
        Student student = currentStudent();
        String fieldLabel = MODIFIABLE_FIELDS.get(request.fieldName());
        if (fieldLabel == null) {
            throw new BizException("该字段不允许申请修改");
        }
        boolean hasPending = modificationMapper.selectCount(new QueryWrapper<QualificationModification>()
                .eq("student_id", student.getStudentId())
                .eq("field_name", request.fieldName())
                .eq("status", "PENDING")) > 0;
        if (hasPending) {
            throw new BizException("该字段已有待审核申请，请勿重复提交");
        }
        QualificationModification modification = new QualificationModification();
        modification.setStudentId(student.getStudentId());
        modification.setFieldName(request.fieldName());
        modification.setFieldLabel(fieldLabel);
        modification.setOldValue(fieldValue(student, request.fieldName()));
        modification.setNewValue(request.newValue());
        modification.setReason(request.reason());
        modification.setStatus("PENDING");
        modificationMapper.insert(modification);
        return modification;
    }

    public List<Map<String, Object>> paymentItems() {
        Student student = currentStudent();
        List<FeeItem> items = feeItemMapper.selectList(new QueryWrapper<FeeItem>()
                .eq("is_enabled", true)
                .orderByDesc("is_required")
                .orderByAsc("id"));
        Set<Long> paidIds = paymentRecordMapper.selectList(new QueryWrapper<PaymentRecord>()
                        .eq("student_id", student.getStudentId())
                        .eq("status", "PAID"))
                .stream()
                .map(PaymentRecord::getFeeItemId)
                .collect(Collectors.toSet());
        List<Map<String, Object>> result = new ArrayList<>();
        for (FeeItem item : items) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("name", item.getName());
            row.put("amount", item.getAmount());
            row.put("required", item.getRequired());
            row.put("enabled", item.getEnabled());
            row.put("description", item.getDescription());
            row.put("paid", paidIds.contains(item.getId()));
            result.add(row);
        }
        return result;
    }

    @Transactional
    public Map<String, Object> pay(PaymentRequest request) {
        Student student = currentStudent();
        LocalDateTime now = LocalDateTime.now();
        for (Long feeItemId : request.feeItemIds()) {
            FeeItem item = feeItemMapper.selectById(feeItemId);
            if (item == null || !Boolean.TRUE.equals(item.getEnabled())) {
                throw new BizException("缴费项目不存在或已停用");
            }
            PaymentRecord existing = paymentRecordMapper.selectOne(new QueryWrapper<PaymentRecord>()
                    .eq("student_id", student.getStudentId())
                    .eq("fee_item_id", feeItemId));
            if (existing == null) {
                PaymentRecord record = new PaymentRecord();
                record.setStudentId(student.getStudentId());
                record.setFeeItemId(feeItemId);
                record.setAmount(item.getAmount());
                record.setStatus("PAID");
                record.setPayTime(now);
                paymentRecordMapper.insert(record);
            } else if (!"PAID".equals(existing.getStatus())) {
                existing.setStatus("PAID");
                existing.setAmount(item.getAmount());
                existing.setPayTime(now);
                paymentRecordMapper.updateById(existing);
            }
        }
        refreshRequiredPaymentFlag(student);
        return Map.of("profile", profile(), "items", paymentItems());
    }

    @Transactional
    public synchronized Map<String, Object> assignDorm() {
        Student student = currentStudent();
        if (!Boolean.TRUE.equals(student.getPaid())) {
            throw new BizException("请先完成全部必缴项目");
        }
        if (student.getBedId() != null) {
            return dormDetail(student);
        }

        DormRoom room = roomMapper.selectOne(new QueryWrapper<DormRoom>()
                .eq("gender", student.getGender())
                .eq("major", student.getMajor())
                .apply("occupied_count < capacity")
                .orderByDesc("occupied_count")
                .orderByAsc("id")
                .last("LIMIT 1"));

        if (room == null) {
            room = createRoomForStudent(student);
        }

        DormBed bed = bedMapper.selectOne(new QueryWrapper<DormBed>()
                .eq("room_id", room.getId())
                .eq("is_occupied", false)
                .orderByAsc("bed_no")
                .last("LIMIT 1"));
        if (bed == null) {
            throw new BizException("目标房间暂无可用床位，请重试");
        }

        bed.setOccupied(true);
        bed.setStudentId(student.getStudentId());
        bedMapper.updateById(bed);

        room.setOccupiedCount(room.getOccupiedCount() + 1);
        roomMapper.updateById(room);

        student.setBedId(bed.getId());
        studentMapper.updateById(student);
        return dormDetail(currentStudent());
    }

    @Transactional
    public StudentProfileResponse checkin() {
        Student student = currentStudent();
        if (student.getBedId() == null) {
            throw new BizException("请先完成宿舍分配");
        }
        if (!Boolean.TRUE.equals(student.getCheckedIn())) {
            student.setCheckedIn(true);
            studentMapper.updateById(student);

            CheckinRecord record = new CheckinRecord();
            record.setStudentId(student.getStudentId());
            record.setOperator(student.getStudentId());
            record.setRemark("学生端确认到校");
            record.setCheckinTime(LocalDateTime.now());
            checkinRecordMapper.insert(record);
        }
        return profile();
    }

    public List<Announcement> publishedAnnouncements() {
        return announcementMapper.selectList(new QueryWrapper<Announcement>()
                .eq("is_published", true)
                .orderByDesc("create_time"));
    }

    public Map<String, Object> dormDetail(Student student) {
        if (student.getBedId() == null) {
            return null;
        }
        DormBed bed = bedMapper.selectById(student.getBedId());
        if (bed == null) {
            return null;
        }
        DormRoom room = roomMapper.selectById(bed.getRoomId());
        if (room == null) {
            return null;
        }
        DormBuilding building = buildingMapper.selectById(room.getBuildingId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("building", building);
        result.put("room", room);
        result.put("bed", bed);
        result.put("display", (building == null ? "" : building.getName()) + " " + room.getRoomNo() + "室 " + bed.getBedNo() + "床");
        return result;
    }

    private DormRoom createRoomForStudent(Student student) {
        DormBuilding building = buildingMapper.selectOne(new QueryWrapper<DormBuilding>()
                .eq("gender", student.getGender())
                .orderByAsc("sort_no")
                .orderByAsc("id")
                .last("LIMIT 1"));
        if (building == null) {
            throw new BizException("暂无匹配性别的宿舍楼栋，请联系管理员");
        }
        DormRoom lastRoom = roomMapper.selectOne(new QueryWrapper<DormRoom>()
                .eq("building_id", building.getId())
                .orderByDesc("room_no")
                .last("LIMIT 1"));
        int nextNo = lastRoom == null ? 101 : parseRoomNo(lastRoom.getRoomNo()) + 1;
        DormRoom room = new DormRoom();
        room.setBuildingId(building.getId());
        room.setRoomNo(String.valueOf(nextNo));
        room.setCapacity(4);
        room.setOccupiedCount(0);
        room.setMajor(student.getMajor());
        room.setGender(student.getGender());
        roomMapper.insert(room);
        for (int i = 1; i <= room.getCapacity(); i++) {
            DormBed bed = new DormBed();
            bed.setRoomId(room.getId());
            bed.setBedNo(String.valueOf(i));
            bed.setOccupied(false);
            bedMapper.insert(bed);
        }
        return room;
    }

    private int parseRoomNo(String roomNo) {
        try {
            String digits = roomNo == null ? "" : roomNo.replaceAll("\\D", "");
            return digits.isBlank() ? 100 : Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return 100;
        }
    }

    private void refreshRequiredPaymentFlag(Student student) {
        List<FeeItem> requiredItems = feeItemMapper.selectList(new QueryWrapper<FeeItem>()
                .eq("is_enabled", true)
                .eq("is_required", true));
        Set<Long> paidIds = paymentRecordMapper.selectList(new QueryWrapper<PaymentRecord>()
                        .eq("student_id", student.getStudentId())
                        .eq("status", "PAID"))
                .stream()
                .map(PaymentRecord::getFeeItemId)
                .collect(Collectors.toSet());
        boolean allRequiredPaid = requiredItems.stream().allMatch(item -> paidIds.contains(item.getId()));
        student.setPaid(allRequiredPaid);
        studentMapper.updateById(student);
    }

    private int currentStep(Student student, List<QualificationModification> modifications) {
        boolean hasPendingModification = modifications.stream().anyMatch(item -> Objects.equals(item.getStatus(), "PENDING"));
        if (hasPendingModification) {
            return 1;
        }
        if (!Boolean.TRUE.equals(student.getPaid())) {
            return 2;
        }
        if (student.getBedId() == null) {
            return 3;
        }
        if (!Boolean.TRUE.equals(student.getCheckedIn())) {
            return 4;
        }
        return 5;
    }

    private String stepName(int step) {
        return switch (step) {
            case 1 -> "资格核验";
            case 2 -> "缴费";
            case 3 -> "宿舍分配";
            case 4 -> "现场报到";
            default -> "已完成";
        };
    }

    private String fieldValue(Student student, String fieldName) {
        return switch (fieldName) {
            case "name" -> student.getName();
            case "college" -> student.getCollege();
            case "major" -> student.getMajor();
            case "className" -> student.getClassName();
            case "phone" -> student.getPhone();
            case "idCard" -> student.getIdCard();
            case "address" -> student.getAddress();
            default -> "";
        };
    }
}
