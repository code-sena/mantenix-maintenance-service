package com.mantenix.maintenance.repository;

import com.mantenix.maintenance.model.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, UUID> {

    List<MaintenanceRecord> findAllByOrderByServiceDateDesc();

    List<MaintenanceRecord> findByVehicleIdOrderByServiceDateDesc(UUID vehicleId);

    /** Records whose next service is due on or before the given date (base for reminders). */
    List<MaintenanceRecord> findByNextServiceDateLessThanEqualOrderByNextServiceDateAsc(LocalDate date);
}
