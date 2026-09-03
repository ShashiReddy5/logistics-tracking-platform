package com.shashireddy.logistics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

/**
 * Append-only log of every status transition a shipment goes through.
 * This is the real, working piece of the "event-driven" claim in this repo:
 * every transition is persisted here and also published in-process (see
 * {@code event.ShipmentStatusChangedEvent}) so other components can react to
 * it — there's just no Kafka broker behind it, it's Spring's own
 * ApplicationEventPublisher.
 */
@Entity
public class ShipmentEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String trackingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus toStatus;

    @Column(nullable = false)
    private Instant occurredAt;

    protected ShipmentEventLog() {
        // for JPA
    }

    public ShipmentEventLog(String trackingId, ShipmentStatus fromStatus, ShipmentStatus toStatus) {
        this.trackingId = trackingId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.occurredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public ShipmentStatus getFromStatus() {
        return fromStatus;
    }

    public ShipmentStatus getToStatus() {
        return toStatus;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
