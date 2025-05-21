package com.lazardev.FlexCrew.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lazardev.FlexCrew.dao.TeamRepository;
import com.lazardev.FlexCrew.entity.Team;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    @Autowired
    public TeamServiceImpl( TeamRepository theTeamRepository){
        this.teamRepository = theTeamRepository;
    }

    @Override
    public List<Team> findAll(){
        return teamRepository.findAll();
    }

    @Override 
    public Optional<Team> findByName(String teamName){
        return teamRepository.findByName(teamName);
    }

    @Override
    public Optional<Team> findById(Integer teamId){
        return teamRepository.findById(teamId);
    }
}
