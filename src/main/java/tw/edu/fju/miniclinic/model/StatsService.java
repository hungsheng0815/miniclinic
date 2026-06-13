package tw.edu.fju.miniclinic.model;

import tw.edu.fju.miniclinic.model.AppointmentRepository;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import tw.edu.fju.miniclinic.model.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public StatsService(DoctorRepository doctorRepository,
                        PatientRepository patientRepository,
                        AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public StatsResponse getSystemStats() {
        long totalDoctors = doctorRepository.count();
        long totalPatients = patientRepository.count();
        long totalAppointments = appointmentRepository.count();

        // Assuming Appointment entity has a 'status' field (e.g., enum or String)
        // And AppointmentStatus is an enum like BOOKED, COMPLETED, CANCELLED
        List<Object[]> appointmentCountsByStatus = appointmentRepository.countAppointmentsByStatus();

        Map<String, Long> byStatus = appointmentCountsByStatus.stream()
                .collect(Collectors.toMap(
                        arr -> String.valueOf(arr[0]), // 使用 String.valueOf 較為安全
                        arr -> (Long) arr[1]
                ));

        StatsResponse response = new StatsResponse();
        response.setTotalDoctors(totalDoctors);
        response.setTotalPatients(totalPatients);
        response.setTotalAppointments(totalAppointments);
        response.setByStatus(byStatus);
        return response;
    }
}