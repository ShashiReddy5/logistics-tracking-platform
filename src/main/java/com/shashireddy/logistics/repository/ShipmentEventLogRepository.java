package com.shashireddy.logistics.repository;

import com.shashireddy.logistics.model.ShipmentEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentEventLogRepository extends JpaRepository<ShipmentEventLog, Long> {

    List<ShipmentEventLog> findByTrackingIdOrderByOccurredAtAsc(String trackingId);
}
