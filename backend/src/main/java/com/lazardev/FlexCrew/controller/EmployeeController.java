package com.lazardev.FlexCrew.controller;

import com.lazardev.FlexCrew.controller.dto.ChangePasswordRequest;
import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.service.EmployeeService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

  private final EmployeeService employeeService;

  @Autowired
  public EmployeeController(EmployeeService theEmployeeService) {
    this.employeeService = theEmployeeService;
  }

  // Save method (POST mapping)
  @PostMapping
  public ResponseEntity<?> createEmployee(@RequestBody Employee theEmployee) {
    try {
      if (theEmployee == null) {
        return ResponseEntity.badRequest().body("Employee data is missing.");
      }

      Employee savedEmployee = employeeService.save(theEmployee);
      if (savedEmployee == null) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Failed to save the employee.");
      }
      return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred.");
    }
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> findall() {
    try {
      List<Employee> findedEmployees = employeeService.findAll();
      return new ResponseEntity<>(findedEmployees, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred.");
    }
  }

  @GetMapping("/{employeeId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> findById(@PathVariable("employeeId") Integer theId) {
    try {
      if (theId == null || theId <= 0) {
        return ResponseEntity.badRequest().body("Invalid Employee ID.");
      }
      Optional<Employee> findedEmployeeOpt = employeeService.getEmployeeById(theId);
      return findedEmployeeOpt.<ResponseEntity<?>>map(employee -> ResponseEntity.ok(employee))
          .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found."));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred while fetching employee by ID.");
    }
  }

  @GetMapping("/search")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> findByEmail(@RequestParam(name = "employeeEmail") String theEmail) {
    try {
      if (theEmail == null || theEmail.isBlank()) {
        return ResponseEntity.badRequest().body("Employee email cannot be empty.");
      }
      Optional<Employee> findedEmployeeOpt = employeeService.findByEmail(theEmail);
      return findedEmployeeOpt.<ResponseEntity<?>>map(ResponseEntity::ok)
          .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found."));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred while searching for employee by email.");
    }
  }

  @GetMapping("/byTeam/{teamId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> findByTeamId(@PathVariable Long teamId) {
    try {
      if (teamId == null) {
        return ResponseEntity.badRequest().body("Team ID cannot be null.");
      }
      List<Employee> findedEmployees = employeeService.findByTeamId(teamId);
      return new ResponseEntity<>(findedEmployees, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred while fetching employees by team.");
    }
  }

  @PutMapping("/change-password")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
    try {
      String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

      if (!authenticatedUserEmail.equals(request.getEmail())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only change your own password.");
      }

      if (request.getOldPassword() == null || request.getOldPassword().isBlank() ||
          request.getNewPassword() == null || request.getNewPassword().isBlank()) {
        return ResponseEntity.badRequest().body("Old and new passwords are required.");
      }

      employeeService.changePassword(request.getEmail(), request.getOldPassword(), request.getNewPassword());

      return ResponseEntity.ok("Password changed successfully.");

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred while changing the password.");
    }
  }
}
