package tw.edu.fju.miniclinic.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;
import tw.edu.fju.miniclinic.model.Appointment;
import tw.edu.fju.miniclinic.model.AppointmentForm;
import tw.edu.fju.miniclinic.model.AppointmentRepository;
import tw.edu.fju.miniclinic.model.Doctor;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import tw.edu.fju.miniclinic.model.Patient;
import tw.edu.fju.miniclinic.model.PatientRepository;

@Controller
public class AppointmentController {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    // GET：顯示表單
    @GetMapping("/appointment/new")
    public String newAppointmentForm(Model model) {
        model.addAttribute("form", new AppointmentForm());
        model.addAttribute("doctors", doctorRepo.findAll());
        return "appointment-new";
    }

    @PostMapping("/appointment/new")
    public String submitAppointment(
        @Valid @ModelAttribute("form") AppointmentForm form,   // ← 加 @Valid
        BindingResult result,                          // ← 緊接在 @Valid 參數之後
        Model model) {

       if (result.hasErrors()) {
		  model.addAttribute("form", form);
		  model.addAttribute("doctors", doctorRepo.findAll());
		  return "appointment-new";
	}

    // 步驟 1：用表單的字串 ID，從資料庫查出真正的物件
    Patient patient = patientRepo.findById(form.getChartNo()).orElse(null);
    Doctor  doctor  = doctorRepo.findById(form.getDoctorId()).orElse(null);

    // 步驟 2：驗證——找不到就回表單顯示錯誤
    if (patient == null || doctor == null) {
        model.addAttribute("error", "查無此病歷號或醫師，請確認後重試");
        model.addAttribute("form", form);
        model.addAttribute("doctors", doctorRepo.findAll());
        return "appointment-new";   // ← 回到表單頁，不是跳轉
    }

    // 步驟 3：建立 Appointment Entity，設定關聯物件
    Appointment appt = new Appointment();
    appt.setPatient(patient);
    appt.setDoctor(doctor);
    appt.setApptDate(LocalDate.parse(form.getApptDate()));  // 字串 → LocalDate
    appt.setTimeSlot(form.getTimeSlot());
    appt.setStatus("BOOKED");

    // 步驟 4：存入資料庫，JPA 自動填入 apptId
    Appointment saved = appointmentRepo.save(appt);

    // 步驟 5：把儲存後的物件交給結果頁面
    model.addAttribute("appointment", saved);
    return "appointment-result";
}

// API：回傳總掛號數
@GetMapping("/api/appointments/count")
public ResponseEntity<Map<String, Long>> getAppointmentCount() {
    return ResponseEntity.ok(Map.of("count", appointmentRepo.count()));
}

// API：依日期或醫師篩選掛號 (選填參數)
@GetMapping("/api/appointments")
public ResponseEntity<Object> getAppointments(
        @RequestParam(required = false) String date,
        @RequestParam(required = false) String doctorId) {

    if (date != null) {
        return ResponseEntity.ok(appointmentRepo.findByApptDate(LocalDate.parse(date)));
    } else if (doctorId != null) {
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);
        if (doctor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(appointmentRepo.findByDoctor(doctor));
    } else {
        return ResponseEntity.ok(appointmentRepo.findAll());
    }
}

@PostMapping("/api/appointments")
public ResponseEntity<Appointment> createAppointment(
		@RequestBody Map<String, String> request) {

	// 從 request 取出資料
	String chartNo = request.get("chartNo");
	String doctorId = request.get("doctorId");
	LocalDate apptDate = LocalDate.parse(request.get("apptDate"));
	String timeSlot = request.get("timeSlot");

	// 查詢關聯的 Patient 與 Doctor
	Patient patient = patientRepo.findById(chartNo).orElse(null);
	Doctor doctor = doctorRepo.findById(doctorId).orElse(null);

	if (patient == null || doctor == null) {
		return ResponseEntity.badRequest().build();
	}

	// 建立 Appointment 物件
	Appointment appt = new Appointment();
	appt.setPatient(patient);
	appt.setDoctor(doctor);
	appt.setApptDate(apptDate);
	appt.setTimeSlot(timeSlot);
	appt.setStatus("BOOKED");

	Appointment saved = appointmentRepo.save(appt);
	return ResponseEntity.status(201).body(saved);
   }

    // 修正：增加支援 "/api/appointments/{id}/status" 路径，以匹配前端 fetch 的網址
    @RequestMapping(value = {"/api/appointments/{id}", "/api/appointment/{id}", "/api/appointments/{id}/status"}, 
                    method = {RequestMethod.DELETE, RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.PATCH})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAppointment(@PathVariable String id) {
        System.out.println("==== 收到取消掛號請求 ====");
        System.out.println("請求的 ID 字串為: " + id);

        if ("undefined".equals(id) || "null".equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ID 是無效的 undefined"));
        }

        try {
            Long longId = Long.parseLong(id);
            if (!appointmentRepo.existsById(longId)) {
                System.out.println("刪除失敗：資料庫找不到 ID " + longId);
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "找不到掛號紀錄: " + longId));
            }
            appointmentRepo.deleteById(longId);
            System.out.println("成功刪除掛號，ID: " + longId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("刪除過程出錯: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "伺服器錯誤: " + e.getMessage()));
        }
    }

    // 網頁表單直接跳轉用 (處理 HTML <form> 提交)
    @PostMapping("/appointments/cancel/{id}")
    public String cancelAppointmentPage(@PathVariable Long id) {
        try {
            if (appointmentRepo.existsById(id)) {
                appointmentRepo.deleteById(id);
            }
        } catch (Exception e) {
            // 這裡可以記錄日誌
        }
        // 刪除後跳轉回儀表板，重新讀取清單
        return "redirect:/dashboard";
    }
}
