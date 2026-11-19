package com.mantenix.maintenance.dto;

import com.mantenix.maintenance.model.MaintenanceRecord;
import com.mantenix.maintenance.model.MaintenanceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Data the API returns. The JPA entity is never exposed directly. */
public record MaintenanceRecordResponse(
        UUID id,
        UUID vehicleId,
        MaintenanceType type,
        String description,
        LocalDate serviceDate,
        Integer mileage,
        BigDecimal cost,
        String workshop,
        LocalDate nextServiceDate,
        Integer nextServiceMileage,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static MaintenanceRecordResponse from(MaintenanceRecord record) {
        return new MaintenanceRecordResponse(
                record.getId(),
                record.getVehicleId(),
                record.getType(),
                record.getDescription(),
                record.getServiceDate(),
                record.getMileage(),
                record.getCost(),
                record.getWorkshop(),
                record.getNextServiceDate(),
                record.getNextServiceMileage(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
