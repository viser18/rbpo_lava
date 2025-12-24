package com.example.support.controller;

import com.example.support.service.TicketStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private TicketStatsService statsService;

    @GetMapping("/sla-compliance")
    public ResponseEntity<Map<String, Object>> getSlaCompliance() {
        return ResponseEntity.ok(statsService.getSlaComplianceStats());
    }

    @GetMapping("/agent-performance")
    public ResponseEntity<Map<String, Object>> getAgentPerformance() {
        return ResponseEntity.ok(statsService.getAgentPerformanceStats());
    }

    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyStats() {
        return ResponseEntity.ok(statsService.getWeeklyStats());
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> slaStats = statsService.getSlaComplianceStats();
        Map<String, Object> agentStats = statsService.getAgentPerformanceStats();
        Map<String, Object> weeklyStats = statsService.getWeeklyStats();

        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("slaCompliance", slaStats);
        summary.put("agentPerformance", agentStats);
        summary.put("weeklyOverview", weeklyStats);
        summary.put("timestamp", new java.util.Date());

        return ResponseEntity.ok(summary);
    }
}