package com.lazardev.FlexCrew.dao;

import com.lazardev.FlexCrew.entity.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

  Optional<Employee> findByEmail(String email);

  List<Employee> findByTeamId(Long teamId);

  List<Employee> findByTeamId(Integer teamId);
}
