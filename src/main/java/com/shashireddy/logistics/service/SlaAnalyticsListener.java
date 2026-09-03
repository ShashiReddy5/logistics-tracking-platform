package com.shashireddy.logistics.service;

import com.shashireddy.logistics.event.ShipmentStatusChangedEvent;
import com.shashireddy.logistics.model.CarrierSlaStats;
import com.shashireddy.logistics.model.Shipment;
import com.shashireddy.logistics.model.ShipmentStatus;
import com.shashireddy.logistics.repository.CarrierSlaStatsRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reacts to every {@link ShipmentStatusChangedEvent} and, when a shipment
 * reaches DELIVERED, updates that carrier's running on-time/late counters.
 * This is what actually powers {@code GET /api/analytics/sla} — the numbers
 * are computed for real, synchronously, in-process.
 */
@Component
public class SlaAnalyticsListener {

    private final CarrierSlaStatsRepository statsRepository;

    public SlaAnalyticsListener(CarrierSlaStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @EventListener
    @Transactional
    public void onShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        Shipment shipment = event.getShipment();
        if (shipment.getStatus() != ShipmentStatus.DELIVERED) {
            return;
        }

        CarrierSlaStats stats = statsRepository.findById(shipment.getCarrier())
                .orElseGet(() -> new CarrierSlaStats(shipment.getCarrier()));

        boolean onTime = shipment.getActualDelivery() != null
                && shipment.getEstimatedDelivery() != null
                && !shipment.getActualDelivery().isAfter(shipment.getEstimatedDelivery());

        stats.recordDelivery(onTime);
        statsRepository.save(stats);
    }
}
