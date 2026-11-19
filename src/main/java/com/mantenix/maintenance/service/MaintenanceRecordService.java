package com.mantenix.maintenance.service;

import com.mantenix.maintenance.dto.MaintenanceRecordRequest;
import com.mantenix.maintenance.dto.MaintenanceRecordResponse;
import com.mantenix.maintenance.exception.BusinessRuleException;
import com.mantenix.maintenance.exception.ResourceNotFoundException;
import com.mantenix.maintenance.model.MaintenanceRecord;
import com.mantenix.maintenance.repository.MaintenanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Business logic for maintenance records. Controllers only call this class; they never touch the repository. */
@Service
public class MaintenanceRecordService {

    /** Default window for upcoming services when the client does not send a date. */
    static final int DEFAULT_UPCOMING_DAYS = 30;

    private final MaintenanceRecordRepository repository;

    public MaintenanceRecordService(MaintenanceRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MaintenanceRecordResponse create(MaintenanceRecordRequest request) {
        validateNextService(request);
        MaintenanceRecord record = new MaintenanceRecord();
        applyChanges(record, request);
        return MaintenanceRecordResponse.from(repository.save(record));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> findAll(UUID vehicleId) {
        List<MaintenanceRecord> records = (vehicleId == null)
                ? repository.findAllByOrderByServiceDateDesc()
                : repository.findByVehicleIdOrderByServiceDateDesc(vehicleId);
        return records.stream().map(MaintenanceRecordResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceRecordResponse findById(UUID id) {
        return MaintenanceRecordResponse.from(getRecord(id));
    }

    /** Records whose next service date is on or before {@code until} (default: today + 30 days). */
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> findUpcoming(LocalDate until) {
        LocalDate limit = (until == null) ? LocalDate.now().plusDays(DEFAULT_UPCOMING_DAYS) : until;
        return repository.findByNextServiceDateLessThanEqualOrderByNextServiceDateAsc(limit).stream()
                .map(MaintenanceRecordResponse::from)
                .toList();
    }

    @Transactional
    public MaintenanceRecordResponse update(UUID id, MaintenanceRecordRequest request) {
        MaintenanceRecord record = getRecord(id);
        validateNextService(request);
        applyChanges(record, request);
        return MaintenanceRecordResponse.from(repository.saveAndFlush(record));
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(getRecord(id));
    }

    private MaintenanceRecord getRecord(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record " + id + " not found"));
    }

    // Business rule: the next service must come after the service being registered.
    private void validateNextService(MaintenanceRecordRequest request) {
        if (request.nextServiceDate() != null && !request.nextServiceDate().isAfter(request.serviceDate())) {
            throw new BusinessRuleException("INVALID_NEXT_SERVICE_DATE",
                    "The next service date must be after the service date (" + request.serviceDate() + ")");
        }
        if (request.nextServiceMileage() != null && request.nextServiceMileage() <= request.mileage()) {
            throw new BusinessRuleException("INVALID_NEXT_SERVICE_MILEAGE",
                    "The next service mileage must be greater than the current mileage (" + request.mileage() + " km)");
        }
    }

    private void applyChanges(MaintenanceRecord record, MaintenanceRecordRequest request) {
        record.setVehicleId(request.vehicleId());
        record.setType(request.type());
        record.setDescription(trimToNull(request.description()));
        record.setServiceDate(request.serviceDate());
        record.setMileage(request.mileage());
        record.setCost(request.cost());
        record.setWorkshop(trimToNull(request.workshop()));
        record.setNextServiceDate(request.nextServiceDate());
        record.setNextServiceMileage(request.nextServiceMileage());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
