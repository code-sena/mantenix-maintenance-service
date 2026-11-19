package com.mantenix.maintenance.service;

import com.mantenix.maintenance.dto.MaintenanceRecordRequest;
import com.mantenix.maintenance.dto.MaintenanceRecordResponse;
import com.mantenix.maintenance.exception.BusinessRuleException;
import com.mantenix.maintenance.exception.ResourceNotFoundException;
import com.mantenix.maintenance.model.MaintenanceRecord;
import com.mantenix.maintenance.model.MaintenanceType;
import com.mantenix.maintenance.repository.MaintenanceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for the business rules of MaintenanceRecordService (no database needed). */
class MaintenanceRecordServiceTest {

    private static final LocalDate SERVICE_DATE = LocalDate.of(2026, 11, 15);

    private MaintenanceRecordRepository repository;
    private MaintenanceRecordService service;

    private final UUID vehicleId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = mock(MaintenanceRecordRepository.class);
        service = new MaintenanceRecordService(repository);
        when(repository.save(any(MaintenanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private MaintenanceRecordRequest request(LocalDate nextDate, Integer nextMileage) {
        return new MaintenanceRecordRequest(vehicleId, MaintenanceType.OIL_CHANGE, "  Oil and filter  ",
                SERVICE_DATE, 35000, new BigDecimal("180000.00"), "   ", nextDate, nextMileage);
    }

    @Test
    void createSavesTheRecordAndCleansTheTexts() {
        MaintenanceRecordResponse response = service.create(request(LocalDate.of(2027, 5, 15), 40000));

        assertThat(response.vehicleId()).isEqualTo(vehicleId);
        assertThat(response.description()).isEqualTo("Oil and filter");
        assertThat(response.workshop()).isNull();
        assertThat(response.nextServiceMileage()).isEqualTo(40000);
    }

    @Test
    void createRejectsANextServiceDateNotAfterTheServiceDate() {
        assertThatThrownBy(() -> service.create(request(SERVICE_DATE, null)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("INVALID_NEXT_SERVICE_DATE");
        verify(repository, never()).save(any());
    }

    @Test
    void createRejectsANextServiceMileageNotGreaterThanTheMileage() {
        assertThatThrownBy(() -> service.create(request(null, 35000)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("INVALID_NEXT_SERVICE_MILEAGE");
        verify(repository, never()).save(any());
    }

    @Test
    void findUpcomingUsesTheGivenDate() {
        LocalDate until = LocalDate.of(2027, 1, 31);
        MaintenanceRecord record = new MaintenanceRecord();
        record.setVehicleId(vehicleId);
        record.setNextServiceDate(LocalDate.of(2027, 1, 10));
        when(repository.findByNextServiceDateLessThanEqualOrderByNextServiceDateAsc(until)).thenReturn(List.of(record));

        List<MaintenanceRecordResponse> upcoming = service.findUpcoming(until);

        assertThat(upcoming).hasSize(1);
        assertThat(upcoming.getFirst().nextServiceDate()).isEqualTo(LocalDate.of(2027, 1, 10));
    }

    @Test
    void findByIdFailsWhenTheRecordDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
