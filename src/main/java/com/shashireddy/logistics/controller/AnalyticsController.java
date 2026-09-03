package com.shashireddy.logistics.controller;

import com.shashireddy.logistics.dto.ShipmentDtos.SlaStatsResponse;
import com.shashireddy.logistics.service.SlaAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final SlaAnalyticsService analyticsService;

    public AnalyticsController(SlaAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/sla")
    public ResponseEntity<List<SlaStatsResponse>> slaOverview() {
        return ResponseEntity.ok(analyticsService.listAll());
    }

    @GetMapping("/sla/{carrier}")
    public ResponseEntity<SlaStatsResponse> slaForCarrier(@PathVariable String carrier) {
        return ResponseEntity.ok(analyticsService.getForCarrier(carrier));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> handleNotFound() {
        return ResponseEntity.notFound().build();
    }
}
