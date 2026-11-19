package com.mantenix.maintenance.controller;

import com.mantenix.maintenance.dto.MaintenanceRecordRequest;
import com.mantenix.maintenance.dto.MaintenanceRecordResponse;
import com.mantenix.maintenance.service.MaintenanceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** REST endpoints for maintenance records. Receives and returns DTOs only. */
@RestController
@RequestMapping("/maintenance-records")
@Tag(name = "Maintenance records", description = "Maintenance history of each vehicle")
public class MaintenanceRecordController {

    private final MaintenanceRecordService service;

    public MaintenanceRecordController(MaintenanceRecordService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a maintenance record")
    public MaintenanceRecordResponse create(@Valid @RequestBody MaintenanceRecordRequest request) {
        return service.create(request);
    }

    @GetMapping
    @Operation(summary = "List maintenance records", description = "Newest first. Optionally filter by vehicle")
    public List<MaintenanceRecordResponse> findAll(@RequestParam(required = false) UUID vehicleId) {
        return service.findAll(vehicleId);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "List upcoming services",
            description = "Records whose next service date is on or before the until date (default: today + 30 days)")
    public List<MaintenanceRecordResponse> findUpcoming(@RequestParam(required = false) LocalDate until) {
        return service.findUpcoming(until);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a maintenance record by id")
    public MaintenanceRecordResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a maintenance record")
    public MaintenanceRecordResponse update(@PathVariable UUID id,
                                            @Valid @RequestBody MaintenanceRecordRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a maintenance record")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
