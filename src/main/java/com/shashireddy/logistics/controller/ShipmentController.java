package com.shashireddy.logistics.controller;

import com.shashireddy.logistics.dto.ShipmentDtos.CreateShipmentRequest;
import com.shashireddy.logistics.dto.ShipmentDtos.ShipmentResponse;
import com.shashireddy.logistics.dto.ShipmentDtos.UpdateStatusRequest;
import com.shashireddy.logistics.model.ShipmentStatus;
import com.shashireddy.logistics.service.InvalidTransitionException;
import com.shashireddy.logistics.service.ShipmentTrackingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentTrackingService service;

    public ShipmentController(ShipmentTrackingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<ShipmentResponse> get(@PathVariable String trackingId) {
        return ResponseEntity.ok(service.getByTrackingId(trackingId));
    }

    @PostMapping("/{trackingId}/status")
    public ResponseEntity<ShipmentResponse> updateStatus(@PathVariable String trackingId,
                                                           @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(trackingId, request.status()));
    }

    @GetMapping
    public ResponseEntity<Page<ShipmentResponse>> list(
            @RequestParam(required = false) ShipmentStatus status, Pageable pageable) {
        return ResponseEntity.ok(service.list(status, pageable));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> handleNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(InvalidTransitionException.class)
    public ResponseEntity<String> handleInvalidTransition(InvalidTransitionException e) {
        return ResponseEntity.status(409).body(e.getMessage());
    }
}
