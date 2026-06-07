package tw.edu.fju.miniclinic.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByApptDate(LocalDate apptDate);
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByDoctorAndApptDate(Doctor doctor, LocalDate apptDate);

    // 使用 @Query 註解來定義 JPQL 查詢，以實現按科別統計掛號數
    // SELECT d.department, COUNT(a) FROM Appointment a JOIN a.doctor d GROUP BY d.department
    // 返回的 List<Object[]> 中，每個 Object[] 包含 (String departmentName, Long count)
    @Query("SELECT a.doctor.department, COUNT(a) FROM Appointment a GROUP BY a.doctor.department")
    List<Object[]> countAppointmentsByDepartment();
}