package com.lazardev.FlexCrew.service;

import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.dao.ScheduleRepository;
import com.lazardev.FlexCrew.dao.TeamRepository;
import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Schedule;
import com.lazardev.FlexCrew.entity.Team;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Use Lombok for constructor injection
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    // Autowired constructor removed, handled by Lombok

    @Override
    public List<Schedule> findAll() {
        return scheduleRepository.findAll();
    }

    @Override
    public Optional<Schedule> findById(Integer scheduleId) { // Return Optional
        return scheduleRepository.findById(scheduleId);
        // Removed the throwing logic, let controller handle not found
    }

    @Override
    @Transactional // Added for create/save operation
    public Schedule saveSchedule(Schedule schedule) {
        // Reset ID for new entities to ensure creation, or let DB handle if it's 0 or
        // null
        // schedule.setId(null); // Uncomment if necessary based on your ID strategy
        // Add any validation or default setting logic here if needed
        schedule.setCurrentEmployees(0); // Ensure new schedules start with 0 employees
        return scheduleRepository.save(schedule);
    }

    @Override
    @Transactional // Added for update operation
    public void updateSchedule(Schedule schedule) {
        // Add validation: check if schedule exists before saving
        if (schedule.getId() == null || !scheduleRepository.existsById(schedule.getId())) {
            throw new IllegalArgumentException("Cannot update non-existent schedule with ID: " + schedule.getId());
        }
        // Consider fetching the existing entity and merging changes
        // to avoid overwriting fields not present in the input DTO/Entity
        scheduleRepository.save(schedule);
    }

    @Override
    @Transactional // Added for delete operation
    public void deleteScheduleById(Integer scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new IllegalArgumentException("Schedule not found with ID: " + scheduleId);
        }
        // Add logic here to handle employees assigned to this schedule
        // before deleting (e.g., set their schedule to null or reassign)
        // Example: employeeRepository.unassignSchedule(scheduleId);
        scheduleRepository.deleteById(scheduleId);
    }

    // --- Existing Methods (Minor improvements) ---
    @Override
    public List<Schedule> getNextWeekScheduleForEmployee(Integer employeeId) {
        // Consider adding error handling if employee not found
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId)); // More specific error

        Schedule currentSchedule = employee.getSchedule();
        if (currentSchedule == null) {
            return new ArrayList<>();
        }
        // Logic for next week seems okay, but could be simplified or adjusted
        List<Schedule> weekSchedule = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // Assuming 5 workdays
            weekSchedule.add(currentSchedule);
        }
        return weekSchedule;
    }

    @Override
    @Transactional // Ensure atomic operation
    public Schedule assignEmployeeToSchedule(Integer employeeId, Integer scheduleId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found: " + scheduleId));

        if (schedule.getCurrentEmployees() >= schedule.getMaxEmployees()) {
            throw new RuntimeException("Schedule at maximum capacity");
        }

        // Unassign from previous schedule
        Schedule oldSchedule = employee.getSchedule();
        if (oldSchedule != null) {
            if (oldSchedule.getId().equals(schedule.getId())) {
                return schedule; // Already assigned to this schedule
            }
            oldSchedule.setCurrentEmployees(Math.max(0, oldSchedule.getCurrentEmployees() - 1)); // Avoid going below 0
            scheduleRepository.save(oldSchedule);
        }

        // Assign to new schedule
        employee.setSchedule(schedule);
        schedule.setCurrentEmployees(schedule.getCurrentEmployees() + 1);

        employeeRepository.save(employee); // Save employee with updated schedule reference
        return scheduleRepository.save(schedule); // Save schedule with updated count
    }

    @Override
    public Schedule optimizeScheduleForTeam(Integer teamId) {
        // This logic seems overly simplistic, might need rethinking based on actual
        // requirements.
        // It just finds the schedule with the most people already assigned that still
        // has space.
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        List<Employee> teamMembers = employeeRepository.findByTeamId(teamId.longValue()); // Assuming findByTeamId takes
                                                                                          // Long
        if (teamMembers.isEmpty()) {
            throw new RuntimeException("No employees found for team: " + teamId);
        }

        List<Schedule> availableSchedules = scheduleRepository.findAll().stream()
                .filter(s -> s.getCurrentEmployees() < s.getMaxEmployees())
                .sorted(Comparator.comparing(Schedule::getCurrentEmployees).reversed()) // Highest occupancy first
                .collect(Collectors.toList());

        if (availableSchedules.isEmpty()) {
            // Fallback: Return the first schedule found if none are available?
            // Or throw an error?
            return scheduleRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No schedules available at all."));
        }
        return availableSchedules.get(0);
    }

}