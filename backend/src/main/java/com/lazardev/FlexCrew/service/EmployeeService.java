package com.lazardev.FlexCrew.service;

import com.lazardev.FlexCrew.entity.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeService {
  List<Employee> findAll();

  Employee save(Employee theEmployee);

  Optional<Employee> getEmployeeById(Integer theId);

  Optional<Employee> findByEmail(String theEmail);

  List<Employee> findByTeamId(Long theTeamId);

  Employee getCurrentAuthenticatedEmployee();

  // Method for changing password
  void changePassword(String email, String oldPassword, String newPassword);
}
