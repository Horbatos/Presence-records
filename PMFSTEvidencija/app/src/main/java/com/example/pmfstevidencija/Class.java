package com.example.pmfstevidencija;

import java.util.List;

public class Class {
    private String name;
    private List<Schedule> schedule;

    public Class(){}

    public Class(List<Schedule> schedule, String name) {
        this.name = name;
        this.schedule = schedule;
    }


    public String getName() {
        return name;
    }

    public List<Schedule> getSchedule() {
        return schedule;
    }
}
