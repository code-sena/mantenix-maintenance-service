package com.mantenix.maintenance.dto;

import com.mantenix.maintenance.model.MaintenanceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Data the client sends to register or update a maintenance record. */
public record MaintenanceRecordRequest(

        @Schema(description = "Vehicle id (from vehicle-service)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull
        UUID vehicleId,

        @Schema(example = "OIL_CHANGE")
        @NotNull
        MaintenanceType type,

        @Schema(example = "Synthetic oil 5W-30 and oil filter")
        @Size(max = 500)
        String description,

        @Schema(description = "Day the service was done (not in the future)", example = "2026-11-15")
        @NotNull
        @PastOrPresent
        LocalDate serviceDate,

        @Schema(description = "Vehicle mileage in km when the service was done", example = "35000")
        @NotNull
        @PositiveOrZero
        Integer mileage,

        @Schema(description = "Cost in COP", example = "180000.00")
        @PositiveOrZero
        @Digits(integer = 10, fraction = 2)
        BigDecimal cost,

        @Schema(description = "Name of the workshop that did the service", example = "Central Workshop")
        @Size(max = 100)
        String workshop,

        @Schema(description = "Recommended date for the next service (after serviceDate)", example = "2027-05-15")
        LocalDate nextServiceDate,

        @Schema(description = "Recommended mileage for the next service (greater than mileage)", example = "40000")
        @Positive
        Integer nextServiceMileage
) {
}
