package com.lazardev.FlexCrew.service;

import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.entity.Employee;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Use Lombok for constructor injection
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder

  /*
   * // Constructor removed, handled by @RequiredArgsConstructor
   * 
   * @Autowired
   * public EmployeeServiceImpl(EmployeeRepository theEmployeeRepository) {
   * this.employeeRepository = theEmployeeRepository;
   * }
   */

  @Override
  public List<Employee> findAll() {
    return employeeRepository.findAll();
  }

  @Override
  @Transactional // Add transactional annotation for save operations
  public Employee save(Employee theEmployee) {
    // Consider adding logic here to encode password if a new employee is saved
    // or if password field is updated through this method (might need separate
    // update method)
    return employeeRepository.save(theEmployee);
  }

  @Override
  public Optional<Employee> findByEmail(String theEmail) {
    return employeeRepository.findByEmail(theEmail);
  }

  @Override
  public List<Employee> findByTeamId(Long theTeamId) {
    return employeeRepository.findByTeamId(theTeamId);
  }

  @Override
  public Optional<Employee> getEmployeeById(Integer theId) {
    return employeeRepository.findById(theId);
  }

  @Override
  public Employee getCurrentAuthenticatedEmployee() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
      return null; // Or throw an exception if user should always be authenticated here
    }
    String email = ((UserDetails) authentication.getPrincipal()).getUsername();
    // Use orElseThrow for better error handling if employee must exist
    return findByEmail(email).orElse(null);
  }

  @Override
  @Transactional // Ensure the operation is transactional
  public void changePassword(String email, String oldPassword, String newPassword) {
    // 1. Find the employee by email
    Employee employee = employeeRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

    // 2. Verify the old password
    if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
      throw new IllegalArgumentException("Incorrect old password.");
    }

    // 3. Validate the new password (optional, frontend should also validate)
    // Example: if (newPassword.length() < 8) { throw new
    // IllegalArgumentException("Password too short."); }

    // 4. Encode the new password
    String encodedNewPassword = passwordEncoder.encode(newPassword);

    // 5. Update the employee's password in the database
    employee.setPassword(encodedNewPassword);
    employeeRepository.save(employee); // Save the updated employee entity

    // Optionally: Log the successful password change
    // logger.info("Password successfully changed for user: {}", email);
  }
}
