package com.lazardev.FlexCrew.dao;

import com.lazardev.FlexCrew.dao.projection.PunctualityCount;
import com.lazardev.FlexCrew.entity.Record;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordRepository extends JpaRepository<Record, Integer> {

    List<Record> findByEmployeeId(Integer employeeId);

    List<Record> findByEmployeeEmail(String employeeEmail);

    Optional<Record> findTopByEmployeeIdOrderByStartTimeDesc(Integer employeeId);

    @Query(value = """
            select count(r) from Record r
            where r.employee.email = :employeeEmail
            and MONTH(r.startTime) = :month
            and YEAR(r.startTime) = :year
            """)
    int findByEmployeeEmailAndMonth(
            @Param("employeeEmail") String employeeEmail,
            @Param("month") int month,
            @Param("year") int year);

    @Query("SELECT ist.name as statusName, COUNT(r.id) as count " +
            "FROM Record r JOIN r.issue i JOIN i.issueStatus ist " +
            "GROUP BY ist.name")
    List<PunctualityCount> countRecordsByIssueStatus();
}
