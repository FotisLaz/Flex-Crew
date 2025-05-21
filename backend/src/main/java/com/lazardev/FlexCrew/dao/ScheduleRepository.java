package com.lazardev.FlexCrew.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lazardev.FlexCrew.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer>  {

}
