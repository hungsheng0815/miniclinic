package tw.edu.fju.miniclinic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.edu.fju.miniclinic.model.StatsResponse;
import tw.edu.fju.miniclinic.model.StatsService;

@RestController
@RequestMapping("/api")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getSystemStats() {
        StatsResponse stats = statsService.getSystemStats();
        return ResponseEntity.ok(stats);
    }
}