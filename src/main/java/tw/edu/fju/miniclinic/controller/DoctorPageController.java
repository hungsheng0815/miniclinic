package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import tw.edu.fju.miniclinic.model.Doctor;
import tw.edu.fju.miniclinic.model.DoctorRepository;

import java.util.List;
import java.util.Optional;

@Controller
public class DoctorPageController {

    @Autowired
    private DoctorRepository doctorRepo;

    @GetMapping("/doctors")
    public String listDoctors(
            @RequestParam(required = false) String department,
            Model model) {

        List<Doctor> doctors;
        if (department == null || department.isBlank()) {
            doctors = doctorRepo.findAll();
        } else {
            doctors = doctorRepo.findByDepartment(department);
        }

        model.addAttribute("doctors", doctors);
        model.addAttribute("departments", doctorRepo.findAllDepartments());
        model.addAttribute("selectedDept", department);

        return "doctors";
    }

    @GetMapping("/doctors/{doctorId}")
public String doctorDetail(@PathVariable String doctorId, Model model) {
    Optional<Doctor> doctor = doctorRepo.findById(doctorId);

    if (doctor.isEmpty()) {
        return "redirect:/doctors";  // 找不到就回清單頁
    }

    model.addAttribute("doctor", doctor.get());
    return "doctor-detail";
    }
}