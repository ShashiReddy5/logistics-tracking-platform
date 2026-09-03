package com.shashireddy.logistics.event;

import com.shashireddy.logistics.model.Shipment;
import com.shashireddy.logistics.model.ShipmentStatus;
import org.springframework.context.ApplicationEvent;

/**
 * Published in-process (via Spring's {@code ApplicationEventPublisher})
 * every time a shipment's status changes. {@code service.SlaAnalyticsListener}
 * subscribes to this to keep {@code CarrierSlaStats} up to date in real time.
 * <p>
 * This is a genuine publish/subscribe seam, it just runs inside one JVM
 * instead of over a Kafka topic. Swapping in a real broker later means
 * publishing this same event to a topic in addition to (or instead of)
 * firing it locally — the listener contract doesn't have to change.
 */
public class ShipmentStatusChangedEvent extends ApplicationEvent {

    private final Shipment shipment;
    private final ShipmentStatus previousStatus;

    public ShipmentStatusChangedEvent(Object source, Shipment shipment, ShipmentStatus previousStatus) {
        super(source);
        this.shipment = shipment;
        this.previousStatus = previousStatus;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public ShipmentStatus getPreviousStatus() {
        return previousStatus;
    }
}
