package com.shashireddy.logistics.dto;

import com.shashireddy.logistics.model.Shipment;
import com.shashireddy.logistics.model.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class ShipmentDtos {

    private ShipmentDtos() {
    }

    public record CreateShipmentRequest(
            @NotBlank String carrier,
            @NotBlank String originHub,
            @NotBlank String destinationHub,
            @NotNull Instant estimatedDelivery) {
    }

    public record UpdateStatusRequest(@NotNull ShipmentStatus status) {
    }

    public record ShipmentResponse(
            String trackingId,
            String carrier,
            String originHub,
            String destinationHub,
            ShipmentStatus status,
            Instant estimatedDelivery,
            Instant actualDelivery,
            Instant createdAt,
            Instant updatedAt) {

        public static ShipmentResponse from(Shipment shipment) {
            return new ShipmentResponse(
                    shipment.getTrackingId(),
                    shipment.getCarrier(),
                    shipment.getOriginHub(),
                    shipment.getDestinationHub(),
                    shipment.getStatus(),
                    shipment.getEstimatedDelivery(),
                    shipment.getActualDelivery(),
                    shipment.getCreatedAt(),
                    shipment.getUpdatedAt());
        }
    }

    public record SlaStatsResponse(
            String carrier,
            long deliveredCount,
            long onTimeCount,
            long lateCount,
            double onTimeRate) {
    }
}
