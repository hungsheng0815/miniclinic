package tw.edu.fju.miniclinic.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "ok",
            "service", "miniclinic"
        );
    }
      @GetMapping("/api/about")
    public Map<String, String> about() {
        return Map.of(
            "student_id", "414570320",
            "student_name", "林汯陞",
            "project","MiniClinic",
            "version","0.1.0",
            "chapter","Ch09-A"

        );
    }
}
