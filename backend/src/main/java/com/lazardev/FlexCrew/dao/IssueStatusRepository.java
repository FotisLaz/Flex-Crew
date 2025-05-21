package com.lazardev.FlexCrew.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lazardev.FlexCrew.entity.IssueStatus;

public interface IssueStatusRepository extends JpaRepository<IssueStatus, Integer>  {

}
