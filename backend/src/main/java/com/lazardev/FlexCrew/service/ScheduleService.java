package com.lazardev.FlexCrew.service;

import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Schedule;
import java.util.List;
import java.util.Optional;

public interface ScheduleService {

    List<Schedule> findAll();

    Optional<Schedule> findById(Integer scheduleId);

    List<Schedule> getNextWeekScheduleForEmployee(Integer employeeId);

    Schedule assignEmployeeToSchedule(Integer employeeId, Integer scheduleId);

    Schedule optimizeScheduleForTeam(Integer teamId);

    Schedule saveSchedule(Schedule schedule);

    void updateSchedule(Schedule schedule);

    void deleteScheduleById(Integer scheduleId);
}