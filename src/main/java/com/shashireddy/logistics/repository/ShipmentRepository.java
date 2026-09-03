package com.shashireddy.logistics.repository;

import com.shashireddy.logistics.model.Shipment;
import com.shashireddy.logistics.model.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    Page<Shipment> findByCarrier(String carrier, Pageable pageable);
}
