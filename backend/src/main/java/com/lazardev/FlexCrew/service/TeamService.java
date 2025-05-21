package com.lazardev.FlexCrew.service;

import java.util.List;
import java.util.Optional;

import com.lazardev.FlexCrew.entity.Team;

public interface TeamService {
     
    List<Team> findAll();
    Optional<Team> findByName(String teamName); 
    Optional<Team> findById(Integer teamId); 
} 
