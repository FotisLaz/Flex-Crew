package com.lazardev.FlexCrew.controller;

import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Schedule;
import com.lazardev.FlexCrew.service.EmployeeService;
import com.lazardev.FlexCrew.service.ScheduleService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final EmployeeService employeeService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService, EmployeeService employeeService) {
        this.scheduleService = scheduleService;
        this.employeeService = employeeService;
    }

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        List<Schedule> schedules = scheduleService.findAll();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{scheduleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getScheduleById(@PathVariable Integer scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            return ResponseEntity.badRequest().body("Invalid Schedule ID.");
        }
        return scheduleService.findById(scheduleId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Schedule not found."));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Schedule>> getNextWeekScheduleForEmployee(@PathVariable Integer employeeId) {
        return ResponseEntity.ok(scheduleService.getNextWeekScheduleForEmployee(employeeId));
    }

    @GetMapping("/employee/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getNextWeekScheduleForCurrentEmployee() {
        Employee currentEmployee = employeeService.getCurrentAuthenticatedEmployee();
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        try {
            return ResponseEntity.ok(scheduleService.getNextWeekScheduleForEmployee(currentEmployee.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/assign/{employeeId}/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignEmployeeToSchedule(
            @PathVariable Integer employeeId,
            @PathVariable Integer scheduleId) {
        try {
            Schedule updatedSchedule = scheduleService.assignEmployeeToSchedule(employeeId, scheduleId);
            return ResponseEntity.ok(updatedSchedule);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/optimize/team/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> optimizeScheduleForTeam(@PathVariable Integer teamId) {
        try {
            Schedule optimalSchedule = scheduleService.optimizeScheduleForTeam(teamId);
            return ResponseEntity.ok(optimalSchedule);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSchedule(@RequestBody Schedule schedule) {
        try {
            if (schedule == null || schedule.getName() == null || schedule.getName().isBlank() ||
                    schedule.getStartTime() == null || schedule.getEndTime() == null ||
                    schedule.getMaxEmployees() == null || schedule.getMaxEmployees() <= 0) {
                return ResponseEntity.badRequest()
                        .body("Invalid schedule data. Name, start/end times, and max employees are required.");
            }
            schedule.setId(null);
            Schedule createdSchedule = scheduleService.saveSchedule(schedule);
            return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating schedule: " + e.getMessage());
        }
    }

    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Integer scheduleId,
            @RequestBody Schedule schedule) {

        if (scheduleId == null || scheduleId <= 0 || schedule == null || !scheduleId.equals(schedule.getId())) {
            return ResponseEntity.badRequest().body("Invalid request. Schedule ID mismatch or missing data.");
        }

        try {
            if (schedule.getName() == null || schedule.getName().isBlank() ||
                    schedule.getStartTime() == null || schedule.getEndTime() == null ||
                    schedule.getMaxEmployees() == null || schedule.getMaxEmployees() <= 0) {
                return ResponseEntity.badRequest().body("Invalid schedule data for update.");
            }
            scheduleService.updateSchedule(schedule);
            return ResponseEntity.ok("Schedule updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating schedule: " + e.getMessage());
        }
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSchedule(@PathVariable Integer scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            return ResponseEntity.badRequest().body("Invalid Schedule ID.");
        }
        try {
            scheduleService.deleteScheduleById(scheduleId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting schedule: " + e.getMessage());
        }
    }
}