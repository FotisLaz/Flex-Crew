package com.lazardev.FlexCrew.controller;

import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Record;
import com.lazardev.FlexCrew.service.EmployeeService;
import com.lazardev.FlexCrew.service.RecordService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
public class RecordController {

  private RecordService recordService;
  private EmployeeService employeeService;

  @Autowired
  public RecordController(RecordService theRecordService, EmployeeService theEmployeeService) {
    this.recordService = theRecordService;
    this.employeeService = theEmployeeService;
  }

  // Get records by employee id (own records, or any employee's for an admin)
  @GetMapping("/{employeeId}")
  @PreAuthorize("hasRole('ADMIN') or #employeeId == principal.id")
  public ResponseEntity<?> getRecordsbyEmployeeId(@PathVariable("employeeId") Integer employeeId) {
    try {
      if (employeeId <= 0) {
        return ResponseEntity.badRequest().body("Employee ID cannot be negative.");
      }
      List<Record> findedRecords = recordService.findbyEmployeeId(employeeId);
      return new ResponseEntity<>(findedRecords, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred.");
    }
  }

  @GetMapping("/search")
  @PreAuthorize("hasRole('ADMIN') or #employeeEmail == authentication.name")
  public ResponseEntity<?> getRecordsbyEmployeeEmail(
      @RequestParam(name = "employeeEmail") String employeeEmail) {
    try {
      if (employeeEmail == null) {
        return ResponseEntity.badRequest().body("Employee email cannot be empty.");
      }
      List<Record> findedRecords = recordService.findbyEmployeeEmail(employeeEmail);
      return new ResponseEntity<>(findedRecords, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred.");
    }
  }

  @GetMapping("/searchbymonth")
  @PreAuthorize("hasRole('ADMIN') or #employeeEmail == authentication.name")
  public ResponseEntity<?> getRecordsOfTheMonthbyEmployeeEmail(
      @RequestParam(name = "employeeEmail") String employeeEmail,
      @RequestParam(name = "month") int month,
      @RequestParam(name = "year") int year) {
    try {
      if (employeeEmail == null) {
        return ResponseEntity.badRequest().body("Employee email cannot be empty.");
      }
      if (month < 1 || month > 12) {
        return ResponseEntity.badRequest().body("Month invalid.");
      }
      int findedRecordsOfTheMonth =
          recordService.findbyEmployeeEmailAndMonth(employeeEmail, month, year);
      return new ResponseEntity<>(findedRecordsOfTheMonth, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred.");
    }
  }

  // Check in: for yourself, or for any employee if you are an admin. Only employee.id is read from
  // the body; the time and schedule come from the server so a client cannot pick a time that passes
  // the check-in window.
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or #theRecord.employee?.id == principal.id")
  public ResponseEntity<?> saveRecord(@RequestBody Record theRecord) {
    try {
      Optional<Employee> employee =
          employeeService.getEmployeeById(theRecord.getEmployee().getId());
      if (employee.isEmpty()) {
        return message(HttpStatus.NOT_FOUND, "Employee not found");
      }
      if (employee.get().getSchedule() == null) {
        return message(HttpStatus.BAD_REQUEST, "No schedule assigned");
      }
      Record checkIn =
          Record.builder()
              .employee(employee.get())
              .schedule(employee.get().getSchedule())
              .startTime(OffsetDateTime.now())
              .build();

      // Is the user posting only once a day?
      if (recordService.isSavingRestrictedToday(employee.get().getId())) {
        return message(HttpStatus.BAD_REQUEST, "You can only check in once a day");
      }

      // Is the user posting within time?
      if (recordService.isPostedOutOfTime(checkIn)) {
        return message(
            HttpStatus.BAD_REQUEST, "You can only check in up to 60 minutes after your start time");
      }

      // Is the user posting before expected?
      if (recordService.isPostedBeforeExpectedTime(checkIn)) {
        return message(
            HttpStatus.BAD_REQUEST,
            "You can only check in up to 60 minutes before your start time");
      }

      Record savedRecord = recordService.saveRecord(checkIn);
      return new ResponseEntity<>(savedRecord, HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  private static ResponseEntity<Map<String, String>> message(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(Map.of("message", message));
  }
}
