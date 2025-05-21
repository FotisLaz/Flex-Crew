package com.lazardev.FlexCrew.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lazardev.FlexCrew.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Integer>  {
    Optional<Team> findByName(String teamName);
}
