package com.shashireddy.logistics.service;

import com.shashireddy.logistics.dto.ShipmentDtos.CreateShipmentRequest;
import com.shashireddy.logistics.dto.ShipmentDtos.ShipmentResponse;
import com.shashireddy.logistics.event.ShipmentStatusChangedEvent;
import com.shashireddy.logistics.model.Shipment;
import com.shashireddy.logistics.model.ShipmentEventLog;
import com.shashireddy.logistics.model.ShipmentStatus;
import com.shashireddy.logistics.repository.ShipmentEventLogRepository;
import com.shashireddy.logistics.repository.ShipmentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class ShipmentTrackingService {

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ShipmentStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ShipmentStatus.CREATED, EnumSet.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.EXCEPTION));
        ALLOWED_TRANSITIONS.put(ShipmentStatus.IN_TRANSIT, EnumSet.of(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.EXCEPTION));
        ALLOWED_TRANSITIONS.put(ShipmentStatus.OUT_FOR_DELIVERY, EnumSet.of(ShipmentStatus.DELIVERED, ShipmentStatus.EXCEPTION));
        ALLOWED_TRANSITIONS.put(ShipmentStatus.DELIVERED, EnumSet.noneOf(ShipmentStatus.class));
        ALLOWED_TRANSITIONS.put(ShipmentStatus.EXCEPTION, EnumSet.of(ShipmentStatus.IN_TRANSIT));
    }

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventLogRepository eventLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ShipmentTrackingService(ShipmentRepository shipmentRepository,
                                    ShipmentEventLogRepository eventLogRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.eventLogRepository = eventLogRepository;
        this.eventPublisher = eventPublisher;
    }

    public ShipmentResponse create(CreateShipmentRequest request) {
        String trackingId = "TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        Shipment shipment = new Shipment(trackingId, request.carrier(), request.originHub(),
                request.destinationHub(), request.estimatedDelivery());
        shipmentRepository.save(shipment);
        eventLogRepository.save(new ShipmentEventLog(trackingId, ShipmentStatus.CREATED, ShipmentStatus.CREATED));
        return ShipmentResponse.from(shipment);
    }

    public ShipmentResponse updateStatus(String trackingId, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepository.findById(trackingId)
                .orElseThrow(() -> new NoSuchElementException("No shipment with trackingId " + trackingId));

        ShipmentStatus previousStatus = shipment.getStatus();
        Set<ShipmentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(previousStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidTransitionException(
                    "Cannot move shipment from " + previousStatus + " to " + newStatus);
        }

        shipment.setStatus(newStatus);
        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setActualDelivery(Instant.now());
        }
        shipmentRepository.save(shipment);
        eventLogRepository.save(new ShipmentEventLog(trackingId, previousStatus, newStatus));

        eventPublisher.publishEvent(new ShipmentStatusChangedEvent(this, shipment, previousStatus));

        return ShipmentResponse.from(shipment);
    }

    public ShipmentResponse getByTrackingId(String trackingId) {
        return shipmentRepository.findById(trackingId)
                .map(ShipmentResponse::from)
                .orElseThrow(() -> new NoSuchElementException("No shipment with trackingId " + trackingId));
    }

    public Page<ShipmentResponse> list(ShipmentStatus status, Pageable pageable) {
        Page<Shipment> page = status == null
                ? shipmentRepository.findAll(pageable)
                : shipmentRepository.findByStatus(status, pageable);
        return page.map(ShipmentResponse::from);
    }
}
