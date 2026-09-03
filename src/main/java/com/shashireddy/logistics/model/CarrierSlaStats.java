package com.shashireddy.logistics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Real-time, per-carrier on-time delivery aggregate. Updated synchronously
 * (in the same JVM, via a Spring {@code @EventListener}) every time a
 * shipment is marked DELIVERED — this is the honest, in-process stand-in
 * for the "Elasticsearch analytics / Kibana dashboards" claim: the numbers
 * are real and live, they're just served from a JPA-backed table instead of
 * an Elasticsearch cluster.
 */
@Entity
public class CarrierSlaStats {

    @Id
    private String carrier;

    @Column(nullable = false)
    private long deliveredCount;

    @Column(nullable = false)
    private long onTimeCount;

    @Column(nullable = false)
    private long lateCount;

    protected CarrierSlaStats() {
        // for JPA
    }

    public CarrierSlaStats(String carrier) {
        this.carrier = carrier;
        this.deliveredCount = 0;
        this.onTimeCount = 0;
        this.lateCount = 0;
    }

    public void recordDelivery(boolean onTime) {
        this.deliveredCount++;
        if (onTime) {
            this.onTimeCount++;
        } else {
            this.lateCount++;
        }
    }

    public String getCarrier() {
        return carrier;
    }

    public long getDeliveredCount() {
        return deliveredCount;
    }

    public long getOnTimeCount() {
        return onTimeCount;
    }

    public long getLateCount() {
        return lateCount;
    }

    public double getOnTimeRate() {
        return deliveredCount == 0 ? 0.0 : (double) onTimeCount / deliveredCount;
    }
}
